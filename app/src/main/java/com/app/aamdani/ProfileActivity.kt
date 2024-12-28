package com.app.aamdani

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast

import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
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
    private val WRITE_STORAGE_PERMISSION_CODE = 1002

    // Permission requesters for the Activity Result API
    private val storagePermissionRequest = registerForActivityResult(RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            Log.e("ProfileActivity", "Storage permission granted")
            openFilePicker()
        } else {
            Log.e("ProfileActivity", "Storage permission denied")
            showPermissionDeniedDialog()
        }
    }

    private val writeStoragePermissionRequest = registerForActivityResult(RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            Log.e("ProfileActivity", "Write Storage permission granted")
        } else {
            Log.e("ProfileActivity", "Write Storage permission denied")
            showPermissionDeniedDialog()
        }
    }

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

        val uploadButton = dialogView.findViewById<Button>(R.id.button_upload_qr)
        val cancelButton = dialogView.findViewById<Button>(R.id.button_cancel)

        uploadButton.setOnClickListener {
            Log.e("ProfileActivity", "uploadButton clicked")
            // Check for permission before opening the file picker
            if (checkStoragePermission()) {
                openFilePicker()
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
            this, Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Check if the user has denied the permission permanently
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                showPermissionExplanationDialog()
            } else {
                // Permission was denied permanently, open the app settings
                showPermissionDeniedDialog()
            }
        } else {
            // For devices below Android M, no need to check for permissions
            storagePermissionRequest.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun showPermissionExplanationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("We need access to storage for QR code uploads.")
            .setTitle("Permission Required")
            .setCancelable(false)
            .setPositiveButton("Allow") { _, _ ->
                storagePermissionRequest.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            .setNegativeButton("Deny") { dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()
    }

    private fun showPermissionDeniedDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("You have denied storage permission. Please enable it in Settings.")
            .setCancelable(false)
            .setPositiveButton("Go to Settings") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*" // Allow only image files to be picked
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    private fun openAppSettings() {
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            data?.data?.let { imageUri ->
                Log.e("ProfileActivity", "Image picked: $imageUri")
                uploadImageInLocalFile(imageUri)
            }
        }
    }

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
                openFilePicker()
            } else {
                // Permission denied, show a message
                Log.e("ProfileActivity", "Storage permission denied")
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Request other permissions (Write permission)
    private fun requestWriteStoragePermission() {
        writeStoragePermissionRequest.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
}
