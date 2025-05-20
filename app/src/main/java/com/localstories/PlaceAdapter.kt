package com.localstories

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class PlaceAdapter(private val places: List<Place>) :
    RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>() {

    class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.placeName)
        val details: TextView = itemView.findViewById(R.id.placeDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_place, parent, false)
        return PlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = places[position]
        holder.name.text = place.name
        holder.details.text = place.details

        // Optional: what happens when a place is clicked
        holder.itemView.setOnClickListener {
            Toast.makeText(it.context, "Clicked: ${place.name}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int = places.size
}