package com.example.matsandaplus

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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

        val searchBar : SearchView = view.findViewById(R.id.home_search_bar)
        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            @SuppressLint("ShowToast")
            override fun onQueryTextSubmit(p0: String?): Boolean {
                if (!p0?.isEmpty()!!) {
                    val intent = Intent(view.context, SearchActivity::class.java)
                    intent.putExtra("searchQuery", p0)
                    startActivity(intent)
                    return true
                }
                Toast.makeText(view.context, "Query Kosong!", Toast.LENGTH_SHORT).show()
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                return true
            }

        })

        val refreshLayout : androidx.swiperefreshlayout.widget.SwipeRefreshLayout = view.findViewById(R.id.home_refresh_layout)
        refreshLayout.setOnRefreshListener {
            refreshItems(view)
        }

        val chipGroup : ChipGroup = view.findViewById(R.id.chipGroup)
        chipGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.chip_berita -> {
                    fetchItems(view.context, "berita")
                }
                R.id.chip_video -> {
                    fetchItems(view.context, "video")
                }
                R.id.chip_podcast -> {
                    fetchItems(view.context, "podcast")
                }
            }
        }

        val homeMediaRV : RecyclerView = view.findViewById(R.id.home_media_rv)
        homeMediaRV.isNestedScrollingEnabled = false

        fetchItems(view.context, "headline")
        fetchItems(view.context, "berita")
    }

    private fun fetchItems(context: Context, type : String) {
        val recyclerView = when (type) {
            "headline" -> view?.findViewById<RecyclerView>(R.id.headline_rv)
            else -> view?.findViewById(R.id.home_media_rv)
        }
        val progressBar = when (type) {
            "headline" -> view?.findViewById<ProgressBar>(R.id.headline_progress)
            else -> view?.findViewById(R.id.media_progress)
        }
        val url = when (type) {
            "headline" -> URL("http://tour-occupational.gl.at.ply.gg:32499/api/News/Headlines")
            "berita" -> URL("http://tour-occupational.gl.at.ply.gg:32499/api/News")
            "video" -> URL("http://tour-occupational.gl.at.ply.gg:32499/api/Videos")
            "podcast" -> URL("http://tour-occupational.gl.at.ply.gg:32499/api/podcasts")
            else -> null
        }
        recyclerView?.layoutManager = when (type) {
            "headline" -> object : LinearLayoutManager(view?.context, LinearLayoutManager.HORIZONTAL, false) {
            }
            else -> object : LinearLayoutManager(view?.context, LinearLayoutManager.VERTICAL, false) {
                override fun canScrollVertically(): Boolean {
                    return false
                }
            }
        }
        val layout : Int = when (type) {
            "headline" -> R.layout.berita_rv_layout
            else -> R.layout.home_media_rv_layout
        }

        CoroutineScope(Dispatchers.Main).launch {
            progressBar?.visibility = View.VISIBLE
            recyclerView?.visibility = View.GONE

            val newsArray : JSONArray = withContext(Dispatchers.IO) {
                try {
                    val connection = url?.openConnection() as HttpURLConnection
                    connection.connectTimeout = 5000
                    connection.readTimeout = 5000
                    connection.requestMethod = "GET"

                    if (connection.responseCode != HttpURLConnection.HTTP_OK) throw Exception()

                    val response = connection.inputStream.bufferedReader().readText()
                    val jsonData = JSONObject(response).getJSONArray("data")
                    jsonData
                } catch (e: Exception) {
                    // Kalo gagal, return dummy data offline
                    buildOfflineJsonArray()
                }
            }

            recyclerView?.adapter = AdapterRV(newsArray, layout, type)
            progressBar?.visibility = View.GONE
            recyclerView?.visibility = View.VISIBLE
        }
        val swipeRefreshLayout = view?.findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.home_refresh_layout)
        swipeRefreshLayout?.isRefreshing = false
    }

    private fun refreshItems(view: View) {
        val chipGroup : ChipGroup = view.findViewById(R.id.chipGroup)
        val chip = chipGroup.findViewById<Chip>(chipGroup.checkedChipId)

        fetchItems(view.context, "headline")
        fetchItems(view.context, chip.text.toString())
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