import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.photoeditor.R



interface OnItemClickListener {
    fun onItemClick(position: Int)
}

class CarouselAdapter(private val images: List<Int>, private val itemClickListeners:
    List<OnItemClickListener>,private val activity: Activity) :
    RecyclerView.Adapter<CarouselAdapter.CarouselViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_container_filter, parent, false)
        return CarouselViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
        holder.bind(images[position], itemClickListeners[position])

    }

    override fun getItemCount(): Int {
        return images.size
    }

    inner class CarouselViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)

        fun bind(imageResId: Int, itemClickListener: OnItemClickListener) {
            imageView.setImageResource(imageResId)
            itemView.setOnClickListener {
                itemClickListener.onItemClick(adapterPosition)
                val animation = AnimationUtils.loadAnimation(activity,R.anim.fade_out)
               itemView.startAnimation(animation)
            }
        }
    }
}