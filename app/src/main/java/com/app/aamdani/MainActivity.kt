package com.app.aamdani

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.webkit.JavascriptInterface


class MainActivity : AppCompatActivity() {

    private lateinit var fabMain: FloatingActionButton
    private lateinit var fabImage: FloatingActionButton
    private lateinit var fabQR: FloatingActionButton
    private lateinit var fabTransaction: FloatingActionButton

    private lateinit var viewPager: ViewPager2
    private lateinit var handler: Handler

    private var currentPage = 0
    private val slideInterval = 10000L // Auto-slide interval in milliseconds (3 seconds)
    private var autoSlideRunnable = Runnable { startAutoSlide() }
    private var isFabOpen = false
    private lateinit var fabOpenAnim: Animation
    private lateinit var fabCloseAnim: Animation

    private val permissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.CAMERA,
        Manifest.permission.INTERNET // Adding internet permission here
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Request permissions
        requestPermissions()

        // Hide the status bar
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        // Keep the screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        try {
            // Initialize Floating Action Buttons
            fabMain = findViewById(R.id.fab_main)
            fabImage = findViewById(R.id.fab_image)
            fabQR = findViewById(R.id.fab_qr)
            fabTransaction = findViewById(R.id.fab_transaction)

            // Initialize animations
            fabOpenAnim = AnimationUtils.loadAnimation(this, R.anim.fab_open)
            fabCloseAnim = AnimationUtils.loadAnimation(this, R.anim.fab_close)

            // Set up FAB click listeners
            fabMain.setOnClickListener { toggleFabMenu() }
            fabImage.setOnClickListener {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
            }
            fabQR.setOnClickListener {
                openActivity(QRScannerActivity::class.java, "QR Scanner Activity")
            }

            fabTransaction.setOnClickListener {
                openActivity(TransactionHistoryActivity::class.java, "Transaction History Activity")
            }

            // Restore FAB state
            savedInstanceState?.let {
                isFabOpen = it.getBoolean("FAB_STATE", false)
                restoreFabState()
            }

            // Initialize ViewPager2
            setupViewPager()
        } catch (e: Exception) {
            Log.e("MainActivity", "Error during initialization: ${e.message}", e)
            Toast.makeText(this, "Initialization error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestPermissions() {
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), 1)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            1 -> {
                if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
                } else {
                    // Handle denied permissions with an explanation and request again if necessary
                    permissions.forEachIndexed { index, permission ->
                        if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                                // Show rationale and request permission again
                                Toast.makeText(this, "Permission for $permission is needed for app functionality.", Toast.LENGTH_LONG).show()
                                ActivityCompat.requestPermissions(this, arrayOf(permission), 1)
                            } else {
                                // Permission denied permanently, guide user to settings
                                Toast.makeText(this, "Permission denied permanently for $permission. Please enable it in settings.", Toast.LENGTH_LONG).show()
                                Log.e("MainActivity", "Permission denied permanently for $permission")
                            }
                        }
                    }
                    // Handle denied permissions
                    Log.e("MainActivity", "Permissions denied")

                    Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupViewPager() {
        try {
            viewPager = findViewById(R.id.viewPager)

            // List of images (drawable resources) and videos (URLs or file paths)
            val items: List<Any> = listOf(
                R.drawable.food,  // Image resource
                "android.resource://$packageName/${R.raw.maya}",  // Video URI
                R.drawable.logo_web,  // Image resource
                "https://company-task-practics.vercel.app/maya.mp4",
            )

            val adapter = ImageSliderAdapter(items)
            viewPager.adapter = adapter

            // Auto-slide functionality
            handler = Handler(Looper.getMainLooper())
             autoSlideRunnable = object : Runnable {
                override fun run() {
                    val adapter = viewPager.adapter as ImageSliderAdapter

                    // Advance the page index, wrapping around when reaching the last item
                    currentPage = (currentPage + 1) % adapter.itemCount

                    viewPager.setCurrentItem(currentPage, true)

                    // Check if the next item is a video or image
                    val isVideo = adapter.isVideoAtPosition(currentPage)
                    if (isVideo) {
                        // Wait for the video completion event to proceed (no auto-slide)
                        handler.removeCallbacks(autoSlideRunnable)
                    } else {
                        // Schedule the next slide
                        handler.postDelayed(this, slideInterval)
                    }
                }
            }


            // Start the auto-slide initially
            startAutoSlide()

            adapter.setVideoCompletionListener(object : ImageSliderAdapter.VideoCompletionListener {
                override fun onVideoComplete() {
                    // Resume auto-slide after video completes
                    startAutoSlide()
                }
            })


            // Pause auto-slide during user interaction
            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageScrollStateChanged(state: Int) {
                    if (state == ViewPager2.SCROLL_STATE_IDLE) {
                        startAutoSlide()
                    } else {
                        handler.removeCallbacks(autoSlideRunnable)
                    }
                }
            })
        } catch (e: Exception) {
            Log.e("MainActivity", "Error setting up ViewPager: ${e.message}", e)
            Toast.makeText(this, "Error setting up ViewPager: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    private fun startAutoSlide() {
        // Remove any previously scheduled callbacks to prevent overlapping
        handler.removeCallbacks(autoSlideRunnable)

        // Post the auto-slide runnable with a delay
        handler.postDelayed(autoSlideRunnable, slideInterval)
    }



    private fun toggleFabMenu() {
        try {
            if (isFabOpen) {
                fabImage.startAnimation(fabCloseAnim)
                fabQR.startAnimation(fabCloseAnim)
                fabTransaction.startAnimation(fabCloseAnim)
                fabImage.visibility = View.GONE
                fabQR.visibility = View.GONE
                fabTransaction.visibility = View.GONE
            } else {
                fabImage.visibility = View.VISIBLE
                fabQR.visibility = View.VISIBLE
                fabTransaction.visibility = View.VISIBLE
                fabImage.startAnimation(fabOpenAnim)
                fabQR.startAnimation(fabOpenAnim)
                fabTransaction.startAnimation(fabOpenAnim)
            }
            isFabOpen = !isFabOpen
        } catch (e: Exception) {
            Log.e("MainActivity", "Error toggling FAB menu: ${e.message}", e)
        }
    }

    private fun restoreFabState() {
        try {
            if (isFabOpen) {
                fabImage.visibility = View.VISIBLE
                fabQR.visibility = View.VISIBLE
                fabTransaction.visibility = View.VISIBLE
            } else {
                fabImage.visibility = View.GONE
                fabQR.visibility = View.GONE
                fabTransaction.visibility = View.GONE
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error restoring FAB state: ${e.message}", e)
        }
    }

    private fun openActivity(activityClass: Class<*>, activityName: String) {
        try {
            Toast.makeText(this, "Opening $activityName", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, activityClass))
        } catch (e: Exception) {
            Log.e("MainActivity", "Error opening $activityName: ${e.message}", e)
            Toast.makeText(this, "Failed to open $activityName", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("FAB_STATE", isFabOpen)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            handler.removeCallbacks(autoSlideRunnable) // Cleanup auto-slide to prevent leaks
        } catch (e: Exception) {
            Log.e("MainActivity", "Error during cleanup: ${e.message}", e)
        }
    }

}