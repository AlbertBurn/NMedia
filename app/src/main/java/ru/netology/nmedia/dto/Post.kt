package ru.netology.nmedia.dto

import ru.netology.nmedia.entity.Attachment

data class Post(
    val id: Long,
    val author: String,
    val authorAvatar: String,
    val content: String,
    val published: String,
    var likedByMe: Boolean = false,
    var likes: Int = 17,
    var shares: Int = 1099999,
    val videoUrl: String? = null,
    var isRead: Boolean = false
)

data class Attachment(
    var url: String,
    var description: String,
    var type: String
)
