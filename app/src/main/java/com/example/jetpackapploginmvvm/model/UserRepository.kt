package com.example.jetpackapploginmvvm.model

// Object ens diu que aquesta classe és un singleton, directament!
// es crearà quan la intenti fer servir desde els botons onXClick


//he canviat totes les funcions per suspend per treballar amb room de fprma asincrona i no bloquejar elk fil
object UserRepository {

    suspend fun prepararDadesDeProva(dao: UserDao) { // creo un user de prova si la base de dades esta buida.
        if (dao.getUser("a") == null) {
            dao.insert(User("a", "a", 1000))
        }
    }
    suspend fun addUser(user: User, dao: UserDao): Boolean {
        val result = dao.insert(user)
        return result != -1L
    }

    suspend fun getUser(username: String, dao: UserDao): User? {
        return dao.getUser(username)
    }

    suspend fun getTop5(dao: UserDao): List<User> {
        return dao.getTop5Users()
    }

    suspend fun updateHighScore(username: String, newDiners: Int, dao: UserDao) {
        val user = dao.getUser(username)
        if (user != null) { //no ho faig com tu perque tmb puc perdre diners en el blackjack
            val updatedUser = user.copy(diners = newDiners)
            dao.update(updatedUser)
        }
    }
}