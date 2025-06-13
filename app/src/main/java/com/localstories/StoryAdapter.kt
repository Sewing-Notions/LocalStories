package com.localstories

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class StoryAdapter(
    private var stories: MutableList<Story>,
    private val showDeleteButton: Boolean = false,
    private val onDeleteClick: ((Int) -> Unit)? = null
) : RecyclerView.Adapter<StoryAdapter.StoryViewHolder>() {

    class StoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.storyTitle)
        val author: TextView = itemView.findViewById(R.id.storyAuthor)
        val image: ImageView = itemView.findViewById(R.id.storyImage)
        val description: TextView = itemView.findViewById(R.id.storyDescription)
        val deleteButton: ImageButton? = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_story, parent, false)
        return StoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = stories[position]

        holder.title.text = story.title
        holder.author.text = "By ${story.author}"
        holder.description.text = story.description

        // Load image using Glide
        Glide.with(holder.itemView.context)
            .load(story.photoPath)
            .placeholder(R.drawable.bg_rounded_edittext)
            .error(android.R.drawable.ic_dialog_alert)
            .into(holder.image)

        // Show/hide delete button
        holder.deleteButton?.visibility = if (showDeleteButton) View.VISIBLE else View.GONE
        holder.deleteButton?.setOnClickListener {
            onDeleteClick?.invoke(position)
        }

        // Handle card click for story detail
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, StoryDetailActivity::class.java).apply {
                putExtra("storyId", story.storyId)
                putExtra("title", story.title)
                putExtra("description", story.description)
                putExtra("dateOfFact", story.dateOfFact.toString())
                putExtra("photoPath", story.photoPath)
                putExtra("locationId", story.locationId)
                putExtra("userId", story.userId)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = stories.size

    fun removeItem(position: Int) {
        stories.removeAt(position)
        notifyItemRemoved(position)
    }
}