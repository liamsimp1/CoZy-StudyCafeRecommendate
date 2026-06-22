package fuzzy.cozy.com.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import fuzzy.cozy.com.R
import fuzzy.cozy.com.databinding.ItemCafeBinding
import fuzzy.cozy.com.data.Cafe
import java.util.Locale

class CafeAdapter(
    private val onItemClick: (Cafe) -> Unit
) : RecyclerView.Adapter<CafeAdapter.ViewHolder>() {

    private var cafes = listOf<Pair<Cafe, Int>>()

    fun submitList(list: List<Pair<Cafe, Int>>) {
        cafes = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCafeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (cafe, score) = cafes[position]
        holder.bind(cafe, score)
        holder.itemView.setOnClickListener {
            onItemClick(cafe)
        }
    }

    override fun getItemCount(): Int = cafes.size

    class ViewHolder(private val binding: ItemCafeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(cafe: Cafe, score: Int) {
            binding.tvName.text = cafe.name
            binding.tvScore.text = score.toString()
            
            // Indikator kategori
            val (category, colorRes) = when (score) {
                in 0..40 -> "Tidak Direkomendasikan" to R.color.red
                in 41..70 -> "Cukup Direkomendasikan" to R.color.yellow
                else -> "Sangat Direkomendasikan" to R.color.green
            }
            
            binding.tvCategory.text = category
            binding.tvCategory.setTextColor(ContextCompat.getColor(binding.root.context, colorRes))

            // Format info cafe
            binding.tvWifi.text = "${cafe.wifiSpeed} Mbps"
            binding.tvPrice.text = "Rp ${cafe.price}k"
            binding.tvComfort.text = if(cafe.comfort > 7) "Nyaman" else "Cukup"
            binding.tvOutlets.text = if(cafe.outlets > 7) "Banyak Outlet" else "Tersedia"
            
            // Jarak dinamis dari lokasi saat ini
            binding.tvDistance.text = String.format(Locale.getDefault(), "%.1f km", cafe.distance)
            
            // Set warna background skor berdasarkan nilai
            val scoreBgTint = when (score) {
                in 0..40 -> R.color.red
                in 41..70 -> R.color.yellow
                else -> R.color.brown
            }
            binding.tvScore.backgroundTintList = ContextCompat.getColorStateList(binding.root.context, scoreBgTint)
        }
    }
}
