package ru.netology.nmedia.entity

import androidx.room.*
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.AttachmentType
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
    var isRead: Boolean = false,
    val authorId: Long,
    @Embedded
    val attachment: AttachmentEmbeddable?,
) {
    fun toDto() = Post(
        id = id,
        author = author,
        authorAvatar = authorAvatar,
        content = content,
        published = published,
        likedByMe = likedByMe,
        likes = likes,
        shares = shares,
        videoUrl = videoUrl,
        isRead = isRead,
        attachment = attachment?.toDto(),
        authorId = authorId
    )

    companion object {
        fun fromDto(dto: Post) =
            PostEntity(
                id = dto.id,
                author = dto.author,
                authorAvatar = dto.authorAvatar,
                content = dto.content,
                published = dto.published,
                likedByMe = dto.likedByMe,
                likes = dto.likes,
                shares = dto.shares,
                videoUrl = dto.videoUrl,
                isRead = dto.isRead,
                attachment = AttachmentEmbeddable.fromDto(dto.attachment),
                authorId = dto.authorId
            )

    }
}


data class AttachmentEmbeddable(
    var url : String,
    var type : AttachmentType
) {
    fun toDto() = Attachment(url, type)

    companion object {
        fun fromDto(dto : Attachment?) = dto?.let {
            AttachmentEmbeddable(it.url, it.type)
        }
    }

}



fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)
fun List<Post>.toEntity(): List<PostEntity> = map(PostEntity::fromDto)