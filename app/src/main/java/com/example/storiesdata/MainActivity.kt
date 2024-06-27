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
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    lateinit var binding:ActivityMainBinding
    lateinit var collectionList:ArrayList<MainCollectionModel>
    lateinit var collectionAdapter:CollectionAdapter
    private lateinit var shimmerFrameLayout: Any
    private val handler = Handler()
    private var isShown=false


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
        binding.adsCrossIcon.setOnClickListener {
            operateAds()
            isShown=false
        }
        showAdsWithDelay(4000)






    }

    private fun showAds() {

        Handler(Looper.getMainLooper()).postDelayed({
            if (binding.adsContainer.visibility==View.GONE){
                binding.adsContainer.visibility=View.VISIBLE

            }
        }, 500)
    }

    private fun operateAds() {
        binding.adsContainer.visibility=View.GONE
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

    override fun onPause() {
        super.onPause()
        showAdsWithDelay(100)
    }

    override fun onResume() {
        super.onResume()
        showAdsWithDelay(4000)
    }




    private fun  getAllCollections(){
        collectionAdapter=CollectionAdapter(this,collectionList)
        binding.mainRecyclerView.adapter=collectionAdapter
        binding.mainRecyclerView.layoutManager=LinearLayoutManager(this)
        binding.shimmerView.visibility=View.GONE

    }


    private fun showAdsWithDelay(delay: Long) {

        val showButtonRunnable = Runnable {
            if (!isShown) { // Check if not shown yet
                binding.adsContainer.visibility = View.VISIBLE
                isShown = true // Set flag to prevent repeated showing
            }
        }

        handler.postDelayed(showButtonRunnable, delay)
    }


}