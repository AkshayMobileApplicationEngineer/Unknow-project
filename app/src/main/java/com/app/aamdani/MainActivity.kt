package com.app.aamdani

import ImageSliderAdapter
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
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

class MainActivity : AppCompatActivity() {
    private  val LOCATION_PERMISSIONS_CODE = 1001
    private  val NOTIFICATION_PERMISSIONS_CODE = 1002


    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: ImageSliderAdapter
    private var currentPage = 0
    private val handler = Handler()
    private val autoSlideInterval: Long = 3000 // Interval for auto-slide in milliseconds
    private lateinit var fabOpenAnim: Animation
    private lateinit var fabCloseAnim: Animation
    private lateinit var fabMain: FloatingActionButton
    private lateinit var fabImage: FloatingActionButton
    private lateinit var fabQR: FloatingActionButton
    private lateinit var fabTransaction: FloatingActionButton
    private var isFabOpen = false

    private val autoSlideRunnable = Runnable {
        moveToNextSlide()
    }


    private val items: List<Any> = listOf(
        R.drawable.food,
        "android.resource://com.app.aamdani/raw/maya",
        "android.resource://com.app.aamdani/raw/quizparlour",
        //Video resource
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermissions()

        if (!requestPermissions()) {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show()
            Log.e("requestPermissions", "Location permission required")
            finish()
        }
        else{
            Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show()
            Log.e("requestPermissions", "Location permission granted")
        }
        viewPager = findViewById(R.id.viewPager)
        adapter = ImageSliderAdapter(items)


        // Fullscreen

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        }

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN


        // Keep the screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)



        // Set up video completion listener
        adapter.setVideoCompletionListener(object : ImageSliderAdapter.VideoCompletionListener {
            override fun onVideoComplete() {
                moveToNextSlide() // Slide to the next item when the video finishes
            }
        })

        // Set adapter to the ViewPager2
        viewPager.adapter = adapter


        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentPage = position;
                startAutoSlide()

            }

        })


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



        } catch (e: Exception) {
            Log.e("MainActivity", "Error during initialization: ${e.message}", e)
            Toast.makeText(this, "Initialization error: ${e.message}", Toast.LENGTH_SHORT).show()
        }

        // Start auto-slide when the activity is created
        startAutoSlide()

    }

    private fun requestPermissions(): Boolean {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
            }
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(android.Manifest.permission.CAMERA)
            }
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.INTERNET
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(android.Manifest.permission.INTERNET)
            }
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(android.Manifest.permission.ACCESS_COARSE_LOCATION)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
        }
        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), LOCATION_PERMISSIONS_CODE)
        }
        return true
    }




            // Function to start auto-slide
    private fun startAutoSlide() {
                handler.removeCallbacks(autoSlideRunnable) //remove any previous auto slide handler


                // If the current item is an video, then do not schedule auto slide, video completes and call `moveToNextSlide`
                if (adapter.getItemViewType(currentPage) != ImageSliderAdapter.TYPE_VIDEO) {
                    handler.postDelayed(
                        autoSlideRunnable,
                        autoSlideInterval
                    ) // set auto-slide for images if current item is image
                }
            }

            // Function to move to the next slide
            private fun moveToNextSlide() {
                if (currentPage < adapter.itemCount - 1) {
                    currentPage++
                    viewPager.setCurrentItem(currentPage, true)
                } else {
                    currentPage = 0; // Loop back to first slide
                    viewPager.setCurrentItem(currentPage, true);
                }

            }

            override fun onPause() {
                super.onPause()
                handler.removeCallbacks(autoSlideRunnable) //stop auto slide if Activity on pause
            }

            override fun onResume() {
                super.onResume()
                startAutoSlide()// auto slide on resume

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

            override fun onSaveInstanceState(outState: Bundle) {
                super.onSaveInstanceState(outState)
                outState.putBoolean("FAB_STATE", isFabOpen)
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
}

