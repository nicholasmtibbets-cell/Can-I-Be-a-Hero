package com.example.game.engine

import androidx.compose.ui.graphics.Color
import com.example.game.model.*
import com.example.ui.theme.*
import kotlin.math.pow
import kotlin.math.roundToInt

object GameCatalog {

    fun skillsFor(heroClass: HeroClass): List<SkillDefinition> = when (heroClass) {
        HeroClass.MAGE -> listOf(
            SkillDefinition(
                id = "mage_fireball",
                name = "Fireball",
                isPrimary = true,
                category = SkillCategory.OFFENSIVE_MAGIC,
                description = "Hurls a condensed sphere of flame that sears the foe and inflicts Burn.",
                cooldownMs = 1100L,
                damageMultiplier = 1.15f,
                effectType = SkillEffectType.BURN_DOT,
                accentColor = TorchAmber
            ),
            SkillDefinition(
                id = "mage_arcane_lance",
                name = "Arcane Lance",
                isPrimary = false,
                category = SkillCategory.OFFENSIVE_MAGIC,
                description = "Channels a razor beam of pure mana that pierces monster armor.",
                cooldownMs = 2700L,
                damageMultiplier = 1.75f,
                bonusCritChance = 0.12f,
                effectType = SkillEffectType.DIRECT,
                accentColor = ArcaneSapphire
            ),
            SkillDefinition(
                id = "mage_meteor_shards",
                name = "Meteor Shards",
                isPrimary = false,
                category = SkillCategory.OFFENSIVE_MAGIC,
                description = "Summons 3 falling celestial embers that bombard a single target.",
                cooldownMs = 4500L,
                damageMultiplier = 0.72f,
                hitCount = 3,
                effectType = SkillEffectType.MULTI_HIT,
                accentColor = Color(0xFFFF6D42)
            ),
            SkillDefinition(
                id = "mage_inferno_nova",
                name = "Inferno Nova",
                isPrimary = false,
                category = SkillCategory.OFFENSIVE_MAGIC,
                description = "Detonates an arcane sunburst for massive magic damage and heavy Burn.",
                cooldownMs = 7400L,
                damageMultiplier = 2.80f,
                bonusCritChance = 0.15f,
                effectType = SkillEffectType.BURN_DOT,
                accentColor = BossViolet
            )
        )

        HeroClass.WARRIOR -> listOf(
            SkillDefinition(
                id = "warrior_sword_attack",
                name = "Sword Attack",
                isPrimary = true,
                category = SkillCategory.PHYSICAL_ATTACK,
                description = "Delivers a disciplined steel cleave with steady physical force.",
                cooldownMs = 1050L,
                damageMultiplier = 1.22f,
                effectType = SkillEffectType.DIRECT,
                accentColor = EmberCrimson
            ),
            SkillDefinition(
                id = "warrior_shield_bash",
                name = "Shield Bash",
                isPrimary = false,
                category = SkillCategory.PHYSICAL_ATTACK,
                description = "Slams the foe, stunning them for 1.3s and raising a protective Guard.",
                cooldownMs = 3100L,
                damageMultiplier = 1.45f,
                effectType = SkillEffectType.STUN_AND_GUARD,
                accentColor = BurnishedGold
            ),
            SkillDefinition(
                id = "warrior_whirlwind_cleave",
                name = "Whirlwind Cleave",
                isPrimary = false,
                category = SkillCategory.PHYSICAL_ATTACK,
                description = "Spins into a twin-arc heavy blade strike hitting twice.",
                cooldownMs = 4300L,
                damageMultiplier = 1.08f,
                hitCount = 2,
                effectType = SkillEffectType.MULTI_HIT,
                accentColor = TorchAmber
            ),
            SkillDefinition(
                id = "warrior_execution_slam",
                name = "Executioner's Slam",
                isPrimary = false,
                category = SkillCategory.PHYSICAL_ATTACK,
                description = "Crushing overhead finisher dealing +45% bonus damage under 50% HP.",
                cooldownMs = 7000L,
                damageMultiplier = 2.55f,
                effectType = SkillEffectType.EXECUTE,
                accentColor = EmberCrimson
            )
        )

        HeroClass.ARCHER -> listOf(
            SkillDefinition(
                id = "archer_bow_arrow",
                name = "Bow & Arrow",
                isPrimary = true,
                category = SkillCategory.RANGED_PROJECTILE,
                description = "Looses a swift feathered arrow with heightened critical precision.",
                cooldownMs = 920L,
                damageMultiplier = 1.06f,
                bonusCritChance = 0.14f,
                effectType = SkillEffectType.DIRECT,
                accentColor = VerdantEmerald
            ),
            SkillDefinition(
                id = "archer_piercing_volley",
                name = "Piercing Volley",
                isPrimary = false,
                category = SkillCategory.RANGED_PROJECTILE,
                description = "Fires 3 rapid wind-infused arrows in a tight burst.",
                cooldownMs = 2600L,
                damageMultiplier = 0.68f,
                hitCount = 3,
                bonusCritChance = 0.10f,
                effectType = SkillEffectType.MULTI_HIT,
                accentColor = ArcaneSapphire
            ),
            SkillDefinition(
                id = "archer_venom_tip",
                name = "Venom Tip",
                isPrimary = false,
                category = SkillCategory.RANGED_PROJECTILE,
                description = "Shoots a toxic barbed shaft that deals damage and afflicts Poison.",
                cooldownMs = 4400L,
                damageMultiplier = 1.40f,
                effectType = SkillEffectType.POISON_DOT,
                accentColor = VerdantEmerald
            ),
            SkillDefinition(
                id = "archer_starfall_arrow",
                name = "Starfall Arrow",
                isPrimary = false,
                category = SkillCategory.RANGED_PROJECTILE,
                description = "Draws a luminous sky-piercing arrow with a guaranteed Critical Strike.",
                cooldownMs = 6900L,
                damageMultiplier = 2.45f,
                bonusCritChance = 1.0f,
                effectType = SkillEffectType.GUARANTEED_CRIT,
                accentColor = BurnishedGold
            )
        )
    }

