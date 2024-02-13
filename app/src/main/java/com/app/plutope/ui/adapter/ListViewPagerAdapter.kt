package com.app.plutope.ui.adapter

import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.app.plutope.R
import com.app.plutope.ui.fragment.card.card_list.CardListModel


class ListViewPagerAdapter(var list: MutableList<CardListModel>) : PagerAdapter() {

    override fun getCount(): Int {
        return list.size
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun instantiateItem(view: ViewGroup, position: Int): Any {
        val layout =
            LayoutInflater.from(view.context).inflate(R.layout.row_card_list, view, false)!!
        val textTitle = layout.findViewById<TextView>(R.id.txt_title)
        // textTitle.text = list[position].title
        view.addView(layout)
        return layout
    }

    override fun restoreState(state: Parcelable?, loader: ClassLoader?) {}

    override fun saveState(): Parcelable? {
        return null
    }
}