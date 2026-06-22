package fuzzy.cozy.com.fuzzy

import fuzzy.cozy.com.data.Cafe
import fuzzy.cozy.com.data.FuzzyPrefs
import kotlin.math.min

class FuzzyMamdani(private val prefs: FuzzyPrefs) {

    data class Membership(val name: String, val value: Double)
    data class RuleResult(val ruleName: String, val alpha: Double, val z: Double, val output: String)

    fun fuzzifikasi(cafe: Cafe): Map<String, List<Membership>> {
        val result = mutableMapOf<String, List<Membership>>()

        result["wifi"] = listOf(
            Membership("Lambat", membershipLambat(cafe.wifiSpeed.toDouble())),
            Membership("Sedang", membershipSedang(cafe.wifiSpeed.toDouble())),
            Membership("Cepat", membershipCepat(cafe.wifiSpeed.toDouble()))
        )
        result["noise"] = listOf(
            Membership("Sepi", membershipSepi(cafe.noiseLevel.toDouble())),
            Membership("Sedang", membershipSedangNoise(cafe.noiseLevel.toDouble())),
            Membership("Ramai", membershipRamai(cafe.noiseLevel.toDouble()))
        )
        result["comfort"] = listOf(
            Membership("Tidak Nyaman", membershipTidakNyaman(cafe.comfort.toDouble())),
            Membership("Cukup", membershipCukup(cafe.comfort.toDouble())),
            Membership("Nyaman", membershipNyaman(cafe.comfort.toDouble()))
        )
        result["price"] = listOf(
            Membership("Murah", membershipMurah(cafe.price.toDouble())),
            Membership("Sedang", membershipSedangPrice(cafe.price.toDouble())),
            Membership("Mahal", membershipMahal(cafe.price.toDouble()))
        )
        result["outlets"] = listOf(
            Membership("Sedikit", membershipSedikit(cafe.outlets.toDouble())),
            Membership("Sedang", membershipSedangOutlets(cafe.outlets.toDouble())),
            Membership("Banyak", membershipBanyak(cafe.outlets.toDouble()))
        )
        result["distance"] = listOf(
            Membership("Dekat", membershipJarakDekat(cafe.distance)),
            Membership("Sedang", membershipJarakSedang(cafe.distance)),
            Membership("Jauh", membershipJarakJauh(cafe.distance))
        )

        return result
    }

    private fun membershipLambat(v: Double) = if (v <= prefs.getWifiLambatHigh()) 1.0 else 0.0
    private fun membershipSedang(v: Double) = when {
        v <= prefs.getWifiSedangLow() || v >= prefs.getWifiSedangHigh() -> 0.0
        v <= prefs.getWifiSedangMid() -> (v - prefs.getWifiSedangLow()) / (prefs.getWifiSedangMid() - prefs.getWifiSedangLow())
        else -> (prefs.getWifiSedangHigh() - v) / (prefs.getWifiSedangHigh() - prefs.getWifiSedangMid())
    }
    private fun membershipCepat(v: Double) = if (v >= prefs.getWifiCepatLow()) 1.0 else 0.0

    private fun membershipSepi(v: Double) = if (v <= prefs.getNoiseSepiHigh()) 1.0 else 0.0
    private fun membershipSedangNoise(v: Double) = when {
        v <= prefs.getNoiseSedangLow() || v >= prefs.getNoiseSedangHigh() -> 0.0
        v <= prefs.getNoiseSedangMid() -> (v - prefs.getNoiseSedangLow()) / (prefs.getNoiseSedangMid() - prefs.getNoiseSedangLow())
        else -> (prefs.getNoiseSedangHigh() - v) / (prefs.getNoiseSedangHigh() - prefs.getNoiseSedangMid())
    }
    private fun membershipRamai(v: Double) = if (v >= prefs.getNoiseRamaiLow()) 1.0 else 0.0

    private fun membershipTidakNyaman(v: Double) = if (v <= prefs.getComfortTidakHigh()) 1.0 else 0.0
    private fun membershipCukup(v: Double) = when {
        v <= prefs.getComfortCukupLow() || v >= prefs.getComfortCukupHigh() -> 0.0
        v <= prefs.getComfortCukupMid() -> (v - prefs.getComfortCukupLow()) / (prefs.getComfortCukupMid() - prefs.getComfortCukupLow())
        else -> (prefs.getComfortCukupHigh() - v) / (prefs.getComfortCukupHigh() - prefs.getComfortCukupMid())
    }
    private fun membershipNyaman(v: Double) = if (v >= prefs.getComfortNyamanLow()) 1.0 else 0.0

    private fun membershipMurah(v: Double) = if (v <= prefs.getPriceMurahHigh()) 1.0 else 0.0
    private fun membershipSedangPrice(v: Double) = when {
        v <= prefs.getPriceSedangLow() || v >= prefs.getPriceSedangHigh() -> 0.0
        v <= prefs.getPriceSedangMid() -> (v - prefs.getPriceSedangLow()) / (prefs.getPriceSedangMid() - prefs.getPriceSedangLow())
        else -> (prefs.getPriceSedangHigh() - v) / (prefs.getPriceSedangHigh() - prefs.getPriceSedangMid())
    }
    private fun membershipMahal(v: Double) = if (v >= prefs.getPriceMahalLow()) 1.0 else 0.0

