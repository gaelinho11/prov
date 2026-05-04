package com.example.jetpackapploginmvvm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpackapploginmvvm.model.User
import com.example.jetpackapploginmvvm.model.UserDao
import com.example.jetpackapploginmvvm.model.UserRepository
import com.example.jetpackapploginmvvm.navigation.AppScreens
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Estat inicial per la UI de Login.
// Els tres continguts en blanc.
data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val message: String = "",
    val errorMsg: String = "",
    val isLoading: Boolean = false
)

// ViewModel és una classe de Kotlin per aplicacions.
// Aquí estem creant una extensió d'aquesta classe.
class LoginViewModel : ViewModel() {
    // El Hashmap de clients, ara l'hem d'importar

    // L'estat de l'aplicació és privat, només el viewmodel el pot canviar
    // Però la vista l'ha de poder veure.
    // Per assegurar que la vista veu però no canvia, creem _uiState i uiState.
    private val _uiState = MutableStateFlow(LoginUiState())
    // Iniciem tot en blanc amb el LoginUiState, però serà mutable.
    val uiState = _uiState.asStateFlow() // Aquesta, agafará sempre el valor de la privada


    // Canviar de pantalla és un event, però és únic.
    // És millor fer servir Channel en aquests casos.
    private val _navigationChannel = Channel<String>()
    val navigationChannel = _navigationChannel.receiveAsFlow()

    fun onUsernameChange(input: String) {
        _uiState.value = _uiState.value.copy(input, message = "", errorMsg = "")
    }

    fun onPasswordChange(input: String) {
        _uiState.value = _uiState.value.copy(password = input, message = "", errorMsg = "")
    }



    fun onLoginClick(dao: UserDao) {
        val current = _uiState.value

        viewModelScope.launch(Dispatchers.IO) {
            val storedUser = UserRepository.getUser(current.username, dao)

            withContext(Dispatchers.Main) {
                if (storedUser == null) {
                    _uiState.value = current.copy(errorMsg = "ERROR: L'usuari no existeix !!")
                } else if (storedUser.password == current.password) {
                    _navigationChannel.send(AppScreens.Welcome.createRoute(current.username))
                    _uiState.value = LoginUiState()
                } else {
                    _uiState.value = current.copy(errorMsg = "ERROR: Credencials invàlides !!")
                }
            }
        }
    }

    fun onRegisterClick(dao: UserDao) {
        val current = _uiState.value
        viewModelScope.launch(Dispatchers.IO) {
            val newUser = User(current.username, current.password, 1000) //faig que l'usuari comenci amn 1000€
            val isAdded = UserRepository.addUser(newUser, dao)

            withContext(Dispatchers.Main) {
                if (isAdded) {
                    _uiState.value = current.copy(errorMsg = "Usuari registrat correctament!")
                } else {
                    _uiState.value = current.copy(errorMsg = "ERROR: Aquest usuari ja existeix.")
                }
            }
        }
    }

    // onCloseClick  ARA no és responsabilitat del ViewModel
    // Es una acció del Sistema Operatiu i ho gestiona la Vista.
}