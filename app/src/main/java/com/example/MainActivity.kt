package com.example

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.game.model.ScreenMode
import com.example.game.ui.GateboundViewModel
import com.example.game.ui.screens.*
import com.example.ui.theme.GateboundTheme
import com.example.ui.theme.ObsidianVoid

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        setContent {
            GateboundTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ObsidianVoid
                ) {
                    GateboundApp()
                }
            }
        }
    }
}

@Composable
fun GateboundApp(
    viewModel: GateboundViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Crossfade(targetState = state.screenMode, label = "screenTransition") { mode ->
        when (mode) {
            ScreenMode.ClassSelection -> {
                ClassSelectionScreen(
                    selectedClass = state.selectedClassPreview,
                    onSelectClass = viewModel::selectClassPreview,
                    onStartRun = { viewModel.startRun(state.selectedClassPreview) }
                )
            }

            ScreenMode.DungeonExplore -> {
                val hero = state.hero
                val map = state.dungeonMap
                if (hero != null && map != null) {
                    DungeonScreen(
                        hero = hero,
                        map = map,
                        statusToast = state.statusToast,
                        onMove = viewModel::moveHero,
                        onTapTile = viewModel::tapGridTile,
                        onOpenMerchant = viewModel::openMerchant,
                        onOpenGear = viewModel::openHeroGear,
                        onEnterDoorway = viewModel::attemptEnterDoorway,
                        onDismissToast = viewModel::clearStatusToast
                    )
                }
            }

            ScreenMode.AutoBattle -> {
                val hero = state.hero
                val battle = state.activeBattle
                val section = state.dungeonMap?.section
                if (hero != null && battle != null && section != null) {
                    AutoBattleScreen(
                        hero = hero,
                        battle = battle,
                        section = section,
                        onToggleSpeed = viewModel::toggleBattleSpeed,
                        onCollectOutcome = viewModel::collectBattleVictory
                    )
                }
            }

            ScreenMode.Merchant -> {
                val hero = state.hero
                val sectionNum = state.dungeonMap?.section?.sectionNumber ?: 1
                if (hero != null) {
                    MerchantScreen(
                        hero = hero,
                        sectionNumber = sectionNum,
                        elixirCost = viewModel.elixirCost(sectionNum),
                        charmCost = viewModel.charmCost(sectionNum),
                        statusToast = state.statusToast,
                        onUpgradeWeapon = viewModel::upgradeEquippedWeapon,
                        onEquipWeapon = viewModel::equipWeapon,
                        onUpgradeSkill = viewModel::upgradeSkill,
                        onBuyElixir = viewModel::buyElixir,
                        onBuyCharm = viewModel::buyBattleCharm,
                        onBackToDungeon = viewModel::returnToDungeon
                    )
                }
            }

            ScreenMode.HeroGear -> {
                val hero = state.hero
                if (hero != null) {
                    HeroGearScreen(
                        hero = hero,
                        onEquipWeapon = viewModel::equipWeapon,
                        onBack = viewModel::returnToDungeon
                    )
                }
            }

            is ScreenMode.BossReward -> {
                BossRewardScreen(
                    reward = mode,
                    onClaim = viewModel::claimBossReward
                )
            }

            is ScreenMode.RunOutcome -> {
                val hero = state.hero
                if (hero != null) {
                    RunOutcomeScreen(
                        victory = mode.victory,
                        hero = hero,
                        stats = state.runStats,
                        onContinueEndless = viewModel::continueEndlessAfterVictory,
                        onNewRun = viewModel::resetToClassSelection
                    )
                }
            }
        }
    }
}
