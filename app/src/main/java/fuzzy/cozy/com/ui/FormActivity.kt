package fuzzy.cozy.com.ui

import android.Manifest
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputLayout
import fuzzy.cozy.com.R
import fuzzy.cozy.com.data.AppDatabase
import fuzzy.cozy.com.data.Cafe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Calendar
import java.util.Locale

class FormActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private var isEditMode = false
    private var editCafeId: Int = -1
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var selectedLat: Double = 0.0
    private var selectedLng: Double = 0.0

    // Views
    private lateinit var tvTitle: TextView
    private lateinit var etName: EditText
    private lateinit var etSearchAddress: EditText
    private lateinit var tilSearch: TextInputLayout
    private lateinit var etAddress: EditText
    private lateinit var sbWifi: SeekBar
    private lateinit var tvWifiValue: TextView
    private lateinit var sbNoise: SeekBar
    private lateinit var tvNoiseValue: TextView
    private lateinit var sbComfort: SeekBar
    private lateinit var tvComfortValue: TextView
    private lateinit var sbPrice: SeekBar
    private lateinit var tvPriceValue: TextView
    private lateinit var sbOutlets: SeekBar
    private lateinit var tvOutletsValue: TextView
    private lateinit var etOpenTime: EditText
    private lateinit var etCloseTime: EditText
    private lateinit var btnSimpan: Button
    private lateinit var ivBack: ImageView

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            getCurrentLocation()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form)

        db = AppDatabase.getDatabase(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        initViews()
        setupClickListeners()
        setupSeekBars()
        setupBottomNav()
        setupTimePickers()
        setupSearchAddress()

        if (intent.hasExtra("cafe_id")) {
            isEditMode = true
            editCafeId = intent.getIntExtra("cafe_id", -1)
            tvTitle.text = "Edit Data Cafe"
            loadCafeData()
        } else {
            tvTitle.text = "Tambah Cafe Baru"
            checkLocationPermission()
        }
    }

    private fun initViews() {
        tvTitle = findViewById(R.id.tv_title)
        etName = findViewById(R.id.et_name)
        etSearchAddress = findViewById(R.id.et_search_address)
        tilSearch = findViewById(R.id.til_search)
        etAddress = findViewById(R.id.et_address)
        sbWifi = findViewById(R.id.sb_wifi)
        tvWifiValue = findViewById(R.id.tv_wifi_value)
        sbNoise = findViewById(R.id.sb_noise)
        tvNoiseValue = findViewById(R.id.tv_noise_value)
        sbComfort = findViewById(R.id.sb_comfort)
        tvComfortValue = findViewById(R.id.tv_comfort_value)
        sbPrice = findViewById(R.id.sb_price)
        tvPriceValue = findViewById(R.id.tv_price_value)
        sbOutlets = findViewById(R.id.sb_outlets)
        tvOutletsValue = findViewById(R.id.tv_outlets_value)
        etOpenTime = findViewById(R.id.et_open_time)
        etCloseTime = findViewById(R.id.et_close_time)
        btnSimpan = findViewById(R.id.btn_simpan)
        ivBack = findViewById(R.id.iv_back)
    }

    private fun setupSearchAddress() {
        // Klik icon kaca pembesar
        tilSearch.setEndIconOnClickListener {
            searchLocation(etSearchAddress.text.toString())
        }

        // Tekan Enter di keyboard
        etSearchAddress.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchLocation(etSearchAddress.text.toString())
                true
            } else {
                false
            }
        }
    }

    private fun searchLocation(locationName: String) {
        if (locationName.isEmpty()) {
            Toast.makeText(this, "Ketik nama tempat atau jalan", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val geocoder = Geocoder(this@FormActivity, Locale.getDefault())
            try {
                val addressList: List<Address>? = geocoder.getFromLocationName(locationName, 1)
                if (!addressList.isNullOrEmpty()) {
                    val address = addressList[0]
                    selectedLat = address.latitude
                    selectedLng = address.longitude

                    withContext(Dispatchers.Main) {
                        etAddress.setText(address.getAddressLine(0))
                        Toast.makeText(this@FormActivity, "Lokasi ditemukan!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@FormActivity, "Lokasi tidak ditemukan. Coba nama lain.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@FormActivity, "Gagal memuat peta. Cek internet Anda.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupTimePickers() {
        etOpenTime.setOnClickListener { showTimePicker(etOpenTime) }
        etCloseTime.setOnClickListener { showTimePicker(etCloseTime) }
        etOpenTime.isFocusable = false
        etCloseTime.isFocusable = false
    }

    private fun showTimePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(this, R.style.Theme_StudyCafe_TimePicker, { _, selectedHour, selectedMinute ->
            editText.setText(String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute))
        }, hour, minute, true).show()
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        } else {
            getCurrentLocation()
        }
    }

    private fun getCurrentLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    selectedLat = it.latitude
                    selectedLng = it.longitude
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun setupClickListeners() {
        ivBack.setOnClickListener { finish() }
        btnSimpan.setOnClickListener { saveCafe() }
    }

    private fun setupSeekBars() {
        sbWifi.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, f: Boolean) { tvWifiValue.text = "$p Mbps" }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })
        sbNoise.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, f: Boolean) { tvNoiseValue.text = p.toString() }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })
        sbComfort.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, f: Boolean) { tvComfortValue.text = "$p/10" }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })
        sbPrice.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, f: Boolean) { tvPriceValue.text = "Rp ${p}.000" }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })
        sbOutlets.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, f: Boolean) { tvOutletsValue.text = p.toString() }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })
    }

    private fun setupBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.selectedItemId = R.id.nav_form
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> { startActivity(Intent(this, DashboardActivity::class.java)); finish(); true }
                R.id.nav_form -> true
                R.id.nav_ranking -> { startActivity(Intent(this, RankingActivity::class.java)); finish(); true }
                R.id.nav_tentang -> { startActivity(Intent(this, TentangActivity::class.java)); finish(); true }
                else -> false
            }
        }
    }

    private fun loadCafeData() {
        lifecycleScope.launch {
            val cafe = db.cafeDao().getById(editCafeId)
            cafe?.let {
                etName.setText(it.name)
                etAddress.setText(it.address)
                selectedLat = it.latitude
                selectedLng = it.longitude
                sbWifi.progress = it.wifiSpeed
                sbNoise.progress = it.noiseLevel
                sbComfort.progress = it.comfort
                sbPrice.progress = it.price
                sbOutlets.progress = it.outlets
                etOpenTime.setText(it.openTime)
                etCloseTime.setText(it.closeTime)
            }
        }
    }

    private fun saveCafe() {
        val name = etName.text.toString().trim()
        val address = etAddress.text.toString().trim()

        if (name.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Nama dan Alamat harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val cafe = Cafe(
            id = if (isEditMode) editCafeId else 0,
            name = name,
            address = address,
            wifiSpeed = sbWifi.progress,
            noiseLevel = sbNoise.progress,
            comfort = sbComfort.progress,
            price = sbPrice.progress,
            outlets = sbOutlets.progress,
            latitude = selectedLat,
            longitude = selectedLng,
            openTime = etOpenTime.text.toString().ifEmpty { "08:00" },
            closeTime = etCloseTime.text.toString().ifEmpty { "22:00" }
        )

        lifecycleScope.launch {
            if (isEditMode) db.cafeDao().update(cafe) else db.cafeDao().insert(cafe)
            finish()
        }
    }
}
