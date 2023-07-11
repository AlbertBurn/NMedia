package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.util.SingleLiveEvent
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.switchMap

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
    isRead = false,
    authorId = 0L
)

@HiltViewModel
class PostViewModel @Inject constructor(
    appAuth: AppAuth,
    private val repository: PostRepository,
) : ViewModel() {

    val data: Flow<PagingData<Post>> = appAuth.authStateFlow.flatMapLatest { (myId, _) ->

        repository.data
            .map { posts ->
                posts.map { post ->
                    post.copy(ownedByMe = post.authorId == myId)
                }
            }
    }.flowOn(Dispatchers.Default)

    private val _state = MutableLiveData(FeedModelState())
    val state: LiveData<FeedModelState>
        get() = _state

    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val _photoState = MutableLiveData<PhotoModel?>()
    val photoState: LiveData<PhotoModel?>
        get() = _photoState

//    val newerCount: LiveData<Int> = data.switchMap {
//        val id = it.posts.firstOrNull()?.id ?: 0L
//        repository.getNewer(id)
//            .catch { e -> e.printStackTrace() }
//            .flowOn(Dispatchers.Default)
//    }

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

    fun likeById(post: Post) = viewModelScope.launch {
        edited.value?.let {
            _postCreated.value = Unit
            viewModelScope.launch {
                try {
                    repository.likeByIdAsync(post)
                    _state.value = FeedModelState()
                } catch (e: Exception) {
                    _state.value = FeedModelState(error = true)
                }
            }
        }
        edited.value = empty
    }

    fun unLikeById(post: Post) = viewModelScope.launch {
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

    fun deleteById(id: Long) = viewModelScope.launch {
        viewModelScope.launch {
            try {
                repository.deleteByIdAsync(id)
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }

    fun save() = viewModelScope.launch {
        edited.value?.let { post ->
            viewModelScope.launch {
                try {
                    _photoState.value?.file?.let { file ->
                        repository.saveWithAttachment(post, MediaUpload(file))
                    } ?: repository.saveAsync(post)
                    _state.value = FeedModelState()

                    _postCreated.value = Unit

                    edited.value = empty
                } catch (e: Exception) {
                    _state.value = FeedModelState(error = true)
                }
            }
        }

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

    fun changePhoto(photoModel: PhotoModel?) {
        _photoState.value = photoModel
    }

    fun observePostById(id: Long): LiveData<Post?> = repository.observeById(id)
        .asLiveData(Dispatchers.Default)
}