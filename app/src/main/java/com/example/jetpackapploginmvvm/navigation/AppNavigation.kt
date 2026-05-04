package com.example.jetpackapploginmvvm.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.PopUpToBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.jetpackapploginmvvm.model.AppDatabase
import com.example.jetpackapploginmvvm.view.ScreenBlackjack
import com.example.jetpackapploginmvvm.view.ScreenLogin
import com.example.jetpackapploginmvvm.view.ScreenRules
import com.example.jetpackapploginmvvm.view.ScreenWelcome
import com.example.jetpackapploginmvvm.viewmodel.BlackjackViewModel
import com.example.jetpackapploginmvvm.viewmodel.LoginViewModel
import com.example.jetpackapploginmvvm.viewmodel.WelcomeViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.jetpackapploginmvvm.model.UserDao

// FUNCIONS AUXILIARS FIRA DE LA UI

// A  FUNCIÓ PER FER LOGOUT (3 nivells de lambdes Esborrades)
// NIVELL 3 de lambdes:
// significat: quan tornis enrera fins a la pantalla X, esborra de memòria també la pantalla X
fun setInclusiveTrue(builder: PopUpToBuilder) {
    builder.inclusive = true
}
// NIVELL 2: La configuració del PopUp
fun configurarPopUpLogin(builder: NavOptionsBuilder) {
    // El constructor de pantalles saltarà a
    //   ruta   Login
    //   amb les instruccions de la funció (amb setInclusive, veure nivell 3.
    builder.popUpTo(AppScreens.Login.route, ::setInclusiveTrue)
}

// Configura el tipus d'argument 
fun configurarArgUsername(builder: androidx.navigation.NavArgumentBuilder) {
    builder.type = NavType.StringType
}

////////////////////////////////////////////////////////////////
// AQUI COMENCEM LA NAVEGACIÓ
@Composable
fun AppNavigation(
    onCloseApp: () -> Unit
){
    val navController = rememberNavController()

    fun ferLogout() = navController.navigate(AppScreens.Login.route) {
        popUpTo(AppScreens.Login.route) { inclusive = true }
    }

    //aqui he afegit la meva funcio per anar a la meva pantalla de blackjack
    fun anarABlackjack() = navController.navigate(AppScreens.Blackjack.route)

    //aqui igual a la meva 4a pantalla que son les regles
    fun anarARegles() = navController.navigate(AppScreens.Rules.route)

    fun tornarEnrere() = navController.popBackStack()

    fun processarRutaViewModelLogin(route: String) {
        navController.navigate(route)
    }

    NavHost(
        navController = navController,
        startDestination = AppScreens.Login.route
    ){
        composable( route= AppScreens.Login.route ){
            val viewModel: LoginViewModel = viewModel()
            val state by viewModel.uiState.collectAsState()

            val context = LocalContext.current
            val dao = AppDatabase.getDatabase(context).userDao()

            LaunchedEffect(key1 = true) {
                viewModel.navigationChannel.collect { route ->
                    navController.navigate(route)
                }
            }

            ScreenLogin(
                state = state,
                onUsernameChange = viewModel::onUsernameChange,
                onPasswordChange = viewModel::onPasswordChange,
                onRegisterClick = { viewModel.onRegisterClick(dao) },
                onLoginClick = { viewModel.onLoginClick(dao) },
                onCloseClick = onCloseApp
            )
        }

        //el mateix welcome que tenies tu però canvio el simon  per el blackjack i afegeixo la pantalla de les regles
        composable(
            route = AppScreens.Welcome.route,
            arguments = listOf(navArgument("username") { type = NavType.StringType })
        ){ backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: "Desconegut"

            // porto el DAO i instacio el ViewModel amb la Factory
            val context = LocalContext.current
            val dao = AppDatabase.getDatabase(context).userDao()
            val welcomeVM: WelcomeViewModel = viewModel(factory = WelcomeViewModelFactory(dao))

            ScreenWelcome(
                username = username,
                ranking = welcomeVM.rankingMundial,
                isLoading = welcomeVM.estaCarregant,
                mostrarDialogError = welcomeVM.mostrarDialogError,
                missatgeError = welcomeVM.textErrorDialog,
                onDismissDialog = { welcomeVM.amagarDialog() },
                onLogoutClick = ::ferLogout,
                navController = navController,
                onRulesClick = ::anarARegles,
                onCloseApp = onCloseApp
            )
        }

        composable(
            route = AppScreens.Blackjack.route + "/{username}" // Usem 'username'
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: "Desconegut"
            val context = LocalContext.current
            val application = context.applicationContext as Application
            val dao = AppDatabase.getDatabase(context).userDao()

            //faig servir la factory que he creat per crear el viewmodel amb el dao y la application
            val blackjackVM: BlackjackViewModel = viewModel(
                factory = BlackjackViewModelFactory(application, dao, username)
            )

            // li passo el viewModel ja creat a la screen perque tingui be tots els parametres
            ScreenBlackjack(
                viewModel = blackjackVM,
                onBack = { navController.popBackStack() }
            )
        }

        //afegeixo les regles
        composable (route = AppScreens.Rules.route){
            ScreenRules(
                onBack = ::tornarEnrere
            )
        }
    }

}
class BlackjackViewModelFactory(
    private val application: Application,
    private val dao: UserDao,
    private val username: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BlackjackViewModel(application, dao, username) as T
    }
}