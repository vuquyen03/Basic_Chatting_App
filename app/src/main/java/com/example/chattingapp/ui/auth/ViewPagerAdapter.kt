package com.example.chattingapp.ui.auth

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.example.chattingapp.R

class ViewPagerAdapter(private val context: Context) : PagerAdapter() {

    private val sliderAllImages = listOf(
        R.drawable.chatting,
        R.drawable.send,
        R.drawable.free
    )

    private val sliderAllTitle = listOf(
        R.string.screen1,
        R.string.screen2,
        R.string.screen3
    )

    private val sliderAllDesc = listOf(
        R.string.screen1Desc,
        R.string.screen2Desc,
        R.string.screen3Desc
    )

    override fun getCount(): Int {
        return sliderAllImages.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.slider_screen, container, false)

        val sliderImage: ImageView = view.findViewById(R.id.sliderImage)
        val sliderTitle: TextView = view.findViewById(R.id.sliderTitle)
        val sliderDesc: TextView = view.findViewById(R.id.sliderDesc)

        sliderImage.setImageResource(sliderAllImages[position])
        sliderTitle.setText(this.sliderAllTitle[position])
        sliderDesc.setText(this.sliderAllDesc[position])

        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val view = `object` as View
        container.removeView(view)
    }
}