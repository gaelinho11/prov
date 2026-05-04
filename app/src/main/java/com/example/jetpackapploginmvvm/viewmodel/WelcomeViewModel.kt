package com.example.jetpackapploginmvvm.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpackapploginmvvm.model.UserDao
import com.example.jetpackapploginmvvm.model.api.RankingRepository
import com.example.jetpackapploginmvvm.model.api.RemoteUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WelcomeViewModel(private val dao: UserDao) : ViewModel() {

    var rankingMundial by mutableStateOf<List<RemoteUser>>(emptyList())
        private set
    var estaCarregant by mutableStateOf(false)
        private set
    var mostrarDialogError by mutableStateOf(false)
        private set
    var textErrorDialog by mutableStateOf("")
        private set

    init {
        carregarDadesDesDeRepositori()
    }

    private fun carregarDadesDesDeRepositori() {
        viewModelScope.launch(Dispatchers.IO) {
            estaCarregant = true
            val resultat = RankingRepository.getRanking(dao)

            withContext(Dispatchers.Main) {
                rankingMundial = resultat.first
                if (resultat.second != null) {
                    textErrorDialog = resultat.second!!
                    mostrarDialogError = true
                }
                estaCarregant = false
            }
        }
    }

    fun amagarDialog() {
        mostrarDialogError = false
    }
}