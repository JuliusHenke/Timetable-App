package juliushenke.smarttt;

import android.graphics.Color;
import android.os.Build;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ScrollView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

class Util {
    void updateDesign(AppCompatActivity a, boolean homeAsUp){
        ScrollView scrollView = a.findViewById(R.id.scrollview);
        Toolbar toolbar = a.findViewById(R.id.app_bar);
        toolbar.setTitle("");
        try {
            scrollView.setBackgroundResource(R.drawable.background_gradient);
            toolbar.setBackgroundResource(R.color.colorAppBar);
        } catch (Exception e){
            e.printStackTrace();
        }
        a.setSupportActionBar(toolbar);
        try{
            if(homeAsUp && a.getSupportActionBar() != null) {
                a.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        } catch(NullPointerException e){
            e.printStackTrace();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            Window window = a.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(a.getResources().getColor(R.color.colorStatusBar));
        }
    }

    Settings readSettings(AppCompatActivity a){
        Settings settings = new Settings();
        try {
            ObjectInputStream input = new ObjectInputStream(new FileInputStream(new File(new File(a.getFilesDir(), "") + File.separator + "SETTINGS.srl")));
            settings = (Settings) input.readObject();
        } catch (FileNotFoundException e) {
            saveSettings(a, settings);
            e.printStackTrace();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return settings;
    }

    boolean isFirstTimeRunning(AppCompatActivity a){
        File testFile = new File(new File(a.getFilesDir(), "") + File.separator + "SETTINGS.srl");
        return !testFile.exists();
    }

    void saveSettings(AppCompatActivity a, Settings settings){
        try {
            ObjectOutput out = new ObjectOutputStream(new FileOutputStream(new File(a.getFilesDir(), "") + File.separator + "SETTINGS.srl"));
            out.writeObject(settings);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    String getFilenameForDay(AppCompatActivity a) {
        Settings settings = readSettings(a);
        if (settings.getWeekSystem()) {
            if (MainActivity.isEditingMode() && !MainActivity.getSelected_evenWeeks()) return "ODD-DAY-" + MainActivity.getSelected_day_of_week() + ".srl";
            else if (!MainActivity.isEditingMode() && MainActivity.getWeek_isOdd()) return "ODD-DAY-" + MainActivity.getSelected_day_of_week() + ".srl";
        }
        return "DAY-" + MainActivity.getSelected_day_of_week() + ".srl";
    }

    boolean isIntOdd(int val) {
        return (val & 0x01) != 0;
    }

    boolean isColorDark(int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return darkness >= 0.5;
    }
}
