package fuzzy.cozy.com.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
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
import fuzzy.cozy.com.adapter.RankingAdapter
import fuzzy.cozy.com.data.AppDatabase
import fuzzy.cozy.com.data.FuzzyPrefs
import fuzzy.cozy.com.fuzzy.FuzzyMamdani
import kotlinx.coroutines.launch

class RankingActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var prefs: FuzzyPrefs
    private lateinit var fuzzyEngine: FuzzyMamdani
    private lateinit var adapter: RankingAdapter
    private lateinit var rvRanking: RecyclerView
    private lateinit var tvTotalCafe: TextView
    private lateinit var tvAvgScore: TextView
    private lateinit var tvHighest: TextView
    private lateinit var tvLowest: TextView
    private lateinit var ivRefresh: ImageView
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var userLocation: Location? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            getUserLocation()
        } else {
            loadRanking()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ranking)

        db = AppDatabase.getDatabase(this)
        prefs = FuzzyPrefs(this)
        fuzzyEngine = FuzzyMamdani(prefs)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        initViews()
        setupRecyclerView()
        setupBottomNav()
        setupClickListeners()

        checkLocationPermission()
    }

    private fun initViews() {
        rvRanking = findViewById(R.id.rv_ranking)
        tvTotalCafe = findViewById(R.id.tv_total_cafe)
        tvAvgScore = findViewById(R.id.tv_avg_score)
        tvHighest = findViewById(R.id.tv_highest)
        tvLowest = findViewById(R.id.tv_lowest)
        ivRefresh = findViewById(R.id.iv_refresh)
    }

    private fun setupRecyclerView() {
        adapter = RankingAdapter()
        rvRanking.layoutManager = LinearLayoutManager(this)
        rvRanking.adapter = adapter
    }

    private fun setupBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.selectedItemId = R.id.nav_ranking
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_form -> {
                    startActivity(Intent(this, FormActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_ranking -> true
                R.id.nav_tentang -> {
                    startActivity(Intent(this, TentangActivity::class.java))
                    finish()
                    true
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
            loadRanking()
            return
        }

        val cts = CancellationTokenSource()
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
            .addOnSuccessListener { location ->
                userLocation = location
                loadRanking()
            }
            .addOnFailureListener {
                loadRanking()
            }
    }

    private fun setupClickListeners() {
        ivRefresh.setOnClickListener {
            getUserLocation()
        }
    }

    private fun loadRanking() {
        lifecycleScope.launch {
            val cafes = db.cafeDao().getAll()
            val rankedCafes = cafes.map { cafe ->
                userLocation?.let { loc ->
                    val results = FloatArray(1)
                    Location.distanceBetween(loc.latitude, loc.longitude, cafe.latitude, cafe.longitude, results)
                    cafe.distance = (results[0] / 1000.0)
                }
                val (score, category, _) = fuzzyEngine.hitungRekomendasi(cafe)
                Triple(cafe, score.toInt(), category)
            }.sortedByDescending { it.second }

            adapter.submitList(rankedCafes)

            if (rankedCafes.isNotEmpty()) {
                val scores = rankedCafes.map { it.second }
                val total = scores.size
                val avg = scores.average().toInt()
                val highest = scores.maxOrNull() ?: 0
                val lowest = scores.minOrNull() ?: 0
                val highestCafe = rankedCafes.find { it.second == highest }?.first?.name ?: "-"
                val lowestCafe = rankedCafes.find { it.second == lowest }?.first?.name ?: "-"

                tvTotalCafe.text = "Total cafe: $total"
                tvAvgScore.text = "Rata-rata skor: $avg"
                tvHighest.text = "Tertinggi: $highestCafe ($highest)"
                tvLowest.text = "Terendah: $lowestCafe ($lowest)"
            } else {
                tvTotalCafe.text = "Total cafe: 0"
                tvAvgScore.text = "Rata-rata skor: -"
                tvHighest.text = "Tertinggi: -"
                tvLowest.text = "Terendah: -"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getUserLocation()
    }
}
