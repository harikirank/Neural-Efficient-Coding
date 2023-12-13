package com.example.neuralefficientcodepoc

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.neuralefficientcodepoc.databinding.ActivityImageOrSoundSelectionBinding
import com.example.neuroscience.aboutAppPage1
import com.example.neuroscience.appInfo

class ImageOrSoundSelectionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityImageOrSoundSelectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_image_or_sound_selection)

        binding.buttonImages.setOnClickListener {
            val intent = Intent(this@ImageOrSoundSelectionActivity, SelectImageGalleryOrCapture::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            finish()
        }

        binding.buttonSounds.setOnClickListener {
            //Toast.makeText(this, "sound Button clicked", Toast.LENGTH_SHORT).show();
            val soundActivityIntent = Intent(this@ImageOrSoundSelectionActivity, SoundActivity::class.java)
            startActivity(soundActivityIntent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            finish()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.info_menu, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.aboutAppBtn -> {
                val aboutAppPage1Intent = Intent(this@ImageOrSoundSelectionActivity, aboutAppPage1::class.java)
                startActivity(aboutAppPage1Intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                true
            }
            R.id.appInfoBtn -> {
                val appInfoIntent = Intent(this@ImageOrSoundSelectionActivity, appInfo::class.java)
                startActivity(appInfoIntent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }
}