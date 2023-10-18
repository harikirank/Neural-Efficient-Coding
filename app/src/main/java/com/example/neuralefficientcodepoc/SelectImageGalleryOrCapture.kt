package com.example.neuralefficientcodepoc

import android.Manifest
import android.app.ProgressDialog
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
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.example.neuralefficientcodepoc.databinding.ActivitySelectImageGalleryOrCaptureBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

const val REQUEST_IMAGE_CAPTURE = 1
const val REQUEST_CODE_PICK_IMAGE = 2

class SelectImageGalleryOrCapture : AppCompatActivity() {
    private lateinit var binding: ActivitySelectImageGalleryOrCaptureBinding
    private lateinit var selectedBitmap: Bitmap
    val MANAGE_EXTERNAL_STORAGE_REQUEST = 124
    var progressDialog: ProgressDialog? = null
    private var mResultsBitmap: Bitmap? = null
    var photoPreview: ImageView? = null
    private var currentPhotoPath: String? = null
    private var mAppExecutor: AppExecutor? = null
    private var REQUEST_IMAGE_CAPTURE = 389
    private var REQUEST_STORAGE_PERMISSION = 390
    private val REQUEST_PERMISSIONS_CODE = 123 // Request code for permission request
    private var imagePath: String? = null
    private lateinit var progressBarDialogFragment: ProgressBarDialogFragment
    private val FILE_PROVIDER_AUTHORITY = "com.example.neuralefficientcodepoc.fileprovider"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =
            DataBindingUtil.setContentView(this, R.layout.activity_select_image_gallery_or_capture)
        hideActionBar()
        val mainViewModel by viewModels<SelectImageGalleryOrCaptureViewModel>()
        mAppExecutor = AppExecutor()


        binding.captureImage.setOnClickListener {
            dispatchCaptureImageIntent()
        }

        binding.buttonSelectImage.setOnClickListener {
            dispatchPickImageIntent()
        }

        binding.buttonProcessImage.setOnClickListener {
            if (::selectedBitmap.isInitialized) {
                // progressBarDialogFragment = ProgressBarDialogFragment()
                // progressBarDialogFragment.show(supportFragmentManager, "progressDialog")
                // or
                progressDialog = ProgressDialog(this)
                progressDialog?.show()
                progressDialog?.setContentView(R.layout.progress_dialog)
                progressDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
                progressDialog?.setCancelable(false)
                // Start a Coroutine to perform the time-consuming task
                CoroutineScope(Dispatchers.IO).launch {
                    mainViewModel.processImage(
                        imagePath!!)
                }
            } else {
                Toast.makeText(this, "Please select an image!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonTakePicture.setOnClickListener {
                launchCamera()
        }

        mainViewModel.outputProcessedImagePath.observe(this) {
            progressDialog?.dismiss()

            val intent = Intent(this, ShowICAPatchesActivity::class.java)
            intent.putExtra("output_image_path", it)
            startActivity(intent)

            binding.captureImage.isEnabled = true
            binding.buttonSelectImage.isEnabled = true
            binding.processImage.isEnabled = true
        }

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
    }

    private fun launchCamera() {
        // Create the capture image intent
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

//        if (takePictureIntent.resolveActivity(packageManager) != null) {
            // Create the temporary File where the photo should go
            var photoFile: File? = null
            try {
                photoFile = BitmapUtils.createTempImageFile(this)
            } catch (ex: IOException) {
                // Error occurred while creating the File
                ex.printStackTrace()
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {

                // Get the path of the temporary file
                currentPhotoPath = photoFile.absolutePath

                // Get the content URI for the image file
                val photoURI = FileProvider.getUriForFile(this,
                    FILE_PROVIDER_AUTHORITY,
                    photoFile)


                // Add the URI so the camera can store the image
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)

                // Launch the camera activity
                startActivityForResult(takePictureIntent,
                    REQUEST_IMAGE_CAPTURE)
            }
//        }
    }

    private fun hideActionBar() {
        supportActionBar?.hide()
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
            processAndSetImage()
            /*Toast.makeText(this, data.data.toString(), Toast.LENGTH_SHORT).show()
            if (data.data != null) {
                val selectedImage: Uri = data.data!!
                val inputStream = contentResolver.openInputStream(selectedImage)
                selectedBitmap = BitmapFactory.decodeStream(inputStream)
                imagePath = getImagePathFromUri(data.data!!)
                Toast.makeText(this, imagePath, Toast.LENGTH_SHORT).show()

                binding.imageSelectedImagePreview.setImageBitmap(selectedBitmap)
                showProcessButtonAndText()
            } else {
                Toast.makeText(this, "the selected image didn't give anything", Toast.LENGTH_SHORT)
                    .show()
            }*/
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

                binding.imageSelectedImagePreview.setImageBitmap(selectedBitmap)
                showProcessButtonAndText()
            } else {
                Toast.makeText(this, "the selected image didn't give anything", Toast.LENGTH_SHORT)
                    .show()
            }

        }
        binding.buttonProcessImage.visibility = View.VISIBLE
        binding.processImage.visibility = View.VISIBLE
        binding.textViewCloser.visibility = View.VISIBLE
    }

    private fun processAndSetImage() {
        // Resample the saved image to fit the ImageView
        mResultsBitmap = BitmapUtils.resamplePic(this, currentPhotoPath)
        selectedBitmap = BitmapFactory.decodeFile(currentPhotoPath)

        // Set the new bitmap to the ImageView

        // Set the new bitmap to the ImageView
        binding.imageSelectedImagePreview.setImageBitmap(mResultsBitmap)


        //Save Image!!!
        mAppExecutor?.diskIO()?.execute {

            // Delete the temporary image file
            BitmapUtils.deleteImageFile(this, currentPhotoPath)

            // Save the image
            imagePath = BitmapUtils.saveImage(this, mResultsBitmap)
        }

        Toast.makeText(this, "Saved the image", Toast.LENGTH_SHORT).show()

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray,
    ) {
        // Called when you request permission to read and write to external storage
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_STORAGE_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // If you get permission, launch the camera
                    launchCamera()
                } else {
                    // If you do not get permission, show a Toast
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showProcessButtonAndText() {
        binding.processImage.visibility = View.VISIBLE
        binding.buttonProcessImage.visibility = View.VISIBLE
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


}