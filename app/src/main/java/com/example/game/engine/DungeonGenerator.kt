package com.example.game.engine

import com.example.game.model.*
import java.util.ArrayDeque
import kotlin.random.Random

object DungeonGenerator {

    private const val WIDTH = 7
    private const val HEIGHT = 9

    fun generateSection(sectionNumber: Int, seed: Long = System.nanoTime()): DungeonMap {
        val random = Random(seed + sectionNumber * 997L)
        val section = GameCatalog.sectionBiome(sectionNumber)

        val heroSpawn = GridPos(1, HEIGHT - 2)
        val doorwayPos = GridPos(WIDTH - 2, 1)
        val merchantPos = GridPos(1, HEIGHT - 4)

        val cells = mutableMapOf<GridPos, DungeonCell>()
        for (y in 0 until HEIGHT) {
            for (x in 0 until WIDTH) {
                val pos = GridPos(x, y)
                val isBorder = x == 0 || y == 0 || x == WIDTH - 1 || y == HEIGHT - 1
                cells[pos] = DungeonCell(
                    pos = pos,
                    type = if (isBorder) TileType.WALL else TileType.FLOOR
                )
            }
        }

        // Add subtle dungeon pillars while preserving complete connectivity
        val pillarCandidates = listOf(
            GridPos(2, 2), GridPos(4, 2),
            GridPos(2, 4), GridPos(4, 4),
            GridPos(3, 6)
        ).shuffled(random).take(3)

        for (pillar in pillarCandidates) {
            if (pillar != heroSpawn && pillar != doorwayPos && pillar != merchantPos) {
                cells[pillar] = DungeonCell(pillar, TileType.WALL)
            }
        }

        // Verify connectivity from heroSpawn to all non-wall tiles; if blocked, clear pillars
        if (!isFullyConnected(cells, heroSpawn)) {
            for (pillar in pillarCandidates) {
                cells[pillar] = DungeonCell(pillar, TileType.FLOOR)
            }
        }

        cells[merchantPos] = DungeonCell(merchantPos, TileType.MERCHANT)
        cells[doorwayPos] = DungeonCell(doorwayPos, TileType.DOORWAY)

        // Collect available floor tiles ordered by distance from heroSpawn
        val availableFloors = cells.values
            .filter {
                it.type == TileType.FLOOR &&
                    it.pos != heroSpawn &&
                    it.pos != merchantPos &&
                    it.pos != doorwayPos
            }
            .sortedBy { it.pos.manhattanDistance(heroSpawn) }
            .toMutableList()

        // Reserve 1-2 shrines and 1 treasure chest near mid/upper chamber
        val shrinePositions = mutableListOf<GridPos>()
        if (availableFloors.size >= 14) {
            val midIndex = (availableFloors.size / 2).coerceIn(1, availableFloors.size - 2)
            val shrinePos = availableFloors.removeAt(midIndex).pos
            cells[shrinePos] = DungeonCell(shrinePos, TileType.SHRINE)
            shrinePositions.add(shrinePos)

            val chestIndex = (availableFloors.size * 2 / 3).coerceIn(1, availableFloors.size - 1)
            val chestPos = availableFloors.removeAt(chestIndex).pos
            val chestGold = 20 + sectionNumber * 18
            cells[chestPos] = DungeonCell(chestPos, TileType.CHEST, chestGold = chestGold)
        }

        // Place the 10 single progressively higher-leveled monsters!
        // Lv. maxMonsterLevel (Boss or Gatekeeper) sits directly adjacent to the Doorway.
        val doorNeighbors = listOf(
            GridPos(doorwayPos.x - 1, doorwayPos.y),
            GridPos(doorwayPos.x, doorwayPos.y + 1)
        ).filter { pos ->
            cells[pos]?.type == TileType.FLOOR && pos != heroSpawn && pos != merchantPos
        }

        val guardianLevel = section.maxMonsterLevel
        val guardianMonster = GameCatalog.monsterForLevel(guardianLevel, section)
        val guardianPos = doorNeighbors.firstOrNull() ?: availableFloors.last().pos
        // Keep other doorway-adjacent tile open so the doorway and guardian never create a deadlock
        availableFloors.removeAll { it.pos in doorNeighbors }
        // Keep tiles directly next to heroSpawn and merchantPos free of monsters
        val safeEarlySpots = neighbors(heroSpawn).toSet() + neighbors(merchantPos).toSet()
        val safeMonsterFloors = availableFloors.filter { it.pos !in safeEarlySpots }.toMutableList()
        val candidateFloors = if (safeMonsterFloors.size >= section.maxMonsterLevel - section.minMonsterLevel) {
            safeMonsterFloors
        } else {
            availableFloors
        }

        cells[guardianPos] = DungeonCell(guardianPos, TileType.FLOOR, monster = guardianMonster)

        // Place remaining 9 single monsters in ascending level order along distance from spawn
        val regularLevels = (section.minMonsterLevel until section.maxMonsterLevel).toList()
        val chosenSpots = if (candidateFloors.size >= regularLevels.size) {
            // Evenly sample sorted tiles so Lv.1 is near spawn and Lv.9 is near the doorway
            val step = candidateFloors.size.toFloat() / regularLevels.size.toFloat()
            List(regularLevels.size) { idx ->
                val targetIdx = (idx * step).toInt().coerceIn(0, candidateFloors.lastIndex)
                candidateFloors[targetIdx].pos
            }
        } else {
            candidateFloors.map { it.pos }
        }

        regularLevels.zip(chosenSpots).forEach { (lv, pos) ->
            val monster = GameCatalog.monsterForLevel(lv, section)
            cells[pos] = DungeonCell(pos, TileType.FLOOR, monster = monster)
        }

        return DungeonMap(
            width = WIDTH,
            height = HEIGHT,
            section = section,
            cells = cells,
            heroPos = heroSpawn,
            doorwayPos = doorwayPos,
            merchantPos = merchantPos
        )
    }

