package com.example.myjokes.interfaces


import com.example.myjokes.models.Joke


interface HttpManagerCallBack {
    fun onJokesLoaded(list: List<Joke>)

    fun onJokesError(error: String)
}