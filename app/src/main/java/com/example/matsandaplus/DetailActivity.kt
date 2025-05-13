package com.example.matsandaplus

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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

class DetailActivity : AppCompatActivity() {
    var type : String = ""
    var id : Int = 0

    @SuppressLint("MissingInflatedId")
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
            val notFound : TextView = findViewById(R.id.detail_not_found)
            notFound.visibility = View.GONE
        }

        val bI = CoroutineScope(Dispatchers.IO).launch {
            fetchItem()
        }
    }

    private suspend fun fetchItem() : Boolean {
        when (type) {
            "berita" -> {
                return try {
                    withContext(Dispatchers.Main) {
                        val notFound : TextView = findViewById(R.id.detail_not_found)
                        notFound.visibility = View.GONE
                    }

                    val item = withContext(Dispatchers.IO) {
                        val connection = URL("${R.string.API_URL}/api/News/id/${id}").openConnection() as HttpURLConnection
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
                        val webView = findViewById<WebView>(R.id.detail_webview)

                        val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("id"))

                        judulText.text = item.getString("title")
                        tanggalText.text = formatter.format(LocalDate.parse(item.getString("createdAt")))

                        val htmlContent = """
                    <html>
                        <head>
                            <style type="text/css">
                                @font-face {
                                    font-family: 'Poppins';
                                    src: url('file:///android_asset/fonts/poppins.ttf');
                                }
                                body {
                                    font-family: 'Poppins';
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
                        val progressBar : ProgressBar = findViewById(R.id.detail_pg)
                        notFoundText.visibility = View.VISIBLE
                        progressBar.visibility = View.GONE
                    }
                    false
                }
            }
            else -> {
                return try {
                    withContext(Dispatchers.Main) {
                        val notFound : TextView = findViewById(R.id.detail_not_found)
                        notFound.visibility = View.GONE
                    }

                    val item = withContext(Dispatchers.IO) {
                        val connection = URL("${R.string.API_URL}/api/News/id/${id}").openConnection() as HttpURLConnection
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
                        val webView = findViewById<WebView>(R.id.detail_webview)

                        val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("id"))

                        judulText.text = item.getString("title")
                        tanggalText.text = formatter.format(LocalDate.parse(item.getString("createdAt")))

                        val htmlContent = """
                    <html>
                        <head>
                            <style type="text/css">
                                @font-face {
                                    font-family: 'Poppins';
                                    src: url('file:///android_asset/fonts/poppins.ttf');
                                }
                                body {
                                    font-family: 'Poppins';
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
                        val progressBar : ProgressBar = findViewById(R.id.detail_pg)
                        notFoundText.visibility = View.VISIBLE
                        progressBar.visibility = View.GONE
                    }
                    false
                }
            }
        }
        return false
    }
}