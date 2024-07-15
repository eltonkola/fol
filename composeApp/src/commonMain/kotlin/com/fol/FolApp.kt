package com.fol

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fol.com.fol.db.AppSettings
import com.fol.com.fol.model.AppsScreen
import com.fol.com.fol.model.DiGraph
import com.fol.com.fol.ui.SplashScreen
import com.fol.com.fol.ui.app.addcontact.AddContactScreen
import com.fol.com.fol.ui.app.MainScreen
import com.fol.com.fol.ui.app.thread.ThreadScreen
import com.fol.com.fol.ui.app.profile.ProfileScreen
import com.fol.com.fol.ui.app.settings.SettingsScreen
import com.fol.com.fol.ui.onboarding.createaccount.CreateAccountScreen
import com.fol.com.fol.ui.onboarding.LandingScreen
import com.fol.com.fol.ui.onboarding.recoveraccount.RecoverAccountScreen
import com.fol.ui.theme.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun FolApp(
    navController: NavHostController = rememberNavController(),
    appSettings: AppSettings = DiGraph.appSettings,
    viewModel : AppViewModel = viewModel{ AppViewModel() }
) {

    val themeState by appSettings.themeState.collectAsState()

    val darkTheme = if (themeState.system) {
        isSystemInDarkTheme()
    } else {
        themeState.dark
    }
    AppTheme(
        darkTheme = darkTheme
    ) {
        Surface (
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column {

                NavHost(
                navController = navController,
                startDestination = AppsScreen.Splash.name,
                modifier = Modifier.fillMaxSize()
            ) {
                //splash
                composable(route = AppsScreen.Splash.name) {
                    SplashScreen(navController = navController)
                }
                //onboarding
                composable(route = AppsScreen.Landing.name) {
                    LandingScreen(navController = navController)
                }
                composable(route = AppsScreen.CreateAccount.name) {
                    CreateAccountScreen(navController = navController)
                }
                composable(route = AppsScreen.RecoverAccount.name) {
                    RecoverAccountScreen(navController = navController)
                }
                composable(route = AppsScreen.RecoverAccount.name) {
                    RecoverAccountScreen(navController = navController)
                }
                //app
                composable(route = AppsScreen.Main.name) {
                    MainScreen(navController = navController)
                }
                composable(
                    route = "${AppsScreen.Thread.name}/{userId}"
                ) { backStackEntry ->
                    ThreadScreen(
                        navController = navController,
                        backStackEntry.arguments!!.getString("userId")!!
                    )
                }
                composable(route = AppsScreen.AddContact.name) {
                    AddContactScreen(navController = navController)
                }
                composable(route = AppsScreen.Profile.name) {
                    ProfileScreen(navController = navController)
                }
                composable(route = AppsScreen.Settings.name) {
                    SettingsScreen(navController = navController)
                }
            }
            }
        }
    }
}
