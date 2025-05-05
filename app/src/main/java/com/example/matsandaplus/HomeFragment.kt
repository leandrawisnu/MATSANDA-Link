package com.example.matsandaplus

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
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

        refresh()
    }

    fun refresh() {
        val recyclerView = view?.findViewById<RecyclerView>(R.id.headline_rv)

        CoroutineScope(Dispatchers.IO).launch {
            val connection = URL("http://tour-occupational.gl.at.ply.gg:32499/api/News").openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val whole = connection.inputStream.bufferedReader().readText()
            val response = JSONObject(whole)
            val data = response.getJSONArray("data")

            withContext(Dispatchers.Main) {
                recyclerView?.layoutManager = LinearLayoutManager(view?.context, LinearLayoutManager.HORIZONTAL, false)
                recyclerView?.adapter = AdapterRV(data)
            }
        }
    }

    class AdapterRV(private val data : JSONArray) : RecyclerView.Adapter<AdapterRV.Holder>(){
        class Holder(private val view: View) : RecyclerView.ViewHolder(view.rootView) {
            @SuppressLint("SetTextI18n")
            fun bind (item: JSONObject) {

                val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("id"))
                val judul = item.getString("title")

                if(judul.length>55) {
                    view.findViewById<TextView>(R.id.judul_headline_TV).text = judul.substring(0, 54) + "..."
                } else {
                    view.findViewById<TextView>(R.id.judul_headline_TV).text = judul
                }

                view.findViewById<TextView>(R.id.date_headline_TV).text = formatter.format(LocalDate.parse(item.getString("createdAt")))
                val gambar = view.findViewById<ImageView>(R.id.berita_headline_image)

                Glide.with(view.context)
                    .load(item.getString("image"))
                    .into(gambar)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val inflater =
                LayoutInflater.from(parent.context).inflate(R.layout.berita_rv_layout, parent, false)
            val view = Holder(inflater)
            return view
        }

        override fun getItemCount(): Int {
            return data.length()
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            val item = data.getJSONObject(position)
            holder.bind(item)
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