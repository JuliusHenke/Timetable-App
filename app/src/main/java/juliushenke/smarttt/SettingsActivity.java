package juliushenke.smarttt;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends AppCompatActivity {

    private static final Util util = new Util();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        updateActivitySettings();
        util.updateDesign(this, true);

        final EditText[] hourTimeInputs = getHourTimeInputs();
        for (EditText hourTimeInput : hourTimeInputs) {
            hourTimeInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) { }

                @Override
                public void afterTextChanged(Editable editable) {
                    String[] hourTimes = new String[hourTimeInputs.length];
                    for (int i = 0; i < hourTimes.length; i++) {
                        hourTimes[i] = hourTimeInputs[i].getText().toString();
                    }
                    final Settings settings = util.readSettings(SettingsActivity.this);
                    settings.setHourTimes(hourTimes);
                    util.saveSettings(SettingsActivity.this, settings);
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
    private void updateActivitySettings() {
        final Settings settings = util.readSettings(this);
        final AppCompatActivity a = this;

        String[] days = MainActivity.res.getStringArray(R.array.days);
        Button B_startDay = findViewById(R.id.B_settings_startDay);
        Button B_endDay = findViewById(R.id.B_settings_endDay);
        SwitchMaterial Switch_evenOddWeekSystem = findViewById(R.id.Switch_evenOddWeekSystem);
        SwitchMaterial Switch_weekDisplay = findViewById(R.id.Switch_weekDisplay);
        SwitchMaterial Switch_hourTimes = findViewById(R.id.Switch_hourTimes);

        B_startDay.setText(days[settings.getStart_day()]);
        B_endDay.setText(days[settings.getEnd_day()]);

        Switch_evenOddWeekSystem.setChecked(settings.getWeekSystem());
        Switch_weekDisplay.setChecked(settings.getShowWeek());
        Switch_hourTimes.setChecked(settings.getShowHourTimes());

        LinearLayout hourTimeLayout = findViewById(R.id.hourTimes);
        hourTimeLayout.setVisibility(settings.getShowHourTimes() ? View.VISIBLE : View.INVISIBLE);
        if(settings.getShowHourTimes()) {
            final EditText[] hourTimeInputs = getHourTimeInputs();
            final String[] hourTimes = settings.getHourTimes();

            if (hourTimes != null) {
                for (int i=0; i < hourTimeInputs.length && i < hourTimes.length; i++) {
                    hourTimeInputs[i].setText(hourTimes[i]);
                }
            }
        }

        Switch_evenOddWeekSystem.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.setWeekSystem(isChecked);
                util.saveSettings(a, settings);
            }
        });
        Switch_weekDisplay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.setShowWeek(isChecked);
                util.saveSettings(a, settings);

            }
        });
        Switch_hourTimes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.setShowHourTimes(isChecked);
                util.saveSettings(a, settings);
                updateActivitySettings();
            }
        });
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
        String[] days = MainActivity.res.getStringArray(R.array.days);
        String[] options = {days[1], days[2], days[3], days[4], days[5], days[6], days[7]};
        final Settings settings = util.readSettings(this);
        final AppCompatActivity a = this;

        builder.setTitle(null).setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (startDay) settings.setStart_day(which + 1);
                else settings.setEnd_day(which + 1);
                util.saveSettings(a, settings);
                dialog.cancel();

                if(settings.getStart_day() == settings.getEnd_day()) D_warn_manageDays().show();
                updateActivitySettings();
            }
        });
        return builder.create();
    }

    private Dialog D_warn_manageDays() {
        Resources r = MainActivity.res;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(r.getString(R.string.D_warnManageDays_content)).setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        return builder.create();
    }

    private EditText[] getHourTimeInputs() {
        return new EditText[]{
                findViewById(R.id.time1), findViewById(R.id.time2), findViewById(R.id.time3),
                findViewById(R.id.time4), findViewById(R.id.time5), findViewById(R.id.time6),
                findViewById(R.id.time7), findViewById(R.id.time8), findViewById(R.id.time9),
                findViewById(R.id.time10), findViewById(R.id.time11)
        };
    }
}
