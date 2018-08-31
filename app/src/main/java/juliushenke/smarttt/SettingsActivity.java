package juliushenke.smarttt;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

public class SettingsActivity extends AppCompatActivity {

    private static Util util = new Util();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        updateActivitySettings();
        util.updateDesign(this, true);
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

        String[] days = getResources().getStringArray(R.array.days);
        Button B_startDay = findViewById(R.id.B_settings_startDay);
        Button B_endDay = findViewById(R.id.B_settings_endDay);
        Switch Switch_evenOddWeekSystem = findViewById(R.id.Switch_evenOddWeekSystem);
        Switch Switch_weekDisplay = findViewById(R.id.Switch_weekDisplay);

        B_startDay.setText(days[settings.getStart_day()]);
        B_endDay.setText(days[settings.getEnd_day()]);

        if (settings.getWeekSystem()) Switch_evenOddWeekSystem.setChecked(true);
        else Switch_evenOddWeekSystem.setChecked(false);
        if (settings.getShowWeek()) Switch_weekDisplay.setChecked(true);
        else Switch_weekDisplay.setChecked(false);

        Switch_evenOddWeekSystem.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) settings.setWeekSystem(true);
                else settings.setWeekSystem(false);
                util.saveSettings(a, settings);
            }
        });
        Switch_weekDisplay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) settings.setShowWeek(true);
                else settings.setShowWeek(false);
                util.saveSettings(a, settings);

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
        String[] days = getResources().getStringArray(R.array.days);
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
