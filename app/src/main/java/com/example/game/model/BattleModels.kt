package com.example.game.model

import androidx.compose.ui.graphics.Color

enum class VfxStyle {
    FIREBALL,
    ARCANE_LANCE,
    METEOR,
    INFERNO_NOVA,
    SWORD_SLASH,
    SHIELD_BASH,
    WHIRLWIND,
    EXECUTION_SLAM,
    ARROW_SHOT,
    ARROW_VOLLEY,
    VENOM_ARROW,
    STARFALL_ARROW,
    ENEMY_STRIKE
}

data class CombatVfx(
    val id: Long,
    val style: VfxStyle,
    val color: Color,
    val ageMs: Long = 0L,
    val durationMs: Long = 420L
) {
    val progress: Float
        get() = (ageMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
}

data class FloatingNumber(
    val id: Long,
    val text: String,
    val color: Color,
    val isCrit: Boolean,
    val onEnemy: Boolean,
    val ageMs: Long = 0L,
    val durationMs: Long = 850L
) {
    val progress: Float
        get() = (ageMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
}

enum class BattleOutcome {
    ONGOING,
    VICTORY,
    DEFEAT
}

data class ActiveBattleState(
    val monster: MonsterInstance,
    val monsterPos: GridPos,
    val heroHp: Int,
    val heroMaxHp: Int,
    val monsterHp: Int,
    val monsterMaxHp: Int,
    val heroGuardMs: Long = 0L,
    val monsterStunMs: Long = 0L,
    val monsterBurnTicks: Int = 0,
    val monsterBurnDamage: Int = 0,
    val monsterBurnTimerMs: Long = 0L,
    val monsterPoisonTicks: Int = 0,
    val monsterPoisonDamage: Int = 0,
    val monsterPoisonTimerMs: Long = 0L,
    val skillCooldownRemainingMs: List<Long>,
    val skillTotalCooldownMs: List<Long>,
    val enemyAttackTimerMs: Long = 0L,
    val lastTriggeredSkillId: String? = null,
    val lastTriggeredSkillTimerMs: Long = 0L,
    val vfxList: List<CombatVfx> = emptyList(),
    val floatingNumbers: List<FloatingNumber> = emptyList(),
    val combatLog: List<String> = emptyList(),
    val elapsedMs: Long = 0L,
    val speedMultiplier: Int = 1,
    val outcome: BattleOutcome = BattleOutcome.ONGOING,
    val goldEarned: Int = 0,
    val xpEarned: Int = 0,
    val leveledUp: Boolean = false
)

sealed interface ScreenMode {
    data object ClassSelection : ScreenMode
    data object DungeonExplore : ScreenMode
    data object AutoBattle : ScreenMode
    data object Merchant : ScreenMode
    data object HeroGear : ScreenMode
    data class BossReward(
        val bossName: String,
        val goldAwarded: Int,
        val droppedWeapon: WeaponInstance,
        val isEndGameBoss: Boolean
    ) : ScreenMode
    data class RunOutcome(val victory: Boolean) : ScreenMode
}

data class RunStats(
    val highestSection: Int = 1,
    val monstersSlain: Int = 0,
    val bossesSlain: Int = 0,
    val highestMonsterLevel: Int = 0,
    val totalGoldEarned: Int = 0
)

data class GateboundUiState(
    val screenMode: ScreenMode = ScreenMode.ClassSelection,
    val selectedClassPreview: HeroClass = HeroClass.MAGE,
    val hero: HeroState? = null,
    val dungeonMap: DungeonMap? = null,
    val activeBattle: ActiveBattleState? = null,
    val battleSpeed: Int = 1,
    val runStats: RunStats = RunStats(),
    val statusToast: String? = null
)
