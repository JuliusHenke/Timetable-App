package juliushenke.smarttt;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        updateDesign();
        updateActivitySettings();
    }

    //Voids --------------------------------------------------------------------------------
    private void updateActivitySettings() {
        String filename = "SETTINGS.srl";
        Settings settings = new Settings();
        try {
            ObjectInputStream input = new ObjectInputStream(new FileInputStream(new File(new File(getFilesDir(), "") + File.separator + filename)));
            settings = (Settings) input.readObject();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }

        String[] days = getResources().getStringArray(R.array.days);
        Button B_startDay = (Button) findViewById(R.id.B_settings_startDay);
        Button B_endDay = (Button) findViewById(R.id.B_settings_endDay);
        Switch Switch_evenOddWeekSystem = (Switch) findViewById(R.id.Switch_evenOddWeekSystem);
        Switch Switch_weekDisplay = (Switch) findViewById(R.id.Switch_weekDisplay);
        Switch Switch_darkDesign = (Switch) findViewById(R.id.Switch_darkDesign);

        B_startDay.setText(days[settings.getStart_day()]);
        B_endDay.setText(days[settings.getEnd_day()]);
        if (settings.getWeekSystem()) Switch_evenOddWeekSystem.setChecked(true);
        else Switch_evenOddWeekSystem.setChecked(false);
        if (settings.getShowWeek()) Switch_weekDisplay.setChecked(true);
        else Switch_weekDisplay.setChecked(false);
        if (settings.isDarkDesign()) Switch_darkDesign.setChecked(true);
        else Switch_darkDesign.setChecked(false);

        Switch_evenOddWeekSystem.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveSettings();
            }
        });
        Switch_weekDisplay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveSettings();
            }
        });
        Switch_darkDesign.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveSettings();
                updateDesign();
            }
        });
    }

    public void updateDesign(){
        Settings settings = new Settings();
        try {
            ObjectInputStream input = new ObjectInputStream(new FileInputStream(new File(new File(getFilesDir(), "") + File.separator + "SETTINGS.srl")));
            settings = (Settings) input.readObject();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }

        ScrollView scrollView = (ScrollView) findViewById(R.id.scrollview);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        toolbar.setTitle("");

        if(settings.isDarkDesign()) {
            scrollView.setBackgroundResource(R.drawable.background_gradient_dark);
            toolbar.setBackgroundResource(R.color.colorAppBarDark);
        }
        else{
            scrollView.setBackgroundResource(R.drawable.background_gradient);
            toolbar.setBackgroundResource(R.color.colorAppBar);
        }
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void saveSettings(){
        Switch Switch_evenOddWeekSystem = (Switch) findViewById(R.id.Switch_evenOddWeekSystem);
        Switch Switch_weekDisplay = (Switch) findViewById(R.id.Switch_weekDisplay);
        Switch Switch_darkDesign = (Switch) findViewById(R.id.Switch_darkDesign);

        String filename = "SETTINGS.srl";
        Settings settings = new Settings();
        try {
            ObjectInputStream input = new ObjectInputStream(new FileInputStream(new File(new File(getFilesDir(), "") + File.separator + filename)));
            settings = (Settings) input.readObject();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }

        if (Switch_evenOddWeekSystem.isChecked()) settings.setWeekSystem(true);
        else settings.setWeekSystem(false);

        if (Switch_weekDisplay.isChecked()) settings.setShowWeek(true);
        else settings.setShowWeek(false);

        if (Switch_darkDesign.isChecked()) settings.setDarkDesign(true);
        else settings.setDarkDesign(false);

        try {
            ObjectOutput out = new ObjectOutputStream(new FileOutputStream(new File(getFilesDir(), "") + File.separator + filename));
            out.writeObject(settings);
            out.close();
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.Saved), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Clickers ---------------------------------------------------------------------------------
    public void clickB_opt_startDay(View view) {
        D_startDay(true).show();
    }

    public void clickB_opt_endDay(View view) {
        D_startDay(false).show();
    }

    //Dialogs ---------------------------------------------------------------------------------
    private Dialog D_startDay(final boolean startDay) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String[] days = getResources().getStringArray(R.array.days);
        String[] options = {days[1], days[2], days[3], days[4], days[5], days[6], days[7]};

        builder.setTitle(null).setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Settings settings = new Settings();
                try {
                    ObjectInputStream input = new ObjectInputStream(new FileInputStream(new File(new File(getFilesDir(), "") + File.separator + "SETTINGS.srl")));
                    settings = (Settings) input.readObject();
                    input.close();
                } catch (ClassNotFoundException | IOException e) {
                    e.printStackTrace();
                }
                if (startDay){
                    settings.setStart_day(which + 1);
                }
                else{
                    settings.setEnd_day(which + 1);
                }

                try {
                    ObjectOutput out = new ObjectOutputStream(new FileOutputStream(new File(getFilesDir(), "") + File.separator + "SETTINGS.srl"));
                    out.writeObject(settings);
                    out.close();
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.Saved), Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                updateActivitySettings();
                dialog.cancel();
                if(settings.getStart_day() == settings.getEnd_day()) D_warn_manageDays().show();
            }
        });
        return builder.create();
    }

    private Dialog D_warn_manageDays() {
        Resources r = getResources();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(r.getString(R.string.D_warnManageDays_content)).setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        return builder.create();
    }

}
