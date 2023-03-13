package ru.netology.nmedia.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.ChangeNumber.changeNumber
import ru.netology.nmedia.viewmodel.DataModel
import ru.netology.nmedia.viewmodel.PostViewModel

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

        val viewModel: PostViewModel by viewModels(::requireParentFragment)
        with(binding.scrollContent) {
            viewModel.data.observe(viewLifecycleOwner) { feedposts ->
                dataModel.postIdMessage.observe(viewLifecycleOwner) {postIdClicked ->

                    val post = feedposts.posts.find { it.id == postIdClicked }

                    if (post != null) {
                        author.text = post.author
                        published.text = post.published
                        content.text = post.content
                        like.text = changeNumber(post.likes)
                        like.isChecked = post.likedByMe
                        share.text = changeNumber(post.shares)

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
                                            findNavController().navigate(R.id.action_postFragment_to_editPostFragment, bundle)
                    //                                            findNavController().navigate(R.id.action_postFragment_to_editPostFragment,
                    //                                                Bundle().apply {
                    //                                                    textArg = post.content
                    //                                                })
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
        }
        return binding.root
    }



}