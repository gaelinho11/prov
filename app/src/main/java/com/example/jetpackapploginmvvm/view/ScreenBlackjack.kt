package com.example.jetpackapploginmvvm.view

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.jetpackapploginmvvm.viewmodel.BlackjackViewModel

@Composable
fun ScreenBlackjack(
    viewModel: BlackjackViewModel,
    onBack: () -> Unit
) {
    //agafo l'estat del viewmodel
    val state by viewModel.uiState.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current

    //aqui gestiono la pausa automatica al sortir del joc
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.pausarJoc() //quan l'usuari surt es pausa
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    val colorPuntuacio by animateColorAsState( //ho utilitzare per animar el text segons la puntuacio que porta el jugador
        targetValue = if (state.puntuacioJugador > 21) Color.Red
        else if (state.puntuacioJugador >= 17) Color.Yellow
        else Color.White,
        animationSpec = tween(durationMillis = 1000) //afegeixo una transició de 1 segon
    )
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1B5E20))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "El teu saldo: ${state.dinersTotals}€", fontWeight = FontWeight.ExtraBold, color = Color.White)

            if (!state.apostaFeta) {
                Text("Quant vols apostar?", color = Color.White)
                Row {
                    Button(onClick = { viewModel.ferAposta(10) }) { Text("10€") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { viewModel.ferAposta(50) }) { Text("50€") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { viewModel.ferAposta(100) }) { Text("100€") }
                }
            } else {
                Text(text = "PARTIDA DE BLACKJACK", style = MaterialTheme.typography.headlineMedium, color = Color.Yellow)
                Spacer(modifier = Modifier.height(20.dp))

                //ara aqui ja es veuen les cartes de la banca
                Text(text = "CARTES DE LA BANCA", fontWeight = FontWeight.Bold, color = Color.White)
                state.cartesBanca.forEach { carta ->
                    Text(text = "${carta.nom} (val: ${carta.valor})", color = Color.White)
                }
                Text(text = "Puntuació Banca: ${state.puntuacioBanca}", color = Color.White)

                Spacer(modifier = Modifier.height(20.dp))

                //mostro les cartes que te
                Text(text = "Les teves cartes:", color = Color.White)
                //faig un foeach per fer print de cada carta que hi ha a les cartes del jugador del state que m'he portat abans
                state.cartesJugador.forEach { carta ->
                    Text(text = "${carta.nom} (val: ${carta.valor})", color = Color.White) // aqui printejo la carta i el valor, perque si la carta no es un numero el valor no es printeja
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "puntuacio total: ${state.puntuacioJugador}",
                    fontWeight = FontWeight.Bold,
                    color = colorPuntuacio
                )

                Spacer(modifier = Modifier.height(20.dp))
                //aqui printejo el missatge del state que m'he portat però si el jugador ha perdut el poso en vermell
                Text(
                    text = state.missatge,
                    color = if (state.puntuacioJugador > 21) Color.Red else Color.Yellow
                )

                Spacer(modifier = Modifier.height(30.dp))

                if (!state.jocAcabat) { //si el joc encare segueix tornem a demanar si vol  carta amb dos botons
                    Row {
                        Button(onClick = { viewModel.demanarCarta() }) {
                            Text("Demanar Carta")
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(onClick = { viewModel.plantarse() }) {
                            Text("Plantar-se")
                        }
                    }
                } else { // si no segueix poso un botó per tornar al menú
                    Button(onClick = {
                        onBack()
                    }) {
                        Text("Tornar al Menú")
                    }
                }
            }
        }

        //pantalla de pausa per quan es minimitza
        if (state.jocEnPausa) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f)) //ho poso fosc al menu de pausa peruqe no es vegi el joc
                    .clickable(enabled = false) { }, //no es pot clickar el joc a sota
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("JOC EN PAUSA", color = Color.White, style = MaterialTheme.typography.headlineLarge)
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = { viewModel.reprendreJoc() }) {
                        Text("RESUME")
                    }
                }
            }
        }
    }}