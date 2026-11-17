package com.example.push_upcounterapp;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor proximitySensor;

    private TextView tvCount;
    private Button btnReset;

    private int pushUpCount = 0;
    private boolean isDown = false; // Tracks if the user is currently in the down position
    private ToneGenerator toneGenerator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Initialize UI components
        tvCount = findViewById(R.id.tvCount);
        btnReset = findViewById(R.id.btnReset);

        // Initialize ToneGenerator for audio feedback (beep on count)
        toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);

        // Initialize SensorManager and Proximity Sensor
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        }

        // Check if the device actually has a proximity sensor
        if (proximitySensor == null) {
            Toast.makeText(this, "Proximity sensor not available on this device!", Toast.LENGTH_LONG).show();
        }
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pushUpCount = 0;
                isDown = false;
                tvCount.setText(String.valueOf(pushUpCount));
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Register the sensor listener when the app is active
        if (proximitySensor != null) {
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the sensor listener to save battery when app is in background
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            float distance = event.values[0];

            // If distance is less than the sensor's maximum range, an object (your chest) is close
            if (distance < proximitySensor.getMaximumRange()) {
                isDown = true;
            } else {
                // If you were down and now moved back up, count 1 push-up
                if (isDown) {
                    pushUpCount++;
                    tvCount.setText(String.valueOf(pushUpCount));

                    // Play a short beep sound
                    toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 150);

                    isDown = false;
                }
            }
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed for proximity sensor
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (toneGenerator != null) {
            toneGenerator.release();
        }
    }
}