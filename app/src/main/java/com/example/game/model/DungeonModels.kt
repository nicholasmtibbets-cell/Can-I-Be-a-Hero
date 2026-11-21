package com.example.game.model

import androidx.compose.ui.graphics.Color

enum class MonsterArchetype {
    BEAST,
    SLIME,
    SKELETON,
    CULTIST,
    GOLEM,
    WRAITH,
    AUTOMATON,
    DRAGONKIN,
    OVERLORD_BOSS
}

data class MonsterInstance(
    val id: String,
    val name: String,
    val level: Int,
    val archetype: MonsterArchetype,
    val maxHp: Int,
    val attack: Int,
    val defense: Int,
    val attackIntervalMs: Long,
    val goldReward: Int,
    val xpReward: Int,
    val isBoss: Boolean = false,
    val isGatekeeper: Boolean = false,
    val bossTitle: String? = null
)

data class SectionBiome(
    val sectionNumber: Int,
    val name: String,
    val subtitle: String,
    val minMonsterLevel: Int,
    val maxMonsterLevel: Int,
    val isBossSection: Boolean,
    val isEndGameSection: Boolean,
    val accentColor: Color,
    val floorTint: Color
)

enum class TileType {
    WALL,
    FLOOR,
    MERCHANT,
    SHRINE,
    CHEST,
    DOORWAY
}

data class GridPos(val x: Int, val y: Int) {
    fun manhattanDistance(other: GridPos): Int =
        kotlin.math.abs(x - other.x) + kotlin.math.abs(y - other.y)
}

data class DungeonCell(
    val pos: GridPos,
    val type: TileType,
    val monster: MonsterInstance? = null,
    val consumed: Boolean = false,
    val chestGold: Int = 0
)

data class DungeonMap(
    val width: Int,
    val height: Int,
    val section: SectionBiome,
    val cells: Map<GridPos, DungeonCell>,
    val heroPos: GridPos,
    val doorwayPos: GridPos,
    val merchantPos: GridPos
) {
    val monstersRemaining: Int
        get() = cells.values.count { it.monster != null }

    val guardianDefeated: Boolean
        get() = cells.values.none { it.monster != null && (it.monster.isBoss || it.monster.isGatekeeper) }

    val doorwayUnlocked: Boolean
        get() = guardianDefeated
}
