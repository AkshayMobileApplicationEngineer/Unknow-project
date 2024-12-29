import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.app.aamdani.R

class ImageSliderAdapter(private val items: List<Any>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_IMAGE = 0
        const val TYPE_VIDEO = 1
    }

    private var videoCompletionListener: VideoCompletionListener? = null

    interface VideoCompletionListener {
        fun onVideoComplete()
    }

    fun setVideoCompletionListener(listener: VideoCompletionListener) {
        this.videoCompletionListener = listener
    }

    override fun getItemCount(): Int = items.size


    // Override getItemViewType to return appropriate type based on the item
    override fun getItemViewType(position: Int): Int {
        return when (val item = items[position]) {
            is Int -> TYPE_IMAGE // Image resource ID
            is String -> TYPE_VIDEO // Uri for video
            else -> throw IllegalArgumentException("Invalid item type at position $position")
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_IMAGE -> {
                val view = inflater.inflate(R.layout.item_image, parent, false)
                ImageViewHolder(view)
            }
            TYPE_VIDEO -> {
                val view = inflater.inflate(R.layout.item_video, parent, false)
                VideoViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ImageViewHolder -> {
                val item = items[position] as Int// Cast to int here
                holder.bindImage(item)
            }
            is VideoViewHolder -> {
                val item = items[position] as String // Cast to string here
                holder.bindVideo(item, videoCompletionListener)
            }
        }
    }



    // ViewHolder for images
    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)

        fun bindImage(imageResource: Int) {
            imageView.setImageResource(imageResource)
        }
    }

    // ViewHolder for videos
    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val videoView: VideoView = itemView.findViewById(R.id.videoView)
        private lateinit var context : Context

        fun bindVideo(videoUriString : String, listener: VideoCompletionListener?) {
            val uri = Uri.parse(videoUriString)
            context = itemView.context

            videoView.setVideoURI(uri)

            videoView.setOnPreparedListener { mp ->
                mp.isLooping = false  // Don't loop videos
                mp.start()  //Start playing when ready
            }

            videoView.setOnCompletionListener {
                listener?.onVideoComplete() //Notify the completion of video
            }
        }
    }
}