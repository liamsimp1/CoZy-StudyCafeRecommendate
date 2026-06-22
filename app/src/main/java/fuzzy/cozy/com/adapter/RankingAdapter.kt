package fuzzy.cozy.com.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fuzzy.cozy.com.databinding.ItemRankingBinding
import fuzzy.cozy.com.data.Cafe
import java.util.Locale

class RankingAdapter : RecyclerView.Adapter<RankingAdapter.ViewHolder>() {

    private var items = listOf<Triple<Cafe, Int, String>>()

    fun submitList(list: List<Triple<Cafe, Int, String>>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRankingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (cafe, score, category) = items[position]
        holder.bind(cafe, score, category, position + 1)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(private val binding: ItemRankingBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(cafe: Cafe, score: Int, category: String, rank: Int) {
            binding.tvRank.text = rank.toString()
            binding.tvName.text = cafe.name
            binding.tvScore.text = score.toString()
            binding.progressScore.progress = score

            binding.tvCategory.text = category
            
            binding.tvWifi.text = "${cafe.wifiSpeed} Mbps"
            binding.tvPrice.text = "Rp ${cafe.price}k"
            
            // Tampilkan jarak
            binding.tvDistance.text = String.format(Locale.getDefault(), "%.1f km", cafe.distance)
        }
    }
}
