package com.example.jetpackapploginmvvm.model.api

import com.example.jetpackapploginmvvm.model.UserDao

object RankingRepository {
    suspend fun getRanking(dao: UserDao): Pair<List<RemoteUser>, String?> {
        return try {
            val dadesWeb = RetrofitClient.apiService.getRankingMundial()
            Pair(dadesWeb, null)
        } catch (e: Exception) {
            val dadesLocals = dao.getTop5Users().map {
                RemoteUser(it.username, it.diners)
            }
            Pair(dadesLocals, "Sense connexió. Mostrant rànquing local.")
        }
    }
}