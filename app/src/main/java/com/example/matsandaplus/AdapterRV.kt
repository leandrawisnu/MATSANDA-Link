package com.example.matsandaplus

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class AdapterRV(private val data : JSONArray, var layout : Int, var typeRV : String) : RecyclerView.Adapter<AdapterRV.Holder>(){
    class Holder(private val view: View, ) : RecyclerView.ViewHolder(view.rootView) {
        @SuppressLint("SetTextI18n")
        fun bind (item: JSONObject, tipeRV: String) {

            val tipeItem = item.getString("type")

            when (tipeItem) {
                "dummy" -> {
                    when (tipeRV) {
                        "headline" -> {
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
                        }
                        else -> {
                            val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("id"))
                            val judul = item.getString("title")
                            val button = view.findViewById<ImageView>(R.id.home_media_rv_play)

                            button.visibility = View.GONE

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
                else -> {
                    when (tipeRV) {
                        "headline" -> {
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

                            if(!gambarLink.equals("kosong")) {
                                Glide.with(view.context)
                                    .load(gambarLink)
                                    .into(gambar)
                            }
                        }
                        "berita" -> {
                            val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("id"))
                            val judul = item.getString("title")

                            if(judul.length>55) {
                                view.findViewById<TextView>(R.id.media_rv_judul).text = judul.substring(0, 54) + "..."
                            } else {
                                view.findViewById<TextView>(R.id.media_rv_judul).text = judul
                            }

                            view.findViewById<TextView>(R.id.media_rv_tanggal).text = formatter.format(LocalDate.parse(item.getString("createdAt")))
                            val gambar = view.findViewById<ImageView>(R.id.home_media_rv_image)
                            val gambarLink = item.getString("image")

                            if (!gambarLink.equals("kosong")) {
                                Glide.with(view.context)
                                    .load(gambarLink)
                                    .into(gambar)
                            }
                        }
                        else -> {
                            val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("id"))
                            val judul = item.getString("title")

                            if(judul.length>55) {
                                    view.findViewById<TextView>(R.id.media_rv_judul).text = judul.substring(0, 54) + "..."
                            } else {
                                view.findViewById<TextView>(R.id.media_rv_judul).text = judul
                            }

                            view.findViewById<TextView>(R.id.media_rv_tanggal).text = formatter.format(LocalDate.parse(item.getString("createdAt")))
                            val gambar = view.findViewById<ImageView>(R.id.home_media_rv_image)
                            val url = item.getString("link")
                            val idVideo = url.substringAfter("youtu.be/").substringBefore("?")
                            val gambarLink = "https://img.youtube.com/vi/${idVideo}/sddefault.jpg"

                            if (!gambarLink.equals("kosong")) {
                                Glide.with(view.context)
                                    .load(gambarLink)
                                    .into(gambar)
                            }
                        }
                    }
                }
            }

            view.setOnClickListener {
                when (tipeItem) {
                    "news" -> {

                    }
                    "video/podcast" -> {

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
        holder.bind(item, typeRV)
    }

    fun getDetails(item : JSONObject) {

    }
}