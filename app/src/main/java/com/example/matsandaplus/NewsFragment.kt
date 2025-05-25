package com.example.matsandaplus

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [NewsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NewsFragment : Fragment() {
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

        return inflater.inflate(R.layout.fragment_news, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            val refreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.news_refresh_layout)
            refreshLayout.setOnRefreshListener {
                refresh(view)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val headlinesInt = fetchList(view, "headline")
            val listInt = fetchList(view, "berita")

            val aiButton = view.findViewById<TextView>(R.id.news_summary)
            aiButton.text = "Dapatkan Ringkasan"

            if (aiButton.text.toString() == "Dapatkan Ringkasan") {
                aiButton.setOnClickListener {
                    viewLifecycleOwner.lifecycleScope.launch {
                        applySummary(view)
                    }
                }
            }
        }

    }

    private suspend fun applySummary(view: View): Int {
        val summaryTextView = view.findViewById<TextView>(R.id.news_summary)

        val dots = arrayOf("", ".", "..", "...")
        var index = 0

        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                summaryTextView.text = "Menghasilkan Ringkasan${dots[index]}"
                index = (index + 1) % dots.size
                handler.postDelayed(this, 500)
            }
        }
        handler.post(runnable)

        return try {
            val summary = getSummary()
            handler.removeCallbacks(runnable)
            summaryTextView.text = summary
            summaryTextView.setOnClickListener {}
            1
        } catch (e: Exception) {
            handler.removeCallbacks(runnable)
            summaryTextView.text = "Gagal ambil ringkasan, coba lagi"
            summaryTextView.setOnClickListener {
                viewLifecycleOwner.lifecycleScope.launch {
                    applySummary(view)
                }
            }
            0
        }
    }

    private suspend fun getSummary(): String = withContext(Dispatchers.IO) {
        val connection = URL("http://tour-occupational.gl.at.ply.gg:32499/api/News/summary").openConnection() as HttpURLConnection

        connection.apply {
            requestMethod = "GET"
            connectTimeout = 60000
            readTimeout = 60000
        }

        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
            throw IOException("HTTP ${connection.responseCode}")
        }

        val response = connection.inputStream.bufferedReader().readText()
        JSONObject(response).getString("data")
    }

    private suspend fun fetchList(view: View, type: String): Boolean {
        val notFound = view.findViewById<TextView>(R.id.news_not_found)
        val content = view.findViewById<LinearLayout>(R.id.news_content)
        val refreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.news_refresh_layout)

        val rv = when (type) {
            "headline" -> view.findViewById<RecyclerView>(R.id.news_headline_rv)
            "berita" -> view.findViewById<RecyclerView>(R.id.news_more_rv)
            else -> return false
        }

        val url = when (type) {
            "headline" -> URL("http://tour-occupational.gl.at.ply.gg:32499/api/News/headlines")
            "berita" -> URL("http://tour-occupational.gl.at.ply.gg:32499/api/News")
            else -> return false
        }
        val layout = when (type) {
            "headline" -> R.layout.berita_rv_layout
            "berita" -> R.layout.home_media_rv_layout
            else -> return false
        }
        val layoutManager = when (type) {
            "headline" -> LinearLayoutManager(view.context, LinearLayoutManager.HORIZONTAL, false)
            "berita" -> LinearLayoutManager(view.context, LinearLayoutManager.VERTICAL, false)
            else -> return false
        }

        withContext(Dispatchers.Main) {
            notFound?.visibility = View.GONE
            refreshLayout?.isRefreshing = true
        }

        return try {
            val newsArray = withContext(Dispatchers.IO) {
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 100000

                if (connection.responseCode != HttpURLConnection.HTTP_OK) throw Exception("HTTP error code ${connection.responseCode}")

                val response = connection.inputStream.bufferedReader().readText()
                JSONObject(response).getJSONArray("data")
            }

            withContext(Dispatchers.Main) {
                rv.layoutManager = layoutManager
                rv.adapter = AdapterRV(newsArray, layout, type, "news")

                if (type == "berita") {
                    rv.isNestedScrollingEnabled = false
                }

                refreshLayout?.isRefreshing = false
                content?.visibility = View.VISIBLE
            }
            true
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                refreshLayout?.isRefreshing = false
                notFound?.visibility = View.VISIBLE
            }
            false
        }
    }


    private fun refresh(view: View) {
        viewLifecycleOwner.lifecycleScope.launch {
            val headlineSuccess = fetchList(view,"headline")
            val listSuccess = fetchList(view,"berita")
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment NewsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NewsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}