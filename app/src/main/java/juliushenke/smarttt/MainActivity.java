package juliushenke.smarttt;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private int selected_day_of_week = 0;
    private int saved_change = 0;

    private int selected_hour = 1;
    private String selected_subject = "";
    private int selectedColorRGB = 0;

    private boolean editing_mode = false;
    private boolean days_changed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setCurDay();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setContentView(R.layout.activity_main);
            if(days_changed){
                days_changed = false;
                setCurDay();
            }
            else if(editing_mode){
                editing_mode = false;
                setCurDay();
            }
            else setDay(0);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //Voids --------------------------------------------------------------------------------
    private void setCurDay(){
        saved_change = 0;
        setDay(0);
    }

    private void setDay(int input_change){
        Resources r = getResources();

        //Adding a TouchListener for swiping
        ScrollView mParentLayout = (ScrollView) findViewById(R.id.scrollview);
        mParentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        mParentLayout.setOnTouchListener(new OnSwipeTouchListener() {
            @Override
            public boolean onSwipeLeft() {
                setDay(+1);
                return true;
            }

            @Override
            public boolean onSwipeRight() {
                setDay(-1);
                return true;
            }

            @Override
            public boolean onSwipeBottom() {
                return true;
            }
            @Override
            public boolean onSwipeTop() {
                return true;
            }
        });

        //Reading settings
        String filename = "SETTINGS.srl";
        Settings settings = new Settings(1, 5, 11, false);
        try{
            ObjectInputStream input = new ObjectInputStream(new FileInputStream(new File(new File(getFilesDir(), "") + File.separator + filename)));
            settings = (Settings) input.readObject();
        } catch(FileNotFoundException e){
            try {
                ObjectOutput out = new ObjectOutputStream(new FileOutputStream(new File(getFilesDir(), "") + File.separator + filename));
                out.writeObject(settings);
                out.close();

                editing_mode = true;
                input_change = 0;
                Dialog tutorial = help();
                tutorial.show();
            } catch(IOException e2) {
                e2.printStackTrace();
            }
            e.printStackTrace();
        } catch(ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }

        Calendar c = Calendar.getInstance();
        saved_change = saved_change + input_change;
        c.add(Calendar.DAY_OF_YEAR, saved_change);
        //Check if tomorrow has to be shown
        if(c.get(Calendar.HOUR_OF_DAY) > 18){
            c.add(Calendar.DAY_OF_YEAR, +1);
            saved_change++;
        }
        selected_day_of_week = getDay_of_week(c);

        int extra_change = 0;
        //positive change - into the future
        if(input_change >= 0){
            if(selected_day_of_week < settings.getStart_day()) extra_change = settings.getStart_day() - selected_day_of_week;
            else if(selected_day_of_week > settings.getEnd_day()) extra_change = 7 - selected_day_of_week + settings.getStart_day();
        }
        //negative change - into the past
        else if(input_change < 0){
            if(selected_day_of_week < settings.getStart_day()) extra_change = -selected_day_of_week - (7 - settings.getEnd_day());
            else if(selected_day_of_week > settings.getEnd_day()) extra_change = settings.getEnd_day() - selected_day_of_week;
        }
        //update the Calendar
        c.add(Calendar.DAY_OF_YEAR, extra_change);
        saved_change = saved_change + extra_change;

        //update the day_of_week
        selected_day_of_week = getDay_of_week(c);

        //----------------------------------------------------------------------------------------------
        String[] days = r.getStringArray(R.array.days);
        TextView TV_day = (TextView) findViewById(R.id.TV_day);
        TV_day.setText(days[selected_day_of_week]);

        TextView TV_selected_date = (TextView) findViewById(R.id.TV_selected_date);
        if(editing_mode) TV_selected_date.setText("");
        else{
            Calendar c_today = Calendar.getInstance();

            Calendar c_tomorrow = Calendar.getInstance();
            c_tomorrow.add(Calendar.DAY_OF_YEAR, +1);

            Calendar c_yesterday = Calendar.getInstance();
            c_tomorrow.add(Calendar.DAY_OF_YEAR, -1);

            if(getDay_of_week(c_today) == selected_day_of_week) TV_selected_date.setText(r.getString(R.string.date_today));
            else if(getDay_of_week(c_tomorrow) == selected_day_of_week) TV_selected_date.setText(r.getString(R.string.date_tomorrow));
            else if(getDay_of_week(c_yesterday) == selected_day_of_week) TV_selected_date.setText(r.getString(R.string.date_yesterday));
            else{
                DateFormat DATE_FORMAT = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
                String string_date = DATE_FORMAT.format(c.getTime());
                TV_selected_date.setText(string_date);
            }
        }

        int date_week = c.get(Calendar.WEEK_OF_YEAR);
        boolean week_isOdd = isOdd(date_week);

        final TextView [] TVs_room = {null,
                (TextView) findViewById(R.id.r1),(TextView) findViewById(R.id.r2), (TextView) findViewById(R.id.r3),
                (TextView) findViewById(R.id.r4),(TextView) findViewById(R.id.r5), (TextView) findViewById(R.id.r6),
                (TextView) findViewById(R.id.r7),(TextView) findViewById(R.id.r8), (TextView) findViewById(R.id.r9),
                (TextView) findViewById(R.id.r10),(TextView) findViewById(R.id.r11)};

        final TextView [] TVs_hour = {null,
                (TextView) findViewById(R.id.n1),(TextView) findViewById(R.id.n2), (TextView) findViewById(R.id.n3),
                (TextView) findViewById(R.id.n4),(TextView) findViewById(R.id.n5), (TextView) findViewById(R.id.n6),
                (TextView) findViewById(R.id.n7),(TextView) findViewById(R.id.n8), (TextView) findViewById(R.id.n9),
                (TextView) findViewById(R.id.n10),(TextView) findViewById(R.id.n11)};

        final Button [] Bts = {null,
                (Button) findViewById(R.id.h1),(Button) findViewById(R.id.h2), (Button) findViewById(R.id.h3),
                (Button) findViewById(R.id.h4),(Button) findViewById(R.id.h5), (Button) findViewById(R.id.h6),
                (Button) findViewById(R.id.h7),(Button) findViewById(R.id.h8), (Button) findViewById(R.id.h9),
                (Button) findViewById(R.id.h10),(Button) findViewById(R.id.h11)};

        for(int i = 1; i <= 11; i++){
            TVs_hour[i].setVisibility(View.INVISIBLE);
            Bts[i].setVisibility(View.INVISIBLE);
            TVs_room[i].setText("");
        }
        for(int i = 1; i <= settings.getMax_hours(); i++){
            TVs_hour[i].setVisibility(View.VISIBLE);
            Bts[i].getBackground().setColorFilter(null);
            Bts[i].setTextColor(Color.BLACK);
            Bts[i].getBackground().setColorFilter(null);
            Bts[i].setTextColor(Color.BLACK);
            Bts[i].setText("+");
            if(editing_mode)Bts[i].setVisibility(View.VISIBLE);
        }
        try {
            filename = "DAY-" + selected_day_of_week + ".srl";
            ObjectInputStream input = new ObjectInputStream(new FileInputStream(new File(new File(getFilesDir(), "") + File.separator + filename)));
            String [] hours = (String []) input.readObject();
            for(int i = 1; i <= settings.getMax_hours(); i++){
                int a = i;
                try{
                    filename = "SUBJECT-" + hours[i] + ".srl";
                    if(!editing_mode && settings.getTwo_week_system()){
                        if(i == 8 && week_isOdd){
                            a = i + 1;
                            filename = "SUBJECT-" + hours[a] + ".srl";
                        }
                        else if(i == 9 && !week_isOdd){
                            a = i - 1;
                            filename = "SUBJECT-" + hours[a] + ".srl";
                        }
                    }

                    input = new ObjectInputStream(new FileInputStream(new File(new File(getFilesDir(), "") + File.separator + filename)));
                    Subject s = (Subject) input.readObject();

                    Bts[i].setVisibility(View.VISIBLE);
                    if(i == 9 && !editing_mode && settings.getTwo_week_system());
                    else if(! s.getName().equals(hours[i-1])) TVs_room[i].setText(s.getRoom());
                    Bts[i].setText(s.getName());
                    Bts[i].getBackground().setColorFilter(s.getColor(), PorterDuff.Mode.MULTIPLY);
                    if(isColorDark(s.getColor())) Bts[i].setTextColor(Color.WHITE);
                } catch(FileNotFoundException e){
                    filename = "DAY-" + selected_day_of_week + ".srl";
                    try {
                        hours[a] = "";
                        ObjectOutput out = new ObjectOutputStream(new FileOutputStream(new File(getFilesDir(), "") + File.separator + filename));
                        out.writeObject(hours);
                        out.close();
                    } catch(IOException e2) {
                        e2.printStackTrace();
                    }
                    e.printStackTrace();
                } catch(ClassNotFoundException | IOException e) {
                    e.printStackTrace();
                }
            }
            input.close();
        } catch(FileNotFoundException e){
            filename = "DAY-" + selected_day_of_week + ".srl";
            try {
                String [] hours = new String[12];
                ObjectOutput out = new ObjectOutputStream(new FileOutputStream(new File(getFilesDir(), "") + File.separator + filename));
                out.writeObject(hours);
                out.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            e.printStackTrace();
        } catch(ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }

        Button B_mode = (Button) findViewById(R.id.B_mode);
        Button B_options = (Button) findViewById(R.id.B_options);
        if(!editing_mode){
            B_mode.getBackground().setColorFilter(null);
            B_mode.setText(R.string.B_mode_view);
            B_options.setVisibility(View.VISIBLE);
        }
        else{
            B_mode.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
            B_mode.setText(R.string.B_mode);
            B_options.setVisibility(View.INVISIBLE);
        }
    }

    private void createNewSubject(){
        setContentView(R.layout.activity_subject);

        EditText TF_subject = (EditText) findViewById(R.id.TF_subject);
        EditText TF_room = (EditText) findViewById(R.id.TF_room);
        EditText TF_teacher = (EditText) findViewById(R.id.TF_teacher);
        Button B_subject_color = (Button) findViewById(R.id.B_subject_color);

        TF_subject.setText("");
        TF_room.setText("");
        TF_teacher.setText("");

        selectedColorRGB = R.color.Color_B_hour;
        B_subject_color.getBackground().setColorFilter(null);
        B_subject_color.setTextColor(Color.BLACK);
    }

    private void showSubject(){
        setContentView(R.layout.activity_subject);
        try {
            String filename = "SUBJECT-" + selected_subject + ".srl";
            ObjectInputStream input = new ObjectInputStream(new FileInputStream(new File(new File(getFilesDir(), "") + File.separator + filename)));
            Subject subject = (Subject) input.readObject();

            EditText TF_subject = (EditText) findViewById(R.id.TF_subject);
            EditText TF_room = (EditText) findViewById(R.id.TF_room);
            EditText TF_teacher = (EditText) findViewById(R.id.TF_teacher);
            EditText TF_notes = (EditText) findViewById(R.id.TF_notes);
            Button B_subject_color = (Button) findViewById(R.id.B_subject_color);

            TF_subject.setText(subject.getName());
            TF_room.setText(subject.getRoom());
            TF_teacher.setText(subject.getTeacher());
            TF_notes.setText(subject.getNotes());

            selectedColorRGB = subject.getColor();
            B_subject_color.getBackground().setColorFilter(subject.getColor(), PorterDuff.Mode.MULTIPLY);
            if(isColorDark(selectedColorRGB))B_subject_color.setTextColor(Color.WHITE);
            else B_subject_color.setTextColor(Color.BLACK);

            input.close();
        } catch(ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }

    private void update_activityOptions(Settings settings){
        String [] days = getResources().getStringArray(R.array.days);
        Button B_startDay = (Button) findViewById(R.id.B_opt_startDay);
        Button B_endDay = (Button) findViewById(R.id.B_opt_endDay);
        Button B_maxHours = (Button) findViewById(R.id.B_opt_maxHours);
        RadioButton RB_yes = (RadioButton) findViewById(R.id.RB_opt_yes);
        RadioButton RB_no = (RadioButton) findViewById(R.id.RB_opt_no);

        B_startDay.setText(days[settings.getStart_day()]);
        B_endDay.setText(days[settings.getEnd_day()]);
        B_maxHours.setText(""+settings.getMax_hours());
        if(settings.getTwo_week_system()){
            RB_yes.setChecked(true);
            RB_no.setChecked(false);
        }
        else{
            RB_yes.setChecked(false);
            RB_no.setChecked(true);
        }


    }

    //Clickers ---------------------------------------------------------------------------------
    public void clickB_main_hour(View view){
        Button button = (Button) view;
        selected_subject = button.getText().toString();

        if(view == findViewById(R.id.h1)) selected_hour = 1;
        else if(view == findViewById(R.id.h2)) selected_hour = 2;
        else if(view == findViewById(R.id.h3)) selected_hour = 3;
        else if(view == findViewById(R.id.h4)) selected_hour = 4;
        else if(view == findViewById(R.id.h5)) selected_hour = 5;
        else if(view == findViewById(R.id.h6)) selected_hour = 6;
        else if(view == findViewById(R.id.h7)) selected_hour = 7;
        else if(view == findViewById(R.id.h8)) selected_hour = 8;
        else if(view == findViewById(R.id.h9)) selected_hour = 9;
        else if(view == findViewById(R.id.h10)) selected_hour = 10;
        else if(view == findViewById(R.id.h11)) selected_hour = 11;

        if(!editing_mode){
            showSubject();
        }
        else{
            File[] files = getSubjectFiles();
            if(files.length > 0) {
                Resources r = getResources();
                if(selected_subject.equals("+") || selected_subject.equals("") || selected_subject.equals(null)){
                    String[] options = {r.getString(R.string.D_select_subject),r.getString(R.string.D_createSubject)};
                    Dialog edit_hour = edit_hour(options);
                    edit_hour.show();
                }
                else{
                    String[] options = {r.getString(R.string.D_select_subject),r.getString(R.string.D_createSubject),r.getString(R.string.D_freeHour)};
                    Dialog edit_hour = edit_hour(options);
                    edit_hour.show();
                }
            }
            else{
                createNewSubject();
            }
        }
    }

    public  void clickB_main_editMode(View view){
        setContentView(R.layout.activity_main);
        if(!editing_mode){
            editing_mode = true;
            setDay(0);
        }
        else{
            editing_mode = false;
            setCurDay();
        }
    }

    public void clickB_main_options(View view){
        setContentView(R.layout.activity_options);
        Settings settings = new Settings(1, 5, 11, false);
        try{
            String filename = "SETTINGS.srl";
            ObjectInputStream input = new ObjectInputStream(new FileInputStream(new File(new File(getFilesDir(), "") + File.separator + filename)));
            settings = (Settings) input.readObject();
        } catch(ClassNotFoundException | IOException e){
            e.printStackTrace();
        }
        update_activityOptions(settings);
    }

    public void clickB_opt_startDay(View view){
        Dialog manage_days = manage_days(true);
        manage_days.show();
    }

    public void clickB_opt_endDay(View view){
        Dialog manage_days = manage_days(false);
        manage_days.show();
    }

    public void clickB_opt_maxHours(View view){
        Dialog maxHours = maxHours();
        maxHours.show();
    }

    public void clickRB_opt_twoWeekSystem(View view){
        String filename = "SETTINGS.srl";
        Settings settings = new Settings(1, 5, 11, false);
        try{
            ObjectInputStream input = new ObjectInputStream(new FileInputStream(new File(new File(getFilesDir(), "") + File.separator + filename)));
            settings = (Settings) input.readObject();
        } catch(ClassNotFoundException | IOException e){
            e.printStackTrace();
        }
        if(view.equals(findViewById(R.id.RB_opt_yes))) settings.setTwo_week_system(true);
        else settings.setTwo_week_system(false);
        try {
            ObjectOutput out = new ObjectOutputStream(new FileOutputStream(new File(getFilesDir(), "") + File.separator + filename));
            out.writeObject(settings);
            out.close();
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.Toast_saved), Toast.LENGTH_SHORT).show();
        } catch(IOException e){
            e.printStackTrace();
        }
        update_activityOptions(settings);
    }

    public void clickB_opt_deleteSubject(View view){
        Dialog select_subject = select_subject(true);
        select_subject.show();
    }

    public void  clickB_opt_aboutApp(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(null).setMessage(getResources().getString(R.string.aboutApp));
        builder.show();
    }

    public void  clickB_opt_suppApp(View view){
        Dialog suppApp = suppApp();
        suppApp.show();
    }

    public void  clickB_opt_help(View view){
        Dialog tutorial = help();
        tutorial.show();
    }

    public void clickB_sub_color(View view){
        final ColorPicker cp = new ColorPicker(MainActivity.this, Color.red(selectedColorRGB), Color.green(selectedColorRGB), Color.blue(selectedColorRGB));
        cp.show();

    /* On Click listener for the dialog, when the user select the color */
        Button okColor = (Button)cp.findViewById(R.id.okColorButton);
        okColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* You can get single channel (value 0-255) */
                //selectedColorR = cp.getRed();
                //selectedColorG = cp.getGreen();
                //selectedColorB = cp.getBlue();

                /* Or the android RGB Color (see the android Color class reference) */
                selectedColorRGB = cp.getColor();

                Button B_subject_color = (Button) findViewById(R.id.B_subject_color);
                B_subject_color.getBackground().setColorFilter(selectedColorRGB, PorterDuff.Mode.MULTIPLY);
                if(isColorDark(selectedColorRGB))B_subject_color.setTextColor(Color.WHITE);
                else B_subject_color.setTextColor(Color.BLACK);

                cp.dismiss();
            }
        });
    }

    public void clickB_sub_save(View view){
        EditText TF_subject = (EditText) findViewById(R.id.TF_subject);
        EditText TF_room = (EditText) findViewById(R.id.TF_room);
        EditText TF_teacher = (EditText) findViewById(R.id.TF_teacher);
        EditText TF_notes = (EditText) findViewById(R.id.TF_notes);

        String name = TF_subject.getText().toString().trim();
        String room = TF_room.getText().toString().trim();
        String teacher = TF_teacher.getText().toString().trim();
        String notes = TF_notes.getText().toString().trim();

        if(! name.equals("") && ! name.equals(null) && ! name.equals("+")){
            Subject subject = new Subject(name, room, teacher, notes, selectedColorRGB);
            //delete the subject if it already exists
            try {
                File dir = getFilesDir();
                File file = new File(dir, "SUBJECT-" + name + ".srl");
                file.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                String filename = "SUBJECT-" + subject.getName() + ".srl";
                ObjectOutput out = new ObjectOutputStream(new FileOutputStream(new File(getFilesDir(), "") + File.separator + filename));
                out.writeObject(subject);
                out.close();
                try {
                    filename = "DAY-" + selected_day_of_week + ".srl";
                    ObjectInputStream input = new ObjectInputStream(new FileInputStream(new File(new File(getFilesDir(), "") + File.separator + filename)));
                    String [] hours = (String []) input.readObject();
                    hours[selected_hour] = name;
                    if((selected_hour == 1 || selected_hour == 3 || selected_hour == 5) && (hours[selected_hour+1].equals("") || hours[selected_hour+1].equals(null))){
                        hours[selected_hour + 1] = name;
                    }
                    input.close();

                    out = new ObjectOutputStream(new FileOutputStream(new File(getFilesDir(), "") + File.separator + filename));
                    out.writeObject(hours);
                    out.close();
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.Toast_saved), Toast.LENGTH_SHORT).show();
                } catch (FileNotFoundException | ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Toast.makeText(getApplication(),"Gespeichert",Toast.LENGTH_SHORT);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //Hide keyboard
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        setContentView(R.layout.activity_main);
        setDay(0);
    }

    //Dialogs ---------------------------------------------------------------------------------
    private Dialog edit_hour(String [] options) {

        //Initialize the Alert Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String title = getResources().getString(R.string.dialog2_title, selected_hour);

        builder.setTitle(title).setSingleChoiceItems(options, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    Dialog select_subject = select_subject(false);
                    select_subject.show();
                }
                else if(which == 1){
                    createNewSubject();
                }
                else if(which == 2){
                    try {
                        String filename = "DAY-" + selected_day_of_week + ".srl";
                        ObjectInputStream input = new ObjectInputStream(new FileInputStream(new File(new File(getFilesDir(), "") + File.separator + filename)));
                        String[] hours = (String[]) input.readObject();
                        input.close();
                        hours[selected_hour] = null;

                        try {
                            ObjectOutput out = new ObjectOutputStream(new FileOutputStream(new File(getFilesDir(), "") + File.separator + filename));
                            out.writeObject(hours);
                            out.close();
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.Toast_saved), Toast.LENGTH_SHORT).show();
                            setDay(0);
                        } catch(IOException e) {
                            e.printStackTrace();
                        }
                    } catch(ClassNotFoundException | IOException e){
                        e.printStackTrace();
                    }
                }
                dialog.cancel();
            }
        });
        return builder.create();
    }

    private Dialog select_subject(final boolean delete) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        File[] files = getSubjectFiles();
        final String [] subject_names = new String[files.length];
        for(int i = 0; i < files.length; i++){
            try {
                String filename = files[i].getName();
                ObjectInputStream input = new ObjectInputStream(new FileInputStream(new File(new File(getFilesDir(), "") + File.separator + filename)));

                Subject subject = (Subject) input.readObject();
                subject_names[i] = subject.getName();
                input.close();
            } catch(ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
        }

        String title = getResources().getString(R.string.dialog3_title_1, selected_hour);
        if(delete) title = getResources().getString(R.string.D_delete_subject);
        builder.setTitle(title).setSingleChoiceItems(subject_names, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(!delete){
                    try {
                        String filename = "DAY-" + selected_day_of_week + ".srl";
                        ObjectInputStream input = new ObjectInputStream(new FileInputStream(new File(new File(getFilesDir(), "") + File.separator + filename)));
                        String [] hours = (String []) input.readObject();
                        hours[selected_hour] = subject_names[which];
                        if((selected_hour == 1 || selected_hour == 3 || selected_hour == 5) && (hours[selected_hour+1].equals("") || hours[selected_hour+1].equals(null))){
                            hours[selected_hour + 1] = subject_names[which];
                        }
                        input.close();
                        try {
                            ObjectOutput out = new ObjectOutputStream(new FileOutputStream(new File(getFilesDir(), "") + File.separator + filename));
                            out.writeObject(hours);
                            out.close();

                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.Toast_saved), Toast.LENGTH_SHORT).show();
                            dialog.cancel();
                            setDay(0);
                        } catch(IOException e) {
                            e.printStackTrace();
                        }
                    } catch(ClassNotFoundException | IOException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    try {
                        File dir = getFilesDir();
                        File file = new File(dir, "SUBJECT-" + subject_names[which] + ".srl");
                        boolean deleted = file.delete();
                        if(deleted) Toast.makeText(getApplicationContext(), getResources().getString(R.string.Toast_deleted), Toast.LENGTH_SHORT).show();
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                    dialog.cancel();
                }
                dialog.cancel();
            }
        });
        return builder.create();
    }

    private Dialog manage_days(final boolean startDay) {

        //Initialize the Alert Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String [] days = getResources().getStringArray(R.array.days);
        String[] options = {days[1], days[2], days[3], days[4], days[5], days[6], days[7]};

        builder.setTitle(null).setSingleChoiceItems(options, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Settings settings = new Settings(1, 5, 11, false);
                String filename = "SETTINGS.srl";
                try{
                    ObjectInputStream input = new ObjectInputStream(new FileInputStream(new File(new File(getFilesDir(), "") + File.separator + filename)));
                    settings = (Settings) input.readObject();
                } catch(ClassNotFoundException | IOException e) {
                    e.printStackTrace();
                }
                if(startDay) settings.setStart_day(which+1);
                else settings.setEnd_day(which+1);

                try {
                    ObjectOutput out = new ObjectOutputStream(new FileOutputStream(new File(getFilesDir(), "") + File.separator + filename));
                    out.writeObject(settings);
                    out.close();
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.Toast_saved), Toast.LENGTH_SHORT).show();
                    days_changed = true;
                } catch(IOException e) {
                    e.printStackTrace();
                }
                update_activityOptions(settings);
                dialog.cancel();
            }
        });
        return builder.create();
    }

    private Dialog maxHours() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String[] options = {"1","2","3","4","5","6","7","8","9","10","11"};

        builder.setTitle(null).setSingleChoiceItems(options, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Settings settings = new Settings(1, 5, 11, false);
                String filename = "SETTINGS.srl";
                try{
                    ObjectInputStream input = new ObjectInputStream(new FileInputStream(new File(new File(getFilesDir(), "") + File.separator + filename)));
                    settings = (Settings) input.readObject();
                } catch(ClassNotFoundException | IOException e) {
                    e.printStackTrace();
                }
                settings.setMax_hours(which+1);
                try {
                    ObjectOutput out = new ObjectOutputStream(new FileOutputStream(new File(getFilesDir(), "") + File.separator + filename));
                    out.writeObject(settings);
                    out.close();
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.Toast_saved), Toast.LENGTH_SHORT).show();
                } catch(IOException e) {
                    e.printStackTrace();
                }
                update_activityOptions(settings);
                dialog.cancel();
            }
        });
        return builder.create();
    }

    private Dialog help() {
        Resources r = getResources();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(r.getString(R.string.D_title_help)).setMessage(r.getString(R.string.D_help));
        return builder.create();
    }

    private Dialog suppApp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String[] options = getResources().getStringArray(R.array.suppAppChoices);

        builder.setTitle(null).setSingleChoiceItems(options, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Settings settings = new Settings(1, 5, 11, false);
                String filename = "SETTINGS.srl";
                try{
                    ObjectInputStream input = new ObjectInputStream(new FileInputStream(new File(new File(getFilesDir(), "") + File.separator + filename)));
                    settings = (Settings) input.readObject();
                } catch(ClassNotFoundException | IOException e) {
                    e.printStackTrace();
                }
                settings.setMax_hours(which+1);
                try {
                    ObjectOutput out = new ObjectOutputStream(new FileOutputStream(new File(getFilesDir(), "") + File.separator + filename));
                    out.writeObject(settings);
                    out.close();
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.Toast_saved), Toast.LENGTH_SHORT).show();
                } catch(IOException e) {
                    e.printStackTrace();
                }
                update_activityOptions(settings);
                dialog.cancel();
            }
        });
        return builder.create();
    }

    //Checkers ---------------------------------------------------------------------------------
    boolean isOdd(int val) { return (val & 0x01) != 0; }

    public boolean isColorDark(int color){
        double darkness = 1-(0.299*Color.red(color) + 0.587*Color.green(color) + 0.114*Color.blue(color))/255;
        return darkness >= 0.5;
    }

    //Getters ---------------------------------------------------------------------------------
    private int getDay_of_week(Calendar c){
        int day_of_week = c.get(Calendar.DAY_OF_WEEK) - 1;
        if(day_of_week == - 1) day_of_week = 6;
        else if(day_of_week == 0) day_of_week = 7;
        return  day_of_week;
    }

    private File[] getSubjectFiles() {
        return getFilesDir().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("SUBJECT-");
            }
        });
    }
}


