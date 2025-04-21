package com.example.matsandaplus

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
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

        val tpMenu: com.google.android.material.tabs.TabLayout = findViewById(R.id.tpMenu)
        val vpMenu: androidx.viewpager2.widget.ViewPager2 = findViewById(R.id.vpMenu)

        val fragmentList = listOf(HomeFragment(), NewsFragment(), VideosFragment())
        val nameList = listOf("Home", "News", "Videos")
        val iconMap = mapOf(
            "Home" to R.drawable.baseline_home_24,
            "News" to R.drawable.baseline_newspaper_24,
            "Videos" to R.drawable.baseline_personal_video_24
        )

        vpMenu.adapter = object : FragmentStateAdapter(supportFragmentManager, lifecycle) {
            override fun getItemCount(): Int {
                return fragmentList.size
            }

            override fun createFragment(position: Int): Fragment {
                return fragmentList[position]
            }
        }

        TabLayoutMediator(tpMenu, vpMenu) { tab, position ->
            val name = nameList[position]
            val icon = iconMap[name]
            if (icon != null) {
                tab.setIcon(icon)
            }
        }.attach()
    }
}