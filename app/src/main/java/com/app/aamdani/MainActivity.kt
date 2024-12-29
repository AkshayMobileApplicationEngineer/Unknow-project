package com.app.aamdani

import ImageSliderAdapter
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.health.connect.datatypes.ExerciseRoute
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat.getCurrentLocation
import androidx.viewpager2.widget.ViewPager2

import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
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

    companion object {
        private const val LOCATION_PERMISSIONS_CODE = 1001
        private const val videolink="https://workholicpraveen.in/Quiz%20Parlour.mp4"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
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


        // Download the video
        downloadVideo(videolink)

        // Check if GPS is enabled and show Toast
        checkGpsStatus()

        // // If you want to show the current location
        getCurrentLocation()
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
    // Function to download the video
    private fun downloadVideo(url: String) {
        val request = DownloadManager.Request(Uri.parse(url))
        request.setTitle("Quiz Parlour Video") // You can set a custom title
        request.setDescription("Downloading quiz video...")
        Log.e("downloadVideo", "url: $url")
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        request.setAllowedOverRoaming(false)

        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_MOVIES, "Ads/CureentAdsVideo.mp4")

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
    }

    private fun checkGpsStatus() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        Log.e("checkGpsStatus", "isGpsEnabled: $isGpsEnabled")
        if (!isGpsEnabled) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Enable GPS")
            builder.setMessage("GPS is disabled. Do you want to enable it?")
            builder.setPositiveButton("Yes") { _, _ ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
            builder.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            builder.show()
        }
    }




    private fun getCurrentLocation() {
        Log.e("getCurrentLocation", "getCurrentLocation called")
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Check if GPS is enabled
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Please enable location services", Toast.LENGTH_SHORT).show()
            Log.e(
                "getCurrentLocation",
                "GPS Provider Enabled: ${locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)}"
            )
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
            return
        }

        // Check for location permission
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("getCurrentLocation", "Permission not granted, requesting permissions")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
            }
        } else {
            Log.e("getCurrentLocation", "Permissions granted, starting location updates")
            startLocationUpdates()
        }
    }

    private fun startLocationUpdates() {
        try {
            // Request location updates
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000L, // Minimum time interval between updates (in milliseconds)
                10f // Minimum distance between updates (in meters)
            ) { location: Location ->
                Log.e("startLocationUpdates", "Location received: $location")
                Log.e("startLocationUpdates", "Latitude: ${location.latitude}, Longitude: ${location.longitude}")


                //How to get current location
                val latitude = location.latitude
                val longitude = location.longitude
                sendTotheFirebase(latitude, longitude)
//                Toast.makeText(
//                    this,
//                    "Latitude: ${location.latitude}, Longitude: ${location.longitude}",
//                    Toast.LENGTH_SHORT
//                ).show()
            }
        } catch (e: SecurityException) {
            Log.e("startLocationUpdates", "SecurityException: ${e.message}")
        }
    }




    //Handle the result of the permission request.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()

            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //Send the location to the firebase
    private fun sendTotheFirebase(latitude: Double, longitude: Double) {
        Toast.makeText(this@MainActivity, "Latitude: $latitude, Longitude: $longitude", Toast.LENGTH_SHORT).show()
    }
    
   


}

