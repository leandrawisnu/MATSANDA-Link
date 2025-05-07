package com.example.matsandaplus

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.RoundedCorner
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.sql.Date
import java.sql.Time
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
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
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fetchHeadlineNews(view.context)

        val refreshBtn : ImageButton = view.findViewById(R.id.refreshButton)
        refreshBtn.setOnClickListener {
            fetchHeadlineNews(view.context)
        }
    }

    fun fetchHeadlineNews(context: Context) {
        val recyclerView = view?.findViewById<RecyclerView>(R.id.headline_rv)
        val headlineProgressbar = view?.findViewById<ProgressBar>(R.id.headline_progress)
        recyclerView?.layoutManager = LinearLayoutManager(view?.context, LinearLayoutManager.HORIZONTAL, false)

        // Jalankan coroutine di lifecycle-aware scope kalau bisa (misal lifecycleScope)
        CoroutineScope(Dispatchers.Main).launch {
            headlineProgressbar?.visibility = View.VISIBLE
            recyclerView?.visibility = View.GONE

            val newsArray = withContext(Dispatchers.IO) {
                try {
                    val url = URL("http://tour-occupational.gl.at.ply.gg:32499/api/News")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.connectTimeout = 5000
                    connection.readTimeout = 5000
                    connection.requestMethod = "GET"

                    val responseCode = connection.responseCode

                    if (responseCode != HttpURLConnection.HTTP_OK) throw Exception()

                    val response = connection.inputStream.bufferedReader().readText()
                    val jsonData = JSONObject(response).getJSONArray("data")
                    jsonData
                } catch (e: Exception) {
                    // Kalo gagal, return dummy data offline
                    buildOfflineJsonArray()
                }
            }

            recyclerView?.adapter = AdapterRV(newsArray, R.layout.berita_rv_layout)
            headlineProgressbar?.visibility = View.GONE
            recyclerView?.visibility = View.VISIBLE
        }
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
                }
            )
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}