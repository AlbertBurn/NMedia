package ru.netology.nmedia.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.viewmodel.DataModel
import ru.netology.nmedia.viewmodel.PostViewModel

class FeedFragment : Fragment() {
    private val dataModel : DataModel by activityViewModels()
    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(
            inflater,
            container,
            false
        )

        val adapter = PostsAdapter(object : OnInteractionListener {

            override fun onPostClick(post: Post) {
                val postClikedId = post.id
                dataModel.postIdMessage.value = postClikedId
                findNavController().navigate(R.id.action_feedFragment_to_PostFragment)
            }

            override fun onEdit(post: Post) {
                viewModel.edit(post)
                val text = post.content
                val bundle = Bundle()
                bundle.putString("editedText", text)
                findNavController().navigate(R.id.action_feedFragment_to_editPostFragment, bundle)
            }

            override fun onLike(post: Post) {
                //viewModel.likeById(post.id)
                viewModel.likeById(post)
            }

            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }

                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
            }

            override fun onDelete(post: Post) {
                viewModel.deleteById(post.id)
            }

            override fun onPlayVideo(post: Post) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(post.videoUrl))
                startActivity(intent)
            }
        })

        binding.list.adapter = adapter
        binding.list.itemAnimator = null // эта вставка должна помочь с  проблемой мерцания
        viewModel.data.observe(viewLifecycleOwner, { state: FeedModel ->
            adapter.submitList(state.posts)
            binding.progress.isVisible = state.loading
            binding.errorGroup.isVisible = state.error
            binding.emptyText.isVisible = state.empty
        })

        binding.retryButton.setOnClickListener {
            viewModel.loadPosts()
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadPosts()
        }

        //Add post button
        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        }

        return binding.root
    }
}
