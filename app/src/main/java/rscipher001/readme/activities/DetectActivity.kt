package rscipher001.readme.activities

import android.content.ClipData
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import kotlinx.android.synthetic.main.activity_detect.*
import org.jetbrains.anko.clipboardManager
import org.jetbrains.anko.db.insert
import org.jetbrains.anko.toast
import rscipher001.readme.R
import rscipher001.readme.extra.database


class DetectActivity : AppCompatActivity() {

    private lateinit var shareIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detect)

        decode(MediaStore.Images.Media.getBitmap(this.contentResolver,
                Uri.parse(intent.getStringExtra("data"))))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.detect_activity_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {

            R.id.copy -> {
                clipboardManager.primaryClip = ClipData.newPlainText("Detected Text",
                        detected_text.text.toString())
                toast("Copied")
                return true
            }

            R.id.share -> {
                shareIntent = Intent()
                shareIntent.putExtra(Intent.EXTRA_TEXT, detected_text.text.toString())
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.type = "text/plain"
                if (shareIntent.resolveActivity(packageManager) != null) {
                    startActivity(shareIntent)
                }
                return true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun decode(bmp: Bitmap) {
        val img: FirebaseVisionImage = FirebaseVisionImage.fromBitmap(bmp)
        processTextRecognition(img)
        processQRDetection(img)
    }

    private fun processTextRecognition(img: FirebaseVisionImage) {

        val stringBuffer = StringBuffer()
        val ocrDetector = FirebaseVision.getInstance()
                .onDeviceTextRecognizer

        ocrDetector.processImage(img)
                .addOnSuccessListener { vt ->
                    vt.textBlocks.forEach { tb ->
                        tb.lines.forEach { l ->
                            l.elements.forEach { e ->
                                stringBuffer.append(e.text)
                                stringBuffer.append(" ")
                            }
                        }

                    }

                    if (stringBuffer.toString() == "") {
                        no_text.visibility = View.VISIBLE
                        detected_text.visibility = View.GONE

                    } else {
                        detected_text.text = stringBuffer.toString()
                        database.writableDatabase.insert("History",
                                "text" to stringBuffer.toString())
                    }
                }

    }

    private fun processQRDetection(img: FirebaseVisionImage) {

        val qrOptions = FirebaseVisionBarcodeDetectorOptions.Builder()
                .setBarcodeFormats(
                        FirebaseVisionBarcode.FORMAT_ALL_FORMATS)
                .build()

        val qrDetector = FirebaseVision.getInstance().getVisionBarcodeDetector(qrOptions)

        qrDetector.detectInImage(img)
                .addOnSuccessListener {
                    for (barcode in it) {

                        detected_text.text = barcode.rawValue

                        when (barcode.valueType) {
                            FirebaseVisionBarcode.TYPE_CONTACT_INFO -> barcode.contactInfo
                            FirebaseVisionBarcode.TYPE_CALENDAR_EVENT -> barcode.calendarEvent
                            FirebaseVisionBarcode.TYPE_DRIVER_LICENSE -> barcode.driverLicense
                            FirebaseVisionBarcode.TYPE_TEXT -> barcode.displayValue
                            FirebaseVisionBarcode.TYPE_EMAIL -> barcode.email
                            FirebaseVisionBarcode.TYPE_GEO -> barcode.geoPoint
                            FirebaseVisionBarcode.TYPE_URL -> barcode.url
                            FirebaseVisionBarcode.TYPE_WIFI -> barcode.wifi
                        }
                    }
                }

                .addOnFailureListener {
                    it.printStackTrace()
                }
    }
}
