package com.example.storiesdata

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storiesdata.Adapters.StoriesAdapter
import com.example.storiesdata.Models.StoriesDataModel
import com.example.storiesdata.databinding.ActivityStoriesScreenBinding
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StoriesScreen : AppCompatActivity() {
    lateinit var collectionType:String
    lateinit var stroiesAdapter:StoriesAdapter
    lateinit var storiesList:ArrayList<StoriesDataModel>
    lateinit var binding:ActivityStoriesScreenBinding
    private var mInterstitialAd: InterstitialAd? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityStoriesScreenBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        showFullAds()
        loadVarables()
        loadDataFromDB()
        checkAds()






    }
    private fun loadVarables(){
        storiesList=ArrayList()
        collectionType= intent.getStringExtra("storyCollection").toString()
        Toast.makeText(this,collectionType,Toast.LENGTH_LONG).show()
    }
    private fun loadDataFromDB(){
        val db = FirebaseFirestore.getInstance() // Get Firestore instance

        val docRef = db.collection("stories").whereEqualTo("storyType",collectionType)

        docRef.get() // Fetch all documents at once (consider pagination for large collections)
            .addOnSuccessListener { documents ->
                if (documents != null) {

                    for (document in documents) {
                        val data = document.data ?: continue
                        // Attempt to convert to MainCollectionModel, handle errors gracefully
                        val model = try {
                            StoriesDataModel(
                                data["title"] as? String ?: "",
                                data["content"] as? String ?: "",
                                data["imageUrl"] as? String ?: "",
                                data["storyType"] as? String ?: "",
                                data["isFav"] as Boolean?:false

                            )

                        } catch (e: Exception) {
                            Log.w("Firestore", "Error converting document ${document.id} to MainCollectionModel: $e")
                            continue // Skip this document if conversion fails
                        }
                        storiesList.add(model)



                    }
                    getAllCollections()

                    // Process retrieved data (e.g., update UI, perform calculations)
//                    processData(dataList)
                } else {
                    Log.d("Firestore", "No documents retrieved") // Handle no documents case (optional)
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting documents: $exception") // Handle errors
            }

    }



    private fun  getAllCollections(){
        stroiesAdapter = StoriesAdapter(this,storiesList,false)
        binding.storiesRecyler.adapter=stroiesAdapter
        binding.storiesRecyler.layoutManager= LinearLayoutManager(this)
        binding.stoiesShimmerSecond.visibility=View.GONE

    }
    
    
    private fun checkAds(){
        val backgroundScope = CoroutineScope(Dispatchers.IO)
        backgroundScope.launch {
            // Initialize the Google Mobile Ads SDK on a background thread.
            MobileAds.initialize(this@StoriesScreen) {}

        }
        adsShoen()

    }

    private fun  adsShoen(){
        // Create a new ad view.
        val adView = AdView(this)
        adView.setAdSize(AdSize.BANNER)
        adView.adUnitId ="ca-app-pub-3940256099942544/9214589741"
//            "ca-app-pub-2101779718159669/7036158272"

        // Create an ad request.
        val adRequest = AdRequest.Builder().build()
        binding.storiesAdLayout.addView(adView)

        // Start loading the ad in the background.
        adView.loadAd(adRequest)


    }

    private  fun showFullAds() {
        var adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            this,
            "ca-app-pub-3940256099942544/1033173712",
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                }
            })

        if (mInterstitialAd != null) {
            mInterstitialAd?.show(this)
        } else {
            Log.d("TAG", "The interstitial ad wasn't ready yet.")
        }
    }
}