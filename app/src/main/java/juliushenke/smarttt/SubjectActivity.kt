package juliushenke.smarttt

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import juliushenke.smarttt.MainActivity.Companion.isNewSubject
import juliushenke.smarttt.MainActivity.Companion.selectedSubject
import juliushenke.smarttt.MainActivity.Companion.selectedHour
import juliushenke.smarttt.model.Subject
import petrov.kristiyan.colorpicker.ColorPicker
import petrov.kristiyan.colorpicker.ColorPicker.OnFastChooseColorListener
import java.io.*

class SubjectActivity : AppCompatActivity() {
    private var selectedColor = Color.BLACK
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subject)
        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        initializeActivitySubject()
        util.updateDesign(this, true)
        val inputs = arrayOf(
            findViewById(R.id.textViewRoom),
            findViewById(R.id.textViewTeacher),
            findViewById<EditText>(R.id.textViewNotes)
        )
        for (input in inputs) {
            input.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(editable: Editable) {
                    saveSubject()
                }
            })
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun initializeActivitySubject() {
        val textViewSubject = findViewById<TextView>(R.id.textViewSubject)
        val textViewRoom = findViewById<EditText>(R.id.textViewRoom)
        val textViewTeacher = findViewById<EditText>(R.id.textViewTeacher)
        val textViewNotes = findViewById<EditText>(R.id.textViewNotes)
        if (isNewSubject) {
            textViewSubject.text = selectedSubject
            textViewRoom.setText("")
            textViewTeacher.setText("")
            textViewNotes.setText("")
            saveSubject()
            clickBtnSubjectColor(findViewById<TextView>(R.id.btnSubjectColor))
        } else {
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
                textViewSubject.text = subject.name
                textViewRoom.setText(subject.room)
                textViewTeacher.setText(subject.teacher)
                textViewNotes.setText(subject.notes)
                selectedColor = subject.color
                input.close()
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        changeColorButton()
    }

    fun clickBtnSubjectColor(view: View) {
        val colors = resources.getIntArray(R.array.preset_colors)
        val presetColorPicker = ColorPicker(this@SubjectActivity)
        presetColorPicker.setTitle(getString(R.string.D_editColor_preset_title))
            .setColors(*colors)
            .setDefaultColorButton(selectedColor)
            .disableDefaultButtons(true)
            .setRoundColorButton(true)
            .setOnFastChooseColorListener(object : OnFastChooseColorListener {
                override fun setOnFastChooseColorListener(position: Int, color: Int) {
                    selectedColor = color
                    changeColorButton()
                    saveSubject()
                    presetColorPicker.dismissDialog()
                }

                override fun onCancel() {}
            })
            .addListenerButton(getString(R.string.colorpicker_dialog_cancel)) { _, _, _ -> presetColorPicker.dismissDialog() }
            .addListenerButton(getString(R.string.D_editColor_custom)) { _, _, _ ->
                val builder = ColorPickerDialog.Builder(this@SubjectActivity)
                    .setTitle(getString(R.string.D_editColor_custom))
                    .setPositiveButton(getString(R.string.colorpicker_dialog_ok),
                        ColorEnvelopeListener { envelope, _ ->
                            selectedColor = envelope.color
                            println(selectedColor)
                            changeColorButton()
                            saveSubject()
                        })
                    .setNegativeButton(
                        getString(R.string.colorpicker_dialog_cancel)
                    ) { dialogInterface, _ -> dialogInterface.dismiss() }
                    .attachAlphaSlideBar(false) // the default value is true.
                    .attachBrightnessSlideBar(true) // the default value is true.
                    .setBottomSpace(12)
                val colorPickerView = builder.colorPickerView
                colorPickerView.setInitialColor(selectedColor)
                builder.show()
                presetColorPicker.dismissDialog()
            }
            .dialogViewLayout.findViewById<View>(R.id.buttons_layout).visibility = View.VISIBLE
        presetColorPicker.show()
    }

    fun saveSubject() {
        val textViewSubject = findViewById<TextView>(R.id.textViewSubject)
        val textViewRoom = findViewById<EditText>(R.id.textViewRoom)
        val textViewTeacher = findViewById<EditText>(R.id.textViewTeacher)
        val textViewNotes = findViewById<EditText>(R.id.textViewNotes)
        val name = textViewSubject.text.toString().trim { it <= ' ' }
        val room = textViewRoom.text.toString().trim { it <= ' ' }
        val teacher = textViewTeacher.text.toString().trim { it <= ' ' }
        val notes = textViewNotes.text.toString().trim { it <= ' ' }
        val subject = Subject(name, selectedColor, room, teacher, notes)
        if (isNewSubject) {
            if (name != "" && name != "+") {
                try {
                    var filename: String? = "SUBJECT-" + subject.name + ".srl"
                    var out: ObjectOutput = ObjectOutputStream(
                        FileOutputStream(
                            File(
                                filesDir, ""
                            ).toString() + File.separator + filename
                        )
                    )
                    out.writeObject(subject)
                    out.close()
                    try {
                        filename = util.getFilenameForDay(this)
                        val input = ObjectInputStream(
                            FileInputStream(
                                File(
                                    File(
                                        filesDir, ""
                                    ).toString() + File.separator + filename
                                )
                            )
                        )
                        val hours = input.readObject() as Array<String>
                        input.close()
                        hours[selectedHour] = name
                        out = ObjectOutputStream(
                            FileOutputStream(
                                File(
                                    filesDir, ""
                                ).toString() + File.separator + filename
                            )
                        )
                        out.writeObject(hours)
                        out.close()
                    } catch (e: ClassNotFoundException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        } else {
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
        }
    }

    private fun changeColorButton() {
        val btnSubjectColor = findViewById<Button>(R.id.btnSubjectColor)
        val bgShape = btnSubjectColor.background.current as GradientDrawable
        bgShape.setColor(selectedColor)
    }

    companion object {
        private val util = Util()
    }
}