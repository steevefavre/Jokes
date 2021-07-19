package com.example.myjokes.views

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.example.myjokes.Globals.Companion.TAGLOG
import com.example.myjokes.R
import com.example.myjokes.tflite.Classifier
import com.example.myjokes.tflite.TFLiteObjectDetectionAPIModel
import java.io.IOException

class RandomImageFragment : Fragment() {

    private lateinit var imageDataObserver : Observer<Bitmap>
    private lateinit var objDetectorGuessObserver : Observer<String>
    private lateinit var randomImageView: ImageView
    private lateinit var guessView: TextView

    private lateinit var loadingView : ProgressBar



    companion object {
        fun newInstance() = RandomImageFragment()
    }

    private lateinit var viewModel: RandomImageViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        activity?.assets?.let {
            MainActivity.assets = it
        }



        val rootView = inflater.inflate(R.layout.random_image_fragment, container, false)
        val navTo = NavHostFragment.findNavController(this)

        randomImageView = rootView.findViewById<ImageView>(R.id.iv_random_image_image)
        loadingView = rootView.findViewById<ProgressBar>(R.id.loadingBar)
        guessView = rootView.findViewById<TextView>(R.id.tv_random_image_info_ai)

        rootView.findViewById<AppCompatButton>(R.id.bt_back_to_jokes).setOnClickListener {
            navTo.navigate(R.id.action_randomImageFragment_to_jokesFragment)
        }

        rootView.findViewById<AppCompatButton>(R.id.bt_new_random_image).setOnClickListener {
            loadingView.visibility = View.VISIBLE
            guessView.text= ""
            viewModel.getNewRandomImage()
        }




        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(RandomImageViewModel::class.java)



        imageDataObserver = Observer<Bitmap> {
            loadingView.visibility = View.GONE
            randomImageView.setImageBitmap(it)

        }

        objDetectorGuessObserver = Observer<String> {
            guessView.text = it

        }

        viewModel.getMutableImageData().observe(viewLifecycleOwner, imageDataObserver)
        viewModel.getMutableOjectDetectorGuess().observe(viewLifecycleOwner, objDetectorGuessObserver)


        viewModel.init()
        loadingView.visibility = View.VISIBLE

    }


}