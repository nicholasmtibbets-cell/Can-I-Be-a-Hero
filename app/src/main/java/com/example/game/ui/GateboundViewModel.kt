package com.example.game.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.game.engine.CombatSimulator
import com.example.game.engine.DungeonGenerator
import com.example.game.engine.GameCatalog
import com.example.game.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class GateboundViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GateboundUiState())
    val uiState: StateFlow<GateboundUiState> = _uiState.asStateFlow()

    private var battleJob: Job? = null
    private var walkJob: Job? = null

    fun selectClassPreview(heroClass: HeroClass) {
        _uiState.update { it.copy(selectedClassPreview = heroClass) }
    }

    fun startRun(heroClass: HeroClass = _uiState.value.selectedClassPreview) {
        battleJob?.cancel()
        walkJob?.cancel()
        val initialHero = GameCatalog.createInitialHero(heroClass)
        val firstMap = DungeonGenerator.generateSection(1)
        _uiState.value = GateboundUiState(
            screenMode = ScreenMode.DungeonExplore,
            selectedClassPreview = heroClass,
            hero = initialHero,
            dungeonMap = firstMap,
            runStats = RunStats(highestSection = 1),
            statusToast = "Section 1: ${firstMap.section.name} (Lv. ${firstMap.section.minMonsterLevel}–${firstMap.section.maxMonsterLevel})"
        )
    }

    fun resetToClassSelection() {
        battleJob?.cancel()
        walkJob?.cancel()
        _uiState.value = GateboundUiState(
            screenMode = ScreenMode.ClassSelection,
            selectedClassPreview = _uiState.value.selectedClassPreview
        )
    }

    fun clearStatusToast() {
        _uiState.update { it.copy(statusToast = null) }
    }

    fun moveHero(dx: Int, dy: Int) {
        walkJob?.cancel()
        stepHeroBy(dx, dy)
    }

    fun tapGridTile(target: GridPos) {
        val current = _uiState.value
        if (current.screenMode != ScreenMode.DungeonExplore) return
        val map = current.dungeonMap ?: return
        if (target == map.heroPos) {
            val here = map.cells[target]
            if (here?.type == TileType.MERCHANT) openMerchant()
            if (here?.type == TileType.DOORWAY) attemptEnterDoorway()
            return
        }

        val path = DungeonGenerator.findPath(map, map.heroPos, target)
        if (path.isEmpty()) {
            _uiState.update { it.copy(statusToast = "Path blocked — defeat intervening foes first.") }
            return
        }

        walkJob?.cancel()
        walkJob = viewModelScope.launch {
            for (nextPos in path) {
                val stateNow = _uiState.value
                if (stateNow.screenMode != ScreenMode.DungeonExplore) break
                val mapNow = stateNow.dungeonMap ?: break
                val dx = nextPos.x - mapNow.heroPos.x
                val dy = nextPos.y - mapNow.heroPos.y
                val continued = stepHeroBy(dx, dy)
                if (!continued) break
                delay(125L)
            }
        }
    }

    /** Returns true if exploration can continue walking along a multi-step path, false if an encounter/modal paused movement. */
    private fun stepHeroBy(dx: Int, dy: Int): Boolean {
        val current = _uiState.value
        if (current.screenMode != ScreenMode.DungeonExplore) return false
        val map = current.dungeonMap ?: return false
        val hero = current.hero ?: return false

        val target = GridPos(map.heroPos.x + dx, map.heroPos.y + dy)
        val cell = map.cells[target] ?: return false
        if (cell.type == TileType.WALL) return false

        // Monster encounter -> start automated battle immediately!
        val monster = cell.monster
        if (monster != null) {
            startBattle(hero, monster, target)
            return false
        }

        // Update hero position first
        var updatedHero = hero
        val updatedCells = map.cells.toMutableMap()
        var toast: String? = null
        var nextScreen: ScreenMode = ScreenMode.DungeonExplore

        when (cell.type) {
            TileType.SHRINE -> {
                if (!cell.consumed) {
                    val heal = (hero.maxHp * 0.55f).roundToInt().coerceAtLeast(25)
                    val newHp = (hero.currentHp + heal).coerceAtMost(hero.maxHp)
                    val actualHeal = newHp - hero.currentHp
                    updatedHero = hero.copy(currentHp = newHp)
                    updatedCells[target] = cell.copy(consumed = true)
                    toast = if (actualHeal > 0) "Sanctuary Shrine restored +$actualHeal HP!" else "HP already full at Shrine."
                }
            }
            TileType.CHEST -> {
                if (!cell.consumed) {
                    updatedHero = hero.copy(gold = hero.gold + cell.chestGold)
                    updatedCells[target] = cell.copy(consumed = true)
                    toast = "Treasure Chest! Found +${cell.chestGold} Gold."
                }
            }
            TileType.MERCHANT -> {
                nextScreen = ScreenMode.Merchant
            }
            TileType.DOORWAY -> {
                if (map.doorwayUnlocked) {
                    advanceToNextSection(
                        updatedHero,
                        map.copy(cells = updatedCells, heroPos = target),
                        current.runStats
                    )
                    return false
                } else {
                    toast = "Doorway sealed! Defeat the Lv.${map.section.maxMonsterLevel} Guardian first."
                }
            }
            TileType.FLOOR, TileType.WALL -> Unit
        }

        val updatedMap = map.copy(cells = updatedCells, heroPos = target)
        _uiState.update {
            it.copy(
                hero = updatedHero,
                dungeonMap = updatedMap,
                screenMode = nextScreen,
                statusToast = toast ?: it.statusToast
            )
        }
        return nextScreen == ScreenMode.DungeonExplore
    }

    fun attemptEnterDoorway() {
        val current = _uiState.value
        val map = current.dungeonMap ?: return
        val hero = current.hero ?: return
        if (!map.doorwayUnlocked) {
            _uiState.update {
                it.copy(statusToast = "Defeat Lv.${map.section.maxMonsterLevel} Guardian to unseal Doorway.")
            }
            return
        }
        advanceToNextSection(hero, map, current.runStats)
    }

    private fun advanceToNextSection(hero: HeroState, currentMap: DungeonMap, stats: RunStats) {
        val nextSectionNumber = currentMap.section.sectionNumber + 1
        val nextMap = DungeonGenerator.generateSection(nextSectionNumber)
        val passageHeal = (hero.maxHp * 0.25f).roundToInt().coerceAtLeast(15)
        val healedHero = hero.copy(currentHp = (hero.currentHp + passageHeal).coerceAtMost(hero.maxHp))
        val bossTag = if (nextMap.section.isBossSection) " • BOSS FLOOR!" else ""

        _uiState.update {
            it.copy(
                screenMode = ScreenMode.DungeonExplore,
                hero = healedHero,
                dungeonMap = nextMap,
                runStats = stats.copy(highestSection = maxOf(stats.highestSection, nextSectionNumber)),
                statusToast = "Entered Section $nextSectionNumber: ${nextMap.section.name} (Lv.${nextMap.section.minMonsterLevel}–${nextMap.section.maxMonsterLevel})$bossTag"
            )
        }
    }

    private fun startBattle(hero: HeroState, monster: MonsterInstance, monsterPos: GridPos) {
        battleJob?.cancel()
        val initialBattle = CombatSimulator.createBattleState(
            hero = hero,
            monster = monster,
            monsterPos = monsterPos,
            speedMultiplier = _uiState.value.battleSpeed
        )
        _uiState.update {
            it.copy(
                screenMode = ScreenMode.AutoBattle,
                activeBattle = initialBattle,
                statusToast = null
            )
        }

        battleJob = viewModelScope.launch {
            while (true) {
                delay(33L)
                val stateNow = _uiState.value
                val heroNow = stateNow.hero ?: break
                val battleNow = stateNow.activeBattle ?: break
                if (battleNow.outcome != BattleOutcome.ONGOING) break

                val stepped = CombatSimulator.stepBattle(
                    state = battleNow,
                    hero = heroNow,
                    rawDeltaMs = 33L
                )

                _uiState.update { it.copy(activeBattle = stepped) }

                if (stepped.outcome != BattleOutcome.ONGOING) {
                    // Brief pause for players to see the killing blow / outcome banner, or let them tap Continue
                    break
                }
            }
        }
    }

    fun toggleBattleSpeed() {
        val nextSpeed = if (_uiState.value.battleSpeed == 1) 2 else 1
        _uiState.update { state ->
            state.copy(
                battleSpeed = nextSpeed,
                activeBattle = state.activeBattle?.copy(speedMultiplier = nextSpeed)
            )
        }
    }

    fun collectBattleVictory() {
        val state = _uiState.value
        val battle = state.activeBattle ?: return
        val hero = state.hero ?: return
        val map = state.dungeonMap ?: return

        if (battle.outcome == BattleOutcome.DEFEAT) {
            _uiState.update {
                it.copy(
                    screenMode = ScreenMode.RunOutcome(victory = false),
                    activeBattle = null
                )
            }
            return
        }

        if (battle.outcome != BattleOutcome.VICTORY) return

        // Clear monster tile & move hero onto it
        val updatedCells = map.cells.toMutableMap()
        val oldCell = updatedCells[battle.monsterPos]
        if (oldCell != null) {
            updatedCells[battle.monsterPos] = oldCell.copy(monster = null)
        }
        val updatedMap = map.copy(cells = updatedCells, heroPos = battle.monsterPos)

        // Apply Gold, battle remaining HP, and XP / Level-ups
        var newGold = hero.gold + battle.goldEarned
        var newXp = hero.xp + battle.xpEarned
        var newLevel = hero.level
        var xpNeeded = hero.xpToNextLevel
        var newHp = battle.heroHp.coerceAtLeast(1)
        var leveledUp = false

        while (newXp >= xpNeeded) {
            newXp -= xpNeeded
            newLevel += 1
            xpNeeded = (xpNeeded * 1.32f).roundToInt() + 18
            leveledUp = true
        }

        val postBattleHero = hero.copy(
            level = newLevel,
            xp = newXp,
            xpToNextLevel = xpNeeded,
            gold = newGold,
            currentHp = newHp
        ).let { updated ->
            if (leveledUp) {
                val bonusHeal = (updated.maxHp * 0.35f).roundToInt()
                updated.copy(currentHp = (updated.currentHp + bonusHeal).coerceAtMost(updated.maxHp))
            } else {
                updated
            }
        }

        val updatedStats = state.runStats.copy(
            monstersSlain = state.runStats.monstersSlain + 1,
            bossesSlain = state.runStats.bossesSlain + if (battle.monster.isBoss) 1 else 0,
            highestMonsterLevel = maxOf(state.runStats.highestMonsterLevel, battle.monster.level),
            totalGoldEarned = state.runStats.totalGoldEarned + battle.goldEarned
        )

        if (battle.monster.isBoss) {
            val droppedDef = GameCatalog.bossWeaponForSection(hero.heroClass, map.section.sectionNumber)
            val droppedWeapon = WeaponInstance(droppedDef, upgradeLevel = 0)
            _uiState.update {
                it.copy(
                    hero = postBattleHero,
                    dungeonMap = updatedMap,
                    activeBattle = null,
                    runStats = updatedStats,
                    screenMode = ScreenMode.BossReward(
                        bossName = battle.monster.name,
                        goldAwarded = battle.goldEarned,
                        droppedWeapon = droppedWeapon,
                        isEndGameBoss = map.section.isEndGameSection
                    )
                )
            }
        } else {
            val unlockToast = when {
                updatedMap.doorwayUnlocked && battle.monster.isGatekeeper ->
                    "Gatekeeper slain! Doorway to Section ${map.section.sectionNumber + 1} is UNSEALED!"
                leveledUp -> "Level Up! Reached Hero Lv.$newLevel (+${battle.goldEarned}g)"
                else -> "Victory! +${battle.goldEarned}g & +${battle.xpEarned} XP"
            }
            _uiState.update {
                it.copy(
                    hero = postBattleHero,
                    dungeonMap = updatedMap,
                    activeBattle = null,
                    runStats = updatedStats,
                    screenMode = ScreenMode.DungeonExplore,
                    statusToast = unlockToast
                )
            }
        }
    }

    fun claimBossReward(equipNow: Boolean) {
        val state = _uiState.value
        val reward = state.screenMode as? ScreenMode.BossReward ?: return
        val hero = state.hero ?: return

        val newOwned = if (hero.ownedWeapons.any { it.definition.id == reward.droppedWeapon.definition.id }) {
            hero.ownedWeapons
        } else {
            hero.ownedWeapons + reward.droppedWeapon
        }
        val nextEquipped = if (equipNow) reward.droppedWeapon else hero.equippedWeapon

        val updatedHero = hero.copy(
            ownedWeapons = newOwned,
            equippedWeapon = nextEquipped
        )

        if (reward.isEndGameBoss) {
            _uiState.update {
                it.copy(
                    hero = updatedHero,
                    screenMode = ScreenMode.RunOutcome(victory = true)
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    hero = updatedHero,
                    screenMode = ScreenMode.DungeonExplore,
                    statusToast = "Boss defeated! Step through the unsealed Doorway!"
                )
            }
        }
    }

    fun continueEndlessAfterVictory() {
        val state = _uiState.value
        val hero = state.hero ?: return
        val map = state.dungeonMap ?: return
        advanceToNextSection(hero, map, state.runStats)
    }

    fun openMerchant() {
        val current = _uiState.value
        if (current.hero == null || current.dungeonMap == null) return
        walkJob?.cancel()
        _uiState.update { it.copy(screenMode = ScreenMode.Merchant) }
    }

    fun openHeroGear() {
        val current = _uiState.value
        if (current.hero == null) return
        walkJob?.cancel()
        _uiState.update { it.copy(screenMode = ScreenMode.HeroGear) }
    }

    fun returnToDungeon() {
        _uiState.update { it.copy(screenMode = ScreenMode.DungeonExplore) }
    }

    fun upgradeEquippedWeapon() {
        val state = _uiState.value
        val hero = state.hero ?: return
        val equipped = hero.equippedWeapon
        val cost = equipped.nextUpgradeCost
        if (hero.gold < cost) {
            _uiState.update { it.copy(statusToast = "Need ${cost}g to forge ${equipped.definition.name}.") }
            return
        }

        val upgraded = equipped.copy(upgradeLevel = equipped.upgradeLevel + 1)
        val updatedOwned = hero.ownedWeapons.map {
            if (it.definition.id == upgraded.definition.id) upgraded else it
        }
        _uiState.update {
            it.copy(
                hero = hero.copy(
                    gold = hero.gold - cost,
                    equippedWeapon = upgraded,
                    ownedWeapons = updatedOwned
                ),
                statusToast = "Forged ${upgraded.displayName}!"
            )
        }
    }

    fun equipWeapon(weaponId: String) {
        val hero = _uiState.value.hero ?: return
        val found = hero.ownedWeapons.firstOrNull { it.definition.id == weaponId } ?: return
        _uiState.update {
            it.copy(
                hero = hero.copy(equippedWeapon = found),
                statusToast = "Equipped ${found.displayName}"
            )
        }
    }

    fun upgradeSkill(skillId: String) {
        val hero = _uiState.value.hero ?: return
        val targetSkill = hero.skills.firstOrNull { it.definition.id == skillId } ?: return
        val cost = targetSkill.upgradeCost
        if (hero.gold < cost) {
            _uiState.update { it.copy(statusToast = "Need ${cost}g to empower ${targetSkill.definition.name}.") }
            return
        }
        val updatedSkills = hero.skills.map {
            if (it.definition.id == skillId) it.copy(rank = it.rank + 1) else it
        }
        _uiState.update {
            it.copy(
                hero = hero.copy(gold = hero.gold - cost, skills = updatedSkills),
                statusToast = "${targetSkill.definition.name} empowered to Rank ${targetSkill.rank + 1}!"
            )
        }
    }

    fun elixirCost(sectionNumber: Int): Int = 22 + sectionNumber * 12

    fun buyElixir() {
        val state = _uiState.value
        val hero = state.hero ?: return
        val section = state.dungeonMap?.section?.sectionNumber ?: 1
        val cost = elixirCost(section)
        if (hero.gold < cost) {
            _uiState.update { it.copy(statusToast = "Need ${cost}g for a Healing Elixir.") }
            return
        }
        if (hero.currentHp >= hero.maxHp) {
            _uiState.update { it.copy(statusToast = "HP is already at maximum!") }
            return
        }
        val heal = (hero.maxHp * 0.65f).roundToInt().coerceAtLeast(30)
        val newHp = (hero.currentHp + heal).coerceAtMost(hero.maxHp)
        _uiState.update {
            it.copy(
                hero = hero.copy(gold = hero.gold - cost, currentHp = newHp),
                statusToast = "Drank Elixir: restored +${newHp - hero.currentHp} HP!"
            )
        }
    }

    fun charmCost(sectionNumber: Int): Int = 40 + sectionNumber * 18

    fun buyBattleCharm() {
        val state = _uiState.value
        val hero = state.hero ?: return
        val section = state.dungeonMap?.section?.sectionNumber ?: 1
        val cost = charmCost(section)
        if (hero.gold < cost) {
            _uiState.update { it.copy(statusToast = "Need ${cost}g for a Vitality Ward.") }
            return
        }
        val hpGain = 22 + section * 8
        _uiState.update {
            it.copy(
                hero = hero.copy(
                    gold = hero.gold - cost,
                    bonusMaxHp = hero.bonusMaxHp + hpGain,
                    currentHp = hero.currentHp + hpGain
                ),
                statusToast = "Vitality Ward inscribed: +$hpGain Max HP!"
            )
        }
    }

    override fun onCleared() {
        battleJob?.cancel()
        walkJob?.cancel()
        super.onCleared()
    }
}