    fun starterWeapon(heroClass: HeroClass): WeaponDefinition = when (heroClass) {
        HeroClass.MAGE -> WeaponDefinition(
            id = "wpn_mage_0",
            name = "Ashen Wand",
            heroClass = HeroClass.MAGE,
            rarity = WeaponRarity.COMMON,
            baseAttack = 12,
            critBonus = 0.03f,
            skillPowerBonus = 0.05f,
            lore = "A charred oak focus humming with ember sparks."
        )
        HeroClass.WARRIOR -> WeaponDefinition(
            id = "wpn_warrior_0",
            name = "Iron Broadsword",
            heroClass = HeroClass.WARRIOR,
            rarity = WeaponRarity.COMMON,
            baseAttack = 12,
            critBonus = 0.02f,
            skillPowerBonus = 0.05f,
            lore = "Balanced forged steel trusted in the outer vaults."
        )
        HeroClass.ARCHER -> WeaponDefinition(
            id = "wpn_archer_0",
            name = "Oak Shortbow",
            heroClass = HeroClass.ARCHER,
            rarity = WeaponRarity.COMMON,
            baseAttack = 11,
            critBonus = 0.06f,
            skillPowerBonus = 0.05f,
            lore = "Supple ashwood bow strung for rapid draws."
        )
    }

