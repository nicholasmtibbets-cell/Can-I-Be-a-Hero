package com.example.game.model

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.example.R
import com.example.ui.theme.ArcaneSapphire
import com.example.ui.theme.BurnishedGold
import com.example.ui.theme.EmberCrimson
import com.example.ui.theme.TorchAmber
import com.example.ui.theme.VerdantEmerald

enum class HeroClass(
    val displayName: String,
    val title: String,
    val combatRole: String,
    val primaryAttackName: String,
    val skillSetSummary: String,
    val baseHp: Int,
    val hpPerLevel: Int,
    val baseAttack: Int,
    val attackPerLevel: Int,
    val baseDefense: Int,
    val defensePerLevel: Int,
    val baseCritChance: Float,
    @DrawableRes val portraitRes: Int,
    val accentColor: Color
) {
    MAGE(
        displayName = "Mage",
        title = "Pyromancer Arcanist",
        combatRole = "Offensive Magic & Burn",
        primaryAttackName = "Fireball",
        skillSetSummary = "Fireball, Arcane Lance, Meteor Shards & Inferno Nova",
        baseHp = 140,
        hpPerLevel = 22,
        baseAttack = 18,
        attackPerLevel = 4,
        baseDefense = 4,
        defensePerLevel = 1,
        baseCritChance = 0.12f,
        portraitRes = R.drawable.img_hero_mage,
        accentColor = ArcaneSapphire
    ),
    WARRIOR(
        displayName = "Warrior",
        title = "Ironclad Vanguard",
        combatRole = "Heavy Physical & Stagger",
        primaryAttackName = "Sword Attack",
        skillSetSummary = "Sword Attack, Shield Bash, Whirlwind Cleave & Executioner's Slam",
        baseHp = 190,
        hpPerLevel = 30,
        baseAttack = 16,
        attackPerLevel = 4,
        baseDefense = 8,
        defensePerLevel = 2,
        baseCritChance = 0.10f,
        portraitRes = R.drawable.img_hero_warrior,
        accentColor = EmberCrimson
    ),
    ARCHER(
        displayName = "Archer",
        title = "Galewind Ranger",
        combatRole = "Ranged Projectiles & Crit",
        primaryAttackName = "Bow & Arrow",
        skillSetSummary = "Bow & Arrow, Piercing Volley, Venom Tip & Starfall Arrow",
        baseHp = 155,
        hpPerLevel = 24,
        baseAttack = 17,
        attackPerLevel = 4,
        baseDefense = 5,
        defensePerLevel = 1,
        baseCritChance = 0.20f,
        portraitRes = R.drawable.img_hero_archer,
        accentColor = VerdantEmerald
    )
}

enum class SkillCategory {
    OFFENSIVE_MAGIC,
    PHYSICAL_ATTACK,
    RANGED_PROJECTILE
}

enum class SkillEffectType {
    DIRECT,
    BURN_DOT,
    POISON_DOT,
    STUN_AND_GUARD,
    MULTI_HIT,
    EXECUTE,
    GUARANTEED_CRIT
}

data class SkillDefinition(
    val id: String,
    val name: String,
    val isPrimary: Boolean,
    val category: SkillCategory,
    val description: String,
    val cooldownMs: Long,
    val damageMultiplier: Float,
    val hitCount: Int = 1,
    val bonusCritChance: Float = 0f,
    val effectType: SkillEffectType = SkillEffectType.DIRECT,
    val accentColor: Color = TorchAmber
)

data class HeroSkillState(
    val definition: SkillDefinition,
    val rank: Int = 1
) {
    val effectiveCooldownMs: Long
        get() = (definition.cooldownMs * (1f - (rank - 1) * 0.06f).coerceAtLeast(0.68f)).toLong()

    val effectiveMultiplier: Float
        get() = definition.damageMultiplier * (1f + (rank - 1) * 0.18f)

    val upgradeCost: Int
        get() = 45 * rank + (rank - 1) * 35
}

enum class WeaponRarity(val label: String, val color: Color) {
    COMMON("Forged", Color(0xFFB8B2C8)),
    RARE("Boss Relic", ArcaneSapphire),
    EPIC("Sanctum Relic", Color(0xFFB366FF)),
    MYTHIC("Abyssal Relic", TorchAmber),
    LEGENDARY("Endgame Relic", BurnishedGold)
}

data class WeaponDefinition(
    val id: String,
    val name: String,
    val heroClass: HeroClass,
    val rarity: WeaponRarity,
    val baseAttack: Int,
    val critBonus: Float,
    val skillPowerBonus: Float,
    val lore: String,
    val bossSectionTier: Int = 0
)

data class WeaponInstance(
    val definition: WeaponDefinition,
    val upgradeLevel: Int = 0
) {
    val displayName: String
        get() = if (upgradeLevel > 0) "${definition.name} +$upgradeLevel" else definition.name

    val totalAttack: Int
        get() = definition.baseAttack + upgradeLevel * (5 + definition.bossSectionTier * 2)

    val totalCritBonus: Float
        get() = definition.critBonus + upgradeLevel * 0.015f

    val totalSkillMultiplier: Float
        get() = 1f + definition.skillPowerBonus + upgradeLevel * 0.06f

    val nextUpgradeCost: Int
        get() = 35 + upgradeLevel * 30 + definition.bossSectionTier * 25
}

data class HeroState(
    val heroClass: HeroClass,
    val level: Int = 1,
    val xp: Int = 0,
    val xpToNextLevel: Int = 45,
    val currentHp: Int = heroClass.baseHp,
    val gold: Int = 30,
    val equippedWeapon: WeaponInstance,
    val ownedWeapons: List<WeaponInstance> = listOf(equippedWeapon),
    val skills: List<HeroSkillState>,
    val bonusMaxHp: Int = 0,
    val bonusAttack: Int = 0
) {
    val maxHp: Int
        get() = heroClass.baseHp + (level - 1) * heroClass.hpPerLevel + bonusMaxHp

    val totalAttack: Int
        get() = heroClass.baseAttack + (level - 1) * heroClass.attackPerLevel +
            equippedWeapon.totalAttack + bonusAttack

    val totalDefense: Int
        get() = heroClass.baseDefense + (level - 1) * heroClass.defensePerLevel

    val totalCritChance: Float
        get() = (heroClass.baseCritChance + equippedWeapon.totalCritBonus).coerceAtMost(0.75f)
}
