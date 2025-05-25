package com.example.matsandaplus

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.FrameLayout
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
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class DetailsVideoActivity : AppCompatActivity() {
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_details_video)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val type = intent.getStringExtra("type").toString()
        val id = intent.getIntExtra("id", 0)

        findViewById<TextView>(R.id.detail_not_found).visibility = View.GONE
        findViewById<ImageView>(R.id.detail_back_button).setOnClickListener {
            finish()
        }

        val saveButton = findViewById<ImageView>(R.id.detail_save_button)

        saveButton.apply {
            isSelected = isArticleSaved(this@DetailsVideoActivity, id)
            setOnClickListener {
                val selected = !isSelected
                isSelected = selected

                if (selected) {
                    saveArticle(this@DetailsVideoActivity, id)
                } else {
                    unsaveArticle(this@DetailsVideoActivity, id)
                }
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            val IntI = fetchItem(id)
            val IntL = fetchList("video", id)
        }

        val chipGroup : ChipGroup = findViewById(R.id.chipGroup)
        chipGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.chip_video -> {
                    CoroutineScope(Dispatchers.IO).launch {
                        fetchList("video", id)
                    }
                }
                R.id.chip_podcast -> {
                    CoroutineScope(Dispatchers.IO).launch {
                        fetchList("podcast", id)
                    }
                }
            }
        }
    }

    fun isArticleSaved(context: Context, newsId: Int): Boolean {
        val prefs = context.getSharedPreferences("saved_videos", Context.MODE_PRIVATE)
        return prefs.getStringSet("ids", mutableSetOf())?.contains(newsId.toString()) == true
    }

    fun saveArticle(context: Context, newsId: Int) {
        val prefs = context.getSharedPreferences("saved_videos", Context.MODE_PRIVATE)
        val saved = prefs.getStringSet("ids", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        saved.add(newsId.toString())
        prefs.edit().putStringSet("ids", saved.toSet()).apply()
    }

    fun unsaveArticle(context: Context, newsId: Int) {
        val prefs = context.getSharedPreferences("saved_videos", Context.MODE_PRIVATE)
        val saved = prefs.getStringSet("ids", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        saved.remove(newsId.toString())
        prefs.edit().putStringSet("ids", saved.toSet()).apply()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private suspend fun fetchItem(id: Int) : Boolean {
        val content : LinearLayout = findViewById(R.id.detail_content)
        val progressBar : ProgressBar = findViewById(R.id.detail_pg)
        val webViewVideo : WebView = findViewById(R.id.detail_webview_video)
        val notFound : TextView = findViewById(R.id.detail_not_found)
        val fullscreen : FrameLayout = findViewById(R.id.fullscreen_webview)

        return try {
            withContext(Dispatchers.Main) {
                fullscreen.visibility = View.GONE
                progressBar.visibility = View.VISIBLE
                content.visibility = View.GONE
                notFound.visibility = View.GONE
            }

            val item = withContext(Dispatchers.IO) {
                val connection = URL("http://tour-occupational.gl.at.ply.gg:32499/api/Videos/id/${id}").openConnection() as HttpURLConnection
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
                val shareButton = findViewById<ImageView>(R.id.detail_share_button)

                val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("id"))

                judulText.text = item.getString("title")
                tanggalText.text = formatter.format(LocalDate.parse(item.getString("createdAt")))
                shareButton.setOnClickListener {
                    shareButton.setOnClickListener {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, item.getString("link"))
                            type = "text/plain"
                        }

                        startActivity(Intent.createChooser(shareIntent, "Share via"))

                    }
                }

                val htmlVideo = """
                    <html>
                        <head>
                            <style>
                                body {
                                margin: 0;
                                padding: 0;
                                }
                            </style>
                        </head>
                        <body>
                            <iframe width="100%" height="100%" src="https://www.youtube.com/embed/${item.getString("link").substringAfter("https://youtu.be/")}" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" referrerpolicy="strict-origin-when-cross-origin" allowfullscreen></iframe>
                        </body>
                    </html>
                    """.trimIndent()

                webViewVideo.settings.javaScriptEnabled = true
                webViewVideo.webChromeClient = object : WebChromeClient() {
                    override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                        if (customView != null) {
                            callback?.onCustomViewHidden()
                            return
                        }

                        customView = view
                        customViewCallback = callback
                        fullscreen.visibility = View.VISIBLE
                        fullscreen.addView(view)
                        webViewVideo.visibility = View.GONE

                        // Lock landscape
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

                        window.decorView.systemUiVisibility =
                            View.SYSTEM_UI_FLAG_FULLSCREEN or
                                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    } 

                    override fun onHideCustomView() {
                        fullscreen.removeView(customView)
                        fullscreen.visibility = View.GONE

                        customView = null
                        customViewCallback?.onCustomViewHidden()
                        customViewCallback = null

                        webViewVideo.visibility = View.VISIBLE

                        // Unlock orientation
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                    }
                }

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

    private fun fetchList(type : String, id: Int) : Boolean {
        val rv : RecyclerView = findViewById(R.id.detail_video_rv)
        val layout = R.layout.home_media_rv_layout

        CoroutineScope(Dispatchers.Main).launch {
            rv.visibility = View.GONE
        }

        try {
            when (type) {
                "video" -> {
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
                            rv.visibility = View.VISIBLE
                        }
                    }
                }
                "podcast" -> {
                    CoroutineScope(Dispatchers.IO).launch {
                        val connection = URL("http://tour-occupational.gl.at.ply.gg:32499/api/podcasts/except/${id}").openConnection() as HttpURLConnection
                        connection.connectTimeout = 10000
                        connection.readTimeout = 10000
                        connection.requestMethod = "GET"

                        if (connection.responseCode != HttpURLConnection.HTTP_OK) throw Exception()

                        val response = connection.inputStream.bufferedReader().readText()
                        val list = JSONObject(response).getJSONArray("data")

                        withContext(Dispatchers.Main) {
                            rv.layoutManager = LinearLayoutManager(this@DetailsVideoActivity, LinearLayoutManager.VERTICAL, false)
                            rv.adapter = AdapterRV(list, layout, "video", "detail")
                            rv.visibility = View.VISIBLE
                        }
                    }
                }
                else -> return false
            }
            return true
        } catch (e: Exception) {
            Toast.makeText(this@DetailsVideoActivity, "Error", Toast.LENGTH_SHORT).show()
            return false
        }
    }

    override fun onBackPressed() {
        val webViewVideo = findViewById<WebView>(R.id.detail_webview_video)
        webViewVideo.loadUrl("about:blank")
        super.onBackPressed()
    }

}