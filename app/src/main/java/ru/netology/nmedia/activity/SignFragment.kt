package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentSignBinding
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.viewmodel.SignViewModel
import ru.netology.nmedia.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.auth.AppAuth
import javax.inject.Inject

@AndroidEntryPoint
class SignFragment : Fragment() {

    @Inject
    lateinit var appAuth: AppAuth

    private val viewModel: SignViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentSignBinding.inflate(inflater, container, false)

        binding.signInButton.setOnClickListener {
            if (binding.username.text.isNullOrBlank() || binding.password.text.isNullOrBlank()) {
                Toast.makeText(
                    activity,
                    this.getString(R.string.empty_login),
                    Toast.LENGTH_LONG
                )
                    .show()
            } else {
                viewModel.signIn(binding.username.text.toString(), binding.password.text.toString())
                AndroidUtils.hideKeyboard(requireView())
                //findNavController().navigateUp()
            }
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            if (state.connectionError) {
                Toast.makeText(
                    activity,
                    this.getString(R.string.server_not_connected),
                    Toast.LENGTH_LONG
                )
                    .show()
            } else if (state.loginAndPassError) {
                Toast.makeText(
                    activity,
                    this.getString(R.string.login_pass_error),
                    Toast.LENGTH_LONG
                )
                    .show()
            } else if (state.successfulRequest) {
                findNavController().navigateUp()
            }
        }

        return binding.root
    }

}