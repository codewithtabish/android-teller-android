package com.example.storiesdata

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storiesdata.Adapters.CollectionAdapter
import com.example.storiesdata.Models.MainCollectionModel
import com.example.storiesdata.databinding.ActivityMainBinding
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    lateinit var binding:ActivityMainBinding
    lateinit var collectionList:ArrayList<MainCollectionModel>
    lateinit var collectionAdapter:CollectionAdapter


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

    private fun processData(data: List<HashMap<String, Any?>>) {
        // Implement your logic to handle the retrieved data
        // (e.g., update UI elements, perform calculations)

        // Example: Loop through data and print titles
        for (item in data) {
            val title = item["title"] as? String ?: "No title"
            Log.d("Data", "Title: $title")
        }
    }



    private fun  getAllCollections(){
        collectionAdapter=CollectionAdapter(this,collectionList)
        binding.mainRecyclerView.adapter=collectionAdapter
        binding.mainRecyclerView.layoutManager=LinearLayoutManager(this)

    }
}