package com.example.storiesdata

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.storiesdata.Adapters.CollectionAdapter
import com.example.storiesdata.Models.MainCollectionModel
import com.example.storiesdata.databinding.ActivityMainBinding
import com.example.storiesdata.fragments.SettingFragment
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    lateinit var binding:ActivityMainBinding
    lateinit var collectionList:ArrayList<MainCollectionModel>
    lateinit var collectionAdapter:CollectionAdapter
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var navigationView: NavigationView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout



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

        swipeRefreshLayout.setOnRefreshListener {
            getAllCollectionData()
            Toast.makeText(this,"yes loaded",Toast.LENGTH_LONG).show()

//            swipeRefreshLayout.isRefreshing = false
//            fetchDataFromServer(/)
        }




    }



    private fun loadVaraibles(){
        collectionList=ArrayList()
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)


        setSupportActionBar(toolbar)
//        setSupportActionBar(toolbar)


        drawerLayout = binding.drawerLayout
        navigationView = binding.navView
        supportActionBar?.title=null
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

    private fun getAllCollectionData() {
        val db = FirebaseFirestore.getInstance() // Get Firestore instance
        collectionList.clear()

        val docRef = db.collection("storycollection")

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
                            continue // Skip this document if conversion fails
                        }
                        collectionList.add(model)


                    }
                    getAllCollections()


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
        swipeRefreshLayout.isRefreshing = false

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