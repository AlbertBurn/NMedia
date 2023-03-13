package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.util.SingleLiveEvent

private val empty = Post(
    id = 0,
    author = "",
    authorAvatar = "",
    content = "",
    published = "",
    likedByMe = false,
    likes = 0,
    shares = 0,
    videoUrl = null,
    isRead = false
)

class PostViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PostRepository = PostRepositoryImpl(AppDb.getInstance(context =  application).postDao())

    val data: LiveData<FeedModel> = repository.data
        .map(::FeedModel)
        .asLiveData(Dispatchers.Default)

    private val _state = MutableLiveData(FeedModelState())
    val state: LiveData<FeedModelState>
        get() = _state

    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    val newerCount: LiveData<Int> = data.switchMap {
        val id = it.posts.firstOrNull()?.id ?: 0L
        repository.getNewer(id)
            .catch { e -> e.printStackTrace() }
            .asLiveData(Dispatchers.Default)
    }

    init {
        loadPosts()
    }

    fun refreshPosts() = viewModelScope.launch {
        try {
            _state.value = FeedModelState(refreshing = true)
            repository.getAllAsync()
            _state.value = FeedModelState()
        } catch (e: Exception) {
            _state.value = FeedModelState(error = true)
        }
    }

    fun loadPosts() = viewModelScope.launch {
       try {
           _state.value = FeedModelState(loading = true)
           repository.getAllAsync()
           _state.value = FeedModelState()
       } catch (e: Exception) {
           _state.value = FeedModelState(error = true)
       }
    }

    fun likeById(post: Post) = viewModelScope.launch{
        edited.value?.let {
            _postCreated.value = Unit
            viewModelScope.launch {
                try {
                    repository.likeByIdAsync(post)
                    _state.value = FeedModelState()
                } catch (e:Exception) {
                    _state.value = FeedModelState(error = true)
                }
            }
        }
        edited.value = empty
    }

    fun unLikeById(post: Post) = viewModelScope.launch{
        edited.value?.let {

            _postCreated.value = Unit
            viewModelScope.launch {
                try {
                    repository.unLikeByIdAsync(post)
                    _state.value = FeedModelState()
                } catch (e: Exception) {
                    _state.value = FeedModelState(error = true)
                }
            }
        }
        edited.value = empty
    }

    fun deleteById(id: Long) = viewModelScope.launch{
        viewModelScope.launch {
            try {
                repository.deleteByIdAsync(id)
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }

    fun save()  = viewModelScope.launch{
        edited.value?.let {
            _postCreated.value = Unit
            viewModelScope.launch {
                try {
                    repository.saveAsync(it)
                    _state.value = FeedModelState()
                } catch (e: Exception) {
                    _state.value = FeedModelState(error = true)
                }
            }
        }
        edited.value = empty
    }



    fun shareById(id: Long) = repository.shareById(id)


    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContent(content: String) {
        var text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }
}