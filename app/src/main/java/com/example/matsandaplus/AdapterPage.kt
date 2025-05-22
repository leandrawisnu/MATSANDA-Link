package com.example.matsandaplus

import android.text.Html
import android.text.Layout
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdapterPage(private val pages: List<String>) : RecyclerView.Adapter<AdapterPage.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val webView : TextView = itemView.findViewById(R.id.article_page_textview)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.article_page, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return pages.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.webView.text = Html.fromHtml(pages[position], Html.FROM_HTML_MODE_COMPACT)
    }
}