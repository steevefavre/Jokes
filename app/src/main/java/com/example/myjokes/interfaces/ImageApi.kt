package com.example.myjokes.interfaces

import com.example.myjokes.Globals
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ImageApi {

    @GET(Globals.IMG_SIZE.toString())
    fun GetImageData() : Call<ResponseBody>
}