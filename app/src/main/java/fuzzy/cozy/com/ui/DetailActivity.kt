package fuzzy.cozy.com.ui

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import fuzzy.cozy.com.R
import fuzzy.cozy.com.data.AppDatabase
import fuzzy.cozy.com.data.Cafe
import fuzzy.cozy.com.data.FuzzyPrefs
import fuzzy.cozy.com.fuzzy.FuzzyMamdani
import kotlinx.coroutines.launch
import java.util.Locale

class DetailActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var prefs: FuzzyPrefs
    private lateinit var fuzzyEngine: FuzzyMamdani
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var cafeId: Int = -1
    private var currentCafe: Cafe? = null

    // Views
    private lateinit var tvCafeName: TextView
    private lateinit var tvAddress: TextView
    private lateinit var tvHours: TextView
    private lateinit var tvDistance: TextView
    private lateinit var tvWifiDetail: TextView
    private lateinit var tvNoiseDetail: TextView
    private lateinit var tvComfortDetail: TextView
    private lateinit var tvPriceDetail: TextView
    private lateinit var tvOutletsDetail: TextView
    private lateinit var tvScoreHasil: TextView
    private lateinit var tvCategoryHasil: TextView
    private lateinit var tvSaran: TextView
    private lateinit var btnHitung: Button
    private lateinit var btnDetailPerhitungan: Button
    private lateinit var ivBack: ImageView
    private lateinit var ivEdit: ImageView
    private lateinit var ivDelete: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        db = AppDatabase.getDatabase(this)
        prefs = FuzzyPrefs(this)
        fuzzyEngine = FuzzyMamdani(prefs)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        initViews()
        setupClickListeners()

        cafeId = intent.getIntExtra("cafe_id", -1)
        if (cafeId != -1) {
            loadCafeData()
        } else {
            Toast.makeText(this, "Data tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun initViews() {
        tvCafeName = findViewById(R.id.tv_cafe_name)
        tvAddress = findViewById(R.id.tv_address)
        tvHours = findViewById(R.id.tv_hours)
        tvDistance = findViewById(R.id.tv_distance)
        tvWifiDetail = findViewById(R.id.tv_wifi_detail)
        tvNoiseDetail = findViewById(R.id.tv_noise_detail)
        tvComfortDetail = findViewById(R.id.tv_comfort_detail)
        tvPriceDetail = findViewById(R.id.tv_price_detail)
        tvOutletsDetail = findViewById(R.id.tv_outlets_detail)
        tvScoreHasil = findViewById(R.id.tv_score_hasil)
        tvCategoryHasil = findViewById(R.id.tv_category_hasil)
        tvSaran = findViewById(R.id.tv_saran)
        btnHitung = findViewById(R.id.btn_hitung)
        btnDetailPerhitungan = findViewById(R.id.btn_detail_perhitungan)
        ivBack = findViewById(R.id.iv_back)
        ivEdit = findViewById(R.id.iv_edit)
        ivDelete = findViewById(R.id.iv_delete)
    }

    private fun setupClickListeners() {
        ivBack.setOnClickListener { finish() }
        ivEdit.setOnClickListener {
            val intent = Intent(this, FormActivity::class.java)
            intent.putExtra("cafe_id", cafeId)
            startActivity(intent)
        }
        ivDelete.setOnClickListener { showDeleteConfirmationDialog() }
        btnHitung.setOnClickListener { hitungRekomendasi() }
        btnDetailPerhitungan.setOnClickListener {
            if (currentCafe != null) {
                val intent = Intent(this, DetailPerhitunganActivity::class.java)
                intent.putExtra("cafe_id", cafeId)
                startActivity(intent)
            }
        }
    }

    private fun loadCafeData() {
        lifecycleScope.launch {
            currentCafe = db.cafeDao().getById(cafeId)
            currentCafe?.let { cafe ->
                if (ActivityCompat.checkSelfPermission(this@DetailActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    val cts = CancellationTokenSource()
                    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                        .addOnSuccessListener { loc: Location? ->
                            if (loc != null) {
                                val results = FloatArray(1)
                                Location.distanceBetween(loc.latitude, loc.longitude, cafe.latitude, cafe.longitude, results)
                                cafe.distance = (results[0] / 1000.0).toDouble()
                            }
                            displayCafeData(cafe)
                            hitungRekomendasi()
                        }
                        .addOnFailureListener {
                            displayCafeData(cafe)
                            hitungRekomendasi()
                        }
                } else {
                    displayCafeData(cafe)
                    hitungRekomendasi()
                }
            }
        }
    }

    private fun displayCafeData(cafe: Cafe) {
        tvCafeName.text = cafe.name
        tvAddress.text = cafe.address.ifEmpty { "Alamat tidak tersedia" }
        tvHours.text = "Operasional: ${cafe.openTime} - ${cafe.closeTime}"
        tvDistance.text = String.format(Locale.getDefault(), "Jarak: %.1f km dari lokasi Anda", cafe.distance)

        tvWifiDetail.text = "WiFi: ${cafe.wifiSpeed} Mbps"
        tvNoiseDetail.text = "Kebisingan: ${cafe.noiseLevel}"
        tvComfortDetail.text = "Kenyamanan: ${cafe.comfort}/10"
        tvPriceDetail.text = "Harga Rata-rata: Rp ${cafe.price}.000"
        tvOutletsDetail.text = "Stop Kontak: ${cafe.outlets}"
    }

    private fun hitungRekomendasi() {
        currentCafe?.let { cafe ->
            val (score, category, _) = fuzzyEngine.hitungRekomendasi(cafe)
            tvScoreHasil.text = String.format(Locale.getDefault(), "%d/100", score.toInt())
            tvCategoryHasil.text = category.uppercase()
            tvSaran.text = generateSaran(cafe, category)
        }
    }

    private fun generateSaran(cafe: Cafe, category: String): String {
        return when (category) {
            "Sangat Direkomendasikan" -> "Cafe ini sangat ideal untuk fokus belajar. Fasilitas pendukung seperti WiFi, kenyamanan, dan ketersediaan outlet sangat memadai."
            "Cukup Direkomendasikan" -> "Cafe ini cukup baik untuk belajar, namun perhatikan aspek kebisingan atau ketersediaan stop kontak sebelum berkunjung."
            else -> "Cafe ini kurang disarankan untuk durasi belajar yang lama. Pertimbangkan untuk mencari alternatif tempat yang lebih tenang atau memiliki fasilitas lebih lengkap."
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Hapus Data")
            .setMessage("Hapus cafe ini dari daftar?")
            .setPositiveButton("Hapus") { _, _ -> deleteCafe() }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteCafe() {
        lifecycleScope.launch {
            currentCafe?.let {
                db.cafeDao().delete(it)
                finish()
            }
        }
    }
}
