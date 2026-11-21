package com.example.game.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.game.model.*
import com.example.game.ui.components.DungeonCanvas
import com.example.ui.theme.*

@Composable
fun DungeonScreen(
    hero: HeroState,
    map: DungeonMap,
    statusToast: String?,
    onMove: (Int, Int) -> Unit,
    onTapTile: (GridPos) -> Unit,
    onOpenMerchant: () -> Unit,
    onOpenGear: () -> Unit,
    onEnterDoorway: () -> Unit,
    onDismissToast: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Find lowest-level remaining monster as recommended target scout
    val nextMonster = map.cells.values
        .mapNotNull { it.monster }
        .minByOrNull { it.level }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(ObsidianVoid, DungeonSlate, ObsidianVoid)
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(12.dp)
    ) {
        val isWide = maxWidth >= 680.dp

        if (isWide) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                DungeonCanvas(
                    map = map,
                    heroClass = hero.heroClass,
                    onTileTap = onTapTile,
                    modifier = Modifier
                        .weight(1.15f)
                        .fillMaxHeight()
                )
                Column(
                    modifier = Modifier
                        .weight(0.85f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    DungeonHudHeader(hero = hero, map = map)
                    Spacer(modifier = Modifier.height(8.dp))
                    ToastBanner(statusToast = statusToast, onDismiss = onDismissToast)
                    Spacer(modifier = Modifier.height(8.dp))
                    MonsterScoutBar(nextMonster = nextMonster, map = map)
                    Spacer(modifier = Modifier.height(8.dp))
                    DungeonControlDeck(
                        map = map,
                        onMove = onMove,
                        onOpenMerchant = onOpenMerchant,
                        onOpenGear = onOpenGear,
                        onEnterDoorway = onEnterDoorway
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DungeonHudHeader(hero = hero, map = map)

                ToastBanner(statusToast = statusToast, onDismiss = onDismissToast)

                DungeonCanvas(
                    map = map,
                    heroClass = hero.heroClass,
                    onTileTap = onTapTile,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                MonsterScoutBar(nextMonster = nextMonster, map = map)

                DungeonControlDeck(
                    map = map,
                    onMove = onMove,
                    onOpenMerchant = onOpenMerchant,
                    onOpenGear = onOpenGear,
                    onEnterDoorway = onEnterDoorway
                )
            }
        }
    }
}

@Composable
private fun DungeonHudHeader(
    hero: HeroState,
    map: DungeonMap
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardObsidian)
            .border(1.dp, StoneBorder, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = hero.heroClass.portraitRes),
                contentDescription = "${hero.heroClass.displayName} avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .border(2.dp, hero.heroClass.accentColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${hero.heroClass.displayName} Lv.${hero.level}",
                        style = MaterialTheme.typography.titleMedium,
                        color = ParchmentIvory
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = hero.equippedWeapon.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = hero.equippedWeapon.definition.rarity.color,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                // HP Bar
                val hpRatio = (hero.currentHp.toFloat() / hero.maxHp.toFloat()).coerceIn(0f, 1f)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { hpRatio },
                        color = if (hpRatio < 0.32f) EmberCrimson else VerdantEmerald,
                        trackColor = ObsidianVoid,
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${hero.currentHp}/${hero.maxHp}",
                        style = MaterialTheme.typography.labelSmall,
                        color = ParchmentIvory
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            // Gold Pill
            Surface(
                color = BurnishedGold.copy(alpha = 0.16f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.border(1.dp, BurnishedGold.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.MonetizationOn,
                        contentDescription = "Gold",
                        tint = BurnishedGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${hero.gold}g",
                        style = MaterialTheme.typography.labelSmall,
                        color = BurnishedGold,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Section & Doorway Status Strip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val sec = map.section
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(sec.accentColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Sec ${sec.sectionNumber}: ${sec.name} (Lv.${sec.minMonsterLevel}–${sec.maxMonsterLevel})",
                    style = MaterialTheme.typography.labelSmall,
                    color = sec.accentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Surface(
                color = if (map.doorwayUnlocked) BurnishedGold.copy(alpha = 0.2f) else ElevatedObsidian,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = if (map.doorwayUnlocked) "GATE OPEN" else "${map.monstersRemaining} FOES LEFT",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (map.doorwayUnlocked) BurnishedGold else AshMuted,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun ToastBanner(statusToast: String?, onDismiss: () -> Unit) {
    AnimatedVisibility(visible = statusToast != null) {
        if (statusToast != null) {
            Surface(
                color = ElevatedObsidian,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BurnishedGold.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                    .clickable(onClick = onDismiss)
            ) {
                Text(
                    text = statusToast,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ParchmentIvory,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun MonsterScoutBar(nextMonster: MonsterInstance?, map: DungeonMap) {
    Surface(
        color = CardObsidian,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, StoneBorder, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (nextMonster != null) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = if (nextMonster.isBoss) Icons.Filled.Warning else Icons.Filled.GpsFixed,
                        contentDescription = null,
                        tint = if (nextMonster.isBoss) BossViolet else EmberCrimson,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Next Target: Lv.${nextMonster.level} ${nextMonster.name}",
                            style = MaterialTheme.typography.labelLarge,
                            color = ParchmentIvory,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "HP ${nextMonster.maxHp} • ATK ${nextMonster.attack} • Bounty ${nextMonster.goldReward}g",
                            style = MaterialTheme.typography.labelSmall,
                            color = AshMuted
                        )
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.MeetingRoom,
                        contentDescription = null,
                        tint = BurnishedGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Chamber cleared! Step into the Doorway to Sec ${map.section.sectionNumber + 1}.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BurnishedGold
                    )
                }
            }
        }
    }
}

@Composable
private fun DungeonControlDeck(
    map: DungeonMap,
    onMove: (Int, Int) -> Unit,
    onOpenMerchant: () -> Unit,
    onOpenGear: () -> Unit,
    onEnterDoorway: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Quick Action Buttons Column
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onOpenMerchant,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BurnishedGold.copy(alpha = 0.7f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = ElevatedObsidian,
                    contentColor = BurnishedGold
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("merchant_button")
            ) {
                Icon(Icons.Filled.Build, contentDescription = "Merchant Forge", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Merchant", style = MaterialTheme.typography.labelLarge)
            }

            OutlinedButton(
                onClick = onOpenGear,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, StoneBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = CardObsidian,
                    contentColor = ParchmentIvory
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("gear_button")
            ) {
                Icon(Icons.Filled.Shield, contentDescription = "Hero Gear & Skills", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Skills & Gear", style = MaterialTheme.typography.labelLarge)
            }

            if (map.doorwayUnlocked) {
                Button(
                    onClick = onEnterDoorway,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BurnishedGold,
                        contentColor = ObsidianVoid
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("doorway_button")
                ) {
                    Icon(Icons.Filled.MeetingRoom, contentDescription = "Enter Doorway", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Next Area", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Tactile Directional Pad (48dp+ touch targets)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            DpadButton(
                icon = Icons.Filled.KeyboardArrowUp,
                description = "Move Up",
                testTag = "dpad_up",
                onClick = { onMove(0, -1) }
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                DpadButton(
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    description = "Move Left",
                    testTag = "dpad_left",
                    onClick = { onMove(-1, 0) }
                )
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardObsidian.copy(alpha = 0.5f))
                        .border(1.dp, StoneBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("TAP\nMAP", style = MaterialTheme.typography.labelSmall, color = AshMuted)
                }
                DpadButton(
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    description = "Move Right",
                    testTag = "dpad_right",
                    onClick = { onMove(1, 0) }
                )
            }
            DpadButton(
                icon = Icons.Filled.KeyboardArrowDown,
                description = "Move Down",
                testTag = "dpad_down",
                onClick = { onMove(0, 1) }
            )
        }
    }
}

@Composable
private fun DpadButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    testTag: String,
    onClick: () -> Unit
) {
    FilledIconButton(
        onClick = onClick,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = ElevatedObsidian,
            contentColor = ParchmentIvory
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .size(48.dp)
            .border(1.dp, StoneBorder, RoundedCornerShape(12.dp))
            .testTag(testTag)
    ) {
        Icon(imageVector = icon, contentDescription = description)
    }
}