    fun bossWeaponForSection(heroClass: HeroClass, sectionNumber: Int): WeaponDefinition {
        val tier = (sectionNumber / 3).coerceAtLeast(1)
        return when (heroClass) {
            HeroClass.MAGE -> when (tier) {
                1 -> WeaponDefinition(
                    id = "wpn_mage_boss_1",
                    name = "Emberheart Staff",
                    heroClass = HeroClass.MAGE,
                    rarity = WeaponRarity.RARE,
                    baseAttack = 38,
                    critBonus = 0.08f,
                    skillPowerBonus = 0.22f,
                    lore = "Crowned with a living magma core wrested from the Sanctum Warden.",
                    bossSectionTier = 1
                )
                2 -> WeaponDefinition(
                    id = "wpn_mage_boss_2",
                    name = "Voidspire Scepter",
                    heroClass = HeroClass.MAGE,
                    rarity = WeaponRarity.EPIC,
                    baseAttack = 82,
                    critBonus = 0.12f,
                    skillPowerBonus = 0.42f,
                    lore = "Bends crimson lightning into focused spells.",
                    bossSectionTier = 2
                )
                3 -> WeaponDefinition(
                    id = "wpn_mage_boss_3",
                    name = "Solaris Archstaff",
                    heroClass = HeroClass.MAGE,
                    rarity = WeaponRarity.MYTHIC,
                    baseAttack = 148,
                    critBonus = 0.16f,
                    skillPowerBonus = 0.68f,
                    lore = "Forged from fallen starlight inside the Sovereign's Keep.",
                    bossSectionTier = 3
                )
                else -> WeaponDefinition(
                    id = "wpn_mage_boss_$tier",
                    name = if (tier == 4) "Crownfire Astral Rod" else "Crownfire Rod Mk.$tier",
                    heroClass = HeroClass.MAGE,
                    rarity = WeaponRarity.LEGENDARY,
                    baseAttack = 148 + (tier - 3) * 85,
                    critBonus = 0.22f,
                    skillPowerBonus = 0.68f + (tier - 3) * 0.30f,
                    lore = "Sovereign relic claimed from the Abyssal Architect.",
                    bossSectionTier = tier
                )
            }

            HeroClass.WARRIOR -> when (tier) {
                1 -> WeaponDefinition(
                    id = "wpn_warrior_boss_1",
                    name = "Runed Claymore",
                    heroClass = HeroClass.WARRIOR,
                    rarity = WeaponRarity.RARE,
                    baseAttack = 40,
                    critBonus = 0.06f,
                    skillPowerBonus = 0.22f,
                    lore = "Etched with ancient ward-glyphs that cleave bone and stone.",
                    bossSectionTier = 1
                )
                2 -> WeaponDefinition(
                    id = "wpn_warrior_boss_2",
                    name = "Titanbreaker Blade",
                    heroClass = HeroClass.WARRIOR,
                    rarity = WeaponRarity.EPIC,
                    baseAttack = 86,
                    critBonus = 0.10f,
                    skillPowerBonus = 0.42f,
                    lore = "Massive tempered edge forged to shatter bastion plates.",
                    bossSectionTier = 2
                )
                3 -> WeaponDefinition(
                    id = "wpn_warrior_boss_3",
                    name = "Bloodforged Greatsword",
                    heroClass = HeroClass.WARRIOR,
                    rarity = WeaponRarity.MYTHIC,
                    baseAttack = 154,
                    critBonus = 0.14f,
                    skillPowerBonus = 0.68f,
                    lore = "Hums with relentless vanguard fury.",
                    bossSectionTier = 3
                )
                else -> WeaponDefinition(
                    id = "wpn_warrior_boss_$tier",
                    name = if (tier == 4) "Aegis Excalibur" else "Aegis Excalibur Mk.$tier",
                    heroClass = HeroClass.WARRIOR,
                    rarity = WeaponRarity.LEGENDARY,
                    baseAttack = 154 + (tier - 3) * 88,
                    critBonus = 0.20f,
                    skillPowerBonus = 0.68f + (tier - 3) * 0.30f,
                    lore = "Mythic sovereign blade of the Gatebound.",
                    bossSectionTier = tier
                )
            }

            HeroClass.ARCHER -> when (tier) {
                1 -> WeaponDefinition(
                    id = "wpn_archer_boss_1",
                    name = "Windwhisper Longbow",
                    heroClass = HeroClass.ARCHER,
                    rarity = WeaponRarity.RARE,
                    baseAttack = 36,
                    critBonus = 0.13f,
                    skillPowerBonus = 0.22f,
                    lore = "Carved from pale ironwood; its arrows fly without sound.",
                    bossSectionTier = 1
                )
                2 -> WeaponDefinition(
                    id = "wpn_archer_boss_2",
                    name = "Galehawk Recurve",
                    heroClass = HeroClass.ARCHER,
                    rarity = WeaponRarity.EPIC,
                    baseAttack = 78,
                    critBonus = 0.18f,
                    skillPowerBonus = 0.42f,
                    lore = "Twin-limbed storm bow doubling arrow velocity.",
                    bossSectionTier = 2
                )
                3 -> WeaponDefinition(
                    id = "wpn_archer_boss_3",
                    name = "Starfire Greatbow",
                    heroClass = HeroClass.ARCHER,
                    rarity = WeaponRarity.MYTHIC,
                    baseAttack = 142,
                    critBonus = 0.24f,
                    skillPowerBonus = 0.68f,
                    lore = "Draws arrows of condensed gold lightning.",
                    bossSectionTier = 3
                )
                else -> WeaponDefinition(
                    id = "wpn_archer_boss_$tier",
                    name = if (tier == 4) "Eclipse Celestial Bow" else "Eclipse Bow Mk.$tier",
                    heroClass = HeroClass.ARCHER,
                    rarity = WeaponRarity.LEGENDARY,
                    baseAttack = 142 + (tier - 3) * 82,
                    critBonus = 0.30f,
                    skillPowerBonus = 0.68f + (tier - 3) * 0.30f,
                    lore = "Endgame bow capable of piercing the Abyssal veil.",
                    bossSectionTier = tier
                )
            }
        }
    }

