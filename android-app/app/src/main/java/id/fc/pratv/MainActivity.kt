package id.fc.pratv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
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
import id.fc.pratv.ui.theme.themeModeFromKey
import id.fc.pratv.ui.welcome.WelcomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeMode = themeModeFromKey(SettingsStore.getTheme(this))
            PratvTheme(mode = themeMode) {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = "splash") {
        composable("splash") { SplashScreen(nav, application = LocalContext.current.applicationContext as android.app.Application) }
        composable("welcome") { WelcomeScreen(nav) }
        composable("channels") { ChannelsScreen(nav) }
        composable("player") { backStackEntry ->
            val channel = backStackEntry.savedStateHandle.get<Channel>("channel")
            if (channel != null) {
                PlayerScreen(channel = channel, nav = nav)
            } else {
                nav.popBackStack()
            }
        }
        composable("settings") { SettingsScreen(nav) }
    }
}
