package com.example.matsandaplus

import android.text.Layout
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.recyclerview.widget.RecyclerView

class AdapterPage(private val pages: List<String>) : RecyclerView.Adapter<AdapterPage.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val webView : WebView = itemView.findViewById(R.id.article_webview)
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
        val html = """
            <html>
              <head>
                <style>
                  body { font-family: 'sans-serif'; padding: 16px; font-size: 14px; text-align: justify; }
                </style>
              </head>
              <body>${pages[position]}</body>
            </html>
        """.trimIndent()

        holder.webView.settings.javaScriptEnabled = false
        holder.webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }
}