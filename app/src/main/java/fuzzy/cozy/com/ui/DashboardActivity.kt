package fuzzy.cozy.com.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.bottomnavigation.BottomNavigationView
import fuzzy.cozy.com.R
import fuzzy.cozy.com.adapter.CafeAdapter
import fuzzy.cozy.com.data.AppDatabase
import fuzzy.cozy.com.data.Cafe
import fuzzy.cozy.com.data.FuzzyPrefs
import fuzzy.cozy.com.fuzzy.FuzzyMamdani
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var prefs: FuzzyPrefs
    private lateinit var fuzzyEngine: FuzzyMamdani
    private lateinit var adapter: CafeAdapter
    private lateinit var rvCafes: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var spinnerCriteria: Spinner
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var cafesWithScores = mutableListOf<Pair<Cafe, Int>>()
    private var userLocation: Location? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            getUserLocation()
        } else {
            loadCafes() 
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        db = AppDatabase.getDatabase(this)
        prefs = FuzzyPrefs(this)
        fuzzyEngine = FuzzyMamdani(prefs)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        initViews()
        setupRecyclerView()
        setupBottomNav()
        setupSearchAndFilters()

        checkLocationPermission()
    }

    private fun initViews() {
        rvCafes = findViewById(R.id.rv_cafes)
        etSearch = findViewById(R.id.et_search)
        spinnerCriteria = findViewById(R.id.spinner_criteria)
    }

    private fun setupRecyclerView() {
        adapter = CafeAdapter { cafe ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("cafe_id", cafe.id)
            startActivity(intent)
        }
        rvCafes.layoutManager = LinearLayoutManager(this)
        rvCafes.adapter = adapter
    }

    private fun setupBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.selectedItemId = R.id.nav_dashboard
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> true
                R.id.nav_form -> {
                    startActivity(Intent(this, FormActivity::class.java))
                    finish()
                    false
                }
                R.id.nav_ranking -> {
                    startActivity(Intent(this, RankingActivity::class.java))
                    finish()
                    false
                }
                R.id.nav_tentang -> {
                    startActivity(Intent(this, TentangActivity::class.java))
                    finish()
                    false
                }
                else -> false
            }
        }
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        } else {
            getUserLocation()
        }
    }

    private fun getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            loadCafes()
            return
        }

        val cts = CancellationTokenSource()
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
            .addOnSuccessListener { location ->
                userLocation = location
                loadCafes()
            }
            .addOnFailureListener {
                loadCafes()
            }
    }

    private fun setupSearchAndFilters() {
        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { filterCafes() }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        spinnerCriteria.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) { filterCafes() }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun loadCafes() {
        lifecycleScope.launch {
            val cafes = db.cafeDao().getAll()
            val scoredCafes = cafes.map { cafe ->
                userLocation?.let { loc ->
                    val results = FloatArray(1)
                    Location.distanceBetween(loc.latitude, loc.longitude, cafe.latitude, cafe.longitude, results)
                    cafe.distance = (results[0] / 1000.0)
                }
                val (score, _, _) = fuzzyEngine.hitungRekomendasi(cafe)
                cafe to score.toInt()
            }
            cafesWithScores.clear()
            cafesWithScores.addAll(scoredCafes)
            filterCafes()
        }
    }

    private fun filterCafes() {
        if (!::etSearch.isInitialized || !::spinnerCriteria.isInitialized) return

        val query = etSearch.text?.toString()?.lowercase() ?: ""
        val criteria = spinnerCriteria.selectedItem?.toString() ?: "Skor Terbaik"

        var filtered = cafesWithScores.filter {
            it.first.name.lowercase().contains(query)
        }

        filtered = when (criteria) {
            "Terdekat" -> filtered.sortedBy { it.first.distance }
            "WiFi Tercepat" -> filtered.sortedByDescending { it.first.wifiSpeed }
            "Harga Termurah" -> filtered.sortedBy { it.first.price }
            "Paling Tenang" -> filtered.sortedBy { it.first.noiseLevel }
            "Paling Nyaman" -> filtered.sortedByDescending { it.first.comfort }
            else -> filtered.sortedByDescending { it.second }
        }

        adapter.submitList(filtered)
    }

    override fun onResume() {
        super.onResume()
        getUserLocation()
    }
}
