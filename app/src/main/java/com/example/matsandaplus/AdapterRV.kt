package com.example.matsandaplus

import android.annotation.SuppressLint
import android.content.Intent
import android.health.connect.datatypes.units.Length
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.marginEnd
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class AdapterRV(private val data : JSONArray, var layout : Int, var typeRV : String,var page: String) : RecyclerView.Adapter<AdapterRV.Holder>(){
    class Holder(private val view: View, ) : RecyclerView.ViewHolder(view.rootView) {
        @SuppressLint("SetTextI18n")
        fun bind (item: JSONObject, tipeRV: String, page: String, position: Int, length: Int) {

            val tipeItem = item.getString("type")

            when (tipeRV) {
                "headline" -> {
                    CoroutineScope(Dispatchers.Main).launch {
                        val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("id"))
                        val judul = item.getString("title")

                        if(judul.length>55) {
                            view.findViewById<TextView>(R.id.judul_headline_TV).text = judul.substring(0, 54) + "..."
                        } else {
                            view.findViewById<TextView>(R.id.judul_headline_TV).text = judul
                        }

                        view.findViewById<TextView>(R.id.date_headline_TV).text = formatter.format(LocalDate.parse(item.getString("createdAt")))
                        val gambar = view.findViewById<ImageView>(R.id.berita_headline_image)
                        val gambarLink = item.getString("image")

                        if (!gambarLink.equals("kosong")) {
                            Glide.with(view.context)
                                .load(gambarLink)
                                .into(gambar)
                        }

                        if (position == length - 1) {
                            val layoutParams = itemView.layoutParams as? ViewGroup.MarginLayoutParams
                            layoutParams?.let {
                                val marginInDp = 15
                                val scale = itemView.context.resources.displayMetrics.density
                                val marginInPx = (marginInDp * scale).toInt()

                                it.marginEnd = marginInPx
                                itemView.layoutParams = it
                            }
                        }
                    }
                }
                else -> {
                    CoroutineScope(Dispatchers.Main).launch {
                        val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("id"))
                        val judul = item.getString("title")

                        if(judul.length>55) {
                            view.findViewById<TextView>(R.id.media_rv_judul).text = judul.substring(0, 54) + "..."
                        } else {
                            view.findViewById<TextView>(R.id.media_rv_judul).text = judul
                        }

                        view.findViewById<TextView>(R.id.media_rv_tanggal).text= formatter.format(LocalDate.parse(item.getString("createdAt")))
                        val gambar = view.findViewById<ImageView>(R.id.home_media_rv_image)
                        val gambarLink = item.getString("image")

                        if (!gambarLink.equals("kosong")) {
                            Glide.with(view.context)
                                .load(gambarLink)
                                .into(gambar)
                        }
                    }
                }
            }

            view.setOnClickListener {
                when (tipeItem) {
                    "news" -> {
                        val detail = Intent(view.context, DetailActivity::class.java)
                        detail.putExtra("id", item.getInt("id"))
                        detail.putExtra("type", "berita")
                        view.context.startActivity(detail)
                    }
                    "video/podcast" -> {
                        val detail = Intent(view.context, DetailActivity::class.java)
                        detail.putExtra("id", item.getInt("id"))
                        detail.putExtra("type", "video")
                        view.context.startActivity(detail)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflater =
            LayoutInflater.from(parent.context).inflate(layout, parent, false)
        val view = Holder(inflater)
        return view
    }

    override fun getItemCount(): Int {
        return data.length()
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = data.getJSONObject(position)
        holder.bind(item, typeRV, page, position, data.length())
    }

    fun getDetails(item : JSONObject) {

    }
}