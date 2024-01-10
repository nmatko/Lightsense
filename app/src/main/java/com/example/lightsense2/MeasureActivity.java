package com.example.lightsense2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import java.text.DecimalFormat;

public class MeasureActivity extends AppCompatActivity implements SensorEventListener {
    DecimalFormat df = new DecimalFormat("#.##");

    private SensorManager sensorManager;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "MyPrefs";
    private static final String MAX_VALUE_KEY = "maxValue";
    private static final String MIN_VALUE_KEY = "minValue";
    private Sensor lightSensor;
    LogFragment logFragment = new LogFragment();

    View bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_measure_home);
        Log.i("Measure","OVJDE U CREATU");
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        ImageButton resetbtn = findViewById(R.id.resetMinMaxBtn);

        resetbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                TextView minVw = findViewById(R.id.textViewLight);
                String minVal =  minVw.getText().toString();
                editor.putFloat(MAX_VALUE_KEY, 0f);
                editor.putFloat(MIN_VALUE_KEY, Float.parseFloat(minVal));
                editor.apply();
                setMinMaxValues();
            }
        } );

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        setMinMaxValues();
        bottomNavigationView = findViewById(R.id.bottom_nav);

        ImageButton homebutton =  bottomNavigationView.findViewById(R.id.homebutton);
        homebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MeasureActivity.this, MainActivity.class);
                startActivity(intent);
                //overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        ImageButton logbutton =  bottomNavigationView.findViewById(R.id.logbutton);

        logbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // getSupportFragmentManager().beginTransaction().replace(R.id.container,logFragment).commit();
                Intent intent = new Intent(MeasureActivity.this, LoggingActivity.class);
                startActivity(intent);
            }
        });
        ImageButton informationbtn = findViewById(R.id.informationbutton);
        informationbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                String exists = sharedPreferences.getString("noLightSensor", "exists");

                LayoutInflater inflater = getLayoutInflater();
                View layout = inflater.inflate(R.layout.custom_toast_layout, findViewById(R.id.customtoast));
                Log.i("MAIN", "log");
                String displayText="";
                String deviceName = sharedPreferences.getString("deviceName", "exists");
                String deviceType = sharedPreferences.getString("deviceType", "exists");
                String deviceVendor = sharedPreferences.getString("deviceVendor", "exists");

                displayText = displayText + "Device name: "+ deviceName + "\n";
                displayText = displayText + "Device type: " + deviceType+ "\n";
                displayText = displayText + "Device vendor: " + deviceVendor + "\n";
                System.out.println(displayText);
                Toast toast = new Toast(MeasureActivity.this);
                toast.setView(layout);
                toast.setDuration(Toast.LENGTH_LONG);
                TextView toastText = layout.findViewById(R.id.textViewToast);
                toastText.setText(displayText);

                toast.show();


            }
        });

    }


    public void checkMinMaxValue(float value){
        float storedMaxValue = sharedPreferences.getFloat(MAX_VALUE_KEY, Float.MIN_VALUE);
        float storedMinValue = sharedPreferences.getFloat(MIN_VALUE_KEY, Float.MAX_VALUE);
        if (value > storedMaxValue) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putFloat(MAX_VALUE_KEY, value);
            editor.apply();
            TextView vwMax = findViewById(R.id.textLightMax);
            vwMax.setText(df.format(value));
        }

// Update min value if needed
        if (value < storedMinValue) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putFloat(MIN_VALUE_KEY, value);
            editor.apply();
            TextView vwMin = findViewById(R.id.textLightMin);
            vwMin.setText(df.format(value));
        }

    }
    public void setMinMaxValues(){
        float storedMaxValue = sharedPreferences.getFloat(MAX_VALUE_KEY, Float.MIN_VALUE);
        float storedMinValue = sharedPreferences.getFloat(MIN_VALUE_KEY, Float.MAX_VALUE);

        TextView vwMin = findViewById(R.id.textLightMin);
        vwMin.setText(df.format(storedMinValue));
        TextView vwMax = findViewById(R.id.textLightMax);
        vwMax.setText(df.format(storedMaxValue));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            float lightValue = event.values[0]; // Get the light value in lux
            TextView vw = findViewById(R.id.textViewLight);
            checkMinMaxValue(lightValue);
            Log.i("TAG","OVDJE");

            vw.setText(df.format(lightValue));

            // Do something with the light value (e.g., display it, perform actions based on the light level)
        }
    }
    protected void onResume() {
        super.onResume();
        if (lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


}