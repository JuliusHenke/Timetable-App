package juliushenke.smarttt

import android.graphics.Color
import android.os.Build
import android.view.WindowManager
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import juliushenke.smarttt.MainActivity.Companion.selectedDayOfWeek
import juliushenke.smarttt.MainActivity.Companion.selectedEvenWeeks
import juliushenke.smarttt.MainActivity.Companion.weekIsOdd
import juliushenke.smarttt.model.Settings
import java.io.*

internal class Util {
    fun updateDesign(a: AppCompatActivity, homeAsUp: Boolean) {
        val scrollView = a.findViewById<ScrollView>(R.id.scrollview)
        val toolbar = a.findViewById<Toolbar>(R.id.app_bar)
        toolbar.title = ""
        try {
            scrollView.setBackgroundResource(R.drawable.background_gradient)
            toolbar.setBackgroundResource(R.color.colorAppBar)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        a.setSupportActionBar(toolbar)
        try {
            if (homeAsUp && a.supportActionBar != null) {
                a.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = a.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = a.resources.getColor(R.color.colorStatusBar)
        }
    }

    fun readSettings(a: AppCompatActivity): Settings {
        var settings = Settings()
        try {
            val input = ObjectInputStream(
                FileInputStream(
                    File(
                        File(
                            a.filesDir,
                            ""
                        ).toString() + File.separator + "SETTINGS.srl"
                    )
                )
            )
            settings = input.readObject() as Settings
        } catch (e: FileNotFoundException) {
            saveSettings(a, settings)
            e.printStackTrace()
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return settings
    }

    fun isFirstTimeRunning(a: AppCompatActivity): Boolean {
        val testFile = File(File(a.filesDir, "").toString() + File.separator + "SETTINGS.srl")
        return !testFile.exists()
    }

    fun saveSettings(a: AppCompatActivity, settings: Settings?) {
        try {
            val out: ObjectOutput = ObjectOutputStream(
                FileOutputStream(
                    File(
                        a.filesDir,
                        ""
                    ).toString() + File.separator + "SETTINGS.srl"
                )
            )
            out.writeObject(settings)
            out.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun getFilenameForDay(a: AppCompatActivity): String {
        val settings = readSettings(a)
        if (settings.weekSystem) {
            if (MainActivity.isEditingMode && !selectedEvenWeeks) return "ODD-DAY-$selectedDayOfWeek.srl" else if (!MainActivity.isEditingMode && weekIsOdd) return "ODD-DAY-$selectedDayOfWeek.srl"
        }
        return "DAY-$selectedDayOfWeek.srl"
    }

    fun isIntOdd(`val`: Int): Boolean {
        return `val` and 0x01 != 0
    }

    fun isColorDark(color: Int): Boolean {
        val darkness =
            1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        return darkness >= 0.5
    }
}