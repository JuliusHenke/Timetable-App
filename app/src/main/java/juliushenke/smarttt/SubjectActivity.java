package juliushenke.smarttt;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.pes.androidmaterialcolorpickerdialog.ColorPicker;

import org.xdty.preference.colorpicker.ColorPickerDialog;
import org.xdty.preference.colorpicker.ColorPickerSwatch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import static android.graphics.Color.BLACK;

public class SubjectActivity extends AppCompatActivity {

    private static Util util = new Util();
    private int selectedColor = BLACK;
    private boolean finishActivity = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Settings settings = util.readSettings(this);
        if(settings.isDarkDesign()) setTheme(R.style.AppThemeDark);
        else setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_subject);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        updateActivitySubject();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_subject, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        Settings settings = util.readSettings(this);
        MenuItem item = menu.findItem(R.id.menu_saveSubject);
        if(settings.isDarkDesign()) item.setIcon(R.drawable.ic_done_white_24dp);
        else item.setIcon(R.drawable.ic_done_black_24dp);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_saveSubject:
                clickB_sub_save(findViewById(R.id.B_subject_save));
                return true;

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
        util.updateDesign(this, true);

        EditText TF_subject = (EditText) findViewById(R.id.TF_subject);
        EditText TF_room = (EditText) findViewById(R.id.TF_room);
        EditText TF_teacher = (EditText) findViewById(R.id.TF_teacher);

        if (MainActivity.isNewSubject()) {
            TF_subject.setEnabled(true);
            TF_subject.setFocusable(true);
            TF_subject.setTypeface(Typeface.DEFAULT);
            TF_subject.setText("");
            TF_room.setText("");
            TF_teacher.setText("");
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
        D_editColor().show();
    }

    public void clickB_sub_save(View view) {
        EditText TF_subject = (EditText) findViewById(R.id.TF_subject);
        EditText TF_room = (EditText) findViewById(R.id.TF_room);
        EditText TF_teacher = (EditText) findViewById(R.id.TF_teacher);

        String name = TF_subject.getText().toString().trim();
        String room = TF_room.getText().toString().trim();
        String teacher = TF_teacher.getText().toString().trim();

        Subject subject = new Subject(name, room, teacher, selectedColor);

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
            } else Toast.makeText(getApplicationContext(), getResources().getString(R.string.Toast_nameNeeded), Toast.LENGTH_SHORT).show();
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
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        finishActivity = true;
    }

    private void changeColorButton(){
        Button B_subject_color = (Button) findViewById(R.id.B_subject_color);
        GradientDrawable bgShape = (GradientDrawable) B_subject_color.getBackground().getCurrent();
        bgShape.setColor(selectedColor);

        if (util.isColorDark(selectedColor)) B_subject_color.setTextColor(Color.WHITE);
        else B_subject_color.setTextColor(BLACK);
    }

    //Dialogs
    private Dialog D_editColor() {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, android.R.style.Theme_Holo_Dialog));
        final String[] options = {getString(R.string.D_editColor_preset), getString(R.string.D_editColor_custom)};

        builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0){
                            int[] colors = getResources().getIntArray(R.array.default_rainbow);

                            ColorPickerDialog colorDialog = ColorPickerDialog.newInstance(R.string.D_editColor_preset_title,
                                    colors,
                                    selectedColor,
                                    5, // Number of columns
                                    ColorPickerDialog.SIZE_SMALL,
                                    true, // True or False to enable or disable the serpentine effect
                                    2, // stroke width
                                    BLACK // stroke color
                            );

                            colorDialog.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener() {

                                @Override
                                public void onColorSelected(int color) {
                                    selectedColor = color;
                                    changeColorButton();
                                    finishActivity = false;
                                    clickB_sub_save(findViewById(R.id.B_subject_save));
                                }

                            });
                            colorDialog.show(getFragmentManager(), "color_dialog_test");
                        }
                        else if(which == 1){
                            final ColorPicker cp = new ColorPicker(SubjectActivity.this, Color.red(selectedColor), Color.green(selectedColor), Color.blue(selectedColor));
                            cp.show();
                            Button okColor = (Button) cp.findViewById(R.id.okColorButton);
                            okColor.setText(R.string.OK);
                            okColor.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    selectedColor = cp.getColor();
                                    changeColorButton();
                                    finishActivity = false;
                                    clickB_sub_save(findViewById(R.id.B_subject_save));
                                    cp.dismiss();
                                }
                            });
                        }
                        dialog.cancel();
                    }
                })
                .setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        return builder.create();
    }

}
