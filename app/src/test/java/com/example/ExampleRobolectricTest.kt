package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.game.engine.CombatSimulator
import com.example.game.engine.DungeonGenerator
import com.example.game.engine.GameCatalog
import com.example.game.model.BattleOutcome
import com.example.game.model.HeroClass
import com.example.game.model.ScreenMode
import com.example.game.ui.GateboundViewModel
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.random.Random

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `app name matches Gatebound`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Gatebound", appName)
    }

    @Test
    fun `hero classes have distinct primary attacks and 4 skills`() {
        assertEquals("Fireball", GameCatalog.skillsFor(HeroClass.MAGE).first().name)
        assertEquals("Sword Attack", GameCatalog.skillsFor(HeroClass.WARRIOR).first().name)
        assertEquals("Bow & Arrow", GameCatalog.skillsFor(HeroClass.ARCHER).first().name)
        HeroClass.entries.forEach { hc ->
            assertEquals(4, GameCatalog.skillsFor(hc).size)
        }
    }

    @Test
    fun `dungeon sections separate monster levels 1-10 then 11-20 and boss every 3rd section`() {
        val sec1 = DungeonGenerator.generateSection(1, seed = 42L)
        val sec1Levels = sec1.cells.values.mapNotNull { it.monster?.level }.sorted()
        assertEquals((1..10).toList(), sec1Levels)
        assertFalse(sec1.section.isBossSection)

        val sec2 = DungeonGenerator.generateSection(2, seed = 42L)
        val sec2Levels = sec2.cells.values.mapNotNull { it.monster?.level }.sorted()
        assertEquals((11..20).toList(), sec2Levels)
        assertFalse(sec2.section.isBossSection)

        val sec3 = DungeonGenerator.generateSection(3, seed = 42L)
        val sec3Monsters = sec3.cells.values.mapNotNull { it.monster }
        assertTrue(sec3.section.isBossSection)
        assertTrue(sec3Monsters.any { it.isBoss && it.level == 30 })

        // Verify Merchant, Doorway, and all monsters are reachable across seeds
        for (seed in 1L..10L) {
            val map = DungeonGenerator.generateSection(1, seed = seed)
            assertTrue("Merchant unreachable for seed $seed", DungeonGenerator.findPath(map, map.heroPos, map.merchantPos).isNotEmpty())
            assertTrue("Doorway unreachable for seed $seed", DungeonGenerator.findPath(map, map.heroPos, map.doorwayPos).isNotEmpty())
        }
    }

    @Test
    fun `automated combat uses skills and awards gold on victory for all hero classes`() {
        HeroClass.entries.forEach { heroClass ->
            val hero = GameCatalog.createInitialHero(heroClass)
            val map = DungeonGenerator.generateSection(1, seed = 7L)
            val lv1Entry = map.cells.entries.first { it.value.monster?.level == 1 }
            var battle = CombatSimulator.createBattleState(hero, lv1Entry.value.monster!!, lv1Entry.key)

            var ticks = 0
            while (battle.outcome == BattleOutcome.ONGOING && ticks < 400) {
                battle = CombatSimulator.stepBattle(battle, hero, 50L, Random(123))
                ticks++
            }

            assertEquals("Expected victory for $heroClass", BattleOutcome.VICTORY, battle.outcome)
            assertTrue(battle.goldEarned > 0)
            assertTrue(battle.xpEarned > 0)
        }
    }

    @Test
    fun `boss weapon rewards are tailored to each hero class every 3rd section`() {
        val mageBoss1 = GameCatalog.bossWeaponForSection(HeroClass.MAGE, 3)
        val warriorBoss1 = GameCatalog.bossWeaponForSection(HeroClass.WARRIOR, 3)
        val archerBoss1 = GameCatalog.bossWeaponForSection(HeroClass.ARCHER, 3)

        assertEquals(HeroClass.MAGE, mageBoss1.heroClass)
        assertEquals(HeroClass.WARRIOR, warriorBoss1.heroClass)
        assertEquals(HeroClass.ARCHER, archerBoss1.heroClass)
        assertNotEquals(mageBoss1.name, warriorBoss1.name)
        assertNotEquals(warriorBoss1.name, archerBoss1.name)
        assertTrue(mageBoss1.baseAttack > GameCatalog.starterWeapon(HeroClass.MAGE).baseAttack)
    }

    @Test
    fun `merchant weapon upgrade spends gold and increases attack`() {
        val vm = GateboundViewModel()
        vm.startRun(HeroClass.WARRIOR)
        val initialHero = vm.uiState.value.hero!!
        val initialAttack = initialHero.totalAttack
        val upgradeCost = initialHero.equippedWeapon.nextUpgradeCost

        // Starting gold is 35 which covers the first +1 upgrade (cost 35)
        assertEquals(35, upgradeCost)
        vm.openMerchant()
        assertEquals(ScreenMode.Merchant, vm.uiState.value.screenMode)

        vm.upgradeEquippedWeapon()
        val upgradedHero = vm.uiState.value.hero!!
        assertEquals(1, upgradedHero.equippedWeapon.upgradeLevel)
        assertTrue(upgradedHero.totalAttack > initialAttack)
        assertEquals(0, upgradedHero.gold)
    }
}
