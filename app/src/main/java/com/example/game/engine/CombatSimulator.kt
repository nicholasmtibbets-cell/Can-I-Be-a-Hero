package com.example.game.engine

import androidx.compose.ui.graphics.Color
import com.example.game.model.*
import com.example.ui.theme.ArcaneSapphire
import com.example.ui.theme.BurnishedGold
import com.example.ui.theme.EmberCrimson
import com.example.ui.theme.TorchAmber
import com.example.ui.theme.VerdantEmerald
import kotlin.math.roundToInt
import kotlin.random.Random

object CombatSimulator {

    private var nextVisualId = 1L

    fun createBattleState(
        hero: HeroState,
        monster: MonsterInstance,
        monsterPos: GridPos,
        speedMultiplier: Int = 1
    ): ActiveBattleState {
        val totalCooldowns = hero.skills.map { it.effectiveCooldownMs }
        // Start with Primary Attack ready almost immediately (150ms), others staggered slightly
        val initialRemaining = hero.skills.mapIndexed { index, skill ->
            when {
                skill.definition.isPrimary -> 150L
                index == 1 -> 900L
                index == 2 -> 1800L
                else -> 2800L
            }
        }
        return ActiveBattleState(
            monster = monster,
            monsterPos = monsterPos,
            heroHp = hero.currentHp,
            heroMaxHp = hero.maxHp,
            monsterHp = monster.maxHp,
            monsterMaxHp = monster.maxHp,
            skillCooldownRemainingMs = initialRemaining,
            skillTotalCooldownMs = totalCooldowns,
            enemyAttackTimerMs = (monster.attackIntervalMs * 0.7f).toLong(),
            combatLog = listOf("Encountered Lv.${monster.level} ${monster.name}!"),
            speedMultiplier = speedMultiplier
        )
    }

