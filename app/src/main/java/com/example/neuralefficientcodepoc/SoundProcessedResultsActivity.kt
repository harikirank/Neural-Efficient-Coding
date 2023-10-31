package com.example.neuralefficientcodepoc

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.neuralefficientcodepoc.databinding.ActivitySoundProcessedResultsNewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class SoundProcessedResultsActivity : AppCompatActivity() {
    var progressDialog: ProgressDialog? = null
    private lateinit var binding: ActivitySoundProcessedResultsNewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =
            DataBindingUtil.setContentView(this, R.layout.activity_sound_processed_results_new)
        val audioViewModel by viewModels<SoundProcessedResultsViewModel>()

        hideActionBar()

        binding.resultImageHomeBtn.setOnClickListener {
            val homePageIntent = Intent(this,
                ImageOrSoundSelectionActivity::class.java)
            startActivity(homePageIntent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        binding.resultImageBackBtn.setOnClickListener {
            val audioListPageIntent = Intent(this, audioList::class.java)
            startActivity(audioListPageIntent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }


        //Intent intent = getIntent();
        //File file = intent.get
        val file = intent.getSerializableExtra("fileToPlay") as File?
        val filePath = file?.absolutePath
        progressDialog = ProgressDialog(this)
        progressDialog?.show()
        progressDialog?.setContentView(R.layout.progress_dialog)
        progressDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        progressDialog?.setCancelable(false)
        CoroutineScope(Dispatchers.Main).launch {
            audioViewModel.processAudio(
                filePath!!)
        }

        audioViewModel.outputProcessedAudioImagePath.observe(this) {
            progressDialog?.dismiss()

            val audioImageOutputPath = it

            val imageFile = File(audioImageOutputPath)
            if (imageFile.exists()) {
                // Decode the image file into a Bitmap
                val bitmap = BitmapFactory.decodeFile(audioImageOutputPath)

                // Set the Bitmap to the ImageView
                binding.audioProcessedImage.setImageBitmap(bitmap)
//                binding.ivShowIcaPatches.scaleX = binding.ivShowIcaPatches.scaleX + 0.8f
//                binding.ivShowIcaPatches.scaleY = binding.ivShowIcaPatches.scaleY + 0.8f
//            binding.ivShowIcaPatches.scaleType = ImageView.ScaleType.FIT_CENTER

            } else {
                // Handle the case where the file does not exist
                // For example, you can show a placeholder image or a message
            }
        }
    }

    private fun hideActionBar() {
        supportActionBar?.hide()
    }
}