    private fun membershipSedikit(v: Double) = if (v <= prefs.getOutletsSedikitHigh()) 1.0 else 0.0
    private fun membershipSedangOutlets(v: Double) = when {
        v <= prefs.getOutletsSedangLow() || v >= prefs.getOutletsSedangHigh() -> 0.0
        v <= prefs.getOutletsSedangMid() -> (v - prefs.getOutletsSedangLow()) / (prefs.getOutletsSedangMid() - prefs.getOutletsSedangLow())
        else -> (prefs.getOutletsSedangHigh() - v) / (prefs.getOutletsSedangHigh() - prefs.getOutletsSedangMid())
    }
    private fun membershipBanyak(v: Double) = if (v >= prefs.getOutletsBanyakLow()) 1.0 else 0.0

    private fun membershipJarakDekat(dist: Double) = when {
        dist <= 1.0 -> 1.0
        dist >= 3.0 -> 0.0
        else -> (3.0 - dist) / (3.0 - 1.0)
    }
    private fun membershipJarakSedang(dist: Double) = when {
        dist <= 1.5 || dist >= 8.0 -> 0.0
        dist in 1.5..4.0 -> (dist - 1.5) / (4.0 - 1.5)
        else -> (8.0 - dist) / (8.0 - 4.0)
    }
    private fun membershipJarakJauh(dist: Double) = when {
        dist >= 8.0 -> 1.0
        dist <= 4.0 -> 0.0
        else -> (dist - 4.0) / (8.0 - 4.0)
    }

    fun hitungRekomendasi(cafe: Cafe): Triple<Double, String, List<RuleResult>> {
        val memberships = fuzzifikasi(cafe)

        val wifi = memberships["wifi"]!!.associate { it.name to it.value }
        val noise = memberships["noise"]!!.associate { it.name to it.value }
        val comfort = memberships["comfort"]!!.associate { it.name to it.value }
        val price = memberships["price"]!!.associate { it.name to it.value }
        val outlets = memberships["outlets"]!!.associate { it.name to it.value }
        val distance = memberships["distance"]!!.associate { it.name to it.value }

        val ruleResults = mutableListOf<RuleResult>()

        // ATURAN POSITIF (Memberikan nilai tinggi)

        // RE1: Murah DAN Banyak Stop Kontak (Rule Request dari User)
        val aEconomic = minOf(price["Murah"]?:0.0, outlets["Banyak"]?:0.0)
        if (aEconomic > 0) ruleResults.add(RuleResult("RE1", aEconomic, 98.0, "Sangat Direkomendasikan"))

        // RE2: WiFi Cepat DAN Banyak Stop Kontak
        val aFasilitas = minOf(wifi["Cepat"]?:0.0, outlets["Banyak"]?:0.0)
        if (aFasilitas > 0) ruleResults.add(RuleResult("RE2", aFasilitas, 95.0, "Sangat Direkomendasikan"))

        // RE3: Dekat DAN Sepi
        val aIdealPos = minOf(distance["Dekat"]?:0.0, noise["Sepi"]?:0.0)
        if (aIdealPos > 0) ruleResults.add(RuleResult("RE3", aIdealPos, 92.0, "Sangat Direkomendasikan"))

        // ATURAN PENALTI (Memberikan nilai rendah)

        // RP1: Mahal DAN Stop Kontak Dikit
        val aBadValue = minOf(price["Mahal"]?:0.0, outlets["Sedikit"]?:0.0)
        if (aBadValue > 0) ruleResults.add(RuleResult("RP1", aBadValue, 20.0, "Tidak Direkomendasikan"))

        // RP2: Sangat Berisik ATAU WiFi Lambat
        val aBadEnv = maxOf(wifi["Lambat"] ?: 0.0, noise["Ramai"] ?: 0.0)
        if (aBadEnv > 0) ruleResults.add(RuleResult("RP2", aBadEnv, 30.0, "Tidak Direkomendasikan"))

        // RP3: Jauh (Hanya penalti ringan agar cafe bagus yang jauh tetap direkomendasikan)
        val aFar = distance["Jauh"] ?: 0.0
        if (aFar > 0) ruleResults.add(RuleResult("RP3", aFar, 60.0, "Cukup Direkomendasikan"))

        // DEFUZZIFIKASI
        var sumAlphaZ = 0.0
        var sumAlpha = 0.0
        for (result in ruleResults) {
            sumAlphaZ += result.alpha * result.z
            sumAlpha += result.alpha
        }

        // Skor default jika tidak ada aturan terpicu
        val finalScore = if (sumAlpha > 0) sumAlphaZ / sumAlpha else 65.0
        val roundedScore = finalScore.coerceIn(0.0, 100.0)

        val category = when {
            roundedScore <= 45 -> "Tidak Direkomendasikan"
            roundedScore <= 75 -> "Cukup Direkomendasikan"
            else -> "Sangat Direkomendasikan"
        }

        return Triple(roundedScore, category, ruleResults)
    }
}
