package com.example.jetpackapploginmvvm.model
import androidx.room.*

@Dao
interface UserDao {
    // ROOM generarà l'SQL de l'Insert per sota automàticament.
    // IGNORE vol dir que si ja existeix, no fa un crash, sinó que l'ignora.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(user: User): Long

    // Per les consultes complexes o cerques, hem de fer l'SQL nosaltres, però Room ho valida en compilar.
    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUser(username: String): User?

    // S08. Obtenir el Top 5
    @Query("SELECT * FROM users ORDER BY diners DESC LIMIT 5")
    suspend fun getTop5Users(): List<User>

    // S08. Actualitzar les dades d'un usuari
    @Update
    suspend fun update(user: User)
}