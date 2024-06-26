package com.example.storiesdata

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storiesdata.Adapters.CollectionAdapter
import com.example.storiesdata.Adapters.StoriesAdapter
import com.example.storiesdata.Models.MainCollectionModel
import com.example.storiesdata.Models.StoriesDataModel
import com.example.storiesdata.databinding.ActivityStoriesScreenBinding
import com.google.firebase.firestore.FirebaseFirestore

class StoriesScreen : AppCompatActivity() {
    lateinit var collectionType:String
    lateinit var stroiesAdapter:StoriesAdapter
    lateinit var storiesList:ArrayList<StoriesDataModel>
    lateinit var binding:ActivityStoriesScreenBinding
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

        loadVarables()
        loadDataFromDB()


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
                                data["storyType"] as? String ?: ""
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
        stroiesAdapter = StoriesAdapter(this,storiesList)
        binding.storiesRecyler.adapter=stroiesAdapter
        binding.storiesRecyler.layoutManager= LinearLayoutManager(this)

    }
}