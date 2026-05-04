package com.example.jetpackapploginmvvm.model.api

import retrofit2.http.GET

interface ApiService {
    // Adreça del JSON a GitHub (com la del profe)
    @GET("lobandolerpi/api-simon/refs/heads/main/ranking-mundial.json")
    suspend fun getRankingMundial(): List<RemoteUser>
}