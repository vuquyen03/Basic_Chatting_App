package com.example.chattingapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.example.chattingapp.R
import com.example.chattingapp.activities.SignInActivity
import com.example.chattingapp.databinding.ActivityOnboardingBinding


class Onboarding : AppCompatActivity() {
    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var slideViewPager: ViewPager
    private lateinit var dotIndicator: LinearLayout
    lateinit var backButton: Button
    lateinit var nextButton: Button
    private lateinit var skipButton: Button
    private lateinit var viewPagerAdapter: ViewPagerAdapter

    private val viewPagerListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            // Implementation of the onPageScrolled method
            // Add your code here
        }

        override fun onPageSelected(position: Int) {
            setDotIndicator(position)
            if (position > 0) {
                backButton.visibility = View.VISIBLE
            } else {
                backButton.visibility = View.INVISIBLE
            }
            if (position == 2) {
                nextButton.text = "Finish"
            } else {
                nextButton.text = "Next"
            }
        }

        override fun onPageScrollStateChanged(state: Int) {
            // Implementation of the onPageScrollStateChanged method
            // Add your code here
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        backButton = binding.backButton
        nextButton = binding.nextButton
        skipButton = binding.skipButton

        slideViewPager = binding.slideViewPage
        viewPagerAdapter = ViewPagerAdapter(this)
        slideViewPager.adapter = viewPagerAdapter

        backButton.setOnClickListener{

            if (getItem(0) > 0) {
                slideViewPager.setCurrentItem(getItem(-1), true)
            }
        }

        nextButton.setOnClickListener {
            if (getItem(0) < 2) {
                slideViewPager.setCurrentItem(getItem(1), true)
            } else {
                val i = Intent(this@Onboarding, GetStarted::class.java)
                startActivity(i)
                finish()
            }
        }

        skipButton.setOnClickListener{
            val i = Intent(this@Onboarding, SignInActivity::class.java)
            startActivity(i)
            finish()
        }

//        slideViewPager = findViewById(R.id.slideViewPage)
        dotIndicator = binding.dotIndicator

        setDotIndicator(0)
        slideViewPager.addOnPageChangeListener(viewPagerListener)
    }

    fun setDotIndicator(position: Int){
        val dots: Array<TextView?> = arrayOfNulls(3)
        dotIndicator.removeAllViews()

        for (i in dots.indices){
            dots[i] = TextView(this)
            dots[i]?.text = Html.fromHtml("&#8226", Html.FROM_HTML_MODE_LEGACY)
            dots[i]?.textSize = 35F
            dots[i]?.setTextColor(resources.getColor(R.color.grey, applicationContext.theme))
            dotIndicator.addView(dots[i])
        }

        dots[position]?.setTextColor(resources.getColor(R.color.lavender, applicationContext.theme))
    }

    private fun getItem(i: Int) : Int {
        return slideViewPager.currentItem + i
    }
}