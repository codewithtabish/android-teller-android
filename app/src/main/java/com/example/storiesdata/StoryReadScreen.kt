package com.example.storiesdata

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
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
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.storiesdata.Models.StoriesDataModel
import com.example.storiesdata.databinding.ActivityStoryReadScreenBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

class StoryReadScreen : AppCompatActivity() {

    private lateinit var binding: ActivityStoryReadScreenBinding
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var content: String
    private var pausedPosition = 0
    private var spokenTextLength = 0
    private var isPaused = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoryReadScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        // Set padding to handle system bars
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        checkAds()
        loadData()

        binding.speakIcon.setOnClickListener {
            if (textToSpeech.isSpeaking) {
                pauseText()
            } else {
                resumeText()
            }
        }
        binding.threeDots.setOnClickListener {
            showPopupMenu(binding.threeDots)
        }
    }

    private fun loadData() {
        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val language = Locale.ENGLISH
                textToSpeech.language = language
                textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
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

                    override fun onRangeStart(
                        utteranceId: String?,
                        start: Int,
                        end: Int,
                        frame: Int
                    ) {
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
        } else {
            // Handle case where object is not present
        }
    }

    private fun speakText(text: String, startPosition: Int = 0) {
        if (::textToSpeech.isInitialized) {
            val subText = text.substring(startPosition)
            val params = Bundle()
            params.putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, TextToSpeech.Engine.DEFAULT_STREAM)
            textToSpeech.speak(subText, TextToSpeech.QUEUE_FLUSH, params, "UniqueID")
            val tintColor = ContextCompat.getColor(this, R.color.red)
            binding.speakIcon.setColorFilter(tintColor)
        } else {
            Log.e("TextToSpeech", "TextToSpeech is not initialized")
        }
    }

    private fun pauseText() {
        if (::textToSpeech.isInitialized && textToSpeech.isSpeaking) {
            textToSpeech.stop()
            isPaused = true
            val tintColor = ContextCompat.getColor(this, R.color.black)
            binding.speakIcon.setColorFilter(tintColor)
        }
    }

    private fun resumeText() {
        if (::textToSpeech.isInitialized && !textToSpeech.isSpeaking) {
            speakText(content, pausedPosition)
        }
    }

    private fun highlightSpokenWord(start: Int, end: Int) {
        val spannableString = SpannableString(content)

        // Color
        val color = ContextCompat.getColor(this, R.color.red)

        // Style: Bold
        val boldSpan = StyleSpan(android.graphics.Typeface.BOLD)

        // Typeface: Custom font family (example uses a system font for demonstration)
        val typefaceSpan = TypefaceSpan("sans-serif-medium")

        // Size: Increase font size (example increases by 4 sp)
        val fontSizeSpan = AbsoluteSizeSpan(17, true) // 20 is in sp

        // Apply spans
        spannableString.setSpan(
            ForegroundColorSpan(color),
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(boldSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(typefaceSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(fontSizeSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Apply to TextView
        binding.storyContent.text = spannableString
    }

    private fun resetTextHighlight() {
        binding.storyContent.text = content
    }

    private fun checkAds() {
        val backgroundScope = CoroutineScope(Dispatchers.IO)
        backgroundScope.launch {
            // Initialize the Google Mobile Ads SDK on a background thread.
            MobileAds.initialize(this@StoryReadScreen) {}

        }
        adsShoen()

    }

    private fun adsShoen() {
        // Create a new ad view.
        val adView = AdView(this)
        adView.setAdSize(AdSize.BANNER)
        adView.adUnitId = "ca-app-pub-3940256099942544/9214589741"
//            "ca-app-pub-3940256099942544/9214589741"
//            "ca-app-pub-2101779718159669/7036158272"

        // Create an ad request.
        val adRequest = AdRequest.Builder().build()
        binding.readScreenAdsContainer.addView(adView)

        // Start loading the ad in the background.
        adView.loadAd(adRequest)


    }


    private fun showPopupMenu(view: View) {
        val popup = PopupMenu(this, view)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.poup_menu, popup.menu)
        popup.setOnMenuItemClickListener { menuItem ->
            handleMenuItemClick(menuItem)
        }
        popup.show()
    }


    private fun handleMenuItemClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.setting_poup -> {
                Toast.makeText(this, "Option 1 selected", Toast.LENGTH_SHORT).show()
                true
            }

            R.id.share_popup -> {
                shareStory()
                Toast.makeText(this, "Option 2 selected", Toast.LENGTH_SHORT).show()
                true
            }

            R.id.popup_logout -> {
                logout()
                true
            }

            else -> false
        }
    }

    private fun shareStory() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"

        // Title and content
        val title = binding.toolbarTitle.text.toString() // Assuming toolbarTitle is where you display the story title
        val content = "${binding.storyContent.text.substring(0, 40)}\n\nRead more on our app:\nhttps://yourapp.com/story?id=12345" // Replace with your app link

        // Add title and content
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, title)
        shareIntent.putExtra(Intent.EXTRA_TEXT, content)

        // Start the chooser
        startActivity(Intent.createChooser(shareIntent, "Share Story via"))
    }

    private fun downloadAndSaveImage(imageUrl: String): Uri? {
        try {
            // Download the image
            val url = URL(imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val inputStream = connection.inputStream
            val bmp = BitmapFactory.decodeStream(inputStream)

            // Save image to local storage
            val cachePath = File(applicationContext.externalCacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "image.png")
            val fileOutputStream = FileOutputStream(file)
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.close()

            // Return Uri for the saved image
            return FileProvider.getUriForFile(this, "$packageName.provider", file)
        } catch (e: Exception) {
            Log.e("ShareStory", "Error downloading image: ${e.message}")
            return null
        }
    }








    private fun logout() {
        // Sign out from Firebase
        FirebaseAuth.getInstance().signOut()

        // Sign out from Google
        GoogleSignIn.getClient(
            this,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        ).signOut().addOnCompleteListener {
            // Navigate back to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }
    }









    override fun onDestroy() {
        super.onDestroy()
        if (::textToSpeech.isInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }

}
