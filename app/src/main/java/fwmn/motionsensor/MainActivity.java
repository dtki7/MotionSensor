package fwmn.motionsensor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener, OnSeekBarChangeListener {

    @SuppressLint("ConstantLocale")
    private static final SimpleDateFormat dateFormat
            = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault());

    private float sensitivity;
    private float[] lastValues = {0, 0, 0};
    private int counter = 0;

    private void init() {
        SensorManager manager = (SensorManager) getSystemService((Context.SENSOR_SERVICE));
        if (manager == null) return;
        Sensor accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        counter = -1;

        ((TextView) findViewById(R.id.startedText)).setText((new Date()).toString());
        ((TextView) findViewById(R.id.countText)).setText(String.valueOf(0));

        ((LinearLayout) findViewById(R.id.listLayout)).removeAllViews();
    }

    public void onResetButtonClick(View view) {
        init();
    }

    public void onStopButtonClick(View view) throws ParseException {
        SensorManager manager = (SensorManager) getSystemService((Context.SENSOR_SERVICE));
        if (manager == null) return;
        manager.unregisterListener(this);

        Date currentDate = new Date(System.currentTimeMillis() - 10 * 1000);

        LinearLayout listLayout = findViewById(R.id.listLayout);
        for (int i = listLayout.getChildCount() - 1; i >= 0; i--) {
            LinearLayout entry = (LinearLayout) listLayout.getChildAt(i);
            Date date = dateFormat.parse((String) ((TextView) entry.getChildAt(0)).getText());
            if (currentDate.compareTo(date) < 0) {
                listLayout.removeView(entry);
                counter--;
            } else {
                break;
            }
        }

        TextView lastEntry = new TextView(this);
        lastEntry.setText(dateFormat.format(new Date()));
        ((LinearLayout) findViewById(R.id.listLayout)).addView(lastEntry);

        ((TextView) findViewById(R.id.countText)).setText(String.valueOf(counter));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.hide();

        SeekBar seekBar = findViewById(R.id.sensitivityBar);
        sensitivity = (seekBar.getProgress() + 1) / (float) 100;
        seekBar.setOnSeekBarChangeListener(this);

        init();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] change = new float[3];
        boolean significantChange = false;
        for (int i = 0; i < event.values.length; i++) {
            change[i] = Math.abs(event.values[i] - lastValues[i]);
            if (change[i] > sensitivity) {
                significantChange = true;
            }
        }

        if (counter == -1) {
            counter = 0;
            lastValues = event.values.clone();
            return;
        }

        if (significantChange) {
            float intensity = 0;
            for (float v : change) {
                intensity += v / sensitivity;
            }
            intensity /= 3;

            TextView dateText = new TextView(this);
            dateText.setText(dateFormat.format(new Date()));
            TextView intensityText = new TextView(this);
            intensityText.setText(String.valueOf(intensity));
            intensityText.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            intensityText.setGravity(Gravity.END);
            LinearLayout entry = new LinearLayout(this);
            entry.setOrientation(LinearLayout.HORIZONTAL);
            entry.addView(dateText);
            entry.addView(intensityText);
            ((LinearLayout) findViewById(R.id.listLayout)).addView(entry);

            ScrollView scrollView = findViewById(R.id.scrollView);
            scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));

            counter += 1;
            ((TextView) findViewById(R.id.countText)).setText(String.valueOf(counter));

            lastValues = event.values.clone();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        sensitivity = (progress + 1) / (float) 100;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
}