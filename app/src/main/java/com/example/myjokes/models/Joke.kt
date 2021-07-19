package com.example.myjokes.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Joke {
    @SerializedName("id")
    @Expose
    var id : Int = 0

    @SerializedName("type")
    @Expose
    var type : String? = null

    @SerializedName("setup")
    @Expose
    var setup : String? = null

    @SerializedName("punchline")
    @Expose
    var punchline : String? = null

    var showPunchline: Boolean = false

    var favoriteJoke: Boolean = false

}