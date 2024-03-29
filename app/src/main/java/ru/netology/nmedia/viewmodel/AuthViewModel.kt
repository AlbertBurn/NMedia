package ru.netology.nmedia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import ru.netology.nmedia.auth.AppAuth

class AuthViewModel : ViewModel() {

    val state = AppAuth.getInstance().authStateFlow.asLiveData()

    val authorized: Boolean
        get() = AppAuth.getInstance().authStateFlow.value.id != 0L
}