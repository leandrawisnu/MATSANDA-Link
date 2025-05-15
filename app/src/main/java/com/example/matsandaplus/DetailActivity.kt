package com.example.matsandaplus

import android.os.Bundle
import android.text.Html
import android.view.View
import android.webkit.WebView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.ceil

class DetailActivity : AppCompatActivity() {
    private var type : String = ""
    private var id = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        this.type = intent.getStringExtra("type").toString()
        this.id = intent.getIntExtra("id", 0)

        CoroutineScope(Dispatchers.Main).launch {
            findViewById<TextView>(R.id.detail_not_found).visibility = View.GONE
            findViewById<ImageView>(R.id.detail_back_button).setOnClickListener {
                finish()
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            fetchItem()

            CoroutineScope(Dispatchers.Main).launch {
                findViewById<ProgressBar>(R.id.detail_pg).visibility = View.GONE
            }
        }

        val refreshLayout : androidx.swiperefreshlayout.widget.SwipeRefreshLayout = findViewById(R.id.detail_refresh_layout)
        refreshLayout.setOnRefreshListener {
            CoroutineScope(Dispatchers.IO).launch {
                fetchItem()
            }
        }
    }

    private suspend fun fetchItem() : Boolean {
        when (type) {
            "berita" -> {
                withContext(Dispatchers.Main) {
                    val content : LinearLayout = findViewById(R.id.detail_content)
                    val progressBar : ProgressBar = findViewById(R.id.detail_pg)
                    progressBar.visibility = View.VISIBLE
                    content.visibility = View.GONE
                }

                return try {
                    withContext(Dispatchers.Main) {
                        val notFound : TextView = findViewById(R.id.detail_not_found)
                        notFound.visibility = View.GONE
                    }

                    val item = withContext(Dispatchers.IO) {
                        val connection = URL("http://tour-occupational.gl.at.ply.gg:32499/api/News/id/${id}").openConnection() as HttpURLConnection
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
                        val menitText = findViewById<TextView>(R.id.detail_baca)
                        val webView = findViewById<WebView>(R.id.detail_webview)

                        val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("id"))

                        judulText.text = item.getString("title")
                        tanggalText.text = formatter.format(LocalDate.parse(item.getString("createdAt")))
                        estimateReadingTime(item.getString("description")).also { menitText.text = it }

                        val htmlContent = """
                            <html>
                                <head>
                                    <link href="https://fonts.googleapis.com/css2?family=Poppins&display=swap" rel="stylesheet">
                                    <style type="text/css">
                                        body {
                                            text-align: justify;
                                            padding: 0;
                                            margin: 0;
                                            font-family: 'Poppins', sans-serif;
                                            font-size: 13px;
                                        }
                                    </style>
                                </head>
                                <body>
                                    ${item.getString("description")}
                                </body>
                            </html>
                            """.trimIndent()

                        webView.settings.javaScriptEnabled = false
                        webView.loadData(htmlContent, "text/html", "UTF-8")
                    }

                    withContext(Dispatchers.Main) {
                        val gambar = findViewById<ImageView>(R.id.detail_image)
                        val gambarLink = item.getString("image")

                        if (!gambarLink.equals("kosong")) {
                            Glide.with(this@DetailActivity)
                                .load(gambarLink)
                                .into(gambar)
                        }

                        val progressBar : ProgressBar = findViewById(R.id.detail_pg)
                        val content : LinearLayout = findViewById(R.id.detail_content)
                        progressBar.visibility = View.GONE
                        content.visibility = View.VISIBLE
                    }
                    true
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        val notFoundText : TextView = findViewById(R.id.detail_not_found)
                        val progressBar : ProgressBar = findViewById(R.id.detail_pg)
                        notFoundText.visibility = View.VISIBLE
                        progressBar.visibility = View.GONE
                    }
                    false
                }.also {
                    CoroutineScope(Dispatchers.Main).launch {
                        findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.detail_refresh_layout).isRefreshing = false
                    }
                }
            }
            else -> {
                val progressBar : ProgressBar = findViewById(R.id.detail_pg)
                progressBar.visibility = View.VISIBLE

                return try {
                    withContext(Dispatchers.Main) {
                        val notFound : TextView = findViewById(R.id.detail_not_found)
                        notFound.visibility = View.GONE
                    }

                    val item = withContext(Dispatchers.IO) {
                        val connection = URL("http://tour-occupational.gl.at.ply.gg:32499/api/Video/id/${id}").openConnection() as HttpURLConnection
                        connection.connectTimeout = 10000
                        connection.readTimeout = 10000
                        connection.requestMethod = "GET"

                        if (connection.responseCode != HttpURLConnection.HTTP_OK) throw Exception()

                        val response = connection.inputStream.bufferedReader().readText()
                        JSONObject(response).getJSONObject("data")
                    }

                    withContext(Dispatchers.Main) {
                        val judulText = findViewById<TextView>(R.id.detail_judul)
                        val menitText = findViewById<TextView>(R.id.detail_baca)
                        val tanggalText = findViewById<TextView>(R.id.detail_tanggal)
                        val webView = findViewById<WebView>(R.id.detail_webview)

                        val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("id"))

                        judulText.text = item.getString("title")
                        tanggalText.text = formatter.format(LocalDate.parse(item.getString("createdAt")))
                        estimateReadingTime(item.getString("description")).also { menitText.text = it }

                        val htmlContent = """
                            <html>
                                <head>
                                    <link href="https://fonts.googleapis.com/css2?family=Poppins&display=swap" rel="stylesheet">
                                    <style type="text/css">
                                        body {
                                            text-align: justify;
                                            padding: 0;
                                            margin: 0;
                                            font-family: 'Poppins', sans-serif;
                                            font-size: 13px;
                                        }
                                    </style>
                                </head>
                                <body>
                                    ${item.getString("description")}
                                </body>
                            </html>
                            """.trimIndent()

                        webView.settings.javaScriptEnabled = false
                        webView.loadData(htmlContent, "text/html", "UTF-8")
                    }
                    true
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        val notFoundText : TextView = findViewById(R.id.detail_not_found)
                        notFoundText.visibility = View.VISIBLE
                        progressBar.visibility = View.GONE
                    }
                    false
                }.also {
                    CoroutineScope(Dispatchers.Main).launch {
                        findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.detail_refresh_layout).isRefreshing = false
                    }
                }
            }
        }
    }

    private fun estimateReadingTime(html: String): String {
        val plainText = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString()

        val words = plainText.trim().split("\\s+".toRegex()).size
        val minutes = ceil(words / 200.0).toInt() // 200 kata/menit

        return "Bacaan $minutes menit"
    }

}