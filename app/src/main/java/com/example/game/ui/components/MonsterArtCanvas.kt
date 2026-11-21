package com.example.game.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.game.model.*
import com.example.ui.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MonsterSpriteCanvas(
    monster: MonsterInstance,
    isStunned: Boolean,
    hitFlash: Boolean,
    modifier: Modifier = Modifier
) {
    val infinite = rememberInfiniteTransition(label = "monsterIdle")
    val bob by infinite.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bob"
    )

    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f + bob
        val r = size.minDimension * 0.34f

        val baseColor = when {
            hitFlash -> ParchmentIvory
            monster.isBoss -> BossViolet
            monster.archetype == MonsterArchetype.SLIME -> VerdantEmerald
            monster.archetype == MonsterArchetype.SKELETON -> Color(0xFFD7D2E0)
            monster.archetype == MonsterArchetype.CULTIST -> Color(0xFF8A4FFF)
            monster.archetype == MonsterArchetype.GOLEM -> Color(0xFF7B6F8C)
            monster.archetype == MonsterArchetype.WRAITH -> ArcaneSapphire
            monster.archetype == MonsterArchetype.AUTOMATON -> BurnishedGold
            monster.archetype == MonsterArchetype.DRAGONKIN -> EmberCrimson
            else -> Color(0xFFC4514B)
        }

        // Boss / Monster aura
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    (if (monster.isBoss) BossViolet else baseColor).copy(alpha = 0.34f),
                    Color.Transparent
                ),
                center = Offset(cx, cy),
                radius = r * 1.55f
            ),
            radius = r * 1.55f,
            center = Offset(cx, cy)
        )

        when (monster.archetype) {
            MonsterArchetype.SLIME -> {
                val slimePath = Path().apply {
                    moveTo(cx - r * 1.05f, cy + r * 0.6f)
                    quadraticTo(cx - r * 1.1f, cy - r * 0.85f, cx, cy - r * 0.85f)
                    quadraticTo(cx + r * 1.1f, cy - r * 0.85f, cx + r * 1.05f, cy + r * 0.6f)
                    close()
                }
                drawPath(slimePath, color = baseColor.copy(alpha = 0.88f))
                drawPath(slimePath, color = ParchmentIvory.copy(alpha = 0.5f), style = Stroke(width = 3f))
            }
            MonsterArchetype.GOLEM, MonsterArchetype.AUTOMATON -> {
                drawRoundRect(
                    color = baseColor,
                    topLeft = Offset(cx - r * 0.88f, cy - r * 0.85f),
                    size = Size(r * 1.76f, r * 1.7f),
                    cornerRadius = CornerRadius(18f, 18f)
                )
                drawRoundRect(
                    color = ObsidianVoid,
                    topLeft = Offset(cx - r * 0.6f, cy - r * 0.35f),
                    size = Size(r * 1.2f, r * 0.42f),
                    cornerRadius = CornerRadius(8f, 8f)
                )
            }
            MonsterArchetype.DRAGONKIN, MonsterArchetype.OVERLORD_BOSS -> {
                // Horns + Winged Silhouette
                val wingL = Path().apply {
                    moveTo(cx, cy)
                    lineTo(cx - r * 1.4f, cy - r * 0.9f)
                    lineTo(cx - r * 1.05f, cy + r * 0.35f)
                    close()
                }
                val wingR = Path().apply {
                    moveTo(cx, cy)
                    lineTo(cx + r * 1.4f, cy - r * 0.9f)
                    lineTo(cx + r * 1.05f, cy + r * 0.35f)
                    close()
                }
                drawPath(wingL, color = baseColor.copy(alpha = 0.55f))
                drawPath(wingR, color = baseColor.copy(alpha = 0.55f))
                drawCircle(color = baseColor, radius = r * 0.9f, center = Offset(cx, cy))
                // Crown Horns
                val hornL = Path().apply {
                    moveTo(cx - r * 0.5f, cy - r * 0.65f)
                    lineTo(cx - r * 0.95f, cy - r * 1.35f)
                    lineTo(cx - r * 0.15f, cy - r * 0.8f)
                    close()
                }
                val hornR = Path().apply {
                    moveTo(cx + r * 0.5f, cy - r * 0.65f)
                    lineTo(cx + r * 0.95f, cy - r * 1.35f)
                    lineTo(cx + r * 0.15f, cy - r * 0.8f)
                    close()
                }
                drawPath(hornL, color = BurnishedGold)
                drawPath(hornR, color = BurnishedGold)
            }
            else -> {
                // Beasts, Skeletons, Cultists, Wraiths
                drawCircle(color = baseColor, radius = r * 0.85f, center = Offset(cx, cy))
                drawCircle(
                    color = ParchmentIvory.copy(alpha = 0.45f),
                    radius = r * 0.85f,
                    center = Offset(cx, cy),
                    style = Stroke(width = 2.5f)
                )
            }
        }

        // Fierce Eyes
        val eyeColor = if (isStunned) AshMuted else if (monster.isBoss) BurnishedGold else TorchAmber
        drawCircle(color = eyeColor, radius = r * 0.14f, center = Offset(cx - r * 0.32f, cy - r * 0.12f))
        drawCircle(color = eyeColor, radius = r * 0.14f, center = Offset(cx + r * 0.32f, cy - r * 0.12f))

        if (isStunned) {
            drawCircle(
                color = BurnishedGold,
                radius = r * 1.08f,
                center = Offset(cx, cy - r * 0.7f),
                style = Stroke(width = 3f)
            )
        }
    }
}

