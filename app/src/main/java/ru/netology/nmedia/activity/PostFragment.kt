package ru.netology.nmedia.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.NonCancellable.start
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.ChangeNumber.changeNumber
import ru.netology.nmedia.viewmodel.DataModel
import ru.netology.nmedia.viewmodel.PostViewModel

@AndroidEntryPoint
class PostFragment : Fragment() {

    private val dataModel: DataModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPostBinding.inflate(
            inflater,
            container,
            false
        )


        val viewModel: PostViewModel by activityViewModels()


        with(binding.scrollContent) {
            viewModel.observePostById(requireNotNull(dataModel.postIdMessage.value)).observe(viewLifecycleOwner) { post ->

                    if (post != null) {
                        author.text = post.author
                        published.text = post.published
                        content.text = post.content
                        like.text = changeNumber(post.likes)
                        like.isChecked = post.likedByMe
                        share.text = changeNumber(post.shares)

                        val urlAvatar = "http://10.0.2.2:9999/avatars/${post.authorAvatar}"

                        Glide.with(this.avatar).load(urlAvatar).circleCrop()
                            .placeholder(R.drawable.ic_baseline_miscellaneous_services_24)
                            .error(R.drawable.ic_baseline_error_24).into(this.avatar)

                        if (post.attachment != null) {
                            this.attachmentImage.isVisible = true

                            val urlAttachments =
                                "http://10.0.2.2:9999/media/${post.attachment?.url}"
                            Glide.with(this.attachmentImage)
                                .load(urlAttachments)
                                .into(this.attachmentImage)
                        }

                        if (post.videoUrl != null) {
                            this.videoLayout.visibility = View.VISIBLE
                            videoView.apply {
                                setVideoURI(Uri.parse(post.videoUrl))
                                requestFocus()
                                start()
                            }
                        } else {
                            videoLayout.visibility = View.GONE
                        }

                        like.setOnClickListener {
                            viewModel.likeById(post)
                        }

                        share.setOnClickListener {
                            val intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, post.content)
                            }

                            val shareIntent =
                                Intent.createChooser(intent, getString(R.string.chooser_share_post))
                            startActivity(shareIntent)
                        }


                        videoLayout.setOnClickListener {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(post.videoUrl))
                            startActivity(intent)
                        }

                        attachmentImage.setOnClickListener {
                            val likes = post.likes.toString()
                            val id = post.id
                            val isLikedByMe = post.likedByMe
                            val url = post.attachment!!.url
                            val bundle = Bundle()
                            bundle.putString("likes", likes)
                            bundle.putBoolean("likedByMe", isLikedByMe)
                            bundle.putString("url", url)
                            bundle.putLong("id", id)
                            findNavController().navigate(R.id.action_feedFragment_to_photo, bundle)
                        }

                        menu.setOnClickListener {
                            PopupMenu(it.context, it).apply {
                                inflate(R.menu.options_post)
                                setOnMenuItemClickListener {
                                    when (it.itemId) {
                                        R.id.remove -> {
                                            findNavController().navigateUp()
                                            viewModel.deleteById(post.id)
                                            true
                                        }
                                        R.id.edit -> {
                                            viewModel.edit(post)
                                            val bundle = Bundle()
                                            bundle.putString("editedText", post.content)
                                            findNavController().navigate(
                                                R.id.action_postFragment_to_editPostFragment,
                                                bundle
                                            )
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
        return binding.root
    }


}