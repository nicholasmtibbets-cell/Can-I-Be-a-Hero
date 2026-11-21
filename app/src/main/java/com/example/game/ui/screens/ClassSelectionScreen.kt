package com.example.game.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.game.engine.GameCatalog
import com.example.game.model.HeroClass
import com.example.ui.theme.*

@Composable
fun ClassSelectionScreen(
    selectedClass: HeroClass,
    onSelectClass: (HeroClass) -> Unit,
    onStartRun: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedSkills = GameCatalog.skillsFor(selectedClass)
    val starterWeapon = GameCatalog.starterWeapon(selectedClass)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(ObsidianVoid, DungeonSlate, ObsidianVoid)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hero Banner Art
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
                    .height(168.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, StoneBorder, RoundedCornerShape(20.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_dungeon_banner),
                    contentDescription = "Gatebound ancient dungeon portal",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    ObsidianVoid.copy(alpha = 0.72f),
                                    ObsidianVoid.copy(alpha = 0.94f)
                                )
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.app_name).uppercase(),
                        style = MaterialTheme.typography.displayLarge,
                        color = BurnishedGold
                    )
                    Text(
                        text = stringResource(R.string.choose_hero_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = ParchmentIvory.copy(alpha = 0.9f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.choose_hero_title),
                style = MaterialTheme.typography.titleLarge,
                color = ParchmentIvory,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 3 Class Cards Row / Adaptive
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HeroClass.entries.forEach { heroClass ->
                    val isSelected = heroClass == selectedClass
                    ClassPortraitCard(
                        heroClass = heroClass,
                        isSelected = isSelected,
                        onClick = { onSelectClass(heroClass) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("class_card_${heroClass.name.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Detailed Class Arsenal & Skill Set Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(CardObsidian)
                    .border(1.5.dp, selectedClass.accentColor.copy(alpha = 0.65f), RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${selectedClass.displayName} • ${selectedClass.title}",
                            style = MaterialTheme.typography.titleMedium,
                            color = selectedClass.accentColor
                        )
                        Text(
                            text = "Starter Weapon: ${starterWeapon.name} (+${starterWeapon.baseAttack} ATK)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = BurnishedGold
                        )
                    }
                    Surface(
                        color = ElevatedObsidian,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "HP ${selectedClass.baseHp} • ATK ${selectedClass.baseAttack + starterWeapon.baseAttack}",
                            style = MaterialTheme.typography.labelSmall,
                            color = ParchmentIvory,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = StoneBorder)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "UNIQUE AUTOMATED SKILL SET",
                    style = MaterialTheme.typography.labelSmall,
                    color = AshMuted
                )
                Spacer(modifier = Modifier.height(8.dp))

                selectedSkills.forEach { skill ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(skill.accentColor.copy(alpha = 0.18f))
                                .border(1.dp, skill.accentColor.copy(alpha = 0.55f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when {
                                    skill.isPrimary -> Icons.Filled.Whatshot
                                    skill.effectType.name.contains("STUN") -> Icons.Filled.Shield
                                    skill.effectType.name.contains("CRIT") -> Icons.Filled.AutoAwesome
                                    else -> Icons.Filled.Bolt
                                },
                                contentDescription = skill.name,
                                tint = skill.accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = skill.name,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = ParchmentIvory
                                )
                                if (skill.isPrimary) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "PRIMARY",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = BurnishedGold,
                                        modifier = Modifier
                                            .background(BurnishedGold.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = skill.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = AshMuted,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${"%.1f".format(skill.cooldownMs / 1000f)}s",
                            style = MaterialTheme.typography.labelSmall,
                            color = skill.accentColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dungeon Loop Primer Strip
            Surface(
                color = ElevatedObsidian.copy(alpha = 0.7f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
                    .border(1.dp, StoneBorder, RoundedCornerShape(14.dp))
            ) {
                Text(
                    text = "Engage single leveled monsters (Lv.1–10 per section) in automated skill battles. Spend gold at merchants to forge stronger weapons. Every 3rd section features a Boss awarding new class weapons and vast gold.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AshMuted,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onStartRun,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BurnishedGold,
                    contentColor = ObsidianVoid
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
                    .heightIn(min = 54.dp)
                    .testTag("start_run_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${stringResource(R.string.begin_descent)} as ${selectedClass.displayName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ClassPortraitCard(
    heroClass: HeroClass,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) heroClass.accentColor else StoneBorder
    val bgColor = if (isSelected) ElevatedObsidian else CardObsidian

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .minimumInteractiveComponentSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .border(2.dp, if (isSelected) BurnishedGold else StoneBorder, CircleShape)
        ) {
            Image(
                painter = painterResource(id = heroClass.portraitRes),
                contentDescription = "${heroClass.displayName} portrait",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = heroClass.displayName,
            style = MaterialTheme.typography.titleMedium,
            color = if (isSelected) ParchmentIvory else AshMuted,
            maxLines = 1
        )
        Text(
            text = heroClass.primaryAttackName,
            style = MaterialTheme.typography.labelSmall,
            color = heroClass.accentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
