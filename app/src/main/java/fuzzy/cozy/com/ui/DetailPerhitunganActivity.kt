package fuzzy.cozy.com.ui

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import fuzzy.cozy.com.R
import fuzzy.cozy.com.data.AppDatabase
import fuzzy.cozy.com.data.FuzzyPrefs
import fuzzy.cozy.com.fuzzy.FuzzyMamdani
import fuzzy.cozy.com.data.Cafe
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import java.util.Locale

class DetailPerhitunganActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var prefs: FuzzyPrefs
    private lateinit var fuzzyEngine: FuzzyMamdani
    private var cafeId: Int = -1

    private lateinit var tvCafeName: TextView
    private lateinit var tvFuzzifikasi: TextView
    private lateinit var tvInferensi: TextView
    private lateinit var tvDefuzzifikasi: TextView
    private lateinit var tvFinalScore: TextView
    private lateinit var tvFinalCategory: TextView
    private lateinit var ivBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_perhitungan)

        db = AppDatabase.getDatabase(this)
        prefs = FuzzyPrefs(this)
        fuzzyEngine = FuzzyMamdani(prefs)

        initViews()
        setupClickListeners()

        cafeId = intent.getIntExtra("cafe_id", -1)
        if (cafeId != -1) {
            loadAndCalculate()
        } else {
            finish()
        }
    }

    private fun initViews() {
        tvCafeName = findViewById(R.id.tv_cafe_name)
        tvFuzzifikasi = findViewById(R.id.tv_fuzzifikasi)
        tvInferensi = findViewById(R.id.tv_inferensi)
        tvDefuzzifikasi = findViewById(R.id.tv_defuzzifikasi)
        tvFinalScore = findViewById(R.id.tv_final_score)
        tvFinalCategory = findViewById(R.id.tv_final_category)
        ivBack = findViewById(R.id.iv_back)
    }

    private fun setupClickListeners() {
        ivBack.setOnClickListener { finish() }
    }

    private fun loadAndCalculate() {
        lifecycleScope.launch {
            val cafe = db.cafeDao().getById(cafeId)
            cafe?.let {
                displayPerhitungan(it)
            }
        }
    }

    private fun displayPerhitungan(cafe: Cafe) {
        tvCafeName.text = cafe.name

        // 1. Fuzzifikasi
        val memberships = fuzzyEngine.fuzzifikasi(cafe)
        val fuzzifikasiText = buildString {
            append("WiFi = ${cafe.wifiSpeed} Mbps\n")
            memberships["wifi"]?.forEach {
                append("   • ${it.name}: ${String.format(Locale.US, "%.2f", it.value)}\n")
            }
            append("\nKebisingan = ${cafe.noiseLevel}\n")
            memberships["noise"]?.forEach {
                append("   • ${it.name}: ${String.format(Locale.US, "%.2f", it.value)}\n")
            }
            append("\nKenyamanan = ${cafe.comfort}/10\n")
            memberships["comfort"]?.forEach {
                append("   • ${it.name}: ${String.format(Locale.US, "%.2f", it.value)}\n")
            }
            append("\nHarga = Rp${cafe.price}.000\n")
            memberships["price"]?.forEach {
                append("   • ${it.name}: ${String.format(Locale.US, "%.2f", it.value)}\n")
            }
            append("\nStop Kontak = ${cafe.outlets}\n")
            memberships["outlets"]?.forEach {
                append("   • ${it.name}: ${String.format(Locale.US, "%.2f", it.value)}\n")
            }
        }
        tvFuzzifikasi.text = fuzzifikasiText

        // 2. Inferensi
        val (_, category, ruleResults) = fuzzyEngine.hitungRekomendasi(cafe)
        val inferensiText = buildString {
            for (result in ruleResults) {
                append("${result.ruleName}: ")
                append("α = ${String.format(Locale.US, "%.2f", result.alpha)} → ")
                append("${result.output} (z = ${result.z.toInt()})\n")
            }
            if (ruleResults.isEmpty()) {
                append("Tidak ada rules yang terpicu\n")
            }
        }
        tvInferensi.text = inferensiText

        // 3. Defuzzifikasi
        var sumAlphaZ = 0.0
        var sumAlpha = 0.0
        for (result in ruleResults) {
            sumAlphaZ += result.alpha * result.z
            sumAlpha += result.alpha
        }
        val finalScore = if (sumAlpha > 0) sumAlphaZ / sumAlpha else 0.0

        val defuzzifikasiText = buildString {
            append("Metode: Centroid (COA)\n\n")
            append("Σ(αi × zi) = ")
            for ((i, result) in ruleResults.withIndex()) {
                append("(${String.format(Locale.US, "%.2f", result.alpha)}×${result.z.toInt()})")
                if (i < ruleResults.size - 1) append(" + ")
            }
            append("\n            = ${String.format(Locale.US, "%.2f", sumAlphaZ)}\n\n")
            append("Σαi = ${String.format(Locale.US, "%.2f", sumAlpha)}\n\n")
            append("Skor Akhir = Σ(αi × zi) / Σαi\n")
            append("           = ${String.format(Locale.US, "%.2f", sumAlphaZ)} / ${String.format(Locale.US, "%.2f", sumAlpha)}\n")
            append("           = ${String.format(Locale.US, "%.2f", finalScore)}\n")
            append("           ≈ ${finalScore.toInt()}")
        }
        tvDefuzzifikasi.text = defuzzifikasiText

        // 4. Kesimpulan
        tvFinalScore.text = getString(R.string.score_format, finalScore.toInt())
        tvFinalCategory.text = when (category) {
            "Tidak Direkomendasikan" -> "🔴 $category"
            "Cukup Direkomendasikan" -> "🟡 $category"
            else -> "🟢 $category"
        }
    }
}
