package com.example.matsandaplus

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toolbar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.http.params.HttpConnectionParams
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SearchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val searchIndicator = findViewById<TextView>(R.id.search_indicator)
        val query = intent.getStringExtra("searchQuery").toString()
        val backButton : ImageView = findViewById(R.id.search_back_button)

        backButton.setOnClickListener {
            finish()
        }
        searchIndicator.text = "Pencarian: ${query}"

        lifecycleScope.launch {
            fetchItems(this@SearchActivity, "headline", query)
            fetchItems(this@SearchActivity, "berita", query)
            fetchItems(this@SearchActivity, "video", query)
            fetchItems(this@SearchActivity, "podcast", query)
        }

    }

    private suspend fun fetchItems(context: Context, type : String, query: String): Boolean {
        val noResult = when (type) {
            "headline" -> findViewById<TextView>(R.id.search_top_no_results)
            "berita" -> findViewById(R.id.search_berita_no_results)
            "video" -> findViewById(R.id.search_video_no_results)
            else -> findViewById(R.id.search_podcast_no_results)
        }
        val recyclerView = when (type) {
            "headline" -> findViewById<RecyclerView>(R.id.search_top_rv)
            "berita" -> findViewById(R.id.search_berita_rv)
            "video" -> findViewById(R.id.search_video_rv)
            else -> findViewById(R.id.search_podcast_rv)
        }
        val progressBar = when (type) {
            "headline" -> findViewById<ProgressBar>(R.id.search_top_pb)
            "berita" -> findViewById(R.id.search_berita_pb)
            "video" -> findViewById(R.id.search_videos_pb)
            else -> findViewById(R.id.search_podcasts_pb)
        }
        val url = when (type) {
            "headline" -> URL("http://tour-occupational.gl.at.ply.gg:32499/api/search?query=${query}")
            "berita" -> URL("http://tour-occupational.gl.at.ply.gg:32499/api/News?search=${query}")
            "video" -> URL("http://tour-occupational.gl.at.ply.gg:32499/api/Videos?search=${query}")
            else -> URL("http://tour-occupational.gl.at.ply.gg:32499/api/podcasts?search=${query}")
        }
        val layout : Int = when (type) {
            "headline" -> R.layout.berita_rv_layout
            else -> R.layout.home_media_rv_layout
        }
        recyclerView.layoutManager = when (type) {
            "headline" -> LinearLayoutManager(this@SearchActivity, LinearLayoutManager.HORIZONTAL, false)
            else -> LinearLayoutManager(this@SearchActivity, LinearLayoutManager.VERTICAL, false)
        }

        return try {
            withContext(Dispatchers.Main) {
                progressBar?.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            }

            val newsArray = withContext(Dispatchers.IO) {
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                connection.requestMethod = "GET"

                if (connection.responseCode != HttpURLConnection.HTTP_OK) throw Exception()

                val response = connection.inputStream.bufferedReader().readText()
                JSONObject(response).getJSONArray("data")
            }

            withContext(Dispatchers.Main) {
                if (newsArray.length() > 0) {
                    recyclerView.adapter = AdapterRV(newsArray, layout, type, "search")
                    noResult.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                } else {
                    noResult.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                }
                progressBar?.visibility = View.GONE
            }
            true
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                noResult.visibility = View.VISIBLE
                progressBar?.visibility = View.GONE
                recyclerView.visibility = View.GONE
            }
            false
        }.also {
            withContext(Dispatchers.Main) {
                findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.home_refresh_layout)?.isRefreshing = false
            }
        }
    }
}