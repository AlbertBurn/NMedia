package ru.netology.nmedia.auth

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAuth @Inject constructor(@ApplicationContext context: Context) {

    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val _authStateFlow = MutableStateFlow(AuthState())
    val authStateFlow = _authStateFlow

    init {
        var id: Long = 0L
        var token: String? = null

        token = prefs.getString(TOKEN, null)
        id = prefs.getLong(ID, 0L)

        if (id == 0L || token == null) {
            _authStateFlow.value = AuthState()
            prefs.edit {
                clear()
            }
        } else {
            _authStateFlow.value = AuthState(id = id, token = token)
        }
    }

    @Synchronized
    fun clear() {
        _authStateFlow.value = AuthState()
        prefs.edit {
            clear()
        }
    }

    @Synchronized
    fun setAuth(id: Long, token: String) {
        _authStateFlow.value = AuthState(id = id, token = token)
        prefs.edit {
            putLong(ID,id)
            putString(TOKEN, token)
        }
    }

    companion object {

        const val ID = "id"
        const val TOKEN = "token"

        @Volatile
        private var INSTANCE: AppAuth? = null

        fun init(context: Context) {
            synchronized(this) {
                INSTANCE = AppAuth(context)
            }
        }

        fun getInstance(): AppAuth {
            return synchronized(this) {
                requireNotNull(INSTANCE) { "Make init" }
            }
        }
    }
}

data class AuthState(val id: Long = 0L, val token: String? = null)