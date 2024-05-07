package com.example.photoeditor

import CarouselAdapter
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

class EffectsMenuAdapter(images: List<ItemData>, activity: Activity) : CarouselAdapter(images, activity) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.effects_item_container, parent, false)
        return EffectsAdapterViewHolder(view)
    }

    inner class EffectsAdapterViewHolder(itemView: View) : CarouselViewHolder(itemView) {
        val effectImage: ImageView = itemView.findViewById(R.id.imageView)
        val effectTitle: TextView = itemView.findViewById(R.id.infoTextView)
    }
}