    fun sectionBiome(sectionNumber: Int): SectionBiome {
        val minLv = (sectionNumber - 1) * 10 + 1
        val maxLv = sectionNumber * 10
        val isBoss = sectionNumber % 3 == 0
        val isEndGame = sectionNumber == 12
        return when (sectionNumber) {
            1 -> SectionBiome(1, "Ashen Threshold", "Shallow Vaults", minLv, maxLv, false, false, TorchAmber, Color(0xFF231D28))
            2 -> SectionBiome(2, "Sunken Catacombs", "Flooded Crypts", minLv, maxLv, false, false, ArcaneSapphire, Color(0xFF192230))
            3 -> SectionBiome(3, "Obsidian Sanctum", "First Boss Citadel", minLv, maxLv, true, false, BossViolet, Color(0xFF261A33))
            4 -> SectionBiome(4, "Blighted Mire", "Venomous Ruins", minLv, maxLv, false, false, VerdantEmerald, Color(0xFF192622))
            5 -> SectionBiome(5, "Clockwork Foundry", "Runed Forgeworks", minLv, maxLv, false, false, BurnishedGold, Color(0xFF29221A))
            6 -> SectionBiome(6, "Crimson Bastion", "Second Boss Fortress", minLv, maxLv, true, false, EmberCrimson, Color(0xFF2E181D))
            7 -> SectionBiome(7, "Frostbound Spire", "Glacial Ascent", minLv, maxLv, false, false, ArcaneSapphire, Color(0xFF1A2536))
            8 -> SectionBiome(8, "Astral Rift", "Shattered Void", minLv, maxLv, false, false, BossViolet, Color(0xFF211B36))
            9 -> SectionBiome(9, "Sovereign's Keep", "Third Boss Throne", minLv, maxLv, true, false, BurnishedGold, Color(0xFF2B231A))
            10 -> SectionBiome(10, "Cinder Chasm", "Molten Labyrinth", minLv, maxLv, false, false, TorchAmber, Color(0xFF2E1C18))
            11 -> SectionBiome(11, "Gilded Ossuary", "Outer Sanctum", minLv, maxLv, false, false, Color(0xFFE0B45A), Color(0xFF26211A))
            12 -> SectionBiome(12, "The Abyssal Throne", "Endgame Final Boss", minLv, maxLv, true, true, EmberCrimson, Color(0xFF2B131E))
            else -> SectionBiome(
                sectionNumber,
                "Endless Abyss $sectionNumber",
                if (isBoss) "Endless Boss Floor" else "Endless Depths",
                minLv,
                maxLv,
                isBoss,
                false,
                if (isBoss) BossViolet else ArcaneSapphire,
                Color(0xFF1E192B)
            )
        }
    }

