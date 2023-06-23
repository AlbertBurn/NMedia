package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.model.AuthModelState
import ru.netology.nmedia.model.FeedModelState

import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.util.SingleLiveEvent
import java.net.SocketTimeoutException
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ru.netology.nmedia.api.PostsApiService
import ru.netology.nmedia.dto.UserKey
import javax.inject.Inject

@HiltViewModel
class SignViewModel @Inject constructor(
    private val apiService: PostsApiService,
    private val appAuth: AppAuth
) : ViewModel() {


    private val _data: MutableLiveData<UserKey> = MutableLiveData<UserKey>()
    val data: LiveData<UserKey>
        get() = _data

    private val _state = SingleLiveEvent<AuthModelState>()
    val state: LiveData<AuthModelState>
        get() = _state


    fun signIn(login: String, pass: String) =
        viewModelScope.launch {
//        val response = repository.signIn()
//        response.token?.let { AppAuth.getInstance().setAuth(response.id, response.token) }
            try {
                val response = apiService.updateUser(login, pass)
                response.body()?.let { body ->
                    appAuth.setAuth(body.id, body.token.orEmpty())
                }
                _state.value = AuthModelState(successfulRequest = true)
            } catch (e: ApiError) {
                _state.value = AuthModelState(loginAndPassError = true)
            } catch (e: Exception) {
                _state.value = AuthModelState(connectionError = true)
            }
        }

}