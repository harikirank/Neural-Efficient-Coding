package com.example.neuralefficientcodepoc

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
//import org.opencv.core.Mat
//import org.opencv.android.Utils
import java.io.ByteArrayOutputStream

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val _outputProcessedImagePath = MutableLiveData<String>()
    val outputProcessedImagePath: LiveData<String>
        get() = _outputProcessedImagePath

    private val app = application as MyApp

    fun processImage(

        imagePath: String,
    ) {
        viewModelScope.launch {
            val py = app.pythonInstance

            val imageProcessor = py.getModule("multimodal_efficient_coding_integration")
            val outputImagePathAfterProcessing = imageProcessor.callAttr("process_image",
                imagePath
            )

            withContext(Dispatchers.Main) {
                if (outputImagePathAfterProcessing != null) {
                    _outputProcessedImagePath.value = outputImagePathAfterProcessing.toString()
                }
            }
        }
    }
}