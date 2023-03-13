package ru.netology.nmedia.adapter

import android.net.Uri
import android.view.View
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.ChangeNumber


class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener
) : RecyclerView.ViewHolder(binding.root) {

    private lateinit var currentPost: Post

    fun bind(post: Post) {
        currentPost = post
        val urlAvatar = "http://10.0.2.2:9999/avatars/${post.authorAvatar}"

        binding.apply {
            author.text = post.author
            published.text = post.published
            content.text = post.content
            //likeCount.text = changeNumber(post.likes)
            like.isChecked = post.likedByMe
            like.text = ChangeNumber.changeNumber(post.likes)
            share.text = ChangeNumber.changeNumber(post.shares)

            Glide.with(binding.avatar).load(urlAvatar).circleCrop().placeholder(R.drawable.ic_baseline_miscellaneous_services_24)
                .error(R.drawable.ic_baseline_error_24).into(binding.avatar)

            if (post.videoUrl != null) {
                videoLayout.visibility = View.VISIBLE
                videoView.apply {
                    setVideoURI(Uri.parse(post.videoUrl))
                    requestFocus()
                    start()
                }
            } else {
                videoLayout.visibility = View.GONE
            }

            cardPost.setOnClickListener {
                onInteractionListener.onPostClick(post)
            }

            videoLayout.setOnClickListener {
                onInteractionListener.onPlayVideo(post)
            }

            like.setOnClickListener {
                onInteractionListener.onLike(post)
            }

            share.setOnClickListener {
                onInteractionListener.onShare(post)
            }

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onDelete(post)
                                true
                            }
                            R.id.edit -> {
                                onInteractionListener.onEdit(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }
        }
    }
}