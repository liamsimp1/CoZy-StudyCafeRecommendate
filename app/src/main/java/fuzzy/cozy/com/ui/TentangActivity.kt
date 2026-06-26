package fuzzy.cozy.com.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import fuzzy.cozy.com.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class TentangActivity : AppCompatActivity() {

    private lateinit var tvHimpunan: TextView
    private lateinit var tvRules: TextView
    private lateinit var tvReferensi: TextView
    private lateinit var btnEditParameter: Button
    private lateinit var ivBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tentang)

        initViews()
        setupClickListeners()
        setupBottomNav()
        loadData()
    }

    private fun initViews() {
        tvHimpunan = findViewById(R.id.tv_himpunan)
        tvRules = findViewById(R.id.tv_rules)
        tvReferensi = findViewById(R.id.tv_referensi)
        btnEditParameter = findViewById(R.id.btn_edit_parameter)
        ivBack = findViewById(R.id.iv_back)
    }

    private fun setupClickListeners() {
        ivBack.setOnClickListener { finish() }
        btnEditParameter.setOnClickListener {
            startActivity(Intent(this, EditParameterActivity::class.java))
        }
    }

    private fun setupBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.selectedItemId = R.id.nav_tentang
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
                R.id.nav_ranking -> {
                    startActivity(Intent(this, RankingActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_tentang -> true
                else -> false
            }
        }
    }

    private fun loadData() {
        // Himpunan Fuzzy
        tvHimpunan.text = buildString {
            append("WiFi (Mbps)\n")
            append("   • Lambat: 0 - 40\n")
            append("   • Sedang: 30 - 70\n")
            append("   • Cepat: 60 - 100\n\n")

            append("Kebisingan\n")
            append("   • Sepi: 0 - 40\n")
            append("   • Sedang: 30 - 70\n")
            append("   • Ramai: 60 - 100\n\n")

            append("Kenyamanan Kursi (0-10)\n")
            append("   • Tidak Nyaman: 0 - 4\n")
            append("   • Cukup: 3 - 7\n")
            append("   • Nyaman: 6 - 10\n\n")

            append("Harga (ribu rupiah)\n")
            append("   • Murah: 0 - 40\n")
            append("   • Sedang: 30 - 70\n")
            append("   • Mahal: 60 - 100\n\n")

            append("Stop Kontak (jumlah)\n")
            append("   • Sedikit: 0 - 4\n")
            append("   • Sedang: 3 - 7\n")
            append("   • Banyak: 6 - 10")
        }

        // Basis Aturan
        tvRules.text = buildString {
            append("R1: IF Cepat AND Sepi AND Nyaman AND Dekat\n")
            append("    → Sangat Direkomendasikan (95)\n\n")

            append("R2: IF Jauh\n")
            append("    → Cukup Direkomendasikan (40)\n\n")

            append("R_Def: IF Cepat AND Banyak\n")
            append("    → Sangat Direkomendasikan (80)")
        }

        // Referensi
        tvReferensi.text = buildString {
            append("[1] Kusumadewi, S., & Purnomo, H. (2010). Aplikasi Logika Fuzzy untuk Pendukung Keputusan. Graha Ilmu.\n\n")
            append("[2] Zulfikar, dkk. (2023). Sistem Rekomendasi Tempat Kuliner Menggunakan Metode Fuzzy Mamdani. COREAI, 4(1).\n\n")
            append("[3] ResearchGate. (2019). Sistem Pendukung Keputusan Pemilihan Tempat Kuliner Berbasis Android Menggunakan Model Fuzzy Mamdani.")
        }
    }
}
