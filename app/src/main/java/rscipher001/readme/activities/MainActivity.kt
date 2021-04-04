package rscipher001.readme.activities

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import rscipher001.readme.R
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private val galleryIntent: Int = 123
    private val cropIntent: Int = 95

    private lateinit var sharedPref: SharedPreferences
    private lateinit var photoURI: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Shared preferences manager
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this)

        if (Intent.ACTION_SEND == intent.action && intent.type != null) {
            if (intent.type.startsWith("image/")) {
                val intent = CropImage.activity(intent.getParcelableExtra(Intent.EXTRA_STREAM) as Uri)
                        .getIntent(this)
                startActivityForResult(intent, cropIntent)
            }
        }

        //Gallery Button
        galleryButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"

            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(intent, galleryIntent)
            } else {
                toast("You don 't have a Gallery App.")
            }
        }

        //Detect Button
        detectButton.setOnClickListener {
            startActivity<DetectActivity>("data" to photoURI.toString())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_activity_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {

            R.id.history -> {
                startActivity<HistoryActivity>()
                true
            }

            R.id.exit -> {
                toast("Have a nice day!.")
                finish()
                true
            }


            else -> super.onOptionsItemSelected(item)
        }
    }


    //Handle Activity Result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                galleryIntent -> {
                    onSelectImageFromGalleryResult(data)
                    return
                }

                cropIntent -> {
                    onCropResult(data)
                }
            }
        } else {
            toast("Something went wrong")
        }
    }

    //On Getting Image from Gallery
    private fun onSelectImageFromGalleryResult(data: Intent?) {
        if (data != null) {
            try {
                photoURI = data.data as Uri
                val intent = CropImage.activity(photoURI)
                        .setInitialCropWindowPaddingRatio(0F)
                        .getIntent(this)
                Glide.with(this).load(photoURI).into(thumbnail)
                detectButton.visibility = View.VISIBLE
                startActivityForResult(intent, cropIntent)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun onCropResult(data: Intent?) {
        val result = CropImage.getActivityResult(data)
        photoURI = result.uri
        Glide.with(this).load(result.uri).into(thumbnail)
        detectButton.visibility = View.VISIBLE
    }
}
