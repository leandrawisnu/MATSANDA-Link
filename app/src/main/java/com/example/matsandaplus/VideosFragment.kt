package com.example.matsandaplus

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [VideosFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class VideosFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_videos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val refreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.news_refresh_layout)
        refreshLayout.setOnRefreshListener {
            refresh(view)
        }

        refresh(view)
    }

    private suspend fun fetchList(type: String): Boolean {
        val notFound = view?.findViewById<TextView>(R.id.news_not_found)
        val content = view?.findViewById<LinearLayout>(R.id.news_content)
        val refreshLayout = view?.findViewById<SwipeRefreshLayout>(R.id.news_refresh_layout)

        val rv = when (type) {
            "headline" -> view?.findViewById<RecyclerView>(R.id.news_headline_rv)
            "video" -> view?.findViewById(R.id.news_more_rv)
            else -> return false
        }

        val url = when (type) {
            "headline" -> URL("http://tour-occupational.gl.at.ply.gg:32499/api/Videos?take=5")
            "video" -> URL("http://tour-occupational.gl.at.ply.gg:32499/api/Videos")
            else -> return false
        }
        val layout = when (type) {
            "headline" -> R.layout.berita_rv_layout
            "video" -> R.layout.home_media_rv_layout
            else -> return false
        }
        val layoutManager = when (type) {
            "headline" -> LinearLayoutManager(view?.context, LinearLayoutManager.HORIZONTAL, false)
            "video" -> LinearLayoutManager(view?.context, LinearLayoutManager.VERTICAL, false)
            else -> return false
        }

        withContext(Dispatchers.Main) {
            notFound?.visibility = View.GONE
            content?.visibility = View.GONE
            refreshLayout?.isRefreshing = true
        }

        try {
            val newsArray = withContext(Dispatchers.IO) {
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 100000

                if (connection.responseCode != HttpURLConnection.HTTP_OK) throw Exception()

                val response = connection.inputStream.bufferedReader().readText()
                JSONObject(response).getJSONArray("data")
            }

            CoroutineScope(Dispatchers.Main).launch {
                rv?.layoutManager = layoutManager
                rv?.adapter = AdapterRV(newsArray, layout, type, "news")

                when (type) {
                    "video" -> rv?.isNestedScrollingEnabled = false
                }

                refreshLayout?.isRefreshing = false
                content?.visibility = View.VISIBLE
            }
            return true
        } catch (e: Exception) {
            notFound?.visibility = View.VISIBLE
            content?.visibility = View.GONE
        }
        return false
    }

    private fun refresh(view: View) {
        CoroutineScope(Dispatchers.Main).launch {
            fetchList("headline")
            fetchList("berita")
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment VideosFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            VideosFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}