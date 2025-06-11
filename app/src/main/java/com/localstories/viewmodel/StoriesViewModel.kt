package com.localstories.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.localstories.Story

object StoryRepository {
    private val storiesRepository = MutableLiveData<List<Story>>()

    fun getStories(): LiveData<List<Story>> {
        return storiesRepository
    }
    fun addStory(story: Story) {
        val currentStories = storiesRepository.value.orEmpty().toMutableList()
        currentStories.add(story)
        storiesRepository.value = currentStories
    }
    fun removeStory(story: Story) {
        val currentStories = storiesRepository.value.orEmpty().toMutableList()
        currentStories.remove(story)
        storiesRepository.value = currentStories
    }
}

class StoriesViewModel : ViewModel() {
    private var _stories = MutableLiveData<List<Story>>()
    val stories: LiveData<List<Story>> get() = _stories

    fun getStories(): List<Story>? {
        return StoryRepository.getStories().value
    }
    fun addStory(story: Story) {
        StoryRepository.addStory(story)
    }
    fun addStories(stories: List<Story>) {
        for (story in stories) addStory(story)
    }
    fun removeStory(story: Story) {
        StoryRepository.removeStory(story)
    }
}