    fun stepBattle(
        state: ActiveBattleState,
        hero: HeroState,
        rawDeltaMs: Long,
        random: Random = Random.Default
    ): ActiveBattleState {
        if (state.outcome != BattleOutcome.ONGOING) return state

        val deltaMs = rawDeltaMs * state.speedMultiplier
        var heroHp = state.heroHp
        var monsterHp = state.monsterHp
        var heroGuardMs = (state.heroGuardMs - deltaMs).coerceAtLeast(0L)
        var monsterStunMs = (state.monsterStunMs - deltaMs).coerceAtLeast(0L)
        var burnTicks = state.monsterBurnTicks
        var burnDmg = state.monsterBurnDamage
        var burnTimer = state.monsterBurnTimerMs
        var poisonTicks = state.monsterPoisonTicks
        var poisonDmg = state.monsterPoisonDamage
        var poisonTimer = state.monsterPoisonTimerMs
        var enemyAttackTimer = state.enemyAttackTimerMs
        var lastSkillId = state.lastTriggeredSkillId
        var lastSkillTimer = (state.lastTriggeredSkillTimerMs - deltaMs).coerceAtLeast(0L)
        if (lastSkillTimer == 0L) lastSkillId = null

        val cooldowns = state.skillCooldownRemainingMs.map { (it - deltaMs).coerceAtLeast(0L) }.toMutableList()
        val vfx = state.vfxList
            .map { it.copy(ageMs = it.ageMs + deltaMs) }
            .filter { it.ageMs < it.durationMs }
            .toMutableList()
        val floats = state.floatingNumbers
            .map { it.copy(ageMs = it.ageMs + deltaMs) }
            .filter { it.ageMs < it.durationMs }
            .toMutableList()
        val logs = state.combatLog.toMutableList()

        fun addLog(entry: String) {
            logs.add(0, entry)
            if (logs.size > 6) logs.removeAt(logs.lastIndex)
        }

        // 1. Tick Burn & Poison DoTs on Monster
        if (burnTicks > 0 && monsterHp > 0) {
            burnTimer -= deltaMs
            if (burnTimer <= 0L) {
                burnTimer += 900L
                burnTicks -= 1
                monsterHp = (monsterHp - burnDmg).coerceAtLeast(0)
                floats.add(
                    FloatingNumber(
                        id = nextVisualId++,
                        text = "BURN -$burnDmg",
                        color = TorchAmber,
                        isCrit = false,
                        onEnemy = true
                    )
                )
            }
        }

        if (poisonTicks > 0 && monsterHp > 0) {
            poisonTimer -= deltaMs
            if (poisonTimer <= 0L) {
                poisonTimer += 950L
                poisonTicks -= 1
                monsterHp = (monsterHp - poisonDmg).coerceAtLeast(0)
                floats.add(
                    FloatingNumber(
                        id = nextVisualId++,
                        text = "POISON -$poisonDmg",
                        color = VerdantEmerald,
                        isCrit = false,
                        onEnemy = true
                    )
                )
            }
        }

        // 2. Hero Automated Skill Selection (Ultimate -> Skill 3 -> Skill 2 -> Primary)
        if (monsterHp > 0) {
            val readyIndex = (hero.skills.indices.reversed()).firstOrNull { cooldowns[it] <= 0L }
            if (readyIndex != null) {
                val skillState = hero.skills[readyIndex]
                val def = skillState.definition
                cooldowns[readyIndex] = state.skillTotalCooldownMs[readyIndex]
                lastSkillId = def.id
                lastSkillTimer = 450L

                val weaponMult = hero.equippedWeapon.totalSkillMultiplier
                var totalDamageDealt = 0
                var anyCrit = false

                repeat(def.hitCount) {
                    val isCrit = def.effectType == SkillEffectType.GUARANTEED_CRIT ||
                        random.nextFloat() < (hero.totalCritChance + def.bonusCritChance)
                    if (isCrit) anyCrit = true

                    val critFactor = if (isCrit) 1.75f else 1.0f
                    val executeBonus = if (
                        def.effectType == SkillEffectType.EXECUTE &&
                        monsterHp <= state.monsterMaxHp * 0.5f
                    ) 1.45f else 1.0f
                    val variance = 0.94f + random.nextFloat() * 0.12f

                    val rawDmg = hero.totalAttack *
                        skillState.effectiveMultiplier *
                        weaponMult *
                        critFactor *
                        executeBonus *
                        variance
                    val mitigated = (rawDmg - state.monster.defense * 0.45f)
                        .roundToInt()
                        .coerceAtLeast(4)
                    totalDamageDealt += mitigated
                }

                monsterHp = (monsterHp - totalDamageDealt).coerceAtLeast(0)

                // Apply secondary skill effects
                when (def.effectType) {
                    SkillEffectType.BURN_DOT -> {
                        burnTicks = 3
                        burnDmg = (hero.totalAttack * 0.28f * skillState.rank).roundToInt().coerceAtLeast(3)
                        burnTimer = 850L
                    }
                    SkillEffectType.POISON_DOT -> {
                        poisonTicks = 4
                        poisonDmg = (hero.totalAttack * 0.25f * skillState.rank).roundToInt().coerceAtLeast(3)
                        poisonTimer = 850L
                    }
                    SkillEffectType.STUN_AND_GUARD -> {
                        monsterStunMs = 1300L
                        heroGuardMs = 2400L
                    }
                    else -> Unit
                }

                vfx.add(
                    CombatVfx(
                        id = nextVisualId++,
                        style = mapVfxStyle(def.id),
                        color = def.accentColor
                    )
                )
                floats.add(
                    FloatingNumber(
                        id = nextVisualId++,
                        text = if (anyCrit) "CRIT -$totalDamageDealt!" else "-$totalDamageDealt",
                        color = if (anyCrit) BurnishedGold else def.accentColor,
                        isCrit = anyCrit,
                        onEnemy = true
                    )
                )
                addLog("${def.name} dealt $totalDamageDealt${if (anyCrit) " (CRIT)" else ""}")
            }
        }

        // 3. Monster Attack Tick (if still alive and not stunned)
        if (monsterHp > 0) {
            if (monsterStunMs > 0L) {
                // Stunned monster cannot advance attack swing
            } else {
                enemyAttackTimer -= deltaMs
                if (enemyAttackTimer <= 0L) {
                    enemyAttackTimer += state.monster.attackIntervalMs
                    val variance = 0.92f + random.nextFloat() * 0.16f
                    val guardFactor = if (heroGuardMs > 0L) 0.55f else 1.0f
                    val rawEnemyDmg = (state.monster.attack * variance - hero.totalDefense * 0.6f) * guardFactor
                    val dmgToHero = rawEnemyDmg.roundToInt().coerceAtLeast(2)
                    heroHp = (heroHp - dmgToHero).coerceAtLeast(0)

                    vfx.add(
                        CombatVfx(
                            id = nextVisualId++,
                            style = VfxStyle.ENEMY_STRIKE,
                            color = EmberCrimson,
                            durationMs = 340L
                        )
                    )
                    floats.add(
                        FloatingNumber(
                            id = nextVisualId++,
                            text = if (heroGuardMs > 0L) "GUARD -$dmgToHero" else "-$dmgToHero",
                            color = if (heroGuardMs > 0L) ArcaneSapphire else EmberCrimson,
                            isCrit = false,
                            onEnemy = false
                        )
                    )
                    addLog("${state.monster.name} struck for $dmgToHero")
                }
            }
        }

        // 4. Check Victory / Defeat
        val outcome = when {
            monsterHp <= 0 -> BattleOutcome.VICTORY
            heroHp <= 0 -> BattleOutcome.DEFEAT
            else -> BattleOutcome.ONGOING
        }

        val goldEarned = if (outcome == BattleOutcome.VICTORY) state.monster.goldReward else 0
        val xpEarned = if (outcome == BattleOutcome.VICTORY) state.monster.xpReward else 0

        if (outcome == BattleOutcome.VICTORY) {
            addLog("Victory! Looted ${goldEarned}g & $xpEarned XP.")
        } else if (outcome == BattleOutcome.DEFEAT) {
            addLog("Defeated by Lv.${state.monster.level} ${state.monster.name}...")
        }

        return state.copy(
            heroHp = heroHp,
            monsterHp = monsterHp,
            heroGuardMs = heroGuardMs,
            monsterStunMs = monsterStunMs,
            monsterBurnTicks = burnTicks,
            monsterBurnDamage = burnDmg,
            monsterBurnTimerMs = burnTimer,
            monsterPoisonTicks = poisonTicks,
            monsterPoisonDamage = poisonDmg,
            monsterPoisonTimerMs = poisonTimer,
            skillCooldownRemainingMs = cooldowns,
            enemyAttackTimerMs = enemyAttackTimer,
            lastTriggeredSkillId = lastSkillId,
            lastTriggeredSkillTimerMs = lastSkillTimer,
            vfxList = vfx,
            floatingNumbers = floats,
            combatLog = logs,
            elapsedMs = state.elapsedMs + deltaMs,
            outcome = outcome,
            goldEarned = goldEarned,
            xpEarned = xpEarned
        )
    }

    private fun mapVfxStyle(skillId: String): VfxStyle = when (skillId) {
        "mage_fireball" -> VfxStyle.FIREBALL
        "mage_arcane_lance" -> VfxStyle.ARCANE_LANCE
        "mage_meteor_shards" -> VfxStyle.METEOR
        "mage_inferno_nova" -> VfxStyle.INFERNO_NOVA
        "warrior_sword_attack" -> VfxStyle.SWORD_SLASH
        "warrior_shield_bash" -> VfxStyle.SHIELD_BASH
        "warrior_whirlwind_cleave" -> VfxStyle.WHIRLWIND
        "warrior_execution_slam" -> VfxStyle.EXECUTION_SLAM
        "archer_bow_arrow" -> VfxStyle.ARROW_SHOT
        "archer_piercing_volley" -> VfxStyle.ARROW_VOLLEY
        "archer_venom_tip" -> VfxStyle.VENOM_ARROW
        "archer_starfall_arrow" -> VfxStyle.STARFALL_ARROW
        else -> VfxStyle.FIREBALL
    }
}
