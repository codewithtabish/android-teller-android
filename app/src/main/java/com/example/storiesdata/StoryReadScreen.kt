package com.example.storiesdata

import android.graphics.Typeface
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.storiesdata.Models.StoriesDataModel
import com.example.storiesdata.databinding.ActivityStoryReadScreenBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class StoryReadScreen : AppCompatActivity() {
    private var textToSpeech: TextToSpeech? = null
    private lateinit var binding: ActivityStoryReadScreenBinding
    private lateinit var content: String
    private var pausedPosition = 0
    private var spokenTextLength = 0
    private var isPaused = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoryReadScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        checkAds()
        loadData()

        binding.speakIcon.setOnClickListener {
            if (textToSpeech != null && textToSpeech!!.isSpeaking) {
                pauseText()
            } else {
                resumeText()
            }
        }
    }

    private fun loadData() {
        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val language = Locale.ENGLISH
                textToSpeech?.setLanguage(language)
                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        // Called when the utterance starts
                    }

                    override fun onDone(utteranceId: String?) {
                        // Called when the utterance is done
                        runOnUiThread {
                            resetTextHighlight()
                        }
                    }

                    override fun onError(utteranceId: String?) {
                        // Called when an error occurs
                    }

                    override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                        super.onRangeStart(utteranceId, start, end, frame)
                        runOnUiThread {
                            highlightSpokenWord(start, end)
                        }
                        spokenTextLength = end - start
                        if (isPaused) {
                            // Adjust paused position to start from where it was paused
                            pausedPosition = start
                            isPaused = false
                        }
                    }
                })
            } else {
                Log.e("TextToSpeech", "Initialization failed with status: $status")
            }
        }
        val receivedObject = intent.getParcelableExtra<StoriesDataModel>("myObject")
        if (receivedObject != null) {
            val title = receivedObject.title
            val storyImage = receivedObject.imageUrl
            content = receivedObject.content
            binding.storyTitleReadScreen.text = title
            binding.storyContent.text = content
            Picasso.get()
                .load(storyImage)
                .into(binding.storyReadImage)
//            binding.storyReadImage.setImageURI(storyImage )
        } else {
            // Handle case where object is not present
        }
    }

    private fun speakText(text: String, startPosition: Int = 0) {
        if (textToSpeech != null) {
            val subText = text.substring(startPosition)
            val params = Bundle()
            params.putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, TextToSpeech.Engine.DEFAULT_STREAM)
            textToSpeech?.speak(subText, TextToSpeech.QUEUE_FLUSH, params, "UniqueID")
            val tintColor = ContextCompat.getColor(this, R.color.red)
            binding.speakIcon.setColorFilter(tintColor)
        } else {
            Log.e("TextToSpeech", "TextToSpeech is null")
        }
    }

    private fun pauseText() {
        if (textToSpeech != null && textToSpeech!!.isSpeaking) {
            textToSpeech?.stop()
            isPaused = true

            val tintColor = ContextCompat.getColor(this, R.color.black)
            binding.speakIcon.setColorFilter(tintColor)
        }
    }

    private fun resumeText() {
        if (textToSpeech != null && !textToSpeech!!.isSpeaking) {
            speakText(content, pausedPosition)
        }
    }

    private fun highlightSpokenWord(start: Int, end: Int) {
        val spannableString = SpannableString(content)

        // Color
        val color = ContextCompat.getColor(this, R.color.red)

        // Style: Bold
        val boldSpan = StyleSpan(Typeface.BOLD)

        // Typeface: Custom font family (example uses a system font for demonstration)
        val typefaceSpan = TypefaceSpan("sans-serif-medium")

        // Size: Increase font size (example increases by 4 sp)
        val fontSizeSpan = AbsoluteSizeSpan(17, true) // 20 is in sp

        // Apply spans
        spannableString.setSpan(ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(boldSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(typefaceSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(fontSizeSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Apply to TextView
        binding.storyContent.text = spannableString
    }


    private fun resetTextHighlight() {
        binding.storyContent.text = content
    }

    override fun onDestroy() {
        super.onDestroy()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
    }

    private fun checkAds(){
        val backgroundScope = CoroutineScope(Dispatchers.IO)
        backgroundScope.launch {
            // Initialize the Google Mobile Ads SDK on a background thread.
            MobileAds.initialize(this@StoryReadScreen) {}

        }
        adsShoen()

    }

    private fun  adsShoen(){
        // Create a new ad view.
        val adView = AdView(this)
        adView.setAdSize(AdSize.BANNER)
        adView.adUnitId ="ca-app-pub-3940256099942544/9214589741"
//            "ca-app-pub-2101779718159669/7036158272"
//            "ca-app-pub-3940256099942544/9214589741"
//            "ca-app-pub-2101779718159669/7036158272"

        // Create an ad request.
        val adRequest = AdRequest.Builder().build()
        binding.readScreenAdsContainer.addView(adView)

        // Start loading the ad in the background.
        adView.loadAd(adRequest)


    }

    override fun onPause() {
        super.onPause()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
    }




}
