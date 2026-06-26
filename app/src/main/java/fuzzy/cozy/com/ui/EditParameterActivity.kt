package fuzzy.cozy.com.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import fuzzy.cozy.com.R

class EditParameterActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    // WiFi EditTexts
    private lateinit var etWifiLambatHigh: EditText
    private lateinit var etWifiSedangLow: EditText
    private lateinit var etWifiSedangMid: EditText
    private lateinit var etWifiSedangHigh: EditText
    private lateinit var etWifiCepatLow: EditText

    // Noise EditTexts
    private lateinit var etNoiseSepiHigh: EditText
    private lateinit var etNoiseSedangLow: EditText
    private lateinit var etNoiseSedangMid: EditText
    private lateinit var etNoiseSedangHigh: EditText
    private lateinit var etNoiseRamaiLow: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_parameter)

        prefs = getSharedPreferences("fuzzy_config", Context.MODE_PRIVATE)

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        // Initialize Views
        initViews()

        // Load existing values
        loadValues()

        // Buttons
        findViewById<Button>(R.id.btn_reset).setOnClickListener { resetToDefault() }
        findViewById<Button>(R.id.btn_simpan).setOnClickListener { saveValues() }

        // Save icon
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                android.R.id.home -> finish()
            }
            true
        }
    }

    private fun initViews() {
        // WiFi
        etWifiLambatHigh = findViewById(R.id.et_wifi_lambat_high)
        etWifiSedangLow = findViewById(R.id.et_wifi_sedang_low)
        etWifiSedangMid = findViewById(R.id.et_wifi_sedang_mid)
        etWifiSedangHigh = findViewById(R.id.et_wifi_sedang_high)
        etWifiCepatLow = findViewById(R.id.et_wifi_cepat_low)

        // Noise
        etNoiseSepiHigh = findViewById(R.id.et_noise_sepi_high)
        etNoiseSedangLow = findViewById(R.id.et_noise_sedang_low)
        etNoiseSedangMid = findViewById(R.id.et_noise_sedang_mid)
        etNoiseSedangHigh = findViewById(R.id.et_noise_sedang_high)
        etNoiseRamaiLow = findViewById(R.id.et_noise_ramai_low)
    }

    private fun loadValues() {
        // WiFi
        etWifiLambatHigh.setText(prefs.getInt("wifi_lambat_high", 40).toString())
        etWifiSedangLow.setText(prefs.getInt("wifi_sedang_low", 30).toString())
        etWifiSedangMid.setText(prefs.getInt("wifi_sedang_mid", 50).toString())
        etWifiSedangHigh.setText(prefs.getInt("wifi_sedang_high", 70).toString())
        etWifiCepatLow.setText(prefs.getInt("wifi_cepat_low", 60).toString())

        // Noise
        etNoiseSepiHigh.setText(prefs.getInt("noise_sepi_high", 40).toString())
        etNoiseSedangLow.setText(prefs.getInt("noise_sedang_low", 30).toString())
        etNoiseSedangMid.setText(prefs.getInt("noise_sedang_mid", 50).toString())
        etNoiseSedangHigh.setText(prefs.getInt("noise_sedang_high", 70).toString())
        etNoiseRamaiLow.setText(prefs.getInt("noise_ramai_low", 60).toString())
    }

    private fun resetToDefault() {
        // WiFi default
        etWifiLambatHigh.setText("40")
        etWifiSedangLow.setText("30")
        etWifiSedangMid.setText("50")
        etWifiSedangHigh.setText("70")
        etWifiCepatLow.setText("60")

        // Noise default
        etNoiseSepiHigh.setText("40")
        etNoiseSedangLow.setText("30")
        etNoiseSedangMid.setText("50")
        etNoiseSedangHigh.setText("70")
        etNoiseRamaiLow.setText("60")

        Toast.makeText(this, "Reset ke nilai default", Toast.LENGTH_SHORT).show()
    }

    private fun saveValues() {
        val editor = prefs.edit()

        try {
            // WiFi
            editor.putInt("wifi_lambat_high", etWifiLambatHigh.text.toString().toInt())
            editor.putInt("wifi_sedang_low", etWifiSedangLow.text.toString().toInt())
            editor.putInt("wifi_sedang_mid", etWifiSedangMid.text.toString().toInt())
            editor.putInt("wifi_sedang_high", etWifiSedangHigh.text.toString().toInt())
            editor.putInt("wifi_cepat_low", etWifiCepatLow.text.toString().toInt())

            // Noise
            editor.putInt("noise_sepi_high", etNoiseSepiHigh.text.toString().toInt())
            editor.putInt("noise_sedang_low", etNoiseSedangLow.text.toString().toInt())
            editor.putInt("noise_sedang_mid", etNoiseSedangMid.text.toString().toInt())
            editor.putInt("noise_sedang_high", etNoiseSedangHigh.text.toString().toInt())
            editor.putInt("noise_ramai_low", etNoiseRamaiLow.text.toString().toInt())

            editor.apply()

            Toast.makeText(this, "Parameter berhasil disimpan!", Toast.LENGTH_SHORT).show()
            finish()

        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Isi semua field dengan angka yang valid!", Toast.LENGTH_SHORT).show()
        }
    }
}