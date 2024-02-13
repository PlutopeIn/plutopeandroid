package com.app.plutope.ui.fragment.splash


import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.app.plutope.R
import com.app.plutope.databinding.FragmentSplashBinding
import com.app.plutope.utils.extras.PreferenceHelper
import com.app.plutope.utils.safeNavigate
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class Splash : Fragment() {
    private lateinit var binding: FragmentSplashBinding
    @Inject
    lateinit var preferenceHelper: PreferenceHelper
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSplashBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val videoPath =
            "android.resource://" + requireContext().packageName + "/" + R.raw.splash_video_short
        val videoUri = Uri.parse(videoPath)
        binding.videoView.setVideoURI(videoUri)
        binding.videoView.start()

        binding.videoView.setOnCompletionListener { mp ->
            binding.videoView.stopPlayback()
            if (preferenceHelper.menomonicWallet != "") {

                if (preferenceHelper.appUpdatedFlag == "") {
                   // findNavController().safeNavigate(SplashDirections.actionSplashToWalletSetup())

                    findNavController().safeNavigate(SplashDirections.actionSplashToUpdateAnything())

                }else {
                    findNavController().safeNavigate(SplashDirections.actionSplashToDashboard())

                    // findNavController().safeNavigate(SplashDirections.actionSplashToUpdateAnything())
                }


            } else {
                findNavController().safeNavigate(SplashDirections.actionSplashToWalletSetup())
            }
        }

    }

    override fun onDestroyView() {
        binding.videoView.stopPlayback()
        super.onDestroyView()
    }

}