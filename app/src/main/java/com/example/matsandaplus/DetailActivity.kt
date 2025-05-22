package com.example.matsandaplus

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Html
import android.view.View
import android.webkit.WebView
import android.widget.Button
import android.widget.ImageButton
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
            fetchList()

            withContext(Dispatchers.Main) {
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

    @SuppressLint("SetJavaScriptEnabled")
    private suspend fun fetchItem() : Boolean {
        val content : LinearLayout = findViewById(R.id.detail_content)
        val bottom : LinearLayout = findViewById(R.id.detail_content_bottom)
        val progressBar : ProgressBar = findViewById(R.id.detail_pg)
        val gambar = findViewById<ImageView>(R.id.detail_image)
        val notFound : TextView = findViewById(R.id.detail_not_found)
        val vp : ViewPager2 = findViewById(R.id.detail_page_vp)
        val pageCount : TextView = findViewById(R.id.detail_page_count)

        return try {
            withContext(Dispatchers.Main) {
                progressBar.visibility = View.VISIBLE
                content.visibility = View.GONE
                bottom.visibility = View.GONE
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

                val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("id"))

                judulText.text = item.getString("title")
                tanggalText.text = formatter.format(LocalDate.parse(item.getString("createdAt")))
                estimateReadingTime(item.getString("description")).also { menitText.text = it }

                val list = paginateByMaxTwoParagraphs( item.getString("description"))

                val adapter = AdapterPage(list)
                vp.adapter = adapter
                vp.isUserInputEnabled = false

                val buttonPrev : ImageButton = findViewById(R.id.btnPrev)
                val buttonNext : ImageButton = findViewById(R.id.btnNext)
                pageCount.text = "1 - ${list.size}"

                buttonPrev.setOnClickListener {
                    if (vp.currentItem > 0) {
                        vp.currentItem = vp.currentItem - 1
                    }
                }

                buttonNext.setOnClickListener {
                    if (vp.currentItem < adapter.itemCount - 1) {
                        vp.currentItem = vp.currentItem + 1
                    }
                }
            }

            withContext(Dispatchers.Main) {
                val gambarLink = item.getString("image")

                if (!gambarLink.equals("kosong")) {
                    Glide.with(this@DetailActivity)
                        .load(gambarLink)
                        .into(gambar)
                }

                progressBar.visibility = View.GONE
                content.visibility = View.VISIBLE
                bottom.visibility = View.VISIBLE
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

    private fun fetchList() {
        CoroutineScope(Dispatchers.Main).launch {
            val rv : RecyclerView = findViewById(R.id.detail_more_rv)
            val layout = R.layout.berita_rv_layout

            try {
                val list = withContext(Dispatchers.IO) {
                    val connection = URL("http://tour-occupational.gl.at.ply.gg:32499/api/News/except/${id}?take=5").openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.connectTimeout = 10000
                    connection.readTimeout = 10000

                    if (connection.responseCode != HttpURLConnection.HTTP_OK) throw Exception()

                    val response = connection.inputStream.bufferedReader().readText()
                    JSONObject(response).getJSONArray("data")
                }

                withContext(Dispatchers.Main) {
                    rv.layoutManager = LinearLayoutManager(this@DetailActivity, LinearLayoutManager.HORIZONTAL, false)
                    rv.adapter = AdapterRV(list, layout, "headline", "detail")
                }
            } catch (_: Exception) {

            }
        }
    }

    private fun estimateReadingTime(html: String): String {
        val plainText = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString()

        val words = plainText.trim().split("\\s+".toRegex()).size
        val minutes = ceil(words / 200.0).toInt() // 200 kata/menit

        return "Bacaan $minutes menit"
    }

    private fun paginateByMaxTwoParagraphs(htmlContent: String): List<String> {
        val paragraphRegex = Regex("<p[^>]*>.*?</p>", RegexOption.DOT_MATCHES_ALL)
        val paragraphs = paragraphRegex.findAll(htmlContent).map { it.value.trim() }.toList()

        val pages = mutableListOf<String>()

        for (i in paragraphs.indices step 2) {
            val pageContent = buildString {
                append(paragraphs[i])
                if (i + 1 < paragraphs.size) {
                    append("<br>").append(paragraphs[i + 1])
                }
            }
            pages.add(pageContent)
        }

        return pages
    }
}