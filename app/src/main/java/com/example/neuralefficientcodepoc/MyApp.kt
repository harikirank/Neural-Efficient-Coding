package com.example.neuralefficientcodepoc

import android.app.Application
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform

class MyApp : Application() {
    lateinit var pythonInstance: Python
    override fun onCreate() {
        super.onCreate()
        Python.start(AndroidPlatform(this))
        pythonInstance = Python.getInstance()
    }
}