package com.example.jetpackapploginmvvm.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.jetpackapploginmvvm.model.api.RemoteUser
import com.example.jetpackapploginmvvm.navigation.AppScreens

// COM PINTO LA PANTALLA?
@Composable
fun ScreenWelcome(
    username: String,
    ranking: List<RemoteUser>,
    isLoading: Boolean,
    mostrarDialogError: Boolean,
    missatgeError: String,
    onDismissDialog: () -> Unit,
    onLogoutClick: () -> Unit,
    navController: NavHostController,
    onRulesClick: () -> Unit,
    onCloseApp: () -> Unit
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Hola, $username!", fontSize = 28.sp)

        Spacer(modifier = Modifier.height(24.dp))

        //  mostro el ranking
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.LightGray.copy(alpha = 0.3f))
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Top Mundial Blackjack", style = MaterialTheme.typography.titleMedium)

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp)) // Cercle de càrrega
            } else {
                LazyColumn {
                    items(ranking) { user ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = user.username)
                            Text(text = "${user.diners} pts")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // botons
        Button(onClick = {

            navController.navigate(AppScreens.Blackjack.route + "/$username")
        }) {
            Text("Jugar al Blackjack")
        }
        Button(onClick = onRulesClick, modifier = Modifier.fillMaxWidth()) {
            Text("Veure Regles")
        }
        OutlinedButton(onClick = onLogoutClick, modifier = Modifier.fillMaxWidth()) {
            Text("Canviar d'usuari")
        }
        TextButton(onClick = onCloseApp) {
            Text("Sortir de l'aplicació")
        }

        // mostro el dialeg d'error si hi ha errors
        if (mostrarDialogError) {
            AlertDialog(
                onDismissRequest = onDismissDialog,
                title = { Text(text = "Avís de connexió") },
                text = { Text(text = missatgeError) },
                confirmButton = {
                    Button(onClick = onDismissDialog) {
                        Text("D'acord")
                    }
                }
            )
        }
    }
}