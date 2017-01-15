package juliushenke.smarttt;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.pes.androidmaterialcolorpickerdialog.ColorPicker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Arrays;

public class SubjectActivity extends AppCompatActivity {

    private int selectedColorRGB = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        updateActivitySubject();
    }

    //Voids --------------------------------------------------------------------------------
    public void updateActivitySubject() {
        EditText TF_subject = (EditText) findViewById(R.id.TF_subject);
        EditText TF_room = (EditText) findViewById(R.id.TF_room);
        EditText TF_teacher = (EditText) findViewById(R.id.TF_teacher);
        Button B_subject_color = (Button) findViewById(R.id.B_subject_color);

        if (MainActivity.getNewSubject()) {
            TF_subject.setEnabled(true);
            TF_subject.setTypeface(Typeface.DEFAULT);
            TF_subject.setText("");
            TF_room.setText("");
            TF_teacher.setText("");

            selectedColorRGB = Color.parseColor("#cacaca");
            B_subject_color.setBackgroundColor(selectedColorRGB);
            if(MainActivity.isColorDark(selectedColorRGB)) B_subject_color.setTextColor(Color.WHITE);
            else B_subject_color.setTextColor(Color.BLACK);
        } else {
            TF_subject.setEnabled(false);
            TF_subject.setTextColor(Color.BLACK);
            TF_subject.setTypeface(Typeface.DEFAULT_BOLD);
            try {
                String filename = "SUBJECT-" + MainActivity.getSelected_subject() + ".srl";
                ObjectInputStream input = new ObjectInputStream(new FileInputStream(new File(new File(getFilesDir(), "") + File.separator + filename)));
                Subject subject = (Subject) input.readObject();

                TF_subject.setText(subject.getName());
                TF_room.setText(subject.getRoom());
                TF_teacher.setText(subject.getTeacher());

                selectedColorRGB = subject.getColor();
                B_subject_color.setBackgroundColor(selectedColorRGB);
                if (MainActivity.isColorDark(selectedColorRGB))
                    B_subject_color.setTextColor(Color.WHITE);
                else
                    B_subject_color.setTextColor(Color.BLACK);


                input.close();
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    //Clickers ---------------------------------------------------------------------------------
    public void clickB_sub_color(View view) {
        final ColorPicker cp = new ColorPicker(SubjectActivity.this, Color.red(selectedColorRGB), Color.green(selectedColorRGB), Color.blue(selectedColorRGB));
        cp.show();

        Button okColor = (Button) cp.findViewById(R.id.okColorButton);
        okColor.setText(R.string.OK);
        okColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                selectedColorRGB = cp.getColor();

                Button B_subject_color = (Button) findViewById(R.id.B_subject_color);
                B_subject_color.setBackgroundColor(selectedColorRGB);
                if (MainActivity.isColorDark(selectedColorRGB))
                    B_subject_color.setTextColor(Color.WHITE);
                else
                    B_subject_color.setTextColor(Color.BLACK);

                cp.dismiss();
            }
        });
    }

    public void clickB_sub_save(View view) {
        int selected_hour = MainActivity.getSelected_hour();
        EditText TF_subject = (EditText) findViewById(R.id.TF_subject);
        EditText TF_room = (EditText) findViewById(R.id.TF_room);
        EditText TF_teacher = (EditText) findViewById(R.id.TF_teacher);

        String name = TF_subject.getText().toString().trim();
        String room = TF_room.getText().toString().trim();
        String teacher = TF_teacher.getText().toString().trim();

        boolean isNameCorrect = true;
        String[] subjectNames = getSubjectNames();
        for (String subjectName : subjectNames) if (subjectName.equals(name)) isNameCorrect = false;

        if (!name.equals("") && !name.equals("+")) {
            if (!MainActivity.getNewSubject() || isNameCorrect) {
                Subject subject = new Subject(name, room, teacher, selectedColorRGB);

                try {
                    String filename = "SUBJECT-" + subject.getName() + ".srl";
                    ObjectOutput out = new ObjectOutputStream(new FileOutputStream(new File(getFilesDir(), "") + File.separator + filename));
                    out.writeObject(subject);
                    out.close();
                    try {
                        filename = getFilenameForDay();
                        ObjectInputStream input = new ObjectInputStream(new FileInputStream(new File(new File(getFilesDir(), "") + File.separator + filename)));
                        String[] hours = (String[]) input.readObject();
                        input.close();
                        hours[selected_hour] = name;
                        //if(isIntOdd(selected_hour) && hours[selected_hour + 1].equals("")) hours[selected_hour + 1] = subject_names[which];

                        out = new ObjectOutputStream(new FileOutputStream(new File(getFilesDir(), "") + File.separator + filename));
                        out.writeObject(hours);
                        out.close();
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.Saved), Toast.LENGTH_SHORT).show();
                    } catch (ClassNotFoundException | IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.Saved), Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //Hide keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                //Change Activity
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            } else
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.Toast_nameAlreadyTaken), Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.Toast_nameNeeded), Toast.LENGTH_SHORT).show();
    }

    //Getters ---------------------------------------------------------------------------------
    private String[] getSubjectNames() {
        File[] files = getFilesDir().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("SUBJECT-");
            }
        });
        Arrays.sort(files);

        String[] subject_names = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            try {
                String filename = files[i].getName();
                ObjectInputStream input = new ObjectInputStream(new FileInputStream(new File(new File(getFilesDir(), "") + File.separator + filename)));

                Subject subject = (Subject) input.readObject();
                subject_names[i] = subject.getName();
                input.close();
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
        }
        return subject_names;
    }

    private String getFilenameForDay() {
        String filename = "SETTINGS.srl";
        Settings settings = new Settings();
        try {
            ObjectInputStream input = new ObjectInputStream(new FileInputStream(new File(new File(getFilesDir(), "") + File.separator + filename)));
            settings = (Settings) input.readObject();
        } catch (FileNotFoundException e) {
            try {
                ObjectOutput out = new ObjectOutputStream(new FileOutputStream(new File(getFilesDir(), "") + File.separator + filename));
                out.writeObject(settings);
                out.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            e.printStackTrace();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }

        if (settings.getWeekSystem()) {
            if (MainActivity.getEditing_mode() && !MainActivity.getSelected_evenWeeks())
                return "ODD-DAY-" + MainActivity.getSelected_day_of_week() + ".srl";
            else if (!MainActivity.getEditing_mode() && MainActivity.getWeek_isOdd()) return "ODD-DAY-" + MainActivity.getSelected_day_of_week() + ".srl";
        }
        return "DAY-" + MainActivity.getSelected_day_of_week() + ".srl";
    }

}
