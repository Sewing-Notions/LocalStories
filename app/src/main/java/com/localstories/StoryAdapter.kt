package com.localstories

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StoryAdapter(
    private var stories: MutableList<Story>,
    private val showDeleteButton: Boolean = false,
    private val onDeleteClick: ((Int) -> Unit)? = null
) : RecyclerView.Adapter<StoryAdapter.StoryViewHolder>() {

    class StoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.storyTitle)
        val author: TextView = itemView.findViewById(R.id.storyAuthor)
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

        holder.deleteButton?.visibility = if (showDeleteButton) View.VISIBLE else View.GONE

        holder.deleteButton?.setOnClickListener {
            onDeleteClick?.invoke(position)
        }
    }

    override fun getItemCount(): Int = stories.size

    fun removeItem(position: Int) {
        stories.removeAt(position)
        notifyItemRemoved(position)
    }
}