    private fun isFullyConnected(cells: Map<GridPos, DungeonCell>, start: GridPos): Boolean {
        val passable = cells.values.filter { it.type != TileType.WALL }.map { it.pos }.toSet()
        val visited = mutableSetOf<GridPos>()
        val queue = ArrayDeque<GridPos>()
        queue.add(start)
        visited.add(start)
        while (queue.isNotEmpty()) {
            val cur = queue.removeFirst()
            for (n in neighbors(cur)) {
                if (n in passable && n !in visited) {
                    visited.add(n)
                    queue.add(n)
                }
            }
        }
        return visited.size == passable.size
    }

    fun findPath(map: DungeonMap, from: GridPos, target: GridPos): List<GridPos> {
        if (from == target) return emptyList()
        val targetCell = map.cells[target] ?: return emptyList()
        if (targetCell.type == TileType.WALL) return emptyList()

        val parent = mutableMapOf<GridPos, GridPos?>()
        val queue = ArrayDeque<GridPos>()
        queue.add(from)
        parent[from] = null

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            if (current == target) break
            for (next in neighbors(current)) {
                val cell = map.cells[next] ?: continue
                if (cell.type == TileType.WALL) continue
                // Don't path *through* another monster unless it's the final target
                if (cell.monster != null && next != target) continue
                if (next !in parent) {
                    parent[next] = current
                    queue.add(next)
                }
            }
        }

        if (target !in parent) return emptyList()
        val path = mutableListOf<GridPos>()
        var step: GridPos? = target
        while (step != null && step != from) {
            path.add(step)
            step = parent[step]
        }
        return path.reversed()
    }

    private fun neighbors(pos: GridPos): List<GridPos> = listOf(
        GridPos(pos.x + 1, pos.y),
        GridPos(pos.x - 1, pos.y),
        GridPos(pos.x, pos.y + 1),
        GridPos(pos.x, pos.y - 1)
    )
}
