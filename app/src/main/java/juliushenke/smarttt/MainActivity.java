package juliushenke.smarttt;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static Util util = new Util();
    public static Resources res;
    private static int selected_day_of_week = 0;
    private static int saved_change = 0;

    private static int selected_hour = 1;
    private static String selected_subject = "";

    private static boolean newSubject = true;
    private static boolean selected_evenWeeks = true;
    private static boolean week_isOdd = false;
    private static boolean editing_mode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        res = getResources();
        if(util.isFirstTimeRunning(this)) initialSetup();
        updateActivityMain();
        util.updateDesign(this, false);

        final Button[] Bts = {null,
                findViewById(R.id.h1), findViewById(R.id.h2), findViewById(R.id.h3),
                findViewById(R.id.h4), findViewById(R.id.h5), findViewById(R.id.h6),
                findViewById(R.id.h7), findViewById(R.id.h8), findViewById(R.id.h9),
                findViewById(R.id.h10), findViewById(R.id.h11)};

        for(int i=1; i <= 11; i++){
            Bts[i].setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    selected_subject = getButtonName(v);
                    newSubject = selected_subject.equals("+");
                    setActivitySubject();
                    return true;
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateActivityMain();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        MenuItem item = menu.findItem(R.id.menu_editMode);
        if (editing_mode) item.setIcon(R.drawable.ic_done_white_24dp);
        else item.setIcon(R.drawable.ic_edit_white_24dp);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_editMode:
                changeMode();
                return true;

            case R.id.menu_editSubject:
                D_editSubject().show();
                return true;

            case R.id.menu_deleteSubject:
                D_deleteSubject().show();
                return true;

            case R.id.menu_settings:
                setActivitySettings();
                return true;

            case R.id.menu_aboutApp:
                D_aboutApp().show();
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
            if (editing_mode) changeMode();
            else finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //Voids --------------------------------------------------------------------------------
    private void updateActivityMain(){
        setDay(0);
    }

    private void setActivitySettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void setActivitySubject() {
        Intent intent = new Intent(this, SubjectActivity.class);
        startActivity(intent);
    }

    private void setDay(int shift) {
        Settings settings = util.readSettings(this);

        //Mathematical calculation of the new date
        Calendar c = Calendar.getInstance();
        saved_change = saved_change + shift;
        c.add(Calendar.DAY_OF_YEAR, saved_change);
        selected_day_of_week = getDayOfWeek(c);

        int extra_change = 0;
        //positive change - into the future
        if (0 <= shift) {
            if (selected_day_of_week < settings.getStart_day())
                extra_change = settings.getStart_day() - selected_day_of_week;
            else if (selected_day_of_week > settings.getEnd_day())
                extra_change = 7 - selected_day_of_week + settings.getStart_day();
        }
        //negative change - into the past
        else{
            if (selected_day_of_week < settings.getStart_day())
                extra_change = -selected_day_of_week - (7 - settings.getEnd_day());
            else if (selected_day_of_week > settings.getEnd_day())
                extra_change = settings.getEnd_day() - selected_day_of_week;
        }
        //update the Calendar
        c.add(Calendar.DAY_OF_YEAR, extra_change);
        saved_change = saved_change + extra_change;

        //update the day_of_week
        selected_day_of_week = getDayOfWeek(c);

        String[] days = res.getStringArray(R.array.days);
        TextView TV_main_day = findViewById(R.id.TV_main_day);
        TV_main_day.setText(days[selected_day_of_week]);

        Button B_main_dateLeft = findViewById(R.id.B_main_dateLeft);
        Button B_main_dateCenter = findViewById(R.id.B_main_dateCenter);
        Button B_main_dateRight = findViewById(R.id.B_main_dateRight);
        Button B_main_weekType = findViewById(R.id.B_main_weekType);

        int date_week = c.get(Calendar.WEEK_OF_YEAR);
        week_isOdd = util.isIntOdd(date_week);

        //updating views of activity_main -------------------------------------------------------------------------------
        B_main_dateLeft.setVisibility(View.VISIBLE);
        B_main_dateRight.setVisibility(View.VISIBLE);
        if (editing_mode) {
            B_main_dateCenter.setVisibility(View.INVISIBLE);
            if (selected_day_of_week == settings.getStart_day())
                B_main_dateLeft.setVisibility(View.INVISIBLE);
            if (selected_day_of_week == settings.getEnd_day())
                B_main_dateRight.setVisibility(View.INVISIBLE);
            B_main_dateCenter.setText("");
            if (settings.getWeekSystem()) {
                B_main_dateCenter.setVisibility(View.GONE);
                B_main_weekType.setVisibility(View.VISIBLE);
                if (selected_evenWeeks) {
                    B_main_weekType.setText(R.string.Menu_evenWeek);
                } else {
                    B_main_weekType.setText(R.string.Menu_oddWeek);
                }
            } else {
                B_main_weekType.setVisibility(View.GONE);
            }
        } else {
            DateFormat DATE_FORMAT = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
            String string_date = DATE_FORMAT.format(c.getTime());
            String s = string_date + " (" + res.getString(R.string.week) + " " + date_week + ")";
            if (settings.getShowWeek()) B_main_dateCenter.setText(s);
            else B_main_dateCenter.setText(string_date);
            B_main_dateCenter.setVisibility(View.VISIBLE);
            B_main_weekType.setVisibility(View.GONE);
        }

        final TextView[] TVs_room = {null,
                findViewById(R.id.r1), findViewById(R.id.r2), findViewById(R.id.r3),
                findViewById(R.id.r4), findViewById(R.id.r5), findViewById(R.id.r6),
                findViewById(R.id.r7), findViewById(R.id.r8), findViewById(R.id.r9),
                findViewById(R.id.r10), findViewById(R.id.r11)};

        final TextView[] TVs_hour = {null,
                findViewById(R.id.n1), findViewById(R.id.n2), findViewById(R.id.n3),
                findViewById(R.id.n4), findViewById(R.id.n5), findViewById(R.id.n6),
                findViewById(R.id.n7), findViewById(R.id.n8), findViewById(R.id.n9),
                findViewById(R.id.n10), findViewById(R.id.n11)};

        final Button[] Bts = {null,
                findViewById(R.id.h1), findViewById(R.id.h2), findViewById(R.id.h3),
                findViewById(R.id.h4), findViewById(R.id.h5), findViewById(R.id.h6),
                findViewById(R.id.h7), findViewById(R.id.h8), findViewById(R.id.h9),
                findViewById(R.id.h10), findViewById(R.id.h11)};

        for (int i = 1; i <= 11; i++) {
            TVs_room[i].setText("");
            Bts[i].setTextColor(Color.BLACK);
            Bts[i].getBackground().setColorFilter(null);
            if (editing_mode) {
                Bts[i].setVisibility(View.VISIBLE);
                Bts[i].setText("+");
                TVs_hour[i].setVisibility(View.VISIBLE);
            } else {
                Bts[i].setVisibility(View.INVISIBLE);
                TVs_hour[i].setVisibility(View.INVISIBLE);
            }
        }
        try {
            String filename = util.getFilenameForDay(this);
            ObjectInputStream input = new ObjectInputStream(new FileInputStream(new File(new File(getFilesDir(), "") + File.separator + filename)));
            String[] hours = (String[]) input.readObject();
            for (int i = 1; i <= 11; i++) {
                if(hours[i] != null && !hours[i].equals("")) {
                    try {
                        filename = "SUBJECT-" + hours[i] + ".srl";
                        input = new ObjectInputStream(new FileInputStream(new File(new File(getFilesDir(), "") + File.separator + filename)));
                        Subject s = (Subject) input.readObject();

                        //TextViews (number of hour)
                        for (TextView view : TVs_hour) {
                            if(view != null) view.setVisibility(View.VISIBLE);
                        }


                        //Buttons (subject name)
                        Bts[i].setVisibility(View.VISIBLE);
                        Bts[i].setText(s.getName());
                        Bts[i].getBackground().setColorFilter(s.getColor(), PorterDuff.Mode.MULTIPLY);
                        if (util.isColorDark(s.getColor())) Bts[i].setTextColor(Color.WHITE);
                        else Bts[i].setTextColor(Color.BLACK);

                        //TextViews (subject room)
                        if (!s.getName().equals(hours[i - 1])) TVs_room[i].setText(s.getRoom());
                    } catch (FileNotFoundException e) {
                        filename = util.getFilenameForDay(this);
                        try {
                            hours[i] = "";
                            ObjectOutput out = new ObjectOutputStream(new FileOutputStream(new File(getFilesDir(), "") + File.separator + filename));
                            out.writeObject(hours);
                            out.close();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                        e.printStackTrace();
                    } catch (ClassNotFoundException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            input.close();
        } catch (FileNotFoundException e) {
            String[] hours = new String[12];
            String outputName = util.getFilenameForDay(this);
            try {
                ObjectOutput out = new ObjectOutputStream(new FileOutputStream(new File(getFilesDir(), "") + File.separator + outputName));
                out.writeObject(hours);
                out.close();
            } catch (IOException e1) {
                e.printStackTrace();
            }
            if (editing_mode && settings.getWeekSystem()) D_copyDay();

        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }

    private void changeMode() {
        if (!editing_mode) {
            editing_mode = true;

        } else {
            editing_mode = false;
            int act_day_of_week = getDayOfWeek(Calendar.getInstance());
            saved_change = selected_day_of_week - act_day_of_week;
        }
        updateActivityMain();
        invalidateOptionsMenu();
    }

    private void initialSetup() {
        changeMode();
        setDay(8 - selected_day_of_week);

        //Setup preset subjects
        try {
            String[] subject_names = res.getStringArray(R.array.subject_names);
            int[] colors = res.getIntArray(R.array.subject_colors);
            for (int i = 0; i < subject_names.length && subject_names.length <= colors.length; i++) {
                Subject subject = new Subject(subject_names[i], null, null, colors[i]);
                try {
                    String filename = "SUBJECT-" + subject.getName() + ".srl";
                    ObjectOutput out = new ObjectOutputStream(new FileOutputStream(new File(getFilesDir(), "") + File.separator + filename));
                    out.writeObject(subject);
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        D_intro().show();
    }

    //Clickers ---------------------------------------------------------------------------------
    public void click_B_main_weekType(View view) {
        Button B_main_weekType = findViewById(R.id.B_main_weekType);

        PopupMenu popup = new PopupMenu(MainActivity.this, B_main_weekType);
        //Inflating the Popup using xml file
        if (selected_evenWeeks)
            popup.getMenuInflater().inflate(R.menu.menu_even_weeks, popup.getMenu());
        else popup.getMenuInflater().inflate(R.menu.menu_odd_weeks, popup.getMenu());

        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.act_evenWeeks) selected_evenWeeks = true;
                else if (item.getItemId() == R.id.act_oddWeeks) selected_evenWeeks = false;
                updateActivityMain();
                return true;
            }
        });

        popup.show();
    }

    public void clickB_main_dateLeft(View view) {
        setDay(-1);
        updateActivityMain();
    }

    public void clickB_main_dateCenter(View view) {
        if (!editing_mode) saved_change = 0;
        updateActivityMain();
    }

    public void clickB_main_dateRight(View view) {
        setDay(+1);
        updateActivityMain();
    }

    public void clickB_main_editHour(View view) {
        selected_subject = getButtonName(view);

        if (view == findViewById(R.id.h1)) selected_hour = 1;
        else if (view == findViewById(R.id.h2)) selected_hour = 2;
        else if (view == findViewById(R.id.h3)) selected_hour = 3;
        else if (view == findViewById(R.id.h4)) selected_hour = 4;
        else if (view == findViewById(R.id.h5)) selected_hour = 5;
        else if (view == findViewById(R.id.h6)) selected_hour = 6;
        else if (view == findViewById(R.id.h7)) selected_hour = 7;
        else if (view == findViewById(R.id.h8)) selected_hour = 8;
        else if (view == findViewById(R.id.h9)) selected_hour = 9;
        else if (view == findViewById(R.id.h10)) selected_hour = 10;
        else if (view == findViewById(R.id.h11)) selected_hour = 11;

        if (!editing_mode) {
            newSubject = false;
            setActivitySubject();
        } else {
            if(selected_subject.equals("+")){ //hour without subject
                if (getSubjectNames().length > 0) {
                    D_editEmptyHour().show();
                } else {
                    newSubject = true;
                    setActivitySubject();
                }
            }
            else { //hour with subject
                D_editTakenHour().show();
            }
        }
    }

    //Dialogs ---------------------------------------------------------------------------------
    private Dialog D_intro() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(res.getString(R.string.D_intro_title)).setMessage(res.getString(R.string.D_intro_content)).setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Settings settings = util.readSettings(MainActivity.this);
                settings.setWeekSystem(true);
                settings.setShowWeek(true);
                util.saveSettings(MainActivity.this, settings);

                updateActivityMain();
                dialog.cancel();
            }
        }).setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        return builder.create();
    }

    private void D_copyDay() {
        final String outputName = util.getFilenameForDay(this);
        String inputName = "DAY-" + selected_day_of_week + ".srl";
        String title = res.getString(R.string.D_copyEvenDay);

        if (outputName.equals("DAY-" + selected_day_of_week + ".srl")) {
            inputName = "ODD-DAY-" + selected_day_of_week + ".srl";
            title = res.getString(R.string.D_copyOddDay);
        }

        String[] hoursTemp = new String[12];
        boolean inputEmpty = true;
        //read the input
        try {
            ObjectInputStream input = new ObjectInputStream(new FileInputStream(new File(new File(getFilesDir(), "") + File.separator + inputName)));
            hoursTemp = (String[]) input.readObject();
            for(String s : hoursTemp){
                if(s != null && !s.equals("")){
                    inputEmpty = false;
                    break;
                }
            }
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        final String [] hours = hoursTemp;

        if(!inputEmpty) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(title).setMessage(null).setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //overwrite the output with input
                    try {
                        ObjectOutput out = new ObjectOutputStream(new FileOutputStream(new File(getFilesDir(), "") + File.separator + outputName));
                        out.writeObject(hours);
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    updateActivityMain();
                    dialog.cancel();
                }
            }).setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.create().show();
        }
    }

    private Dialog D_editEmptyHour() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final String[] subject_names = getSubjectNames();
        final AppCompatActivity a = this;

        builder.setTitle(res.getString(R.string.D_editEmptyHour_title))
            .setItems(subject_names, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        String filename = util.getFilenameForDay(a);
                        ObjectInputStream input = new ObjectInputStream(new FileInputStream(new File(new File(getFilesDir(), "") + File.separator + filename)));
                        String[] hours = (String[]) input.readObject();
                        input.close();

                        selected_subject = subject_names[which];
                        hours[selected_hour] = selected_subject;
                        try {
                            ObjectOutput out = new ObjectOutputStream(new FileOutputStream(new File(getFilesDir(), "") + File.separator + filename));
                            out.writeObject(hours);
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (ClassNotFoundException | IOException e) {
                        e.printStackTrace();
                    }
                    dialog.cancel();
                    try {
                        String filename = "SUBJECT-" + selected_subject + ".srl";
                        ObjectInputStream input = new ObjectInputStream(new FileInputStream(new File(new File(getFilesDir(), "") + File.separator + filename)));
                        Subject subject = (Subject) input.readObject();
                        input.close();

                        if(subject.getTeacher() == null){
                            subject.setTeacher("");
                            try {
                                ObjectOutput out = new ObjectOutputStream(new FileOutputStream(new File(getFilesDir(), "") + File.separator + filename));
                                out.writeObject(subject);
                                out.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            newSubject = false;
                            setActivitySubject();
                        }
                    } catch (ClassNotFoundException | IOException e) {
                        e.printStackTrace();
                    }
                    updateActivityMain();
                }
            })
            .setPositiveButton(res.getString(R.string.D_editEmptyHour_YES), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                    newSubject = true;
                    setActivitySubject();
                }
            });
        return builder.create();
    }

    private Dialog D_editTakenHour() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final String[] options = {res.getString(R.string.free_period), res.getString(R.string.different_subject)};
        final AppCompatActivity a = this;

        builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0){
                            dialog.cancel();
                            try {
                                String filename = util.getFilenameForDay(a);
                                ObjectInputStream input = new ObjectInputStream(new FileInputStream(new File(new File(getFilesDir(), "") + File.separator + filename)));
                                String[] hours = (String[]) input.readObject();
                                input.close();
                                hours[selected_hour] = null;

                                try {
                                    ObjectOutput out = new ObjectOutputStream(new FileOutputStream(new File(getFilesDir(), "") + File.separator + filename));
                                    out.writeObject(hours);
                                    out.close();
                                    updateActivityMain();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } catch (ClassNotFoundException | IOException e) {
                                e.printStackTrace();
                            }
                        }
                        else if(which == 1){
                            dialog.cancel();
                            D_editEmptyHour().show();
                        }
                    }
                });
        return builder.create();
    }

    private Dialog D_editSubject() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final String[] subject_names = getSubjectNames();

        builder.setTitle(res.getString(R.string.D_editSubject_title))
                .setItems(subject_names, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selected_subject = subject_names[which];
                        newSubject = false;
                        dialog.cancel();
                        setActivitySubject();
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

    private Dialog D_deleteSubject() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final String[] subject_names = getSubjectNames();

        builder.setTitle(res.getString(R.string.D_deleteSubject_title))
                .setItems(subject_names, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            File dir = getFilesDir();
                            File file = new File(dir, "SUBJECT-" + subject_names[which] + ".srl");
                            boolean deleted = file.delete();
                            if (deleted)
                                Toast.makeText(getApplicationContext(), res.getString(R.string.Deleted), Toast.LENGTH_SHORT).show();
                            updateActivityMain();
                        } catch (Exception e) {
                            e.printStackTrace();
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

    private Dialog D_aboutApp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(null).setMessage(res.getString(R.string.D_aboutApp_content));
        return builder.create();
    }

    //Getters ---------------------------------------------------------------------------------
    private int getDayOfWeek(Calendar c) {
        int day_of_week = c.get(Calendar.DAY_OF_WEEK) - 1;
        if (day_of_week == -1) day_of_week = 6;
        else if (day_of_week == 0) day_of_week = 7;
        return day_of_week;
    }

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

    public static int getSelected_day_of_week(){
        return selected_day_of_week;
    }

    public static int getSelected_hour(){
        return  selected_hour;
    }

    public static String getSelected_subject(){
        return  selected_subject;
    }

    public static boolean isNewSubject() {
        return newSubject;
    }

    public static boolean getSelected_evenWeeks() {
        return selected_evenWeeks;
    }

    public static boolean getWeek_isOdd() {
        return week_isOdd;
    }

    public static boolean isEditingMode() { return editing_mode; }

    private String getButtonName(View v){
        Button btn = (Button) v;
        return btn.getText().toString();
    }

}


