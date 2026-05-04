package com.example.jetpackapploginmvvm.navigation

import com.example.jetpackapploginmvvm.model.UserDao
import com.example.jetpackapploginmvvm.viewmodel.WelcomeViewModel

class WelcomeViewModelFactory(private val dao: UserDao) :
    androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel>
            create(modelClass: Class<T>): T
    {
        if (modelClass.isAssignableFrom(
                WelcomeViewModel::class.java
            )) {
            @Suppress("UNCHECKED_CAST")
            return WelcomeViewModel(dao) as T
        }
        throw IllegalArgumentException(
            "Unknown ViewModel class"
        )
    }
}