package com.example.myjokes.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class JokeList {
    @SerializedName("products")
    @Expose
    var jokes : List<Joke> = emptyList()


}