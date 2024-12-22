package com.app.unknownproject

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.app.unknowproject.ProfileActivity
import com.app.unknowproject.ImageSliderAdapter
import com.app.unknowproject.QRScannerActivity
import com.app.unknowproject.R
import com.app.unknowproject.TransactionHistoryActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton


class MainActivity : AppCompatActivity() {

    private lateinit var fabMain: FloatingActionButton
    private lateinit var fabImage: FloatingActionButton
    private lateinit var fabQR: FloatingActionButton
    private lateinit var fabTransaction: FloatingActionButton

    private lateinit var viewPager: ViewPager2
    private lateinit var handler: Handler
    private lateinit var autoSlideRunnable: Runnable
    private var currentPage = 0
    private val slideInterval = 3000L // Auto-slide interval in milliseconds

    private var isFabOpen = false
    private lateinit var fabOpenAnim: Animation
    private lateinit var fabCloseAnim: Animation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
            fabQR.setOnClickListener { openActivity(QRScannerActivity::class.java, "QR Scanner Activity") }
            fabTransaction.setOnClickListener { openActivity(TransactionHistoryActivity::class.java, "Transaction History Activity") }

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

    private fun setupViewPager() {
        try {
            viewPager = findViewById(R.id.viewPager)
            val images = listOf(
                R.drawable.food,  // Replace with actual drawable resources
                R.drawable.logo_web,
                R.drawable.rapido
            )
            val adapter = ImageSliderAdapter(images)
            viewPager.adapter = adapter

            // Auto-slide functionality
            handler = Handler(Looper.getMainLooper())
            autoSlideRunnable = Runnable {
                currentPage = (currentPage + 1) % adapter.itemCount
                viewPager.setCurrentItem(currentPage, true)
                handler.postDelayed(autoSlideRunnable, slideInterval)
            }
            handler.postDelayed(autoSlideRunnable, slideInterval)

            // Pause auto-slide during user interaction
            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageScrollStateChanged(state: Int) {
                    if (state == ViewPager2.SCROLL_STATE_IDLE) {
                        handler.postDelayed(autoSlideRunnable, slideInterval)
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
