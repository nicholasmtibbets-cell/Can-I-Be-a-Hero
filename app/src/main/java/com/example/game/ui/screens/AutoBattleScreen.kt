package com.example.game.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.model.*
import com.example.game.ui.components.BattleVfxOverlay
import com.example.game.ui.components.MonsterSpriteCanvas
import com.example.ui.theme.*

@Composable
fun AutoBattleScreen(
    hero: HeroState,
    battle: ActiveBattleState,
    section: SectionBiome,
    onToggleSpeed: () -> Unit,
    onCollectOutcome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val heroHpAnim by animateFloatAsState(
        targetValue = (battle.heroHp.toFloat() / battle.heroMaxHp.toFloat()).coerceIn(0f, 1f),
        label = "heroHp"
    )
    val monsterHpAnim by animateFloatAsState(
        targetValue = (battle.monsterHp.toFloat() / battle.monsterMaxHp.toFloat()).coerceIn(0f, 1f),
        label = "monsterHp"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        ObsidianVoid,
                        if (battle.monster.isBoss) Color(0xFF21122E) else section.floorTint,
                        ObsidianVoid
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (battle.monster.isBoss) "BOSS ENCOUNTER" else "AUTOMATED BATTLE",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (battle.monster.isBoss) BossViolet else BurnishedGold
                )
                Text(
                    text = "Sec ${section.sectionNumber}: ${section.name}",
                    style = MaterialTheme.typography.titleMedium,
                    color = ParchmentIvory,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            FilledTonalButton(
                onClick = onToggleSpeed,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (battle.speedMultiplier > 1) BurnishedGold else ElevatedObsidian,
                    contentColor = if (battle.speedMultiplier > 1) ObsidianVoid else ParchmentIvory
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("speed_toggle_button")
            ) {
                Icon(Icons.Filled.FastForward, contentDescription = "Toggle Battle Speed", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("${battle.speedMultiplier}x", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        }

        // Main Combat Arena Stage
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(ElevatedObsidian, ObsidianVoid)
                    )
                )
                .border(
                    1.5.dp,
                    if (battle.monster.isBoss) BossViolet.copy(alpha = 0.7f) else StoneBorder,
                    RoundedCornerShape(22.dp)
                )
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hero Combatant Side
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Image(
                            painter = painterResource(id = hero.heroClass.portraitRes),
                            contentDescription = hero.heroClass.displayName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(92.dp)
                                .clip(CircleShape)
                                .border(2.5.dp, hero.heroClass.accentColor, CircleShape)
                        )
                        if (battle.heroGuardMs > 0L) {
                            Surface(
                                color = ArcaneSapphire,
                                shape = CircleShape,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Filled.Shield,
                                        contentDescription = "Guard Active",
                                        tint = ObsidianVoid,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "${hero.heroClass.displayName} Lv.${hero.level}",
                        style = MaterialTheme.typography.titleMedium,
                        color = ParchmentIvory
                    )
                    Text(
                        text = hero.equippedWeapon.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = hero.equippedWeapon.definition.rarity.color,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { heroHpAnim },
                        color = if (heroHpAnim < 0.3f) EmberCrimson else VerdantEmerald,
                        trackColor = ObsidianVoid,
                        modifier = Modifier
                            .fillMaxWidth(0.88f)
                            .height(11.dp)
                            .clip(RoundedCornerShape(6.dp))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${battle.heroHp} / ${battle.heroMaxHp}",
                        style = MaterialTheme.typography.labelSmall,
                        color = ParchmentIvory
                    )
                }

                // VS divider
                Text(
                    text = "VS",
                    style = MaterialTheme.typography.titleLarge,
                    color = StoneBorder,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                // Single Monster Combatant Side
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MonsterSpriteCanvas(
                        monster = battle.monster,
                        isStunned = battle.monsterStunMs > 0L,
                        hitFlash = battle.floatingNumbers.any { it.onEnemy && it.ageMs < 130L },
                        modifier = Modifier.size(100.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Lv.${battle.monster.level} ${battle.monster.name}",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (battle.monster.isBoss) BurnishedGold else ParchmentIvory,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (battle.monster.bossTitle != null) {
                        Text(
                            text = battle.monster.bossTitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = BossViolet,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { monsterHpAnim },
                        color = if (battle.monster.isBoss) BossViolet else EmberCrimson,
                        trackColor = ObsidianVoid,
                        modifier = Modifier
                            .fillMaxWidth(0.88f)
                            .height(11.dp)
                            .clip(RoundedCornerShape(6.dp))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${battle.monsterHp} / ${battle.monsterMaxHp}",
                        style = MaterialTheme.typography.labelSmall,
                        color = ParchmentIvory
                    )
                    // Status Debuffs Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        if (battle.monsterStunMs > 0L) {
                            StatusChip("STUN", BurnishedGold)
                        }
                        if (battle.monsterBurnTicks > 0) {
                            StatusChip("BURN", TorchAmber)
                        }
                        if (battle.monsterPoisonTicks > 0) {
                            StatusChip("POISON", VerdantEmerald)
                        }
                    }
                }
            }

            // Animated Skill VFX Overlay
            BattleVfxOverlay(
                vfxList = battle.vfxList,
                modifier = Modifier.fillMaxSize()
            )

            // Floating Damage Numbers
            battle.floatingNumbers.forEach { num ->
                val riseDp = (-14 - (num.progress * 44f)).dp
                val alpha = (1f - num.progress * 0.75f).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    contentAlignment = if (num.onEnemy) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Text(
                        text = num.text,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = if (num.isCrit) 19.sp else 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = num.color.copy(alpha = alpha),
                        modifier = Modifier.offset(y = riseDp)
                    )
                }
            }
        }

        // Automated 4-Skill Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardObsidian)
                .border(1.dp, StoneBorder, RoundedCornerShape(16.dp))
                .padding(10.dp)
        ) {
            Text(
                text = "HERO AUTO-SKILLS",
                style = MaterialTheme.typography.labelSmall,
                color = AshMuted
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                hero.skills.forEachIndexed { idx, skillState ->
                    val rem = battle.skillCooldownRemainingMs.getOrElse(idx) { 0L }
                    val total = battle.skillTotalCooldownMs.getOrElse(idx) { 1000L }.coerceAtLeast(1L)
                    val readyRatio = 1f - (rem.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                    val justFired = battle.lastTriggeredSkillId == skillState.definition.id

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (justFired) skillState.definition.accentColor.copy(alpha = 0.25f)
                                else ElevatedObsidian
                            )
                            .border(
                                width = if (justFired) 1.8.dp else 1.dp,
                                color = if (justFired) skillState.definition.accentColor else StoneBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = skillState.definition.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = ParchmentIvory,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        LinearProgressIndicator(
                            progress = { readyRatio },
                            color = skillState.definition.accentColor,
                            trackColor = ObsidianVoid,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = if (rem <= 0L) "READY" else "${"%.1f".format(rem / 1000f)}s",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (rem <= 0L) BurnishedGold else AshMuted,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }

        // Compact Combat Log
        Surface(
            color = CardObsidian.copy(alpha = 0.85f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, StoneBorder, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                battle.combatLog.take(2).forEachIndexed { idx, line ->
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (idx == 0) ParchmentIvory else AshMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Outcome Banner (when Victory or Defeat)
        AnimatedVisibility(visible = battle.outcome != BattleOutcome.ONGOING) {
            val isVictory = battle.outcome == BattleOutcome.VICTORY
            Surface(
                color = if (isVictory) ElevatedObsidian else Color(0xFF2E1216),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.5.dp,
                        if (isVictory) BurnishedGold else EmberCrimson,
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isVictory) "VICTORY!" else "FALLEN IN BATTLE",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isVictory) BurnishedGold else EmberCrimson
                        )
                        if (isVictory) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.MonetizationOn, contentDescription = null, tint = BurnishedGold, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "+${battle.goldEarned} Gold  •  +${battle.xpEarned} XP",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ParchmentIvory
                                )
                            }
                        } else {
                            Text(
                                text = "Slain by Lv.${battle.monster.level} ${battle.monster.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AshMuted
                            )
                        }
                    }
                    Button(
                        onClick = onCollectOutcome,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isVictory) BurnishedGold else EmberCrimson,
                            contentColor = ObsidianVoid
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("collect_victory_button")
                    ) {
                        Text(
                            text = if (isVictory) "Claim Loot" else "Summary",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(label: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.border(1.dp, color.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontSize = 9.sp,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
        )
    }
}
