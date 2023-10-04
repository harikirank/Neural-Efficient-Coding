package com.example.neuralefficientcodepoc

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.neuralefficientcodepoc.databinding.ActivityMainBinding
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer



class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var selectedBitmap: Bitmap
    val MANAGE_EXTERNAL_STORAGE_REQUEST = 124
    private val REQUEST_PERMISSIONS_CODE = 123 // Request code for permission request
    private var imagePath: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val mainViewModel by viewModels<MainViewModel>()

        // --------------------------------------Permissions----------------------------------------
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                // Permission is already granted
                // Proceed with your image selection code
            } else {
                // Request the permission
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                startActivityForResult(intent, MANAGE_EXTERNAL_STORAGE_REQUEST)
            }
        } else {
            // For devices running Android 10 (API 29) and below
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    MANAGE_EXTERNAL_STORAGE_REQUEST
                )
            } else {
                // Permission is already granted
                // Proceed with your image selection code
            }
        }
        // -----------------------------------------------------------------------------------------

        //--------------------------------------------------------------------------------------------------
        // Check if permissions are required and not granted
        val readPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        val writePermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        if (!readPermissionGranted || !writePermissionGranted) {
            // Request both permissions
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                REQUEST_PERMISSIONS_CODE
            )
        } else {
            // Both permissions are already granted or not required, you can proceed with file access
        }
        // request read image permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request the permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_PERMISSIONS_CODE
            )
        } else {
            // Permission is already granted or not required, you can proceed with file access
        }
        // -----------------------------------------------------------------------------------------

        binding.btnCaptureImage.setOnClickListener {
            dispatchCaptureImageIntent()
        }

        binding.btnSelectImage.setOnClickListener {
            dispatchPickImageIntent()
        }

        binding.btnProcess.setOnClickListener {
            if (::selectedBitmap.isInitialized) {
                Toast.makeText(this, "Please Wait!", Toast.LENGTH_SHORT).show()

                binding.btnCaptureImage.isEnabled = false
                binding.btnSelectImage.isEnabled = false
                binding.btnProcess.isEnabled = false


                mainViewModel.processImage(
                    imagePath!!)
            } else {
                Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
            }
        }

        mainViewModel.outputProcessedImagePath.observe(this) {
            val intent = Intent(this, ShowICAPatchesActivity::class.java)
            intent.putExtra("output_image_path", it)
            startActivity(intent)
//            binding.ivShowSelectedImage.setImageBitmap(it)

            Toast.makeText(this, "$it", Toast.LENGTH_SHORT).show()
            binding.btnCaptureImage.isEnabled = true
            binding.btnSelectImage.isEnabled = true
            binding.btnProcess.isEnabled = true
        }

    }

    private fun dispatchCaptureImageIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            // Handle the case where the camera app is not available
        }
    }

    private fun dispatchPickImageIntent() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        try {
            startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
        } catch (e: ActivityNotFoundException) {
            // Handle the case where the camera app is not available
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Toast.makeText(this, data.data.toString(), Toast.LENGTH_SHORT).show()
            if (data.data != null) {
                val selectedImage: Uri = data.data!!
                val inputStream = contentResolver.openInputStream(selectedImage)
                selectedBitmap = BitmapFactory.decodeStream(inputStream)
                imagePath = getImagePathFromUri(data.data!!)
                Toast.makeText(this, imagePath, Toast.LENGTH_SHORT).show()

                binding.ivShowSelectedImage.setImageBitmap(selectedBitmap)
            } else {
                Toast.makeText(this, "the selected image didn't give anything", Toast.LENGTH_SHORT)
                    .show()
            }
            // Use the imageBitmap to display or save the captured image
        }

        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Toast.makeText(this, data.data.toString(), Toast.LENGTH_SHORT).show()
            if (data.data != null) {
                val selectedImage: Uri = data.data!!
                val inputStream = contentResolver.openInputStream(selectedImage)
                selectedBitmap = BitmapFactory.decodeStream(inputStream)
                imagePath = getImagePathFromUri(data.data!!)
                Toast.makeText(this, imagePath, Toast.LENGTH_SHORT).show()

                inputStream?.close()

                binding.ivShowSelectedImage.setImageBitmap(selectedBitmap)
            } else {
                Toast.makeText(this, "the selected image didn't give anything", Toast.LENGTH_SHORT)
                    .show()
            }

        }
    }

    private fun getImagePathFromUri(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)

        cursor?.use {
            val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            it.moveToFirst()
            return it.getString(columnIndex)
        }

        return null
    }

    // Handle the permission request result
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray,
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        when (requestCode) {
//            REQUEST_PERMISSIONS_CODE -> {
//                // Check if all requested permissions were granted
//                val allPermissionsGranted =
//                    grantResults.all { it == PackageManager.PERMISSION_GRANTED }
//                if (allPermissionsGranted) {
//                    // Both permissions granted, you can proceed with file access
//                    Toast.makeText(this, "all perms accepted", Toast.LENGTH_SHORT).show()
//                } else {
//                    // Permissions denied, handle it accordingly (e.g., show a message)
//                    Toast.makeText(this, "permiss not handled", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == MANAGE_EXTERNAL_STORAGE_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can now proceed with image selection
            } else {
                // Permission denied, handle it gracefully or inform the user
            }
        }
    }

}