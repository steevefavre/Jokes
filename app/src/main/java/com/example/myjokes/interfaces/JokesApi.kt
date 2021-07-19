package com.example.myjokes.interfaces

import com.example.myjokes.models.Joke
import com.example.myjokes.models.JokeList
import com.google.gson.JsonObject
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface JokesApi {
    @GET("random_ten")
    fun GetTenRandomJokes() : Call<List<Joke>>

//    @GET("view?")
//    fun GetProductData2(@Query("usp") usp: String = "sharing") : Call<ProductsData>



//    @GET("uc?")
//    fun GetImageData(@Query("export") export:String, @Query("id") id:String) : Call<ResponseBody>
}