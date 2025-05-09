package com.example.matsandaplus

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_layout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val tpMenu: TabLayout = findViewById(R.id.tpMenu)
        val vpMenu: androidx.viewpager2.widget.ViewPager2 = findViewById(R.id.vpMenu)

        val fragmentList = listOf(HomeFragment(), NewsFragment(), VideosFragment(), SavedFragment())
        val nameList = listOf("Beranda", "Berita", "Media", "Tersimpan")
        val iconMapNormal = mapOf(
            "Beranda" to R.drawable.beranda,
            "Berita" to R.drawable.berita,
            "Media" to R.drawable.media,
            "Tersimpan" to R.drawable.saved
        )
        val iconMapActive = mapOf(
            "Beranda" to R.drawable.beranda_active,
            "Berita" to R.drawable.berita_active,
            "Media" to R.drawable.media_active,
            "Tersimpan" to R.drawable.saved_active
        )

        vpMenu.isUserInputEnabled = false
        vpMenu.adapter = object : FragmentStateAdapter(supportFragmentManager, lifecycle) {
            override fun getItemCount(): Int {
                return fragmentList.size
            }

            override fun createFragment(position: Int): Fragment {
                return fragmentList[position]
            }
        }

        tpMenu.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val name = tab.tag.toString()
                tab.setIcon(iconMapActive[name]!!)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                val name = tab.tag.toString()
                tab.setIcon(iconMapNormal[name]!!)
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                // Optional: bisa kosong
            }
        })

        TabLayoutMediator(tpMenu, vpMenu) { tab, position ->
            val name = nameList[position]
            val icon = iconMapNormal[name]
            if (icon != null) {
                tab.setIcon(icon)
            }
            tab.tag = name
        }.attach()
    }
}