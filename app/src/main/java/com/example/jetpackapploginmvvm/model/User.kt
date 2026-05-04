package com.example.jetpackapploginmvvm.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users") //dic a Room que això és una taula
data class User(
    @PrimaryKey val username: String, //clau primària
    val password: String,
    val diners: Int = 1000 //aqui guardare les apostes del usuari
)