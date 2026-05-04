package com.example.jetpackapploginmvvm.navigation

sealed class AppScreens (val route: String ){
    data object Login : AppScreens("login_screen")
    data object Welcome : AppScreens("welcome_screen/{username}"){
        fun createRoute(username: String) ="welcome_screen/${username}"
    }
    object Blackjack : AppScreens("blackjack_screen")
    object Rules : AppScreens("rules_screen")
}