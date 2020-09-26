package juliushenke.smarttt;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
    private boolean finishActivity = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        updateActivitySubject();
        util.updateDesign(this, true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_subject, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        MenuItem item = menu.findItem(R.id.menu_saveSubject);
        item.setIcon(R.drawable.ic_done_white_24dp);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_saveSubject:
                clickB_sub_save(findViewById(R.id.B_subject_save));
                return true;
            // TODO add delete option
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

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
    public void updateActivitySubject() {
        EditText TF_subject = findViewById(R.id.TF_subject);
        EditText TF_room = findViewById(R.id.TF_room);
        EditText TF_teacher = findViewById(R.id.TF_teacher);
        EditText TF_notes = findViewById(R.id.TF_notes);

        if (MainActivity.isNewSubject()) {
            TF_subject.setEnabled(true);
            TF_subject.setFocusable(true);
            TF_subject.setTypeface(Typeface.DEFAULT);
            TF_subject.setText("");
            TF_room.setText("");
            TF_teacher.setText("");
            TF_notes.setText("");
        }
        else {
            TF_subject.setEnabled(false);
            TF_subject.setFocusable(false);
            TF_subject.setTextColor(Color.BLACK);
            TF_subject.setTypeface(Typeface.DEFAULT_BOLD);
            try {
                String filename = "SUBJECT-" + MainActivity.getSelected_subject() + ".srl";
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
        int[] colors = MainActivity.res.getIntArray(R.array.subject_colors);

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
                        finishActivity = false;
                        clickB_sub_save(findViewById(R.id.B_subject_save));
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
                                        finishActivity = false;
                                        clickB_sub_save(findViewById(R.id.B_subject_save));
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

    public void clickB_sub_save(View view) {
        EditText TF_subject = findViewById(R.id.TF_subject);
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

                    if (finishActivity) finish();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else Toast.makeText(getApplicationContext(), MainActivity.res.getString(R.string.Toast_nameNeeded), Toast.LENGTH_SHORT).show();
        }
        else{
            try {
                String filename = "SUBJECT-" + subject.getName() + ".srl";
                ObjectOutput out = new ObjectOutputStream(new FileOutputStream(new File(getFilesDir(), "") + File.separator + filename));
                out.writeObject(subject);
                out.close();

                if (finishActivity) finish();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //Hide keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        finishActivity = true;
    }

    private void changeColorButton(){
        Button B_subject_color = findViewById(R.id.B_subject_color);
        GradientDrawable bgShape = (GradientDrawable) B_subject_color.getBackground().getCurrent();
        bgShape.setColor(selectedColor);

        if (util.isColorDark(selectedColor)) B_subject_color.setTextColor(Color.WHITE);
        else B_subject_color.setTextColor(BLACK);
    }

    //Dialogs
//    private Dialog D_editColor() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        final String[] options = {getString(R.string.D_editColor_preset), getString(R.string.D_editColor_custom)};
//
//        builder.setItems(options, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        if(which == 0){
//
//                            ColorPickerDialog colorDialog = ColorPickerDialog.newInstance(R.string.D_editColor_preset_title,
//                                    colors,
//                                    selectedColor,
//                                    5, // Number of columns
//                                    ColorPickerDialog.SIZE_SMALL,
//                                    true, // True or False to enable or disable the serpentine effect
//                                    2, // stroke width
//                                    BLACK // stroke color
//                            );
//
//                            colorDialog.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener() {
//
//                                @Override
//                                public void onColorSelected(int color) {
//                                    selectedColor = color;
//                                    changeColorButton();
//                                    finishActivity = false;
//                                    clickB_sub_save(findViewById(R.id.B_subject_save));
//                                }
//
//                            });
//                            colorDialog.show(getFragmentManager(), "color_dialog_test");
//                        }
//                        else if(which == 1){
//                            final ColorPicker cp = new ColorPicker(SubjectActivity.this, Color.red(selectedColor), Color.green(selectedColor), Color.blue(selectedColor));
//                            cp.show();
//                            Button okColor = cp.findViewById(R.id.okColorButton);
//                            okColor.setText(R.string.OK);
//                            okColor.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    selectedColor = cp.getColor();
//                                    changeColorButton();
//                                    finishActivity = false;
//                                    clickB_sub_save(findViewById(R.id.B_subject_save));
//                                    cp.dismiss();
//                                }
//                            });
//                        }
//                        dialog.cancel();
//                    }
//                })
//                .setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int id) {
//                        dialog.cancel();
//                    }
//                });
//        return builder.create();
//    }

}