    fun monsterForLevel(level: Int, section: SectionBiome): MonsterInstance {
        val isBoss = section.isBossSection && level == section.maxMonsterLevel
        val isGatekeeper = !isBoss && level == section.maxMonsterLevel

        if (isBoss) {
            val (bossName, bossTitle, archetype) = when (section.sectionNumber) {
                3 -> Triple("Vaelgor, Sanctum Warden", "Keeper of the First Seal", MonsterArchetype.GOLEM)
                6 -> Triple("Kaelis, Crimson Regent", "Lord of the Blood Bastion", MonsterArchetype.DRAGONKIN)
                9 -> Triple("Nyxara, Astral Sovereign", "Empress of Fractured Stars", MonsterArchetype.WRAITH)
                12 -> Triple("Malakor, Architect of Ash", "Endgame Lord of the Abyss", MonsterArchetype.OVERLORD_BOSS)
                else -> Triple("Abyssal Overlord Lv.$level", "Endless Realm Tyrant", MonsterArchetype.OVERLORD_BOSS)
            }
            val hp = (360 + level * 48 + (level.toDouble().pow(1.42) * 3.8)).roundToInt()
            val atk = (16 + level * 2.55).roundToInt()
            val def = (4 + level * 0.45).roundToInt()
            val gold = when (section.sectionNumber) {
                3 -> 360
                6 -> 920
                9 -> 1850
                12 -> 3600
                else -> 1200 + level * 35
            }
            return MonsterInstance(
                id = "boss_s${section.sectionNumber}_lv$level",
                name = bossName,
                level = level,
                archetype = archetype,
                maxHp = hp,
                attack = atk,
                defense = def,
                attackIntervalMs = 1550L,
                goldReward = gold,
                xpReward = 90 + level * 14,
                isBoss = true,
                isGatekeeper = true,
                bossTitle = bossTitle
            )
        }

        val pool: List<Pair<String, MonsterArchetype>> = when ((section.sectionNumber - 1) % 6) {
            0 -> listOf(
                "Cave Stalker" to MonsterArchetype.BEAST,
                "Ash Slime" to MonsterArchetype.SLIME,
                "Vault Skeleton" to MonsterArchetype.SKELETON,
                "Crypt Cultist" to MonsterArchetype.CULTIST,
                "Stone Gargoyle" to MonsterArchetype.GOLEM
            )
            1 -> listOf(
                "Mire Leech" to MonsterArchetype.SLIME,
                "Drowned Boneguard" to MonsterArchetype.SKELETON,
                "Gloom Wraith" to MonsterArchetype.WRAITH,
                "Sunken Cultist" to MonsterArchetype.CULTIST,
                "Abyssal Hound" to MonsterArchetype.BEAST
            )
            2 -> listOf(
                "Obsidian Hound" to MonsterArchetype.BEAST,
                "Rune Sentinel" to MonsterArchetype.GOLEM,
                "Cinder Wraith" to MonsterArchetype.WRAITH,
                "Sanctum Zealot" to MonsterArchetype.CULTIST,
                "Drakeling" to MonsterArchetype.DRAGONKIN
            )
            3 -> listOf(
                "Venom Ooze" to MonsterArchetype.SLIME,
                "Plague Fang" to MonsterArchetype.BEAST,
                "Blight Necrolyte" to MonsterArchetype.CULTIST,
                "Bog Colossus" to MonsterArchetype.GOLEM,
                "Marsh Spectre" to MonsterArchetype.WRAITH
            )
            4 -> listOf(
                "Brass Construct" to MonsterArchetype.AUTOMATON,
                "Steam Warden" to MonsterArchetype.AUTOMATON,
                "Iron Hound" to MonsterArchetype.BEAST,
                "Furnace Golem" to MonsterArchetype.GOLEM,
                "Arcane Tinker" to MonsterArchetype.CULTIST
            )
            else -> listOf(
                "Rift Shade" to MonsterArchetype.WRAITH,
                "Crimson Wyrm" to MonsterArchetype.DRAGONKIN,
                "Dread Knight" to MonsterArchetype.SKELETON,
                "Void Stalker" to MonsterArchetype.BEAST,
                "Soul Harvester" to MonsterArchetype.CULTIST
            )
        }

        val pick = pool[(level - 1) % pool.size]
        val baseName = if (isGatekeeper) "Gatekeeper ${pick.first}" else pick.first
        val gateBoost = if (isGatekeeper) 1.32f else 1.0f

        val hp = ((36 + level * 19 + level.toDouble().pow(1.32) * 1.45) * gateBoost).roundToInt()
        val atk = ((6 + level * 1.85) * (if (isGatekeeper) 1.16f else 1f)).roundToInt()
        val def = (1 + level * 0.32).roundToInt()
        val gold = ((14 + level * 6.5) * (if (isGatekeeper) 1.8f else 1f)).roundToInt()
        val xp = ((20 + level * 8) * (if (isGatekeeper) 1.6f else 1f)).roundToInt()

        return MonsterInstance(
            id = "mob_s${section.sectionNumber}_lv$level",
            name = baseName,
            level = level,
            archetype = pick.second,
            maxHp = hp,
            attack = atk,
            defense = def,
            attackIntervalMs = (1650L - (level * 3L)).coerceAtLeast(1150L),
            goldReward = gold,
            xpReward = xp,
            isBoss = false,
            isGatekeeper = isGatekeeper
        )
    }

    fun createInitialHero(heroClass: HeroClass): HeroState {
        val starter = WeaponInstance(starterWeapon(heroClass), upgradeLevel = 0)
        val skills = skillsFor(heroClass).map { HeroSkillState(it, rank = 1) }
        return HeroState(
            heroClass = heroClass,
            level = 1,
            xp = 0,
            xpToNextLevel = 45,
            currentHp = heroClass.baseHp,
            gold = 35,
            equippedWeapon = starter,
            ownedWeapons = listOf(starter),
            skills = skills
        )
    }
}
