package com.example.storiesdata

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var collectionList: ArrayList<MainCollectionModel>
    lateinit var collectionAdapter: CollectionAdapter
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var navigationView: NavigationView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    var fromCat: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loadVaraibles()
        if (!fromCat) {
            getAllCollectionData()

        } else {
            getAllCollectionDataOfCategory()
        }
        checkAds()

        swipeRefreshLayout.setOnRefreshListener {
            if (!fromCat) {
                getAllCollectionData()

            } else {
                getAllCollectionDataOfCategory()

            }

            Toast.makeText(this, "yes loaded", Toast.LENGTH_LONG).show()

//            swipeRefreshLayout.isRefreshing = false
//            fetchDataFromServer(/)
        }
        binding.MAINThreeDots.setOnClickListener {
            showPopupMenu(binding.MAINThreeDots)
        }


    }


    private fun loadVaraibles() {
        fromCat = intent.getBooleanExtra("fromCat", false)
        collectionList = ArrayList()
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)


        setSupportActionBar(toolbar)
//        setSupportActionBar(toolbar)


        drawerLayout = binding.drawerLayout
        navigationView = binding.navView
        supportActionBar?.title = null
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupDrawerContent(navigationView)
        if (fromCat) {
            binding.appNameId.text = "Categories"
        }

        drawerToggle =
            ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close)
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
                    Log.d(
                        "Firestore",
                        "No documents retrieved"
                    ) // Handle no documents case (optional)
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting documents: $exception") // Handle errors
            }
    }

    private fun getAllCollectionDataOfCategory() {
        val db = FirebaseFirestore.getInstance() // Get Firestore instance
        collectionList.clear()
        val uniqueCategories =
            mutableSetOf<MainCollectionModel>() // Use a set to store unique categories

        val docRef = db.collection("stories")
            .whereArrayContains("users", FirebaseAuth.getInstance().uid.toString())

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
                        uniqueCategories.add(model) // Add to set to ensure uniqueness
                    }
                    collectionList.addAll(uniqueCategories) // Add unique categories to the collectionList
                    getAllCollections()
                } else {
                    Log.d(
                        "Firestore",
                        "No documents retrieved"
                    ) // Handle no documents case (optional)
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting documents: $exception") // Handle errors
            }
    }


    private fun getAllCollections() {
        collectionAdapter = CollectionAdapter(this, collectionList)
        binding.mainRecyclerView.adapter = collectionAdapter
        binding.mainRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.shimmerView.visibility = View.GONE
        swipeRefreshLayout.isRefreshing = false

    }


    private fun checkAds() {
        val backgroundScope = CoroutineScope(Dispatchers.IO)
        backgroundScope.launch {
            // Initialize the Google Mobile Ads SDK on a background thread.
            MobileAds.initialize(this@MainActivity) {}

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
                startActivity(Intent(this, FavActivity::class.java))

            }

            R.id.category_option -> {
                startActivity(Intent(this, MainActivity::class.java).putExtra("fromCat", true))

//                startActivity(Intent(this,CategoryScreen::class.java))

            }

            R.id.logoutButtonInMenu -> {
                logout()


            }


        }

        drawerLayout.closeDrawers()
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
                shareApp()
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



    private fun shareApp() {
        val appPackageName = packageName
        val appName = getString(R.string.app_name)
        val appIconDrawable = R.drawable.story // Replace with your actual drawable resource ID

        val shareMessage = getString(R.string.share_message, appName, appPackageName)

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, appName)
        intent.putExtra(Intent.EXTRA_TEXT, shareMessage)
//        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("android.resource://$appPackageName/$appIconDrawable")) // Include the drawable as a stream

        startActivity(Intent.createChooser(intent, "Share $appName via"))
    }






}





