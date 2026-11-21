package com.example.game.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.game.model.*
import com.example.ui.theme.*

@Composable
fun MerchantScreen(
    hero: HeroState,
    sectionNumber: Int,
    elixirCost: Int,
    charmCost: Int,
    statusToast: String?,
    onUpgradeWeapon: () -> Unit,
    onEquipWeapon: (String) -> Unit,
    onUpgradeSkill: (String) -> Unit,
    onBuyElixir: () -> Unit,
    onBuyCharm: () -> Unit,
    onBackToDungeon: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackToDungeon)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(ObsidianVoid, DungeonSlate, ObsidianVoid)))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                IconButton(
                    onClick = onBackToDungeon,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("close_merchant_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Return to Dungeon",
                        tint = ParchmentIvory
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = stringResource(R.string.merchant_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = BurnishedGold
                    )
                    Text(
                        text = "Section $sectionNumber Camp • Upgrade Gear with Gold",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AshMuted
                    )
                }
            }

            Surface(
                color = BurnishedGold.copy(alpha = 0.16f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.border(1.dp, BurnishedGold, RoundedCornerShape(10.dp))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Filled.MonetizationOn, contentDescription = "Gold", tint = BurnishedGold, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${hero.gold}g", style = MaterialTheme.typography.titleMedium, color = BurnishedGold)
                }
            }
        }

        if (statusToast != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = ElevatedObsidian,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BurnishedGold.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
            ) {
                Text(
                    text = statusToast,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ParchmentIvory,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Equipped Weapon Upgrade Card
            val equipped = hero.equippedWeapon
            val upgradeCost = equipped.nextUpgradeCost
            val canAffordUpgrade = hero.gold >= upgradeCost
            val nextLevelPreview = equipped.copy(upgradeLevel = equipped.upgradeLevel + 1)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(CardObsidian)
                    .border(1.5.dp, equipped.definition.rarity.color.copy(alpha = 0.7f), RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Text("FORGE EQUIPPED WEAPON", style = MaterialTheme.typography.labelSmall, color = AshMuted)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = equipped.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            color = equipped.definition.rarity.color
                        )
                        Text(
                            text = "ATK ${equipped.totalAttack} → ${nextLevelPreview.totalAttack}  •  Skill +${((nextLevelPreview.totalSkillMultiplier - 1f) * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = VerdantEmerald
                        )
                    }
                    Button(
                        onClick = onUpgradeWeapon,
                        enabled = canAffordUpgrade,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BurnishedGold,
                            contentColor = ObsidianVoid
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("upgrade_weapon_button")
                    ) {
                        Icon(Icons.Filled.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Forge ${upgradeCost}g", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Owned Weapons Arsenal (e.g. Boss Drops)
            if (hero.ownedWeapons.size > 1) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CardObsidian)
                        .border(1.dp, StoneBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Text("WEAPON ARMORY", style = MaterialTheme.typography.labelSmall, color = AshMuted)
                    Spacer(modifier = Modifier.height(8.dp))
                    hero.ownedWeapons.forEach { wpn ->
                        val isEquipped = wpn.definition.id == hero.equippedWeapon.definition.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(wpn.displayName, style = MaterialTheme.typography.labelLarge, color = wpn.definition.rarity.color)
                                Text("ATK ${wpn.totalAttack} • Crit +${(wpn.totalCritBonus * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium, color = AshMuted)
                            }
                            if (isEquipped) {
                                Text("EQUIPPED", style = MaterialTheme.typography.labelSmall, color = BurnishedGold)
                            } else {
                                OutlinedButton(
                                    onClick = { onEquipWeapon(wpn.definition.id) },
                                    modifier = Modifier.heightIn(min = 48.dp)
                                ) {
                                    Text("Equip")
                                }
                            }
                        }
                    }
                }
            }

            // Field Supplies (Healing Elixir & Vitality Ward)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(CardObsidian)
                        .border(1.dp, StoneBorder, RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Text("Healing Elixir", style = MaterialTheme.typography.titleMedium, color = VerdantEmerald)
                    Text(
                        text = "Restores 65% HP (${hero.currentHp}/${hero.maxHp})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AshMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onBuyElixir,
                        enabled = hero.gold >= elixirCost && hero.currentHp < hero.maxHp,
                        colors = ButtonDefaults.buttonColors(containerColor = VerdantEmerald, contentColor = ObsidianVoid),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("buy_elixir_button")
                    ) {
                        Text("Buy ${elixirCost}g", fontWeight = FontWeight.Bold)
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(CardObsidian)
                        .border(1.dp, StoneBorder, RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Text("Vitality Ward", style = MaterialTheme.typography.titleMedium, color = ArcaneSapphire)
                    Text(
                        text = "Permanently +${22 + sectionNumber * 8} Max HP",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AshMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onBuyCharm,
                        enabled = hero.gold >= charmCost,
                        colors = ButtonDefaults.buttonColors(containerColor = ArcaneSapphire, contentColor = ObsidianVoid),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                    ) {
                        Text("Buy ${charmCost}g", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Skill Empowerment Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardObsidian)
                    .border(1.dp, StoneBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Text("EMPOWER CLASS SKILLS", style = MaterialTheme.typography.labelSmall, color = AshMuted)
                Spacer(modifier = Modifier.height(8.dp))
                hero.skills.forEach { skillState ->
                    val cost = skillState.upgradeCost
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${skillState.definition.name} (Rank ${skillState.rank})",
                                style = MaterialTheme.typography.labelLarge,
                                color = skillState.definition.accentColor
                            )
                            Text(
                                text = "Power ${(skillState.effectiveMultiplier * 100).toInt()}% • CD ${"%.1f".format(skillState.effectiveCooldownMs / 1000f)}s",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AshMuted
                            )
                        }
                        OutlinedButton(
                            onClick = { onUpgradeSkill(skillState.definition.id) },
                            enabled = hero.gold >= cost,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Text("Rank+ ${cost}g")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeroGearScreen(
    hero: HeroState,
    onEquipWeapon: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(ObsidianVoid, DungeonSlate, ObsidianVoid)))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = ParchmentIvory)
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text("${hero.heroClass.displayName} Arsenal", style = MaterialTheme.typography.titleLarge, color = ParchmentIvory)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Core Stats Summary
            Surface(
                color = CardObsidian,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, hero.heroClass.accentColor.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Level ${hero.level} ${hero.heroClass.title}", style = MaterialTheme.typography.titleMedium, color = hero.heroClass.accentColor)
                    Text("HP: ${hero.currentHp} / ${hero.maxHp}   •   Attack: ${hero.totalAttack}   •   Defense: ${hero.totalDefense}", style = MaterialTheme.typography.bodyMedium, color = ParchmentIvory)
                    Text("Crit Chance: ${(hero.totalCritChance * 100).toInt()}%   •   XP: ${hero.xp} / ${hero.xpToNextLevel}", style = MaterialTheme.typography.labelSmall, color = AshMuted)
                }
            }

            // Weapons
            Surface(
                color = CardObsidian,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, StoneBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("COLLECTED WEAPONS", style = MaterialTheme.typography.labelSmall, color = AshMuted)
                    hero.ownedWeapons.forEach { wpn ->
                        val equipped = wpn.definition.id == hero.equippedWeapon.definition.id
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(wpn.displayName, style = MaterialTheme.typography.titleMedium, color = wpn.definition.rarity.color)
                                Text(wpn.definition.lore, style = MaterialTheme.typography.bodyMedium, color = AshMuted)
                            }
                            if (equipped) {
                                Text("EQUIPPED", style = MaterialTheme.typography.labelSmall, color = BurnishedGold)
                            } else {
                                OutlinedButton(onClick = { onEquipWeapon(wpn.definition.id) }, modifier = Modifier.heightIn(min = 48.dp)) {
                                    Text("Equip")
                                }
                            }
                        }
                    }
                }
            }

            // Skills
            Surface(
                color = CardObsidian,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, StoneBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("ACTIVE AUTO-SKILLS", style = MaterialTheme.typography.labelSmall, color = AshMuted)
                    hero.skills.forEach { skillState ->
                        Column {
                            Text(
                                "${skillState.definition.name} • Rank ${skillState.rank}",
                                style = MaterialTheme.typography.labelLarge,
                                color = skillState.definition.accentColor
                            )
                            Text(skillState.definition.description, style = MaterialTheme.typography.bodyMedium, color = AshMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BossRewardScreen(
    reward: ScreenMode.BossReward,
    onClaim: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(ObsidianVoid, Color(0xFF28153A), ObsidianVoid)))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(CardObsidian)
                .border(2.dp, BurnishedGold, RoundedCornerShape(24.dp))
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.EmojiEvents,
                contentDescription = "Boss Trophy",
                tint = BurnishedGold,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "BOSS VANQUISHED",
                style = MaterialTheme.typography.labelSmall,
                color = BossViolet
            )
            Text(
                text = reward.bossName,
                style = MaterialTheme.typography.headlineMedium,
                color = ParchmentIvory,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Huge Gold Treasure Box
            Surface(
                color = BurnishedGold.copy(alpha = 0.14f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BurnishedGold, RoundedCornerShape(14.dp))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Filled.MonetizationOn, contentDescription = null, tint = BurnishedGold, modifier = Modifier.size(26.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "+${reward.goldAwarded} GOLD HOARD",
                        style = MaterialTheme.typography.titleMedium,
                        color = BurnishedGold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // New Hero-Class Weapon Card
            val wpn = reward.droppedWeapon
            Surface(
                color = ElevatedObsidian,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, wpn.definition.rarity.color, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "NEW ${wpn.definition.heroClass.displayName.uppercase()} WEAPON • ${wpn.definition.rarity.label.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = wpn.definition.rarity.color
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = wpn.displayName,
                        style = MaterialTheme.typography.titleLarge,
                        color = ParchmentIvory
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = wpn.definition.lore,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AshMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "+${wpn.totalAttack} ATK  •  +${(wpn.totalCritBonus * 100).toInt()}% Crit  •  +${((wpn.totalSkillMultiplier - 1f) * 100).toInt()}% Skill Power",
                        style = MaterialTheme.typography.labelSmall,
                        color = VerdantEmerald
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { onClaim(true) },
                colors = ButtonDefaults.buttonColors(containerColor = BurnishedGold, contentColor = ObsidianVoid),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .testTag("claim_boss_reward_button")
            ) {
                Text("Equip Weapon & Claim Gold", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun RunOutcomeScreen(
    victory: Boolean,
    hero: HeroState,
    stats: RunStats,
    onContinueEndless: () -> Unit,
    onNewRun: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onNewRun)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        ObsidianVoid,
                        if (victory) Color(0xFF282012) else Color(0xFF281116),
                        ObsidianVoid
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(CardObsidian)
                .border(1.5.dp, if (victory) BurnishedGold else EmberCrimson, RoundedCornerShape(24.dp))
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (victory) Icons.Filled.EmojiEvents else Icons.Filled.Dangerous,
                contentDescription = null,
                tint = if (victory) BurnishedGold else EmberCrimson,
                modifier = Modifier.size(60.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = if (victory) stringResource(R.string.endgame_victory) else stringResource(R.string.battle_defeat),
                style = MaterialTheme.typography.headlineMedium,
                color = if (victory) BurnishedGold else EmberCrimson,
                textAlign = TextAlign.Center
            )
            Text(
                text = if (victory) {
                    "You conquered Section 12 and shattered the Abyssal Throne!"
                } else {
                    "Your ${hero.heroClass.displayName} fell in Section ${stats.highestSection}."
                },
                style = MaterialTheme.typography.bodyLarge,
                color = AshMuted,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            Surface(
                color = ElevatedObsidian,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, StoneBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatLine("Hero Level", "Lv.${hero.level} ${hero.heroClass.displayName}")
                    StatLine("Highest Section", "Section ${stats.highestSection}")
                    StatLine("Monsters Slain", "${stats.monstersSlain}")
                    StatLine("Highest Monster Level", "Lv.${stats.highestMonsterLevel}")
                    StatLine("Bosses Defeated", "${stats.bossesSlain}")
                    StatLine("Total Gold Earned", "${stats.totalGoldEarned}g")
                    StatLine("Final Weapon", hero.equippedWeapon.displayName)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (victory) {
                Button(
                    onClick = onContinueEndless,
                    colors = ButtonDefaults.buttonColors(containerColor = BossViolet, contentColor = ParchmentIvory),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                ) {
                    Text("Enter Endless Depths (Section 13+)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Button(
                onClick = onNewRun,
                colors = ButtonDefaults.buttonColors(containerColor = BurnishedGold, contentColor = ObsidianVoid),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .testTag("new_run_button")
            ) {
                Text(stringResource(R.string.new_run), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun StatLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = AshMuted)
        Text(value, style = MaterialTheme.typography.labelLarge, color = ParchmentIvory)
    }
}
