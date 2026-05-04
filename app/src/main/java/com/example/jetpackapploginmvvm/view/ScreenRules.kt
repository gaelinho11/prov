package com.example.jetpackapploginmvvm.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ScreenRules(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text(text = "Regles del Joc", style = MaterialTheme.typography.headlineSmall)
        Text(text = "1. L'objectiu és sumar 21 o apropar-se sense passar-se.")
        Text(text = "2. Si passes de 21, perds automàticament.")
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = onBack) { Text("Tornar") }
    }
}