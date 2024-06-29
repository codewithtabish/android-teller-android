package com.example.storiesdata

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storiesdata.Adapters.CollectionAdapter
import com.example.storiesdata.Models.MainCollectionModel
import com.example.storiesdata.databinding.ActivityMainBinding
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    lateinit var binding:ActivityMainBinding
    lateinit var collectionList:ArrayList<MainCollectionModel>
    lateinit var collectionAdapter:CollectionAdapter
    private lateinit var shimmerFrameLayout: Any
    private lateinit var query:Query
    private val LIMIT = 10


    override fun onCreate(savedInstanceState: Bundle?) {
        binding=ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loadVaraibles()
        getAllCollectionData()
        checkAds()







    }



    private fun loadVaraibles(){
        collectionList=ArrayList()


    }

    private fun getAllCollectionData() {
        val db = FirebaseFirestore.getInstance() // Get Firestore instance

        val docRef = db.collection("storycollection") // Reference the collection

        docRef.get() // Fetch all documents at once (consider pagination for large collections)
            .addOnSuccessListener { documents ->
                if (documents != null) {

                    for (document in documents) {
                        val data = document.data ?: continue
                        // Attempt to convert to MainCollectionModel, handle errors gracefully
                        val model = try {
                            MainCollectionModel(
                                data["imageUrl"] as? String ?: "",
                                data["storyType"] as? String ?: ""
                            )
                        } catch (e: Exception) {
                            Log.w("Firestore", "Error converting document ${document.id} to MainCollectionModel: $e")
                            continue // Skip this document if conversion fails
                        }
                        collectionList.add(model)


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
        collectionAdapter=CollectionAdapter(this,collectionList)
        binding.mainRecyclerView.adapter=collectionAdapter
        binding.mainRecyclerView.layoutManager=LinearLayoutManager(this)
        binding.shimmerView.visibility=View.GONE

    }


    private fun checkAds(){
        val backgroundScope = CoroutineScope(Dispatchers.IO)
        backgroundScope.launch {
            // Initialize the Google Mobile Ads SDK on a background thread.
            MobileAds.initialize(this@MainActivity) {}

        }
        adsShoen()

    }

    private fun  adsShoen(){
        // Create a new ad view.
        val adView = AdView(this)
        adView.setAdSize(AdSize.BANNER)
        adView.adUnitId ="ca-app-pub-3940256099942544/9214589741"
//            "ca-app-pub-3940256099942544/9214589741"
//            "ca-app-pub-2101779718159669/7036158272"

        // Create an ad request.
        val adRequest = AdRequest.Builder().build()
        binding.adsMainContainer.addView(adView)

        // Start loading the ad in the background.
        adView.loadAd(adRequest)


    }




}