package ru.netology.nmedia.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import okhttp3.*
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nmedia.api.PostsApiService
import ru.netology.nmedia.api.SMediaService
import ru.netology.nmedia.auth.AuthState
import java.io.IOException
import java.util.concurrent.TimeUnit
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.*
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import ru.netology.nmedia.model.PhotoModel
import java.lang.Exception
import java.net.ConnectException
import java.util.concurrent.CancellationException
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val postDao: PostDao,
    private val postsApiService: PostsApiService,
    private val mediaService: SMediaService,
) : PostRepository {


    override val data = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = false),
        pagingSourceFactory = {
            PostPagingSource(
                postsApiService
            )
        }
    ).flow

    override fun getNewer(id: Long): Flow<Int> = flow {
        while (true) {
            try {
                delay(10_000L)
                val response = postsApiService.getNewer(id)

                val posts = response.body().orEmpty()
                postDao.insert(posts.toEntity())
                emit(posts.size)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    override suspend fun getAllAsync() {
        val response = postsApiService.getAll()
        if (!response.isSuccessful) throw RuntimeException("api error")
        response.body() ?: throw RuntimeException("body is null")
        //set isRead to 1
        postDao.insert(response.body()!!.map { it -> PostEntity.fromDto(it) })
        //set isRead to 1
        postDao.readNewPost()
    }


    override fun shareById(id: Long) {
    }

    override suspend fun uploadPhoto(uploadedMedia: MediaUpload): Media {
        try {
            val media = MultipartBody.Part.createFormData(
                "file", uploadedMedia.file.name, uploadedMedia.file.asRequestBody()
            )

            val response = mediaService.uploadPhoto(media)
            if (!response.isSuccessful) {
                throw ApiError(response.message())
            }

            return response.body() ?: throw ApiError(response.message())
        } catch (e: IOException) {
            throw NetworkError
        }
    }

    override suspend fun signIn(login: String, pass: String): AuthState {
        val response = postsApiService.updateUser(login, pass)

        if (!response.isSuccessful) {
            throw ApiError(response.message())
        }

        return response.body() ?: throw ApiError(response.message())
    }

    override suspend fun deleteByIdAsync(id: Long) {
        val response = postsApiService.deleteById(id)
        if (!response.isSuccessful) throw RuntimeException("api error")
        val body = response.body() ?: throw RuntimeException("body is null")
        postDao.removeById(id)
    }

    override suspend fun saveAsync(post: Post) {
        try {
            val response = postsApiService.save(post)
            if (!response.isSuccessful) {
                throw ApiError(response.message())
            }
            val body = response.body() ?: throw ApiError(response.message())
            postDao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun saveWithAttachment(post: Post, upload: MediaUpload) {
        try {
            val media = uploadPhoto(upload)
            val response = postsApiService.save(
                post.copy(
                    attachment = Attachment(media.id, AttachmentType.IMAGE)
                )
            )
            if (!response.isSuccessful) {
                throw ApiError(response.message())
            }
            val body = response.body() ?: throw ApiError(response.message())
            postDao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    private suspend fun upload(photo: PhotoModel): Media {
        val response = mediaService.uploadPhoto(
            MultipartBody.Part.createFormData("file", photo.file!!.name, photo.file.asRequestBody())
        )

        return response.body() ?: throw ApiError(response.message())
    }

    override suspend fun unLikeByIdAsync(post: Post) {
        val response = postsApiService.unlikeById(post.id)
        if (!response.isSuccessful) throw RuntimeException("api error")
        val body = response.body() ?: throw RuntimeException("body is null")
        postDao.insert(PostEntity.fromDto(body))
    }

    override suspend fun likeByIdAsync(post: Post) {
        val response = postsApiService.likeById(post.id)
        if (!response.isSuccessful) throw RuntimeException("api error")
        val body = response.body() ?: throw RuntimeException("body is null")
        postDao.insert(PostEntity.fromDto(body))
    }

    override suspend fun getByIdAsync(id: Long) {
        val response = postsApiService.getById(id)
        if (!response.isSuccessful) throw RuntimeException("api error")
        val body = response.body() ?: throw RuntimeException("body is null")
        postDao.getById(body.id)
    }
}