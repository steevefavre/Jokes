package com.example.myjokes.views

import android.content.SharedPreferences
import android.content.res.AssetManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myjokes.Globals.Companion.SHARED_PREF_JOKES_FILE_NAME
import com.example.myjokes.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


class MainActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun saveFavorites() {
        val sharedPrefs = applicationContext.getSharedPreferences(SHARED_PREF_JOKES_FILE_NAME, 0)
        val editor: SharedPreferences.Editor = sharedPrefs.edit()
        val gson = Gson()

        val json = gson.toJson(favoriteJokesIds)

        editor.putString("favorites", json)
        editor.apply()

    }


    fun readFavorites() {
        val sharedPrefs = applicationContext.getSharedPreferences(SHARED_PREF_JOKES_FILE_NAME, 0)
        val gson = Gson()
        val json = sharedPrefs.getString("favorites", "")
        val type: Type = object : TypeToken<List<Int?>?>() {}.type

        gson.fromJson<ArrayList<Int>>(json, type)?.let{
            favoriteJokesIds = it
        }


    }



    companion object {
        var favoriteJokesIds : ArrayList<Int> = ArrayList()

        lateinit var assets : AssetManager

        fun addFavoriteJoke(jokeId: Int) {
            for (id in favoriteJokesIds) {
                if (id == jokeId) {
                    return
                }
            }
            favoriteJokesIds.add(jokeId)

        }

    }



    override fun onResume(){
        super.onResume()
        readFavorites()

    }


    override fun onPause(){
        super.onPause()
        saveFavorites()

    }


}