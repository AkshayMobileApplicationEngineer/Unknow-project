package com.app.aamdani

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class ProfileActivity : AppCompatActivity() {
    private lateinit var optionQr: LinearLayout
    private val IMAGE_PICK_CODE = 1000
    private val STORAGE_PERMISSION_CODE = 1001
    private val INTERNET_PERMISSION_CODE = 1002
    private val NOTIFICATION_PERMISSION_CODE = 1003

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        initializeViewSetUp()
        initializeViewAction()
    }

    private fun initializeViewAction() {
        optionQr.setOnClickListener {
            uploadTheQRCode()
        }
    }

    private fun initializeViewSetUp() {
        optionQr = findViewById(R.id.option_qr)
    }

    private fun uploadTheQRCode() {
        Log.e("ProfileActivity", "uploadTheQRCode: Started")
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_upload_qr, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .show()

        val qrImageView = dialogView.findViewById<ImageView>(R.id.qr_image_view)
        val uploadButton = dialogView.findViewById<Button>(R.id.button_upload_qr)
        val cancelButton = dialogView.findViewById<Button>(R.id.button_cancel)

        uploadButton.setOnClickListener {
            Log.e("ProfileActivity", "uploadButton clicked")
            // Check for permission before opening the file picker
            if (checkStoragePermission()) {
                // Open the file picker to select an image
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*" // Allow only image files to be picked
                startActivityForResult(intent, IMAGE_PICK_CODE)
            } else {
                // Request permission if not granted
                requestStoragePermission()
            }
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
            Log.e("ProfileActivity", "Dialog dismissed")
        }
    }

    private fun checkStoragePermission(): Boolean {
        Log.e("ProfileActivity", "checkStoragePermission: Checking")
        return ContextCompat.checkSelfPermission(
            this, android.Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        Log.e("ProfileActivity", "requestStoragePermission: Requested")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Check if the user has denied the permission permanently
            if (shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Show an explanation to the user
                Toast.makeText(this, "Storage permission is required to select a QR code image.", Toast.LENGTH_SHORT).show()
                ActivityCompat.requestPermissions(
                    this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    STORAGE_PERMISSION_CODE
                )
            } else {
                // Permission was denied permanently, open the app settings
                Toast.makeText(this, "Permission Denied permanently. Go to Settings to enable it.", Toast.LENGTH_LONG).show()
                openAppSettings()
            }
        } else {
            // For devices below Android M, no need to check for permissions
            ActivityCompat.requestPermissions(
                this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE
            )
        }
    }

    private fun openAppSettings() {
        Log.e("ProfileActivity", "openAppSettings: Opening settings")
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    // Handling the result of the file pick
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            data?.data?.let { imageUri ->
                Log.e("ProfileActivity", "Image picked: $imageUri")
                uploadImageInLocalFile(imageUri)
            }
        }
    }

    // Function to upload image to local storage with a unique file name
    private fun uploadImageInLocalFile(imageUri: Uri) {
        Log.e("ProfileActivity", "uploadImageInLocalFile: Started")
        val inputStream: InputStream? = contentResolver.openInputStream(imageUri)
        val fileName = "uploaded_image_${System.currentTimeMillis()}.jpg" // Unique file name
        val file = File(filesDir, fileName)
        val outputStream = FileOutputStream(file)

        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }

        // Optionally show a toast or update UI
        Toast.makeText(this, "Image uploaded to local storage: $fileName", Toast.LENGTH_SHORT).show()
        Log.e("ProfileActivity", "Image saved as: $fileName")
    }

    // Handle permission request result
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, open the file picker
                Log.e("ProfileActivity", "Storage permission granted")
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                startActivityForResult(intent, IMAGE_PICK_CODE)
            } else {
                // Permission denied, show a message
                Log.e("ProfileActivity", "Storage permission denied")
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                // Optionally, open app settings if permission is permanently denied
                val shouldShowRationale =
                    shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                if (!shouldShowRationale) {
                    // User has denied permission permanently, show message to go to settings
                    Toast.makeText(
                        this, "You have denied storage permission permanently. Go to settings to enable it.",
                        Toast.LENGTH_LONG
                    ).show()
                    openAppSettings()
                }
            }
        }
    }

    // Request other permissions
    private fun requestOtherPermissions() {
        Log.e("ProfileActivity", "requestOtherPermissions: Requesting additional permissions")
        ActivityCompat.requestPermissions(
            this, arrayOf(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.INTERNET,
                android.Manifest.permission.ACCESS_NETWORK_STATE,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.POST_NOTIFICATIONS
            ), 0
        )
    }
}
