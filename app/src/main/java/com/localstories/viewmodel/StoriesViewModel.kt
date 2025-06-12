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
        if (!currentStories.any { it.storyId == story.storyId }) {
            currentStories.add(story)
        }
        storiesRepository.postValue(currentStories)
    }
    fun removeStory(story: Story) {
        val currentStories = storiesRepository.value.orEmpty().toMutableList()
        currentStories.remove(story)
        storiesRepository.postValue(currentStories)
    }

    fun retainStoriesByLocationIds(validLocationIds: List<String>) {
        val currentStories = storiesRepository.value.orEmpty().toMutableList()
        val filteredStories = currentStories.filter { validLocationIds.contains(it.locationId) }
        storiesRepository.value = filteredStories
    }
}

class StoriesViewModel : ViewModel() {
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