package com.example.myjokes.views

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myjokes.R
import com.example.myjokes.adapters.JokesAdapter
import com.example.myjokes.models.Joke

class JokesFragment : Fragment() {

    companion object {
        fun newInstance() = JokesFragment()
    }

    private lateinit var viewModel: JokesViewModel
    private lateinit var tenJokesListObserver : Observer<ArrayList<Joke>>

    private lateinit var jokeListView: RecyclerView
    private lateinit var loaderView: ProgressBar
    private lateinit var jokeListAdapter : JokesAdapter

    private val MIN_LOADING_SHOW_TIME = 1500
    private var last_loading_view_show_time = 0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var rootView = inflater.inflate(R.layout.jokes_fragment, container, false)
        jokeListView = rootView.findViewById<RecyclerView>(R.id.jokes_list_view)
        loaderView = rootView.findViewById<ProgressBar>(R.id.loadingBar)
        jokeListView.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)

        jokeListView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            @Override
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    Log.d("-----", "end");
                    showLoading()
                    viewModel.getTenRandomJokesData(false)

                }

            }
        })


        rootView.findViewById<AppCompatButton>(R.id.bt_load_ten_jokes_button).setOnClickListener {
            jokeListView.scrollToPosition(0)
            showLoading()
            viewModel.getTenRandomJokesData(true)
        }

        rootView.findViewById<AppCompatButton>(R.id.bt_clear_jokes_favorite_button).setOnClickListener {
            viewModel.clearFavorites()
            jokeListAdapter.notifyDataSetChanged()
        }

        return rootView
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(JokesViewModel::class.java)

        val navTo = NavHostFragment.findNavController(this)

        jokeListAdapter = JokesAdapter(viewModel.getTenJokes(),
            object : JokesAdapter.OnItemClickListener {
                override fun onItemClicked(joke: Joke) {
                    navTo.navigate(R.id.action_jokesFragment_to_randomImageFragment)
                }

                override fun onShowPunchClicked(joke: Joke) {
                    viewModel.toggleShowPunch(joke)
                    jokeListAdapter.notifyDataSetChanged()
                }

                override fun onFavoriteClicked(joke: Joke) {
                    viewModel.toggleFavorite(joke)

                    MainActivity.addFavoriteJoke(joke.id)
                    jokeListAdapter.notifyDataSetChanged()
                }
            }
        )

        jokeListView.adapter = jokeListAdapter


        /*jokeListView.addOnItemTouchListener(object: RecyclerView.OnItemTouchListener{
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {

            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {

            }

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {

            }

        })*/

        tenJokesListObserver = Observer<ArrayList<Joke>> {

            jokeListAdapter.notifyDataSetChanged()
            hideLoading()

        }


        viewModel.getTenMutableJokes().observe(viewLifecycleOwner, tenJokesListObserver)

        showLoading()
        viewModel.init()

    }

    private fun showLoading() {
        last_loading_view_show_time = System.currentTimeMillis()
        loaderView.visibility = View.VISIBLE

    }

    private fun hideLoading() {

        if (last_loading_view_show_time+MIN_LOADING_SHOW_TIME>System.currentTimeMillis()) {
            Handler(Looper.getMainLooper()).postDelayed(
                {
                    loaderView.visibility = View.GONE
                },
                (last_loading_view_show_time + MIN_LOADING_SHOW_TIME - System.currentTimeMillis()) // value in milliseconds
            )


        } else {
            loaderView.visibility = View.GONE
        }



    }


}