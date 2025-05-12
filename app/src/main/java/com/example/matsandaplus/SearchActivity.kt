package com.example.matsandaplus

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toolbar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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

        val query = intent.getStringExtra("searchQuery").toString()

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.search_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportActionBar?.title = query

        fetchItems(this, "berita", query)
    }

    private fun fetchItems(context: Context, type : String, query: String) {
        val recyclerView = when (type) {
            "berita" -> findViewById<RecyclerView>(R.id.search_berita_rv)
            "video" -> findViewById(R.id.search_video_rv)
            else -> findViewById(R.id.search_podcast_rv)
        }
        val progressBar = when (type) {
            "headline" -> findViewById<ProgressBar>(R.id.headline_progress)
            else -> findViewById(R.id.media_progress)
        }
        val url = when (type) {
            "berita" -> URL("http://tour-occupational.gl.at.ply.gg:32499/api/News")
            "video" -> URL("http://tour-occupational.gl.at.ply.gg:32499/api/Videos")
            "podcast" -> URL("http://tour-occupational.gl.at.ply.gg:32499/api/podcasts")
            else -> null
        }
        val layout : Int = when (type) {
            "headline" -> R.layout.berita_rv_layout
            else -> R.layout.home_media_rv_layout
        }
        recyclerView.layoutManager = LinearLayoutManager(this@SearchActivity, LinearLayoutManager.HORIZONTAL, false)

        CoroutineScope(Dispatchers.Main).launch {
            progressBar?.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE

            val newsArray : JSONArray = withContext(Dispatchers.IO) {
                try {
                    val connection = url?.openConnection() as HttpURLConnection
                    connection.connectTimeout = 5000
                    connection.readTimeout = 5000
                    connection.requestMethod = "GET"

                    if (connection.responseCode != HttpURLConnection.HTTP_OK) throw Exception("no connection")
                    if (connection.responseCode == HttpURLConnection.HTTP_NOT_FOUND) throw Exception("empty")

                    val response = connection.inputStream.bufferedReader().readText()
                    val jsonData = JSONObject(response).getJSONArray("data")
                    jsonData
                } catch (e: Exception) {
                    // Kalo gagal, return dummy data offline
                    buildOfflineJsonArray()
                }
            }

            recyclerView.adapter = AdapterRV(newsArray, layout, type, "search")
            progressBar?.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
        val swipeRefreshLayout = findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.home_refresh_layout)
        swipeRefreshLayout?.isRefreshing = false
    }

    private fun buildOfflineJsonArray(): JSONArray {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDate = currentDate.format(formatter)

        return JSONArray().apply {
            put(
                JSONObject().apply {
                    put("title", "You're Offline!")
                    put("createdAt", formattedDate)
                    put("image", "kosong")
                    put("type", "dummy")
                }
            )
        }
    }
}