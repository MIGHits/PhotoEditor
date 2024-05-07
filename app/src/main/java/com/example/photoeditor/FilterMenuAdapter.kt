import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.photoeditor.R
import com.example.photoeditor.ItemData

open class CarouselAdapter(private val images: List<ItemData>, private val activity: Activity) :
    RecyclerView.Adapter<CarouselAdapter.CarouselViewHolder>() {
    var clickListener: OnItemClickListener? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_container_filter, parent, false)
        return CarouselViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
        holder.imageView.setImageResource(images[position].image)
        holder.titleTextView.text = images[position].title
        holder.titleTextView.setSelected(true)
        holder.itemView.setOnClickListener {
            clickListener?.onItemClick(position,images[position].image)
            val animation = AnimationUtils.loadAnimation(activity,R.anim.fade_out)
            holder.itemView.startAnimation(animation)
        }
    }

    override fun getItemCount(): Int {
        return images.size
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int, item: Int)
    }
    open inner class CarouselViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
         val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val titleTextView:TextView = itemView.findViewById(R.id.infoTextView)
    }
}