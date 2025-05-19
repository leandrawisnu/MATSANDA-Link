package com.example.matsandaplus

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Html
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
        val progressBar : ProgressBar = findViewById(R.id.detail_pg)
        val webViewVideo : WebView = findViewById(R.id.detail_video_webview)
        val gambar = findViewById<ImageView>(R.id.detail_image)
        val notFound : TextView = findViewById(R.id.detail_not_found)
        val vp : ViewPager2 = findViewById(R.id.detail_page_vp)

        when (type) {
            "berita" -> {
                return try {
                    withContext(Dispatchers.Main) {
                        webViewVideo.visibility = View.GONE
                        progressBar.visibility = View.VISIBLE
                        content.visibility = View.GONE
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
                        val gambarLink = item.getString("image")

                        if (!gambarLink.equals("kosong")) {
                            Glide.with(this@DetailActivity)
                                .load(gambarLink)
                                .into(gambar)
                        }

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
            "video" -> {
                return try {
                    withContext(Dispatchers.Main) {
                        gambar.visibility = View.GONE
                        webViewVideo.visibility = View.VISIBLE
                        progressBar.visibility = View.VISIBLE
                        content.visibility = View.GONE
                        Toast.makeText(this@DetailActivity, "ini video", Toast.LENGTH_SHORT).show()
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
                        val menitText = findViewById<TextView>(R.id.detail_baca)
                        val webView = findViewById<WebView>(R.id.detail_webview)

                        val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("id"))

                        judulText.text = item.getString("title")
                        tanggalText.text = formatter.format(LocalDate.parse(item.getString("createdAt")))
                        estimateReadingTime(item.getString("description")).also { menitText.text = it }

                        val htmlVideo = """
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
                                    <iframe width="100%" height="100%" src="https://www.youtube.com/embed/${item.getString("link").substringAfter("https://youtu.be/")}" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" referrerpolicy="strict-origin-when-cross-origin" allowfullscreen></iframe>
                                </body>
                            </html>
                            """.trimIndent()

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
                                    <p style="line-height: 1.5;"><span class="yt-core-attributed-string--link-inherit-color" dir="auto">===LIKE===SHARE===SUBSCRIBE===LIKE===SHARE===SUBSCRIBE=== </span></p>
                                    <p style="line-height: 1.5;"><span class="yt-core-attributed-string--link-inherit-color" dir="auto"><a class="yt-core-attributed-string__link yt-core-attributed-string__link--call-to-action-color" tabindex="0" href="https://www.youtube.com/hashtag/mtsn2kotamalang" target="">#MTsN2KOTAMALANG</a></span> <span class="yt-core-attributed-string--link-inherit-color" dir="auto"><a class="yt-core-attributed-string__link yt-core-attributed-string__link--call-to-action-color" tabindex="0" href="https://www.youtube.com/hashtag/matsanda" target="">#MATSANDA</a></span> <span class="yt-core-attributed-string--link-inherit-color" dir="auto"><a class="yt-core-attributed-string__link yt-core-attributed-string__link--call-to-action-color" tabindex="0" href="https://www.youtube.com/hashtag/matsandatama" target="">#MATSANDATAMA</a></span></p>
                                    <p style="line-height: 1.5;"><span class="yt-core-attributed-string--link-inherit-color" dir="auto"><a class="yt-core-attributed-string__link yt-core-attributed-string__link--call-to-action-color" tabindex="0" href="https://www.youtube.com/hashtag/kemenagkotamalang" target="">#KemenagKotaMalang</a></span> <span class="yt-core-attributed-string--link-inherit-color" dir="auto"><a class="yt-core-attributed-string__link yt-core-attributed-string__link--call-to-action-color" tabindex="0" href="https://www.youtube.com/hashtag/kemenagri" target="">#KemenagRI</a></span> <span class="yt-core-attributed-string--link-inherit-color" dir="auto"><a class="yt-core-attributed-string__link yt-core-attributed-string__link--call-to-action-color" tabindex="0" href="https://www.youtube.com/hashtag/kemenag" target="">#Kemenag</a></span> <span class="yt-core-attributed-string--link-inherit-color" dir="auto"><a class="yt-core-attributed-string__link yt-core-attributed-string__link--call-to-action-color" tabindex="0" href="https://www.youtube.com/hashtag/merdekabelajar" target="">#MerdekaBelajar</a></span></p>
                                    <p style="line-height: 1.5;"><span class="yt-core-attributed-string--link-inherit-color" dir="auto"><a class="yt-core-attributed-string__link yt-core-attributed-string__link--call-to-action-color" tabindex="0" href="https://www.youtube.com/hashtag/pelajarpancasila" target="">#PelajarPancasila</a></span> <span class="yt-core-attributed-string--link-inherit-color" dir="auto"><a class="yt-core-attributed-string__link yt-core-attributed-string__link--call-to-action-color" tabindex="0" href="https://www.youtube.com/hashtag/ramahanak" target="">#ramahanak</a></span> <span class="yt-core-attributed-string--link-inherit-color" dir="auto"><a class="yt-core-attributed-string__link yt-core-attributed-string__link--call-to-action-color" tabindex="0" href="https://www.youtube.com/hashtag/stopbullying" target="">#stopbullying</a></span> </p>
                                    <p style="line-height: 1.5;"><span class="yt-core-attributed-string--link-inherit-color" dir="auto">Ikuti dan Dukung Sosial Media Kami. Akun Resmi MTsN 2 Kota Malang : </span></p>
                                    <p style="line-height: 1.5;"><span class="yt-core-attributed-string--link-inherit-color" dir="auto">Email Creator Team : <a href="mailto:admyt.mtsn2kotamalang@gmail.com">admyt.mtsn2kotamalang@gmail.com</a> </span></p>
                                    <p style="line-height: 1.5;"><span class="yt-core-attributed-string--link-inherit-color" dir="auto">Instagram :&nbsp;</span><span class="yt-core-attributed-string--highlight-text-decorator" dir="auto"><a class="yt-core-attributed-string__link yt-core-attributed-string__link--call-to-action-color" tabindex="0" href="https://www.youtube.com/redirect?event=video_description&amp;redir_token=QUFFLUhqbHM4LXVVLWtfVlFXV1VDeXV0VTZmaXVCTTM5Z3xBQ3Jtc0ttNE43WXIxallHdHZRZmJVWDhqRzZ5TUI2aWZJbGpuNEItZExnNDE5TEx2c3JfOTFTVHFHcHdVR1QyUWdfd3dUNWJfdjgzUGRMWWo2eFBzSHVvRFBTM0M4YzZMcVlJTXJGNnE2bnZoZGZNVHFBcXl0aw&amp;q=https%3A%2F%2Fwww.instagram.com%2Fmtsn2kota_malang%2F&amp;v=8HvgBuaB5Yo" target="_blank" rel="nofollow noopener" aria-label="Instagram Channel Link: mtsn2kota_malang">&nbsp;<span class="yt-core-attributed-string--inline-block-mod"><img class="yt-core-image yt-core-attributed-string__image-element yt-core-attributed-string__image-element--image-alignment-vertical-center yt-core-image--content-mode-scale-to-fill yt-core-image--loaded" src="https://www.gstatic.com/youtube/img/watch/social_media/instagram_1x.png" alt=""></span>&nbsp;/&nbsp;mtsn2kota_malang&nbsp;&nbsp;</a></span> </p>
                                    <p style="line-height: 1.5;"><span class="yt-core-attributed-string--link-inherit-color" dir="auto">Facebook :&nbsp;</span><span class="yt-core-attributed-string--highlight-text-decorator" dir="auto"><a class="yt-core-attributed-string__link yt-core-attributed-string__link--call-to-action-color" tabindex="0" href="https://www.youtube.com/redirect?event=video_description&amp;redir_token=QUFFLUhqbnNVRXc4b0RWeWdjaUZkdTg0Yy1LOXhRQzVSZ3xBQ3Jtc0trNWw2dGhDdEpTRlZLVTdEcEh1cUQtcUN2VU9MTGFqbE9LS0JhZVdnbDVjeFdpaUFVN21kWFJscGZXSWtkRU9HeFR6RU9aTEVycmtEaDg5aklNNnE3MFRrcmlFQ0pXbHZ5aGVuMXZIRXBwclByZkE0UQ&amp;q=https%3A%2F%2Fwww.facebook.com%2Fmtsn2kotamalang&amp;v=8HvgBuaB5Yo" target="_blank" rel="nofollow noopener" aria-label="Facebook Channel Link: mtsn2kotamalang">&nbsp;<span class="yt-core-attributed-string--inline-block-mod"><img class="yt-core-image yt-core-attributed-string__image-element yt-core-attributed-string__image-element--image-alignment-vertical-center yt-core-image--content-mode-scale-to-fill yt-core-image--loaded" src="https://www.gstatic.com/youtube/img/watch/social_media/facebook_1x.png" alt=""></span>&nbsp;/&nbsp;mtsn2kotamalang&nbsp;&nbsp;</a></span> </p>
                                    <p style="line-height: 1.5;"><span class="yt-core-attributed-string--link-inherit-color" dir="auto">Youtube :&nbsp;</span><span class="yt-core-attributed-string--highlight-text-decorator" dir="auto"><a class="yt-core-attributed-string__link yt-core-attributed-string__link--call-to-action-color" tabindex="0" href="https://www.youtube.com/c/MTsNegeri2KotaMalang" target="" rel="nofollow" aria-label="YouTube Channel Link: MTsNegeri2KotaMalang">&nbsp;&nbsp;<span class="yt-core-attributed-string--inline-block-mod"><img class="yt-core-image yt-core-attributed-string__image-element yt-core-attributed-string__image-element--image-alignment-vertical-center yt-core-image--content-mode-scale-to-fill yt-core-image--loaded" src="https://www.gstatic.com/youtube/img/watch/yt_favicon_ringo2.png" alt=""></span>&nbsp;/&nbsp;mtsnegeri2kotamalang&nbsp;&nbsp;</a></span> </p>
                                    <p style="line-height: 1.5;"><span class="yt-core-attributed-string--link-inherit-color" dir="auto">Website :&nbsp;</span><span class="yt-core-attributed-string--link-inherit-color" dir="auto"><a class="yt-core-attributed-string__link yt-core-attributed-string__link--call-to-action-color" tabindex="0" href="https://www.youtube.com/redirect?event=video_description&amp;redir_token=QUFFLUhqbW1iYlM4NE0wTGpobUJkWmdQQ0NzU2VobmZjQXxBQ3Jtc0ttcEctamd5UFRjb1pqQVdzaUNQcThmNmFNeFlLNlVBTmp2YVc4LVhoTU15cFdISVoxYjBUVUdKbU1OSlhYc1l5TG1KMEx5dTFMQVdyVnZVSXNCSGFubXZpaUZLMUw5ZUdaQm4yZVpEdjdZcU1kM1YwYw&amp;q=https%3A%2F%2Fwww.mtsn2kotamalang.sch.id%2F&amp;v=8HvgBuaB5Yo" target="_blank" rel="nofollow noopener">https://www.mtsn2kotamalang.sch.id/</a></span> </p>
                                    <p style="line-height: 1.5;"><span class="yt-core-attributed-string--link-inherit-color" dir="auto">MADRASAH TSANAWIYAH NEGERI 2 KOTA MALANG Jl. Raya Cemorokandang No. 77 Kota Malang 65138 (0341) 711500</span></p>
                                    <p style="line-height: 1.5;"><span class="yt-core-attributed-string--link-inherit-color" dir="auto">Madrasah Mandiri Berprestasi, Religius Milenial, dan Berbudaya Lingkungan </span></p>
                                    <p style="line-height: 1.5;"><span class="yt-core-attributed-string--link-inherit-color" dir="auto">===LIKE===SHARE===SUBSCRIBE===LIKE===SHARE===SUBSCRIBE=== </span></p>
                                    <p style="line-height: 1.5;"><span class="yt-core-attributed-string--link-inherit-color" dir="auto">Creator Team MTsN 2 Kota Malang - MATSANDATAMA MEDIA - TIM PUSKOM</span></p>
                                </body>
                            </html>
                            """.trimIndent()

                        webViewVideo.settings.javaScriptEnabled = true
                        webViewVideo.loadDataWithBaseURL(null, htmlVideo, "text/html", "UTF-8", null)
                        webView.settings.javaScriptEnabled = false
                        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
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
            } else -> {
                return false
            }
        }
    }

    private fun estimateReadingTime(html: String): String {
        val plainText = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString()

        val words = plainText.trim().split("\\s+".toRegex()).size
        val minutes = ceil(words / 200.0).toInt() // 200 kata/menit

        return "Bacaan $minutes menit"
    }

    fun splitIntoPages(content: String, wordsPerPage: Int = 150): List<String> {
        val words = content.split(Regex("\\s+"))
        val pages = mutableListOf<String>()
        var buffer = StringBuilder()

        for ((i, word) in words.withIndex()) {
            buffer.append(word).append(" ")
            if ((i + 1) % wordsPerPage == 0 || i == words.size - 1) {
                pages.add(buffer.toString())
                buffer = StringBuilder()
            }
        }

        return pages
    }


}