package id.fc.pratv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import id.fc.pratv.data.SettingsStore
import id.fc.pratv.data.model.Channel
import id.fc.pratv.ui.channels.ChannelsScreen
import id.fc.pratv.ui.player.PlayerScreen
import id.fc.pratv.ui.settings.SettingsScreen
import id.fc.pratv.ui.splash.SplashScreen
import id.fc.pratv.ui.theme.PratvTheme
import id.fc.pratv.ui.theme.ThemeMode
import id.fc.pratv.ui.theme.frameDurationMs
import id.fc.pratv.ui.theme.themeModeFromKey
import id.fc.pratv.ui.welcome.WelcomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeKey by SettingsStore.themeKeyFlow.collectAsStateWithLifecycle(
                initialValue = SettingsStore.getTheme(this)
            )
            PratvTheme(mode = themeModeFromKey(themeKey)) {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val nav = rememberNavController()
    val dur = frameDurationMs(18, LocalContext.current)
    val w = LocalContext.current.resources.displayMetrics.widthPixels
    NavHost(navController = nav, startDestination = "splash") {
        composable(
            "splash",
            enterTransition = { slideInHorizontally(tween(dur)) { 0 } },
            exitTransition = { slideOutHorizontally(tween(dur)) { -w } }
        ) { SplashScreen(nav, application = LocalContext.current.applicationContext as android.app.Application) }
        composable(
            "welcome",
            enterTransition = { slideInHorizontally(tween(dur)) { w } },
            exitTransition = { slideOutHorizontally(tween(dur)) { -w } }
        ) { WelcomeScreen(nav) }
        composable(
            "channels",
            enterTransition = { slideInHorizontally(tween(dur)) { w } },
            exitTransition = { slideOutHorizontally(tween(dur)) { -w } }
        ) { ChannelsScreen(nav) }
        composable(
            "player",
            enterTransition = { slideInHorizontally(tween(dur)) { w } },
            exitTransition = { slideOutHorizontally(tween(dur)) { -w } }
        ) { backStackEntry ->
            val channel = backStackEntry.savedStateHandle.get<Channel>("channel")
            if (channel != null) {
                PlayerScreen(channel = channel, nav = nav)
            } else {
                nav.popBackStack()
            }
        }
        composable(
            "settings",
            enterTransition = { slideInHorizontally(tween(dur)) { w } },
            exitTransition = { slideOutHorizontally(tween(dur)) { -w } }
        ) { SettingsScreen(nav) }
    }
}
