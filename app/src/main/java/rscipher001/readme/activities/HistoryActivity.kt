package rscipher001.readme.activities


import android.content.ClipData
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_history.*
import org.jetbrains.anko.clipboardManager
import org.jetbrains.anko.db.StringParser
import org.jetbrains.anko.db.parseList
import org.jetbrains.anko.db.select
import org.jetbrains.anko.toast
import rscipher001.readme.R
import rscipher001.readme.extra.database

class HistoryActivity : AppCompatActivity() {

    private lateinit var db: SQLiteDatabase
    private lateinit var list: List<String>

    private lateinit var shareIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        db = database.writableDatabase

        db.select("History", "text").exec {
            list = parseList(StringParser)
        }

        val adapter = ArrayAdapter<String>(this,
                R.layout.history_item, list)

        history_list.adapter = adapter

        registerForContextMenu(history_list)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.history_activity_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {

            R.id.clear_history -> {
                toast("Done")
                db.execSQL("delete from History")
                root_activity_history.removeAllViews()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        menuInflater.inflate(R.menu.history_activity_context_menu, menu)
        super.onCreateContextMenu(menu, v, menuInfo)
    }

    override fun onContextItemSelected(item: MenuItem?): Boolean {

        val info = item?.menuInfo as AdapterView.AdapterContextMenuInfo

        return when (item.itemId) {
            R.id.copy_context -> {
                clipboardManager.primaryClip = ClipData.newPlainText("Detected Text", list[info.position])
                toast("Copied")
                return true
            }

            R.id.share_context -> {
                shareIntent = Intent()
                shareIntent.putExtra(Intent.EXTRA_TEXT, list[info.position])
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.type = "text/plain"
                if (shareIntent.resolveActivity(packageManager) != null) {
                    startActivity(shareIntent)
                }
                return true
            }
            else -> super.onContextItemSelected(item)
        }
    }
}
