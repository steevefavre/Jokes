package com.example.myjokes.views

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.RectF
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myjokes.Globals
import com.example.myjokes.Globals.Companion.TAGLOG
import com.example.myjokes.interfaces.ImageApi
import com.example.myjokes.tflite.Classifier
import com.example.myjokes.tflite.TFLiteObjectDetectionAPIModel
import com.google.gson.GsonBuilder
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.IOException

class RandomImageViewModel : ViewModel() {

    private var imageDataMutable: MutableLiveData<Bitmap> = MutableLiveData<Bitmap>()
    private var objDetectorGuess: MutableLiveData<String> = MutableLiveData<String>()


    private val TF_OD_API_INPUT_SIZE = Globals.IMG_SIZE
    private val TF_OD_API_IS_QUANTIZED = true
    private val TF_OD_API_MODEL_FILE = "detect.tflite"
    private val TF_OD_API_LABELS_FILE = "labelmap.txt"
    private var detector: Classifier? = null


    var gson = GsonBuilder()
        .excludeFieldsWithoutExposeAnnotation()
        .setLenient()
        .create()


    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(Globals.URL_RANDOM_PICTURES)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()

    val api: ImageApi = retrofit.create(ImageApi::class.java)


    fun init() {
        // Initialize tflite API
        try {
            detector =
                TFLiteObjectDetectionAPIModel.create(
                    MainActivity.assets,
                    TF_OD_API_MODEL_FILE,
                    TF_OD_API_LABELS_FILE,
                    TF_OD_API_INPUT_SIZE,
                    TF_OD_API_IS_QUANTIZED
                )

        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(Globals.TAGLOG, "Exception initializing classifier! " + e.message)
        }


        getNewRandomImage()


    }

    fun getNewRandomImage() {
        val call: Call<ResponseBody> = api.GetImageData()


        call.enqueue(object : Callback<ResponseBody> {

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    response.body()?.let { jData ->
                        Log.i(Globals.TAGLOG, "Image downloaded!")
                        jData?.let { pList ->

                            val imgBytes = jData.byteStream().readBytes()

                            imageDataMutable.value = BitmapFactory.decodeByteArray(
                                imgBytes,
                                0,
                                imgBytes.size
                            )

                            val results: List<Classifier.Recognition?>? =
                                detector!!.recognizeImage(imageDataMutable.value)

                            for (result in results!!) {
                                    Log.i(
                                        TAGLOG,
                                        "Confidence res = " + result?.confidence
                                            .toString() + " title: " + result?.title
                                    )


                            }


                            results[0]?.let{bestGuess->

                                if (bestGuess.confidence!!>0.85) {
                                    objDetectorGuess.value = "I'm pretty sure it is a " + bestGuess.title

                                } else if (bestGuess.confidence >0.7) {
                                    objDetectorGuess.value = "It is probably a " + bestGuess.title

                                } else if (bestGuess.confidence >0.60) {
                                    objDetectorGuess.value = "It may be a " + bestGuess.title

                                } else if (bestGuess.confidence >0.5) {
                                    objDetectorGuess.value = "Hum... is it a " + bestGuess.title + "?"

                                } else if (bestGuess.confidence >0.4) {
                                    objDetectorGuess.value = "Hard to say but let's try with a " + bestGuess.title

                                } else {
                                    objDetectorGuess.value = "I have no clue of what it is... "
                                }

                            }



                        }

                    } ?: kotlin.run {
                        Log.w(Globals.TAGLOG, "Could not get image form the server")
                    }
                } else {
                    Log.w(
                        Globals.TAGLOG,
                        "Could not get image form the server: " + response.errorBody()
                    )

                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e(Globals.TAGLOG, t.message.toString() + " " + t.stackTrace)

            }

        })


    }

    fun getMutableImageData() : MutableLiveData<Bitmap>  {
        return imageDataMutable
    }


    fun getMutableOjectDetectorGuess() : MutableLiveData<String>  {
        return objDetectorGuess
    }
}