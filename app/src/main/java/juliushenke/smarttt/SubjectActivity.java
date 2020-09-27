package juliushenke.smarttt;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.ColorPickerView;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import petrov.kristiyan.colorpicker.ColorPicker;

import static android.graphics.Color.BLACK;

public class SubjectActivity extends AppCompatActivity {

    private static Util util = new Util();
    private int selectedColor = BLACK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        initializeActivitySubject();
        util.updateDesign(this, true);

        EditText[] inputs = {findViewById(R.id.TF_room), findViewById(R.id.TF_teacher), findViewById(R.id.TF_notes)};
        for (EditText input: inputs) {
            input.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) { }

                @Override
                public void afterTextChanged(Editable editable) {
                    saveSubject();
                }
            });
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //Voids --------------------------------------------------------------------------------
    public void initializeActivitySubject() {
        TextView TF_subject = findViewById(R.id.TF_subject);
        EditText TF_room = findViewById(R.id.TF_room);
        EditText TF_teacher = findViewById(R.id.TF_teacher);
        EditText TF_notes = findViewById(R.id.TF_notes);

        String selectedSubject = MainActivity.getSelectedSubject();
        if (MainActivity.isNewSubject()) {
            TF_subject.setText(selectedSubject);
            TF_room.setText("");
            TF_teacher.setText("");
            TF_notes.setText("");
            saveSubject();
            clickB_sub_color(findViewById(R.id.B_subject_color));
        }
        else {
            try {
                String filename = "SUBJECT-" + selectedSubject + ".srl";
                ObjectInputStream input = new ObjectInputStream(new FileInputStream(new File(new File(getFilesDir(), "") + File.separator + filename)));
                Subject subject = (Subject) input.readObject();

                TF_subject.setText(subject.getName());
                TF_room.setText(subject.getRoom());
                TF_teacher.setText(subject.getTeacher());
                TF_notes.setText(subject.getNotes());
                selectedColor = subject.getColor();
                input.close();
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
        }
        changeColorButton();
    }

    //Clickers ---------------------------------------------------------------------------------
    public void clickB_sub_color(View view) {
        int[] colors = MainActivity.res.getIntArray(R.array.preset_colors);

        final ColorPicker presetColorPicker = new ColorPicker(SubjectActivity.this);
        presetColorPicker.setTitle(getString(R.string.D_editColor_preset_title))
            .setColors(colors)
            .setDefaultColorButton(selectedColor)
            .disableDefaultButtons(true)
            .setRoundColorButton(true)
            .setOnFastChooseColorListener(new ColorPicker.OnFastChooseColorListener() {
                    @Override
                    public void setOnFastChooseColorListener(int position, int color) {
                        selectedColor = color;
                        changeColorButton();
                        saveSubject();
                        presetColorPicker.dismissDialog();
                    }

                    @Override
                    public void onCancel(){ }
            })
            .addListenerButton(getString(R.string.colorpicker_dialog_cancel), new ColorPicker.OnButtonListener() {
                @Override
                public void onClick(View v, int position, int color) {
                    presetColorPicker.dismissDialog();
                }
            })
            .addListenerButton(getString(R.string.D_editColor_custom), new ColorPicker.OnButtonListener() {
                @Override
                public void onClick(View v, int position, int color) {
                    ColorPickerDialog.Builder builder = new ColorPickerDialog.Builder(SubjectActivity.this)
                        .setTitle(getString(R.string.D_editColor_custom))
                        .setPositiveButton(getString(R.string.colorpicker_dialog_ok),
                                new ColorEnvelopeListener() {
                                    @Override
                                    public void onColorSelected(ColorEnvelope envelope, boolean fromUser) {
                                        selectedColor = envelope.getColor();
                                        System.out.println(selectedColor);
                                        changeColorButton();
                                        saveSubject();
                                    }
                                })
                        .setNegativeButton(getString(R.string.colorpicker_dialog_cancel),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                })
                        .attachAlphaSlideBar(false) // the default value is true.
                        .attachBrightnessSlideBar(true)  // the default value is true.
                        .setBottomSpace(12);
                    ColorPickerView colorPickerView = builder.getColorPickerView();
                    colorPickerView.setInitialColor(selectedColor);
                    builder.show();
                    presetColorPicker.dismissDialog();
                }
            })
            .getDialogViewLayout().findViewById(R.id.buttons_layout).setVisibility(View.VISIBLE);
        presetColorPicker.show();
    }

    public void saveSubject() {
        TextView TF_subject = findViewById(R.id.TF_subject);
        EditText TF_room = findViewById(R.id.TF_room);
        EditText TF_teacher = findViewById(R.id.TF_teacher);
        EditText TF_notes = findViewById(R.id.TF_notes);

        String name = TF_subject.getText().toString().trim();
        String room = TF_room.getText().toString().trim();
        String teacher = TF_teacher.getText().toString().trim();
        String notes = TF_notes.getText().toString().trim();

        Subject subject = new Subject(name, selectedColor, room, teacher, notes);

        if(MainActivity.isNewSubject()){
            int selected_hour = MainActivity.getSelected_hour();
            if (!name.equals("") && !name.equals("+")) {
                try {
                    String filename = "SUBJECT-" + subject.getName() + ".srl";
                    ObjectOutput out = new ObjectOutputStream(new FileOutputStream(new File(getFilesDir(), "") + File.separator + filename));
                    out.writeObject(subject);
                    out.close();
                    try {
                        filename = util.getFilenameForDay(this);
                        ObjectInputStream input = new ObjectInputStream(new FileInputStream(new File(new File(getFilesDir(), "") + File.separator + filename)));
                        String[] hours = (String[]) input.readObject();
                        input.close();
                        hours[selected_hour] = name;

                        out = new ObjectOutputStream(new FileOutputStream(new File(getFilesDir(), "") + File.separator + filename));
                        out.writeObject(hours);
                        out.close();
                    } catch (ClassNotFoundException | IOException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        else{
            try {
                String filename = "SUBJECT-" + subject.getName() + ".srl";
                ObjectOutput out = new ObjectOutputStream(new FileOutputStream(new File(getFilesDir(), "") + File.separator + filename));
                out.writeObject(subject);
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void changeColorButton(){
        Button B_subject_color = findViewById(R.id.B_subject_color);
        GradientDrawable bgShape = (GradientDrawable) B_subject_color.getBackground().getCurrent();
        bgShape.setColor(selectedColor);
    }
}
