package com.example.storiesdata

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.storiesdata.Models.StoriesDataModel
import com.example.storiesdata.databinding.ActivityStoriesScreenBinding
import com.example.storiesdata.databinding.ActivityStoryReadScreenBinding

class StoryReadScreen : AppCompatActivity() {
    lateinit var binding: ActivityStoryReadScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityStoryReadScreenBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        loadData()
    }

    private fun loadData() {
        val intent = getIntent()
        val receivedObject = intent.getParcelableExtra<StoriesDataModel>("myObject")
        if (receivedObject != null) {
            val title = receivedObject.title
            val content = receivedObject.content
            val topImage = receivedObject.imageUrl
            binding.storyTitleReadScreen.text=title
            binding.storyContent.text=content
//            binding.storyReadImage.setImageURI(topImage)


            // Access object properties
        } else {
            // Handle case where object is not present
        }
    }

}