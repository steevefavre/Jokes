package com.example.myjokes.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myjokes.R
import com.example.myjokes.models.Joke

class JokesAdapter (val jokeList: ArrayList<Joke>, val itemClickListener: OnItemClickListener)
    : RecyclerView.Adapter<JokesAdapter.ViewHolder>() {

    lateinit var mContex: Context

    interface OnItemClickListener{
        fun onItemClicked(joke: Joke)
        fun onShowPunchClicked(joke: Joke)
        fun onFavoriteClicked(joke: Joke)
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JokesAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.joke_item, parent, false)
        mContex = parent.context

        return ViewHolder(v)
    }


    override fun onBindViewHolder(holder: JokesAdapter.ViewHolder, position: Int) {
        holder.bindItems(mContex, jokeList[position], itemClickListener)
     }


    override fun getItemCount(): Int {
        return jokeList.size
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var jokeSetupView : TextView = itemView.findViewById(R.id.tv_joke_setup) as TextView
        var jokePunchView : TextView = itemView.findViewById(R.id.tv_joke_punchline) as TextView
        var jokeTypeView : TextView = itemView.findViewById(R.id.tv_joke_type) as TextView

        var showPunchButton: ImageView = itemView.findViewById(R.id.iv_show_punchtext) as ImageView
        var favoriteJokeButton: ImageView = itemView.findViewById(R.id.iv_favorite_joke_button) as ImageView

        fun bindItems(context: Context, joke: Joke, clickListener: OnItemClickListener) {

            jokeSetupView.text = joke.setup
            jokePunchView.text = joke.punchline
            jokeTypeView.text = joke.type

            if (joke.showPunchline) {
                showPunchButton.visibility = View.GONE
                jokePunchView.visibility = View.VISIBLE
            } else {
                showPunchButton.visibility = View.VISIBLE
                jokePunchView.visibility = View.GONE
            }

            if (joke.favoriteJoke) {
                favoriteJokeButton.setColorFilter(ContextCompat.getColor(context, R.color.star_yellow), android.graphics.PorterDuff.Mode.SRC_IN);
            } else {
                favoriteJokeButton.setColorFilter(ContextCompat.getColor(context, R.color.star_grey), android.graphics.PorterDuff.Mode.SRC_IN);
            }



            itemView.setOnClickListener {
                clickListener.onItemClicked(joke)
            }


            showPunchButton.setOnClickListener {
                clickListener.onShowPunchClicked(joke)
            }

            favoriteJokeButton.setOnClickListener {
                clickListener.onFavoriteClicked(joke)
            }

        }

    }
}