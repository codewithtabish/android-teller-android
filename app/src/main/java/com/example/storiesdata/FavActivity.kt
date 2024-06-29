package com.example.storiesdata

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storiesdata.Adapters.StoriesAdapter
import com.example.storiesdata.Models.StoriesDataModel
import com.example.storiesdata.databinding.ActivityFavBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.FirebaseFirestore

class FavActivity : AppCompatActivity() {

    lateinit var collectionType:String
    lateinit var stroiesAdapter: StoriesAdapter
    lateinit var storiesList:ArrayList<StoriesDataModel>
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var navigationView: NavigationView
    lateinit var binding:ActivityFavBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityFavBinding.inflate(layoutInflater)

        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.favMainLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        loadVarables()
        loadDataFromDB()

    }

    private fun loadVarables(){
        storiesList=ArrayList()
        collectionType="Fear"


        drawerLayout = binding.drawerLayout
        navigationView = binding.navView
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupDrawerContent(navigationView)

        drawerToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close)
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
        val navigationView: NavigationView = binding.navView
        setupDrawerContent(navigationView)
        binding.drawerIcon.setOnClickListener {
            drawerLayout.open()
        }
    }


    private fun loadDataFromDB(){
        val db = FirebaseFirestore.getInstance() // Get Firestore instance

        val docRef = db.collection("stories").whereEqualTo("isFav",true)

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

                } else {
                    Log.d("Firestore", "No documents retrieved")
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting documents: $exception") // Handle errors
            }

    }



    private fun  getAllCollections(){
        stroiesAdapter = StoriesAdapter(this,storiesList,true)
        binding.savedRecycler.adapter=stroiesAdapter
        binding.savedRecycler.layoutManager= LinearLayoutManager(this)
        binding.savedShimmerView.visibility= View.GONE

    }




    private fun setupDrawerContent(navigationView: NavigationView) {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            selectDrawerItem(menuItem)
            true
        }
    }


    private fun selectDrawerItem(menuItem: MenuItem) {

        // Create a new fragment based on the selected menu item
        var fragment: Fragment? = null
        val fragmentClass: Class<*>

        when (menuItem.itemId) {

            R.id.homeItem -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            R.id.aboutDeveloper -> {
                startActivity(Intent(this, AboutActitity::class.java))


            }
            R.id.faviourites -> {
                startActivity(Intent(this,FavActivity::class.java))

            }



        }

        drawerLayout.closeDrawers()
    }
}