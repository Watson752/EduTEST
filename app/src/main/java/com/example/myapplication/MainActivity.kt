package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.InputStream
import android.content.pm.PackageManager

class MainActivity : AppCompatActivity() {

    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private var selectedImageUri: Uri? = null
    private lateinit var avatarImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.avatar)  // This sets the content to your avatar.xml layout

        avatarImageView = findViewById(R.id.avatar_image)  // Reference to the ImageView

        // Initialize the image picker launcher
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                selectedImageUri = data?.data // Get the selected image URI
                selectedImageUri?.let { uri ->
                    val bitmap = uri.toBitmap()
                    avatarImageView.setImageBitmap(bitmap) // Set the selected image as the avatar
                }
            }
        }

        // Set up click listener for the ImageView to open the image picker
        avatarImageView.setOnClickListener {
            if (hasStoragePermission()) {
                openImagePicker()
            } else {
                requestStoragePermission()
            }
        }

        // Request permissions for camera and storage (if needed)
        requestPermissions()
    }

    // Open the image picker to select an image
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"  // Only show images
        }
        imagePickerLauncher.launch(intent)  // Launch the image picker
    }

    // Convert URI to Bitmap
    private fun Uri.toBitmap(): Bitmap {
        val inputStream: InputStream? = contentResolver.openInputStream(this)
        return BitmapFactory.decodeStream(inputStream)
    }

    // Check if the app has permission to read external storage
    private fun hasStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Request permission to read external storage
    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            REQUEST_STORAGE_PERMISSION
        )
    }

    // Handle the result of the permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                openImagePicker()
            } else {
                Toast.makeText(this, "Permission denied. Cannot select image.", Toast.LENGTH_SHORT).show()
                // Optional: Direct the user to app settings
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            }
        }
    }

    companion object {
        private const val REQUEST_STORAGE_PERMISSION = 1001
    }

    // Optional: Request permissions for camera and storage (if needed)
    private fun requestPermissions() {
        if (!hasStoragePermission()) {
            requestStoragePermission()
        }
    }
}
