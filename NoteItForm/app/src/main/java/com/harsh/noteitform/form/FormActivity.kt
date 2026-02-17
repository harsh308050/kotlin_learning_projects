package com.harsh.noteitform.form

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.transition.Visibility
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.harsh.noteitform.R
import com.harsh.noteitform.form.adapter.FormPagerAdapter
import com.harsh.noteitform.form.repository.FormRepository

class FormActivity : AppCompatActivity() {
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private var isFirstClick = true
    private var currentTypeId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form)
        FormRepository.init(this)
        createTabs()
    }
    fun createTabs(){
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        val types = FormRepository.getTypesSorted()
        val adapter = FormPagerAdapter(this, types)
        viewPager.visibility = View.VISIBLE
        types.forEach { type ->
            val tab = tabLayout.newTab()
            tab.text = type.typeAlias
            tabLayout.addTab(tab, false)
        }
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val selectedType = types[tab.position]
                currentTypeId = selectedType.feedbackTypeId
                if (isFirstClick) {
                    isFirstClick = false
                    viewPager.adapter = adapter
                    viewPager.isVisible = true
                }
                viewPager.setCurrentItem(tab.position, false)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }
}