@Composable
fun BattleVfxOverlay(
    vfxList: List<CombatVfx>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val heroOrigin = Offset(size.width * 0.22f, size.height * 0.55f)
        val enemyCenter = Offset(size.width * 0.78f, size.height * 0.52f)

        for (fx in vfxList) {
            val p = fx.progress
            val alpha = (1f - p * 0.7f).coerceIn(0f, 1f)
            when (fx.style) {
                VfxStyle.FIREBALL, VfxStyle.INFERNO_NOVA -> {
                    val cur = Offset(
                        heroOrigin.x + (enemyCenter.x - heroOrigin.x) * p,
                        heroOrigin.y + (enemyCenter.y - heroOrigin.y) * p
                    )
                    val radius = if (fx.style == VfxStyle.INFERNO_NOVA) 24f + p * 85f else 22f + p * 24f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(BurnishedGold.copy(alpha = alpha), fx.color.copy(alpha = alpha), Color.Transparent),
                            center = if (fx.style == VfxStyle.INFERNO_NOVA) enemyCenter else cur,
                            radius = radius * 1.6f
                        ),
                        radius = radius * 1.6f,
                        center = if (fx.style == VfxStyle.INFERNO_NOVA) enemyCenter else cur
                    )
                }
                VfxStyle.ARCANE_LANCE -> {
                    drawLine(
                        color = fx.color.copy(alpha = alpha),
                        start = heroOrigin,
                        end = enemyCenter,
                        strokeWidth = 14f * (1f - p * 0.5f),
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = ParchmentIvory.copy(alpha = alpha),
                        start = heroOrigin,
                        end = enemyCenter,
                        strokeWidth = 5f * (1f - p * 0.5f),
                        cap = StrokeCap.Round
                    )
                }
                VfxStyle.METEOR -> {
                    repeat(3) { i ->
                        val offsetX = (i - 1) * 32f
                        val startY = 0f
                        val pos = Offset(enemyCenter.x + offsetX, startY + (enemyCenter.y - startY) * p)
                        drawCircle(
                            color = fx.color.copy(alpha = alpha),
                            radius = 18f,
                            center = pos
                        )
                    }
                }
                VfxStyle.SWORD_SLASH, VfxStyle.WHIRLWIND, VfxStyle.EXECUTION_SLAM -> {
                    val sweepR = if (fx.style == VfxStyle.EXECUTION_SLAM) 95f else 70f
                    drawArc(
                        color = fx.color.copy(alpha = alpha),
                        startAngle = -60f + p * 90f,
                        sweepAngle = 140f,
                        useCenter = false,
                        topLeft = Offset(enemyCenter.x - sweepR, enemyCenter.y - sweepR),
                        size = Size(sweepR * 2f, sweepR * 2f),
                        style = Stroke(width = 12f, cap = StrokeCap.Round)
                    )
                    if (fx.style == VfxStyle.WHIRLWIND) {
                        drawCircle(
                            color = TorchAmber.copy(alpha = alpha * 0.7f),
                            radius = sweepR * (0.6f + p * 0.4f),
                            center = enemyCenter,
                            style = Stroke(width = 6f)
                        )
                    }
                }
                VfxStyle.SHIELD_BASH -> {
                    drawCircle(
                        color = BurnishedGold.copy(alpha = alpha * 0.75f),
                        radius = 30f + p * 60f,
                        center = enemyCenter,
                        style = Stroke(width = 8f)
                    )
                }
                VfxStyle.ARROW_SHOT, VfxStyle.ARROW_VOLLEY, VfxStyle.VENOM_ARROW, VfxStyle.STARFALL_ARROW -> {
                    val count = if (fx.style == VfxStyle.ARROW_VOLLEY) 3 else 1
                    repeat(count) { i ->
                        val spread = (i - (count - 1) / 2f) * 26f
                        val cur = Offset(
                            heroOrigin.x + (enemyCenter.x - heroOrigin.x) * p,
                            heroOrigin.y + spread * p + (enemyCenter.y - heroOrigin.y) * p
                        )
                        drawArrowShaft(cur, fx.color.copy(alpha = alpha), fx.style == VfxStyle.STARFALL_ARROW)
                    }
                    if (fx.style == VfxStyle.STARFALL_ARROW) {
                        drawLine(
                            color = BurnishedGold.copy(alpha = alpha * 0.85f),
                            start = Offset(enemyCenter.x, 0f),
                            end = enemyCenter,
                            strokeWidth = 10f,
                            cap = StrokeCap.Round
                        )
                    }
                }
                VfxStyle.ENEMY_STRIKE -> {
                    drawSlashX(heroOrigin, fx.color.copy(alpha = alpha), 44f)
                }
            }
        }
    }
}

private fun DrawScope.drawArrowShaft(tip: Offset, color: Color, isStarfall: Boolean) {
    val tail = Offset(tip.x - 42f, tip.y)
    drawLine(
        color = color,
        start = tail,
        end = tip,
        strokeWidth = if (isStarfall) 8f else 5f,
        cap = StrokeCap.Round
    )
    val head = Path().apply {
        moveTo(tip.x + 12f, tip.y)
        lineTo(tip.x - 6f, tip.y - 8f)
        lineTo(tip.x - 6f, tip.y + 8f)
        close()
    }
    drawPath(head, color = ParchmentIvory.copy(alpha = color.alpha))
}

private fun DrawScope.drawSlashX(center: Offset, color: Color, radius: Float) {
    val angle = (PI / 4.0).toFloat()
    val dx = cos(angle) * radius
    val dy = sin(angle) * radius
    drawLine(color, Offset(center.x - dx, center.y - dy), Offset(center.x + dx, center.y + dy), strokeWidth = 8f, cap = StrokeCap.Round)
    drawLine(color, Offset(center.x - dx, center.y + dy), Offset(center.x + dx, center.y - dy), strokeWidth = 8f, cap = StrokeCap.Round)
}
