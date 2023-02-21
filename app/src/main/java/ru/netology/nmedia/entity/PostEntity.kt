package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Post

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val author: String,
    val authorAvatar: String,
    val content: String,
    val published: String,
    val likedByMe: Boolean,
    val likes: Int = 0,
    val shares: Int = 0,
    val videoUrl: String?,
    val attachment: Attachment?
) {
    fun toDto() = Post(id, author, authorAvatar, content, published, likedByMe, likes, shares, videoUrl, attachment?.toDto())

    companion object {
        fun fromDto(dto: Post) =
            PostEntity(
                dto.id,
                dto.author,
                dto.authorAvatar,
                dto.content,
                dto.published,
                dto.likedByMe,
                dto.likes,
                dto.shares,
                dto.videoUrl,
                Attachment.fromDto(dto.attachment))

    }
}

    data class Attachment(
        val url: String,
        val description: String,
        val type: String
    ) {
        fun toDto() = Attachment(url, description, type)

        companion object {
            fun fromDto(dto: Attachment?) = dto?.let {
                Attachment(it.url, it.description, it.type)
            }
        }
    }