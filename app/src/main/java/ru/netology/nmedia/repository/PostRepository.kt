package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Post

interface PostRepository {
    val data : Flow<List<Post>>

    fun getNewer(id: Long): Flow<Int>
    suspend fun getAllAsync()
    suspend fun getByIdAsync(id: Long)
    suspend fun deleteByIdAsync(id: Long)
    suspend fun saveAsync(post: Post)
    suspend fun unLikeByIdAsync(post: Post)
    suspend fun likeByIdAsync(post: Post)
    fun shareById(id : Long)

}