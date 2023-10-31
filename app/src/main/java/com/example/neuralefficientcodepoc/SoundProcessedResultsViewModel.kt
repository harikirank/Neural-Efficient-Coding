package com.example.neuralefficientcodepoc

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*

class SoundProcessedResultsViewModel(application: Application) :
    AndroidViewModel(application) {
    private val _outputProcessedAudioImagePath = MutableLiveData<String>()
    val outputProcessedAudioImagePath: LiveData<String>
        get() = _outputProcessedAudioImagePath

    private val app = application as MyApp

    fun processAudio(audioPath: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val py = app.pythonInstance

            val audioProcessorModule = py.getModule("multimodal_efficient_coding_integration")
            val outputAudioPathAfterProcessingAudio = audioProcessorModule.callAttr("process_audio", audioPath)

            withContext(Dispatchers.Main) {
                if (outputAudioPathAfterProcessingAudio != null) {
                    _outputProcessedAudioImagePath.value = outputAudioPathAfterProcessingAudio.toString()
                }
            }
        }
    }
}