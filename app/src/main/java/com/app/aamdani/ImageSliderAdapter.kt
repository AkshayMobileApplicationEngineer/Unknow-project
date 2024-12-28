package com.app.aamdani

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.app.aamdani.R

class ImageSliderAdapter(private val items: List<Any>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_IMAGE = 0
    private val TYPE_VIDEO = 1
    private val TYPE_YOUTUBE = 2
    private var videoCompletionListener: VideoCompletionListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_IMAGE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
                ImageViewHolder(view)
            }
            TYPE_VIDEO -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_video, parent, false)
                VideoViewHolder(view)
            }
            TYPE_YOUTUBE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_youtube, parent, false)
                YouTubeViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
                ImageViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_IMAGE -> (holder as ImageViewHolder).bind(items[position] as Int)
            TYPE_VIDEO -> (holder as VideoViewHolder).bind(items[position] as String)
            TYPE_YOUTUBE -> (holder as YouTubeViewHolder).bind(items[position] as String)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = items[position]
        return when {
            item is Int -> TYPE_IMAGE
            item is String && (item.contains("youtube.com") || item.contains("youtu.be")) -> TYPE_YOUTUBE
            item is String -> TYPE_VIDEO
            else -> TYPE_IMAGE
        }
    }

    override fun getItemCount(): Int = items.size

    fun setVideoCompletionListener(listener: VideoCompletionListener) {
        this.videoCompletionListener = listener
    }

    fun isVideoAtPosition(currentPage: Int): Boolean {
        val item = items.getOrNull(currentPage)
        return item is String && !item.contains("youtube.com") && !item.contains("youtu.be")
    }

    // ViewHolder for images
    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)

        fun bind(imageResId: Int) {
            imageView.setImageResource(imageResId)
        }
    }

    // ViewHolder for videos
    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val videoView: VideoView = itemView.findViewById(R.id.videoView)

        fun bind(videoUri: String) {
            videoView.setVideoURI(Uri.parse(videoUri))
            videoView.setOnPreparedListener { mediaPlayer ->
                mediaPlayer.isLooping = false
                videoView.start()

                mediaPlayer.setOnCompletionListener {
                    // Trigger next slide on video completion
                    val nextItemPosition = (bindingAdapterPosition + 1) % items.size
                    val parent = itemView.parent as? ViewPager2
                    parent?.setCurrentItem(nextItemPosition, true)
                    videoCompletionListener?.onVideoComplete()
                }
            }
        }
    }

    // ViewHolder for YouTube videos
    inner class YouTubeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val webView: WebView = itemView.findViewById(R.id.webView)

        fun bind(youtubeUrl: String) {
            webView.settings.javaScriptEnabled = true
            webView.webViewClient = WebViewClient()
            val embedUrl = youtubeUrl.replace("watch?v=", "embed/")
            webView.loadUrl(embedUrl)
        }
    }

    interface VideoCompletionListener {
        fun onVideoComplete()
    }
}
