package com.example.matsandaplus

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SavedFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SavedFragment : Fragment() {
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
        return inflater.inflate(R.layout.fragment_saved, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val refreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.saved_refresh_layout)
        refreshLayout.setOnRefreshListener {
            refresh(view)
        }

        refresh(view)
    }

    private fun reverseJSONArray(jsonArray: JSONArray): JSONArray {
        val list = mutableListOf<JSONObject>()

        for (i in 0 until jsonArray.length()) {
            list.add(jsonArray.getJSONObject(i))
        }

        list.reverse()

        val reversedJsonArray = JSONArray()
        for (item in list) {
            reversedJsonArray.put(item)
        }

        return reversedJsonArray
    }


    private fun count(view: View, prefs: String): MutableSet<String> {
        return view.context.getSharedPreferences(prefs, Context.MODE_PRIVATE)
            .getStringSet("ids", emptySet())?.toMutableSet() ?: mutableSetOf()
    }

    private suspend fun getList(view: View, type: String) {
        val rv = when (type) {
            "berita" -> view.findViewById<RecyclerView>(R.id.saved_news_rv)
            "video" -> view.findViewById<RecyclerView>(R.id.saved_video_rv)
            else -> null
        }
        val prefs = when (type) {
            "berita" -> "saved_articles"
            "video" -> "saved_videos"
            else -> null
        }

        val newsIds = count(view, prefs.toString())
        val newsArray = withContext(Dispatchers.IO) {
            createNewsArray(view, type, newsIds)
        }

        withContext(Dispatchers.Main) {
            rv?.layoutManager = LinearLayoutManager(view.context, LinearLayoutManager.HORIZONTAL, false)
            rv?.adapter = AdapterRV(reverseJSONArray(newsArray), R.layout.berita_rv_layout, "headline", "saved")
            view.findViewById<SwipeRefreshLayout>(R.id.saved_refresh_layout).isRefreshing = false
        }
    }

    private suspend fun createNewsArray(view: View, type: String, newsIds: MutableSet<String>): JSONArray {
        val newsArray = JSONArray()

        val deferredList = newsIds.map { id ->
            CoroutineScope(Dispatchers.IO).async {
                val itemString = getItem(type, id.toInt())
                if (itemString.isNotEmpty()) {
                    try {
                        JSONObject(itemString)
                    } catch (e: Exception) {
                        Log.e("getList", "JSON parse error", e)
                        null
                    }
                } else {
                    Log.w("getList", "Item kosong untuk ID: $id")
                    null
                }
            }
        }

        val results = deferredList.awaitAll()

        results.filterNotNull().forEach { item ->
            newsArray.put(item)
        }
        return newsArray
    }

    private suspend fun getItem(type: String, id: Int): String {
        val url = when (type) {
            "berita" -> URL("http://tour-occupational.gl.at.ply.gg:32499/api/News/id/${id}")
            "video" -> URL("http://tour-occupational.gl.at.ply.gg:32499/api/Videos/id/${id}")
            else -> null
        }
        return try {
            val data = withContext(Dispatchers.IO) {
                val connection = url?.openConnection() as HttpURLConnection
                connection.apply {
                    requestMethod = "GET"
                    connectTimeout = 10000
                    readTimeout = 10000
                }

                if (connection.responseCode != HttpURLConnection.HTTP_OK) throw Exception()

                val response = connection.inputStream.bufferedReader().readText()
                JSONObject(response).getJSONObject("data").toString()
            }
            data
        } catch (_: Exception) {
            ""
        }
    }

    private fun refresh(view: View) {
        viewLifecycleOwner.lifecycleScope.launch {
            val refreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.saved_refresh_layout)
            refreshLayout.isRefreshing = true

            getList(view, "berita")
            getList(view, "video")

            val newsKosong = view.findViewById<TextView>(R.id.saved_berita_kosong)
            val videoKosong = view.findViewById<TextView>(R.id.saved_video_kosong)
            val articleCount = count(view, "saved_articles").size
            val videoCount = count(view, "saved_videos").size
            val articlesCount = count(view, "saved_articles").size + count(view, "saved_videos").size
            val articlesCountTextView = view.findViewById<TextView>(R.id.saved_count)

            if (articlesCount == 0 && videoCount == 0) {
                articlesCountTextView.text = "Anda belum menyimpan konten apapun"
                newsKosong.visibility = View.VISIBLE
                videoKosong.visibility = View.VISIBLE
            } else {
                val totalCount = articlesCount + videoCount
                articlesCountTextView.text = "$totalCount Konten Tersimpan"
                newsKosong.visibility = if (articleCount == 0) View.VISIBLE else View.GONE
                videoKosong.visibility = if (videoCount == 0) View.VISIBLE else View.GONE
            }

        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SavedFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SavedFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}