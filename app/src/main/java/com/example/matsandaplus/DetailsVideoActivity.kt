package com.example.matsandaplus

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class DetailsVideoActivity : AppCompatActivity() {
    private var type : String = ""
    private var id = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_details_video)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        this.type = intent.getStringExtra("type").toString()
        this.id = intent.getIntExtra("id", 0)

        CoroutineScope(Dispatchers.IO).launch {
            val IntI = fetchItem()
            val IntL = fetchList()
        }
    }

    private suspend fun fetchItem() : Boolean {
        val content : LinearLayout = findViewById(R.id.detail_content)
        val progressBar : ProgressBar = findViewById(R.id.detail_pg)
        val webViewVideo : WebView = findViewById(R.id.detail_webview_video)
        val notFound : TextView = findViewById(R.id.detail_not_found)

        return try {
            withContext(Dispatchers.Main) {
                progressBar.visibility = View.VISIBLE
                content.visibility = View.GONE
                notFound.visibility = View.GONE
            }

            val item = withContext(Dispatchers.IO) {
                val connection = URL("http://tour-occupational.gl.at.ply.gg:32499/api/Videos/id/${this@DetailsVideoActivity.id}").openConnection() as HttpURLConnection
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                connection.requestMethod = "GET"

                if (connection.responseCode != HttpURLConnection.HTTP_OK) throw Exception()

                val response = connection.inputStream.bufferedReader().readText()
                JSONObject(response).getJSONObject("data")
            }

            withContext(Dispatchers.Main) {
                val judulText = findViewById<TextView>(R.id.detail_judul)
                val tanggalText = findViewById<TextView>(R.id.detail_tanggal)

                val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("id"))

                judulText.text = item.getString("title")
                tanggalText.text = formatter.format(LocalDate.parse(item.getString("createdAt")))

                val htmlVideo = """
                    <html>
                        <body>
                            <iframe width="100%" height="100%" src="https://www.youtube.com/embed/${item.getString("link").substringAfter("https://youtu.be/")}" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" referrerpolicy="strict-origin-when-cross-origin" allowfullscreen></iframe>
                        </body>
                    </html>
                    """.trimIndent()

                webViewVideo.settings.javaScriptEnabled = true
                webViewVideo.loadDataWithBaseURL(null, htmlVideo, "text/html", "UTF-8", null)
            }

            withContext(Dispatchers.Main) {
                progressBar.visibility = View.GONE
                content.visibility = View.VISIBLE
            }
            true
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                notFound.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
            }
            false
        }.also {
            CoroutineScope(Dispatchers.Main).launch {
                findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.detail_refresh_layout).isRefreshing = false
            }
        }
    }

    private fun fetchList() : Boolean {
        val rv : RecyclerView = findViewById(R.id.detail_video_rv)
        val layout = R.layout.home_media_rv_layout

        try {
            CoroutineScope(Dispatchers.IO).launch {
                val connection = URL("http://tour-occupational.gl.at.ply.gg:32499/api/Videos/except/${id}").openConnection() as HttpURLConnection
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                connection.requestMethod = "GET"

                if (connection.responseCode != HttpURLConnection.HTTP_OK) throw Exception()

                val response = connection.inputStream.bufferedReader().readText()
                val list = JSONObject(response).getJSONArray("data")

                withContext(Dispatchers.Main) {
                    rv.layoutManager = LinearLayoutManager(this@DetailsVideoActivity, LinearLayoutManager.VERTICAL, false)
                    rv.adapter = AdapterRV(list, layout, "video", "detail")
                }
            }
            return true
        } catch (e: Exception) {
            Toast.makeText(this@DetailsVideoActivity, "Error", Toast.LENGTH_SHORT).show()
            return false
        }
    }
}