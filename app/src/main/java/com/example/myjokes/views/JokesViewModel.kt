package com.example.myjokes.views

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myjokes.Globals
import com.example.myjokes.interfaces.JokesApi
import com.example.myjokes.models.Joke
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory


class JokesViewModel() : ViewModel() {

    private val jokesListMutable: MutableLiveData<ArrayList<Joke>> = MutableLiveData<ArrayList<Joke>>()
    private val tenJokes : ArrayList<Joke> = ArrayList()

    var gson = GsonBuilder()
        .excludeFieldsWithoutExposeAnnotation()
        .setLenient()
        .create()


    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(Globals.URL_JOKES_API)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()

    val api: JokesApi = retrofit.create(JokesApi::class.java)


    fun init() {
        getTenRandomJokesData(true)

    }


    fun getTenMutableJokes():LiveData<ArrayList<Joke>> {
        return jokesListMutable
    }

    fun getTenJokes():ArrayList<Joke> {
        return tenJokes
    }

    fun toggleShowPunch(joke: Joke) {
        for (itemJoke in tenJokes) {

            if (joke.id==itemJoke.id) {
                itemJoke.showPunchline = !itemJoke.showPunchline
            }

        }
    }
    fun toggleFavorite(joke: Joke) {
        for (itemJoke in tenJokes) {

            if (joke.id==itemJoke.id) {
                itemJoke.favoriteJoke = !itemJoke.favoriteJoke

            }

        }
    }


    fun getTenRandomJokesData(clearJokeList: Boolean) {
        val call: Call<List<Joke>> = api.GetTenRandomJokes()


        call.enqueue(object : Callback<List<Joke>> {

            override fun onResponse(call: Call<List<Joke>>, response: Response<List<Joke>>) {
                if (response.isSuccessful) {
                    response.body()?.let { jData ->
                        Log.i(Globals.TAGLOG, "Jokes data downloaded!")
                        jData?.let { pList ->

                            if (clearJokeList) {
                                tenJokes.clear()
                            }

                            var tempArrayList: ArrayList<Joke> = ArrayList()
                            tempArrayList.addAll(tenJokes)
                            tempArrayList.addAll(jData)

                            //Removing duplicates
                            val hashSet: LinkedHashSet<Joke> = LinkedHashSet(tempArrayList)
                            tempArrayList = ArrayList(hashSet)

                            tenJokes.clear()
                            tenJokes.addAll(tempArrayList)
                            jokesListMutable.value = tempArrayList

                            setFavorites()

                            Log.i(Globals.TAGLOG, "Jokes data set to mutable var!")
                        }

                    } ?: kotlin.run {
                        Log.w(Globals.TAGLOG, "Could not get jokes form the server")
                    }
                } else {
                    Log.w(
                        Globals.TAGLOG,
                        "Could not get jokes form the server: " + response.errorBody()
                    )

                }

            }

            override fun onFailure(call: Call<List<Joke>>, t: Throwable) {
                Log.e(Globals.TAGLOG, t.message.toString() + " " + t.stackTrace)

            }

        })


    }

    private fun setFavorites() {

        jokesListMutable.value?.let {

            for (joke in it) {
                for (idFav in MainActivity.favoriteJokesIds) {
                    if (idFav==joke.id) {
                        joke.favoriteJoke = true
                        break
                    }

                }

            }
        }
    }



    fun clearFavorites() {
        MainActivity.favoriteJokesIds.clear()

        jokesListMutable.value?.let {

            for (joke in it) {
                joke.favoriteJoke = false
            }
        }
    }









}