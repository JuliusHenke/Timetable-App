package juliushenke.smarttt

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import juliushenke.smarttt.model.Subject
import java.io.*
import java.text.DateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (util.isFirstTimeRunning(this)) initialSetup()
        updateActivityMain()
        util.updateDesign(this, false)
        for (i in 1..11) {
            hourButtons[i]!!.setOnLongClickListener { v ->
                selectedSubject = getButtonName(v)
                isNewSubject = selectedSubject == "+"
                setActivitySubject()
                true
            }
        }
    }

    public override fun onResume() {
        super.onResume()
        updateActivityMain()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val item = menu.findItem(R.id.menu_editMode)
        if (isEditingMode) item.setIcon(R.drawable.ic_done_white_24dp) else item.setIcon(R.drawable.ic_edit_white_24dp)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_editMode -> {
                changeMode()
                true
            }
            R.id.menu_editSubject -> {
                dialogEditSubject().show()
                true
            }
            R.id.menu_deleteSubject -> {
                dialogDeleteSubject().show()
                true
            }
            R.id.menu_settings -> {
                setActivitySettings()
                true
            }
            else ->                 // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                super.onOptionsItemSelected(item)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isEditingMode) changeMode() else finish()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun updateActivityMain() {
        setDay(0)
    }

    private fun setActivitySettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun setActivitySubject() {
        val intent = Intent(this, SubjectActivity::class.java)
        startActivity(intent)
    }

    private fun setDay(shift: Int) {
        val settings = util.readSettings(this)

        //Mathematical calculation of the new date
        val c = Calendar.getInstance()
        savedDayChange += shift
        c.add(Calendar.DAY_OF_YEAR, savedDayChange)
        selectedDayOfWeek = getDayOfWeek(c)
        var extraDayChange = 0
        //positive change - into the future
        if (0 <= shift) {
            if (selectedDayOfWeek < settings.start_day) extraDayChange =
                settings.start_day - selectedDayOfWeek else if (selectedDayOfWeek > settings.end_day) extraDayChange =
                7 - selectedDayOfWeek + settings.start_day
        } else {
            if (selectedDayOfWeek < settings.start_day) extraDayChange =
                -selectedDayOfWeek - (7 - settings.end_day) else if (selectedDayOfWeek > settings.end_day) extraDayChange =
                settings.end_day - selectedDayOfWeek
        }
        //update the Calendar
        c.add(Calendar.DAY_OF_YEAR, extraDayChange)
        savedDayChange += extraDayChange

        //update the day_of_week
        selectedDayOfWeek = getDayOfWeek(c)
        val days = resources!!.getStringArray(R.array.days)
        val textViewDay = findViewById<TextView>(R.id.textViewDay)
        textViewDay.text = days[selectedDayOfWeek]
        val btnDateLeft = findViewById<Button>(R.id.btnDateLeft)
        val btnDateCenter = findViewById<Button>(R.id.btnDateCenter)
        val btnDateRight = findViewById<Button>(R.id.btnDateRight)
        val btnWeekType = findViewById<Button>(R.id.btnWeekType)
        val dateWeek = c[Calendar.WEEK_OF_YEAR]
        weekIsOdd = util.isIntOdd(dateWeek)

        //updating views of activity_main -------------------------------------------------------------------------------
        btnDateLeft.visibility = View.VISIBLE
        btnDateRight.visibility = View.VISIBLE
        if (isEditingMode) {
            btnDateCenter.visibility = View.INVISIBLE
            if (selectedDayOfWeek == settings.start_day) btnDateLeft.visibility =
                View.INVISIBLE
            if (selectedDayOfWeek == settings.end_day) btnDateRight.visibility =
                View.INVISIBLE
            btnDateCenter.text = ""
            if (settings.weekSystem) {
                btnDateCenter.visibility = View.GONE
                btnWeekType.visibility = View.VISIBLE
                if (selectedEvenWeeks) {
                    btnWeekType.setText(R.string.Menu_evenWeek)
                } else {
                    btnWeekType.setText(R.string.Menu_oddWeek)
                }
            } else {
                btnWeekType.visibility = View.GONE
            }
        } else {
            val dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault())
            val stringDate = dateFormat.format(c.time)
            val s = stringDate + " (" + resources.getString(R.string.week) + " " + dateWeek + ")"
            if (settings.showWeek) btnDateCenter.text = s else btnDateCenter.text =
                stringDate
            btnDateCenter.visibility = View.VISIBLE
            btnWeekType.visibility = View.GONE
        }
        val hourTimes = settings.hourTimes
        for (i in 1..11) {
            hourRooms[i]!!.text = ""
            hourButtons[i]!!.setTextColor(Color.BLACK)
            hourButtons[i]!!.background.colorFilter = null
            if (isEditingMode) {
                hourButtons[i]!!.visibility = View.VISIBLE
                hourButtons[i]!!.text = "+"
                hourLabels[i]!!.visibility = View.VISIBLE
            } else {
                hourButtons[i]!!.visibility = View.INVISIBLE
                hourLabels[i]!!.visibility = View.INVISIBLE
            }
            hourLabels[i]!!.text =
                if (settings.showHourTimes && i <= hourTimes.size) hourTimes[i - 1] else i.toString()
        }
        try {
            var filename = util.getFilenameForDay(this)
            var input = ObjectInputStream(
                FileInputStream(
                    File(
                        File(
                            filesDir, ""
                        ).toString() + File.separator + filename
                    )
                )
            )
            val hours = input.readObject() as Array<String?>
            for (i in 1..11) {
                if (hours[i] != null && hours[i] != "") {
                    try {
                        filename = "SUBJECT-" + hours[i] + ".srl"
                        input = ObjectInputStream(
                            FileInputStream(
                                File(
                                    File(
                                        filesDir, ""
                                    ).toString() + File.separator + filename
                                )
                            )
                        )
                        val s = input.readObject() as Subject

                        // TextViews (number/time of hour)
                        hourLabels[i]!!.visibility = View.VISIBLE

                        // Buttons (subject name)
                        hourButtons[i]!!.visibility = View.VISIBLE
                        hourButtons[i]!!.text = s.name
                        hourButtons[i]!!.background.setColorFilter(s.color, PorterDuff.Mode.MULTIPLY)
                        if (util.isColorDark(s.color)) hourButtons[i]!!
                            .setTextColor(Color.WHITE) else hourButtons[i]!!.setTextColor(Color.BLACK)

                        //TextViews (subject room)
                        if (s.name != hours[i - 1]) hourRooms[i]!!.text = s.room
                    } catch (e: FileNotFoundException) {
                        filename = util.getFilenameForDay(this)
                        try {
                            hours[i] = ""
                            val out: ObjectOutput = ObjectOutputStream(
                                FileOutputStream(
                                    File(
                                        filesDir, ""
                                    ).toString() + File.separator + filename
                                )
                            )
                            out.writeObject(hours)
                            out.close()
                        } catch (e2: IOException) {
                            e2.printStackTrace()
                        }
                        e.printStackTrace()
                    } catch (e: ClassNotFoundException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            input.close()
        } catch (e: FileNotFoundException) {
            val hours = arrayOfNulls<String>(12)
            val outputName = util.getFilenameForDay(this)
            try {
                val out: ObjectOutput = ObjectOutputStream(
                    FileOutputStream(
                        File(
                            filesDir, ""
                        ).toString() + File.separator + outputName
                    )
                )
                out.writeObject(hours)
                out.close()
            } catch (e1: IOException) {
                e.printStackTrace()
            }
            if (isEditingMode && settings.weekSystem) dialogCopyDay()
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun changeMode() {
        if (!isEditingMode) {
            isEditingMode = true
        } else {
            isEditingMode = false
            val act_day_of_week = getDayOfWeek(Calendar.getInstance())
            savedDayChange = selectedDayOfWeek - act_day_of_week
        }
        updateActivityMain()
        invalidateOptionsMenu()
    }

    private fun initialSetup() {
        changeMode()
        setDay(8 - selectedDayOfWeek)

        //Setup preset subjects
        try {
            val subject_names = resources!!.getStringArray(R.array.subject_names)
            val colors = resources!!.getIntArray(R.array.preset_colors)
            var i = 0
            while (i < subject_names.size && subject_names.size <= colors.size) {
                val subject = Subject(subject_names[i], colors[i])
                try {
                    val filename = "SUBJECT-" + subject.name + ".srl"
                    val out: ObjectOutput = ObjectOutputStream(
                        FileOutputStream(
                            File(
                                filesDir, ""
                            ).toString() + File.separator + filename
                        )
                    )
                    out.writeObject(subject)
                    out.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                i++
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clickBtnWeekType(view: View) {
        val popup = PopupMenu(this@MainActivity, findViewById<Button>(R.id.btnWeekType))
        //Inflating the Popup using xml file
        if (selectedEvenWeeks) popup.menuInflater.inflate(
            R.menu.menu_even_weeks,
            popup.menu
        ) else popup.menuInflater.inflate(R.menu.menu_odd_weeks, popup.menu)

        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.act_evenWeeks) selectedEvenWeeks =
                true else if (item.itemId == R.id.act_oddWeeks) selectedEvenWeeks = false
            updateActivityMain()
            true
        }
        popup.show()
    }

    fun clickBtnDateLeft(view: View) {
        setDay(-1)
        updateActivityMain()
    }

    fun clickBtnDateCenter(view: View) {
        if (!isEditingMode) savedDayChange = 0
        updateActivityMain()
    }

    fun clickBtnDateRight(view: View) {
        setDay(+1)
        updateActivityMain()
    }

    fun clickBtnEditHour(view: View) {
        selectedSubject = getButtonName(view)
        when {
            view === findViewById<View>(R.id.h1) -> selectedHour =
                1
            view === findViewById<View>(R.id.h2) -> selectedHour =
                2
            view === findViewById<View>(R.id.h3) -> selectedHour =
                3
            view === findViewById<View>(R.id.h4) -> selectedHour =
                4
            view === findViewById<View>(R.id.h5) -> selectedHour =
                5
            view === findViewById<View>(R.id.h6) -> selectedHour =
                6
            view === findViewById<View>(R.id.h7) -> selectedHour =
                7
            view === findViewById<View>(R.id.h8) -> selectedHour =
                8
            view === findViewById<View>(R.id.h9) -> selectedHour =
                9
            view === findViewById<View>(R.id.h10) -> selectedHour =
                10
            view === findViewById<View>(R.id.h11) -> selectedHour = 11
        }
        if (!isEditingMode) {
            isNewSubject = false
            setActivitySubject()
        } else {
            if (selectedSubject == "+") { //hour without subject
                if (readSubjectNames().isNotEmpty()) {
                    dialogEditEmptyHour().show()
                } else {
                    dialogNewSubject().show()
                }
            } else { //hour with subject
                dialogEditTakenHour().show()
            }
        }
    }

    fun switchHourDisplay(view: View) {
        val settings = util.readSettings(this)
        if (settings.showHourTimes) {
            settings.showHourTimes = false
            util.saveSettings(this, settings)
            updateActivityMain()
        } else {
            val hourTimes = settings.hourTimes
            if (hourTimes.isNotEmpty() && hourTimes[0] != null && hourTimes[0] != "") {
                settings.showHourTimes = true
                util.saveSettings(this, settings)
                updateActivityMain()
            } else {
                dialogShowHourTimes().show()
            }
        }
    }

    private fun dialogCopyDay() {
        val outputName = util.getFilenameForDay(this)
        var inputName = "DAY-$selectedDayOfWeek.srl"
        var title = resources!!.getString(R.string.D_copyEvenDay)
        if (outputName == "DAY-$selectedDayOfWeek.srl") {
            inputName = "ODD-DAY-$selectedDayOfWeek.srl"
            title = resources!!.getString(R.string.D_copyOddDay)
        }
        var hoursTemp = arrayOfNulls<String>(12)
        var inputEmpty = true
        //read the input
        try {
            val input = ObjectInputStream(
                FileInputStream(
                    File(
                        File(
                            filesDir, ""
                        ).toString() + File.separator + inputName
                    )
                )
            )
            hoursTemp = input.readObject() as Array<String?>
            for (s in hoursTemp) {
                if (s != null && s != "") {
                    inputEmpty = false
                    break
                }
            }
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val hours = hoursTemp
        if (!inputEmpty) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(title).setMessage(null)
                .setPositiveButton(R.string.Yes) { dialog, _ -> //overwrite the output with input
                    try {
                        val out: ObjectOutput = ObjectOutputStream(
                            FileOutputStream(
                                File(
                                    filesDir, ""
                                ).toString() + File.separator + outputName
                            )
                        )
                        out.writeObject(hours)
                        out.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    updateActivityMain()
                    dialog.cancel()
                }
                .setNegativeButton(R.string.No) { dialog, _ -> dialog.cancel() }
            builder.create().show()
        }
    }

    private fun dialogEditEmptyHour(): Dialog {
        val builder = AlertDialog.Builder(this)
        val subjectNames = readSubjectNames()
        val a: AppCompatActivity = this
        builder.setTitle(resources!!.getString(R.string.D_editEmptyHour_title))
            .setItems(subjectNames) { dialog, which ->
                try {
                    val filename = util.getFilenameForDay(a)
                    val input = ObjectInputStream(
                        FileInputStream(
                            File(
                                File(
                                    filesDir, ""
                                ).toString() + File.separator + filename
                            )
                        )
                    )
                    val hours = input.readObject() as Array<String?>
                    input.close()
                    selectedSubject = subjectNames[which]
                    hours[selectedHour] = selectedSubject
                    try {
                        val out: ObjectOutput = ObjectOutputStream(
                            FileOutputStream(
                                File(
                                    filesDir, ""
                                ).toString() + File.separator + filename
                            )
                        )
                        out.writeObject(hours)
                        out.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                } catch (e: ClassNotFoundException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                dialog.cancel()
                try {
                    val filename = "SUBJECT-$selectedSubject.srl"
                    val input = ObjectInputStream(
                        FileInputStream(
                            File(
                                File(
                                    filesDir, ""
                                ).toString() + File.separator + filename
                            )
                        )
                    )
                    val subject = input.readObject() as Subject
                    input.close()
                    if (subject.isNew) {
                        subject.markNotNew()
                        try {
                            val out: ObjectOutput = ObjectOutputStream(
                                FileOutputStream(
                                    File(
                                        filesDir, ""
                                    ).toString() + File.separator + filename
                                )
                            )
                            out.writeObject(subject)
                            out.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                        isNewSubject = false
                        setActivitySubject()
                    }
                } catch (e: ClassNotFoundException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                updateActivityMain()
            }
            .setPositiveButton(resources!!.getString(R.string.D_editEmptyHour_YES)) { dialog, _ ->
                dialog.cancel()
                dialogNewSubject().show()
            }
        return builder.create()
    }

    private fun dialogEditTakenHour(): Dialog {
        val builder = AlertDialog.Builder(this)
        val options = arrayOf(
            resources!!.getString(R.string.free_period),
            resources!!.getString(R.string.different_subject),
            resources!!.getString(R.string.Menu_editSubject)
        )
        val a: AppCompatActivity = this
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> {
                    dialog.cancel()
                    try {
                        val filename = util.getFilenameForDay(a)
                        val input = ObjectInputStream(
                            FileInputStream(
                                File(
                                    File(
                                        filesDir, ""
                                    ).toString() + File.separator + filename
                                )
                            )
                        )
                        val hours = input.readObject() as Array<String?>
                        input.close()
                        hours[selectedHour] = null
                        try {
                            val out: ObjectOutput = ObjectOutputStream(
                                FileOutputStream(
                                    File(
                                        filesDir, ""
                                    ).toString() + File.separator + filename
                                )
                            )
                            out.writeObject(hours)
                            out.close()
                            updateActivityMain()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    } catch (e: ClassNotFoundException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                1 -> {
                    dialog.cancel()
                    dialogEditEmptyHour().show()
                }
                2 -> {
                    dialog.cancel()
                    isNewSubject = false
                    setActivitySubject()
                }
            }
        }
        return builder.create()
    }

    private fun dialogEditSubject(): Dialog {
        val builder = AlertDialog.Builder(this)
        val subjectNames = readSubjectNames()
        builder.setTitle(resources!!.getString(R.string.D_editSubject_title))
            .setItems(subjectNames) { dialog, which ->
                selectedSubject = subjectNames[which]
                isNewSubject = false
                dialog.cancel()
                setActivitySubject()
            }
            .setNegativeButton(R.string.Cancel) { dialog, _ -> dialog.cancel() }
        return builder.create()
    }

    private fun dialogDeleteSubject(): Dialog {
        val builder = AlertDialog.Builder(this)
        val subjectNames = readSubjectNames()
        builder.setTitle(resources!!.getString(R.string.D_deleteSubject_title))
            .setItems(subjectNames) { dialog, which ->
                try {
                    val dir = filesDir
                    val file = File(dir, "SUBJECT-" + subjectNames[which] + ".srl")
                    val deleted = file.delete()
                    if (deleted) Toast.makeText(
                        applicationContext,
                        resources!!.getString(R.string.Deleted),
                        Toast.LENGTH_SHORT
                    ).show()
                    updateActivityMain()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                dialog.cancel()
            }
            .setNegativeButton(R.string.Cancel) { dialog, _ -> dialog.cancel() }
        return builder.create()
    }

    private fun dialogShowHourTimes(): Dialog {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(null)
            .setMessage(R.string.dialogShowHourTimes)
            .setNegativeButton(R.string.Cancel) { dialog, _ -> dialog.cancel() }
            .setPositiveButton(R.string.Yes) { dialog, _ ->
                val settings = util.readSettings(this@MainActivity)
                settings.showHourTimes = true
                util.saveSettings(this@MainActivity, settings)
                dialog.cancel()
                setActivitySettings()
            }
        return builder.create()
    }

    private fun dialogNewSubject(): Dialog {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.dialogSubjectName)
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            input.textAlignment = View.TEXT_ALIGNMENT_CENTER
        }
        builder.setView(input)
        builder.setNegativeButton(R.string.colorpicker_dialog_cancel) { dialog, _ -> dialog.cancel() }
        builder.setPositiveButton(R.string.colorpicker_dialog_ok) { dialog, _ ->
            val subjectName = input.text.toString()
            if (subjectName != "" && subjectName != "+") {
                selectedSubject = subjectName
                isNewSubject = true
                setActivitySubject()
                dialog.cancel()
            } else {
                Toast.makeText(
                    applicationContext,
                    resources!!.getString(R.string.Toast_nameNeeded),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        return builder.create()
    }

    private fun getDayOfWeek(c: Calendar): Int {
        var dayOfWeek = c[Calendar.DAY_OF_WEEK] - 1
        if (dayOfWeek == -1) dayOfWeek = 6 else if (dayOfWeek == 0) dayOfWeek = 7
        return dayOfWeek
    }

    private fun readSubjectNames(): Array<String?> {
            var subjectNames = arrayOfNulls<String>(0)
            val files = filesDir.listFiles { _, name -> name.startsWith("SUBJECT-") }
            if (files != null) {
                Arrays.sort(files)
                subjectNames = arrayOfNulls(files.size)
                for (i in files.indices) {
                    try {
                        val filename = files[i].name
                        val input = ObjectInputStream(
                            FileInputStream(
                                File(
                                    File(
                                        filesDir, ""
                                    ).toString() + File.separator + filename
                                )
                            )
                        )
                        val subject = input.readObject() as Subject
                        subjectNames[i] = subject.name
                        input.close()
                    } catch (e: ClassNotFoundException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            return subjectNames
        }

    private fun getButtonName(v: View): String {
        val btn = v as Button
        return btn.text.toString()
    }

    private val hourLabels: Array<TextView?>
        get() = arrayOf(
            null,
            findViewById(R.id.n1), findViewById(R.id.n2), findViewById(R.id.n3),
            findViewById(R.id.n4), findViewById(R.id.n5), findViewById(R.id.n6),
            findViewById(R.id.n7), findViewById(R.id.n8), findViewById(R.id.n9),
            findViewById(R.id.n10), findViewById(R.id.n11)
        )
    private val hourButtons: Array<Button?>
        get() =  arrayOf(
            null,
            findViewById(R.id.h1), findViewById(R.id.h2), findViewById(R.id.h3),
            findViewById(R.id.h4), findViewById(R.id.h5), findViewById(R.id.h6),
            findViewById(R.id.h7), findViewById(R.id.h8), findViewById(R.id.h9),
            findViewById(R.id.h10), findViewById(R.id.h11)
        )
    private val hourRooms: Array<TextView?>
        get() =  arrayOf(
            null,
            findViewById(R.id.r1), findViewById(R.id.r2), findViewById(R.id.r3),
            findViewById(R.id.r4), findViewById(R.id.r5), findViewById(R.id.r6),
            findViewById(R.id.r7), findViewById(R.id.r8), findViewById(R.id.r9),
            findViewById(R.id.r10), findViewById(R.id.r11)
        )

    companion object {
        private val util = Util()

        @JvmStatic
        var selectedDayOfWeek = 0
            private set
        private var savedDayChange = 0

        @JvmStatic
        var selectedHour = 1
            private set
        var selectedSubject: String? = ""
            private set
        var isNewSubject = true
            private set

        @JvmStatic
        var selectedEvenWeeks = true
            private set

        @JvmStatic
        var weekIsOdd = false
            private set
        var isEditingMode = false
            private set
    }
}