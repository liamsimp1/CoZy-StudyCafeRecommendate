package fuzzy.cozy.com.data
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cafes")
data class Cafe(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val address: String = "",
    // Default
    val wifiSpeed: Int,      // Mbps 0-100
    val noiseLevel: Int,     // 0-100
    val comfort: Int,        // 0-10
    val price: Int,          // ribu rupiah
    val outlets: Int,        // 0-10
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val openTime: String = "08:00",
    val closeTime: String = "22:00"
) {
    // Properti pembantu untuk jarak (ga disimpen di DB)
    @androidx.room.Ignore
    var distance: Double = 0.0
}
