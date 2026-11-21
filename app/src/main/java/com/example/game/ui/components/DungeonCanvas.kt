package com.example.game.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.model.*
import com.example.ui.theme.*
import kotlin.math.min

@Composable
fun DungeonCanvas(
    map: DungeonMap,
    heroClass: HeroClass,
    onTileTap: (GridPos) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val infiniteTransition = rememberInfiniteTransition(label = "dungeonPulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val labelStyle = MaterialTheme.typography.labelSmall.copy(
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(ObsidianVoid)
            .border(1.5.dp, StoneBorder, RoundedCornerShape(18.dp))
            .testTag("dungeon_grid_canvas")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(map.width, map.height, map.section.sectionNumber) {
                    detectTapGestures { offset ->
                        val tileSize = min(
                            size.width.toFloat() / map.width,
                            size.height.toFloat() / map.height
                        )
                        val boardW = tileSize * map.width
                        val boardH = tileSize * map.height
                        val originX = (size.width - boardW) / 2f
                        val originY = (size.height - boardH) / 2f

                        val gx = ((offset.x - originX) / tileSize).toInt()
                        val gy = ((offset.y - originY) / tileSize).toInt()
                        if (gx in 0 until map.width && gy in 0 until map.height) {
                            onTileTap(GridPos(gx, gy))
                        }
                    }
                }
        ) {
            val tileSize = min(size.width / map.width, size.height / map.height)
            val boardW = tileSize * map.width
            val boardH = tileSize * map.height
            val originX = (size.width - boardW) / 2f
            val originY = (size.height - boardH) / 2f

            // Subtle ambient torchlight behind the hero
            val heroCenter = Offset(
                originX + (map.heroPos.x + 0.5f) * tileSize,
                originY + (map.heroPos.y + 0.5f) * tileSize
            )

            for (y in 0 until map.height) {
                for (x in 0 until map.width) {
                    val pos = GridPos(x, y)
                    val cell = map.cells[pos] ?: continue
                    val left = originX + x * tileSize
                    val top = originY + y * tileSize
                    val pad = tileSize * 0.05f
                    val innerSize = tileSize - pad * 2f
                    val tileCenter = Offset(left + tileSize / 2f, top + tileSize / 2f)

                    if (cell.type == TileType.WALL) {
                        drawRoundRect(
                            color = Color(0xFF100E17),
                            topLeft = Offset(left + pad, top + pad),
                            size = Size(innerSize, innerSize),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                        drawRoundRect(
                            color = Color(0xFF2B253A),
                            topLeft = Offset(left + pad, top + pad),
                            size = Size(innerSize, innerSize * 0.72f),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                        drawRoundRect(
                            color = Color(0xFF423A58).copy(alpha = 0.4f),
                            topLeft = Offset(left + pad, top + pad),
                            size = Size(innerSize, innerSize),
                            cornerRadius = CornerRadius(6f, 6f),
                            style = Stroke(width = 1.5f)
                        )
                        continue
                    }

                    // Floor tile
                    val checker = if ((x + y) % 2 == 0) 1.0f else 0.85f
                    val floorColor = map.section.floorTint.copy(
                        red = map.section.floorTint.red * checker,
                        green = map.section.floorTint.green * checker,
                        blue = map.section.floorTint.blue * checker
                    )
                    drawRoundRect(
                        color = floorColor,
                        topLeft = Offset(left + pad, top + pad),
                        size = Size(innerSize, innerSize),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                    drawRoundRect(
                        color = StoneBorder.copy(alpha = 0.45f),
                        topLeft = Offset(left + pad, top + pad),
                        size = Size(innerSize, innerSize),
                        cornerRadius = CornerRadius(6f, 6f),
                        style = Stroke(width = 1f)
                    )

                    // Special Tile Overlays
                    when (cell.type) {
                        TileType.MERCHANT -> {
                            drawCircle(
                                color = BurnishedGold.copy(alpha = 0.22f * pulse),
                                radius = tileSize * 0.42f,
                                center = tileCenter
                            )
                            // Merchant tent / anvil diamond
                            val r = tileSize * 0.26f
                            val anvil = Path().apply {
                                moveTo(tileCenter.x, tileCenter.y - r)
                                lineTo(tileCenter.x + r, tileCenter.y)
                                lineTo(tileCenter.x, tileCenter.y + r)
                                lineTo(tileCenter.x - r, tileCenter.y)
                                close()
                            }
                            drawPath(anvil, color = BurnishedGold)
                            val tag = textMeasurer.measure("SHOP", labelStyle.copy(color = ObsidianVoid, fontSize = 8.sp))
                            drawText(
                                tag,
                                topLeft = Offset(tileCenter.x - tag.size.width / 2f, tileCenter.y - tag.size.height / 2f)
                            )
                        }
                        TileType.SHRINE -> if (!cell.consumed) {
                            drawCircle(
                                color = VerdantEmerald.copy(alpha = 0.24f * pulse),
                                radius = tileSize * 0.38f,
                                center = tileCenter
                            )
                            val crossW = tileSize * 0.12f
                            val crossL = tileSize * 0.38f
                            drawRoundRect(
                                color = VerdantEmerald,
                                topLeft = Offset(tileCenter.x - crossW / 2f, tileCenter.y - crossL / 2f),
                                size = Size(crossW, crossL),
                                cornerRadius = CornerRadius(4f, 4f)
                            )
                            drawRoundRect(
                                color = VerdantEmerald,
                                topLeft = Offset(tileCenter.x - crossL / 2f, tileCenter.y - crossW / 2f),
                                size = Size(crossL, crossW),
                                cornerRadius = CornerRadius(4f, 4f)
                            )
                        }
                        TileType.CHEST -> if (!cell.consumed) {
                            drawCircle(
                                color = TorchAmber.copy(alpha = 0.22f * pulse),
                                radius = tileSize * 0.36f,
                                center = tileCenter
                            )
                            val cw = tileSize * 0.48f
                            val ch = tileSize * 0.36f
                            drawRoundRect(
                                color = Color(0xFF7A4B1D),
                                topLeft = Offset(tileCenter.x - cw / 2f, tileCenter.y - ch / 2f),
                                size = Size(cw, ch),
                                cornerRadius = CornerRadius(6f, 6f)
                            )
                            drawRoundRect(
                                color = BurnishedGold,
                                topLeft = Offset(tileCenter.x - cw / 2f, tileCenter.y - ch / 2f),
                                size = Size(cw, ch),
                                cornerRadius = CornerRadius(6f, 6f),
                                style = Stroke(width = 2.5f)
                            )
                            drawCircle(
                                color = BurnishedGold,
                                radius = tileSize * 0.06f,
                                center = tileCenter
                            )
                        }
                        TileType.DOORWAY -> {
                            val unlocked = map.doorwayUnlocked
                            val portalColor = if (unlocked) BurnishedGold else EmberCrimson
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        portalColor.copy(alpha = if (unlocked) 0.55f * pulse else 0.30f),
                                        Color.Transparent
                                    ),
                                    center = tileCenter,
                                    radius = tileSize * 0.5f
                                ),
                                radius = tileSize * 0.5f,
                                center = tileCenter
                            )
                            // Archway frame
                            val archW = tileSize * 0.56f
                            val archH = tileSize * 0.66f
                            drawRoundRect(
                                color = if (unlocked) portalColor.copy(alpha = 0.28f) else ObsidianVoid.copy(alpha = 0.7f),
                                topLeft = Offset(tileCenter.x - archW / 2f, tileCenter.y - archH / 2f),
                                size = Size(archW, archH),
                                cornerRadius = CornerRadius(archW / 2f, archW / 2f)
                            )
                            drawRoundRect(
                                color = portalColor,
                                topLeft = Offset(tileCenter.x - archW / 2f, tileCenter.y - archH / 2f),
                                size = Size(archW, archH),
                                cornerRadius = CornerRadius(archW / 2f, archW / 2f),
                                style = Stroke(width = 3f)
                            )
                            val gateText = textMeasurer.measure(
                                if (unlocked) "GATE" else "LOCK",
                                labelStyle.copy(color = ParchmentIvory, fontSize = 8.sp)
                            )
                            drawText(
                                gateText,
                                topLeft = Offset(tileCenter.x - gateText.size.width / 2f, tileCenter.y - gateText.size.height / 2f)
                            )
                        }
                        else -> Unit
                    }

                    // Single Monster Marker
                    val monster = cell.monster
                    if (monster != null) {
                        val tokenColor = when {
                            monster.isBoss -> BossViolet
                            monster.isGatekeeper -> TorchAmber
                            else -> EmberCrimson
                        }
                        val radius = if (monster.isBoss) tileSize * 0.36f else tileSize * 0.30f
                        if (monster.isBoss || monster.isGatekeeper) {
                            drawCircle(
                                color = tokenColor.copy(alpha = 0.30f * pulse),
                                radius = tileSize * 0.46f,
                                center = tileCenter
                            )
                        }
                        drawCircle(
                            color = tokenColor.copy(alpha = 0.90f),
                            radius = radius,
                            center = Offset(tileCenter.x, tileCenter.y - tileSize * 0.05f)
                        )
                        drawCircle(
                            color = ParchmentIvory.copy(alpha = 0.7f),
                            radius = radius,
                            center = Offset(tileCenter.x, tileCenter.y - tileSize * 0.05f),
                            style = Stroke(width = if (monster.isBoss) 3f else 1.5f)
                        )

                        // Eyes on monster token
                        val eyeOffset = radius * 0.34f
                        val eyeY = tileCenter.y - tileSize * 0.08f
                        drawCircle(
                            color = if (monster.isBoss) BurnishedGold else ParchmentIvory,
                            radius = radius * 0.14f,
                            center = Offset(tileCenter.x - eyeOffset, eyeY)
                        )
                        drawCircle(
                            color = if (monster.isBoss) BurnishedGold else ParchmentIvory,
                            radius = radius * 0.14f,
                            center = Offset(tileCenter.x + eyeOffset, eyeY)
                        )

                        // Level badge pill at bottom of tile
                        val badgeLabel = if (monster.isBoss) "BOSS ${monster.level}" else "Lv.${monster.level}"
                        val measured = textMeasurer.measure(
                            badgeLabel,
                            labelStyle.copy(color = ParchmentIvory, fontSize = 8.5.sp)
                        )
                        val pillW = measured.size.width + 10f
                        val pillH = measured.size.height + 3f
                        val pillLeft = tileCenter.x - pillW / 2f
                        val pillTop = top + tileSize - pillH - 3f
                        drawRoundRect(
                            color = ObsidianVoid.copy(alpha = 0.92f),
                            topLeft = Offset(pillLeft, pillTop),
                            size = Size(pillW, pillH),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                        drawRoundRect(
                            color = tokenColor,
                            topLeft = Offset(pillLeft, pillTop),
                            size = Size(pillW, pillH),
                            cornerRadius = CornerRadius(6f, 6f),
                            style = Stroke(width = 1.2f)
                        )
                        drawText(
                            measured,
                            topLeft = Offset(tileCenter.x - measured.size.width / 2f, pillTop + 1.5f)
                        )
                    }
                }
            }

            // Torchlight glow around hero
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(heroClass.accentColor.copy(alpha = 0.35f), Color.Transparent),
                    center = heroCenter,
                    radius = tileSize * 1.45f
                ),
                radius = tileSize * 1.45f,
                center = heroCenter
            )

            // Draw Hero Token
            drawCircle(
                color = heroClass.accentColor,
                radius = tileSize * 0.32f,
                center = heroCenter
            )
            drawCircle(
                color = BurnishedGold,
                radius = tileSize * 0.32f,
                center = heroCenter,
                style = Stroke(width = 3f)
            )
            val heroInitial = when (heroClass) {
                HeroClass.MAGE -> "M"
                HeroClass.WARRIOR -> "W"
                HeroClass.ARCHER -> "A"
            }
            val heroText = textMeasurer.measure(
                heroInitial,
                labelStyle.copy(color = ObsidianVoid, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
            )
            drawText(
                heroText,
                topLeft = Offset(
                    heroCenter.x - heroText.size.width / 2f,
                    heroCenter.y - heroText.size.height / 2f
                )
            )
        }
    }
}
