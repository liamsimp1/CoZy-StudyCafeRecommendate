package fuzzy.cozy.com.data
import android.content.Context
import android.content.SharedPreferences

class FuzzyPrefs(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("fuzzy_config", Context.MODE_PRIVATE)

    // WiFi ranges
    fun getWifiLambatHigh(): Int = prefs.getInt("wifi_lambat_high", 40)
    fun getWifiSedangLow(): Int = prefs.getInt("wifi_sedang_low", 30)
    fun getWifiSedangMid(): Int = prefs.getInt("wifi_sedang_mid", 50)
    fun getWifiSedangHigh(): Int = prefs.getInt("wifi_sedang_high", 70)
    fun getWifiCepatLow(): Int = prefs.getInt("wifi_cepat_low", 60)

    // Noise ranges
    fun getNoiseSepiHigh(): Int = prefs.getInt("noise_sepi_high", 40)
    fun getNoiseSedangLow(): Int = prefs.getInt("noise_sedang_low", 30)
    fun getNoiseSedangMid(): Int = prefs.getInt("noise_sedang_mid", 50)
    fun getNoiseSedangHigh(): Int = prefs.getInt("noise_sedang_high", 70)
    fun getNoiseRamaiLow(): Int = prefs.getInt("noise_ramai_low", 60)

    // Comfort ranges (default tetap 0-10)
    fun getComfortTidakHigh(): Int = 4
    fun getComfortCukupLow(): Int = 3
    fun getComfortCukupMid(): Int = 5
    fun getComfortCukupHigh(): Int = 7
    fun getComfortNyamanLow(): Int = 6

    // Price ranges (default tetap)
    fun getPriceMurahHigh(): Int = 40
    fun getPriceSedangLow(): Int = 30
    fun getPriceSedangMid(): Int = 50
    fun getPriceSedangHigh(): Int = 70
    fun getPriceMahalLow(): Int = 60

    // Outlets ranges (default tetap)
    fun getOutletsSedikitHigh(): Int = 4
    fun getOutletsSedangLow(): Int = 3
    fun getOutletsSedangMid(): Int = 5
    fun getOutletsSedangHigh(): Int = 7
    fun getOutletsBanyakLow(): Int = 6

    fun saveWifiParams(
        lambatHigh: Int,
        sedangLow: Int, sedangMid: Int, sedangHigh: Int,
        cepatLow: Int
    ) {
        prefs.edit().apply {
            putInt("wifi_lambat_high", lambatHigh)
            putInt("wifi_sedang_low", sedangLow)
            putInt("wifi_sedang_mid", sedangMid)
            putInt("wifi_sedang_high", sedangHigh)
            putInt("wifi_cepat_low", cepatLow)
            apply()
        }
    }

    fun saveNoiseParams(
        sepiHigh: Int,
        sedangLow: Int, sedangMid: Int, sedangHigh: Int,
        ramaiLow: Int
    ) {
        prefs.edit().apply {
            putInt("noise_sepi_high", sepiHigh)
            putInt("noise_sedang_low", sedangLow)
            putInt("noise_sedang_mid", sedangMid)
            putInt("noise_sedang_high", sedangHigh)
            putInt("noise_ramai_low", ramaiLow)
            apply()
        }
    }
}