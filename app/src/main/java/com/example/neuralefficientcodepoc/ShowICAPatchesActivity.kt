package com.example.neuralefficientcodepoc

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.neuralefficientcodepoc.databinding.ActivityShowIcapatchesBinding
import java.io.File

class ShowICAPatchesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityShowIcapatchesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_show_icapatches)
        hideActionBar()

        val outputImagePath = intent.getStringExtra("output_image_path") ?: ""

        val file = File(outputImagePath)
        if (file.exists()) {
            // Decode the image file into a Bitmap
            val bitmap = BitmapFactory.decodeFile(outputImagePath)

            // Set the Bitmap to the ImageView
            binding.ivShowIcaPatches.setImageBitmap(bitmap)
            binding.ivShowIcaPatches.scaleX = binding.ivShowIcaPatches.scaleX + 0.8f
            binding.ivShowIcaPatches.scaleY = binding.ivShowIcaPatches.scaleY + 0.8f
//            binding.ivShowIcaPatches.scaleType = ImageView.ScaleType.FIT_CENTER

        } else {
            // Handle the case where the file does not exist
            // For example, you can show a placeholder image or a message
        }
    }

    private fun hideActionBar() {
        supportActionBar?.hide()
    }
}