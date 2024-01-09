package com.example.lightsense2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.view.WindowCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.lightsense2.databinding.ActivityLoggingLightBinding;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoggingActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor lightSensor;
    Boolean record = false;
    View bottomNavigationView;

    MeasureHome measureHome = new MeasureHome();
    HomeFragment home = new HomeFragment();

    private static final int ACCESS_URI = 1;
    private static final String PREF_NAME = "MyPrefs";
    private SharedPreferences sharedPreferences;
    private static final String MAX_VALUE_KEY = "maxValue";
    private static final String MIN_VALUE_KEY = "minValue";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logging_light);
        bottomNavigationView = findViewById(R.id.bottom_nav);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        ImageButton measureButton =  bottomNavigationView.findViewById(R.id.measurebutton);

        measureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager().beginTransaction().replace(R.id.container,measureHome).commit();
                Intent intent = new Intent(LoggingActivity.this, MeasureActivity.class);
                startActivity(intent);
            }
        });
        ImageButton homebutton =  bottomNavigationView.findViewById(R.id.homebutton);

        homebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager().beginTransaction().replace(R.id.container,home).commit();
                Intent intent = new Intent(LoggingActivity.this, MainActivity.class);
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
                Toast toast = new Toast(LoggingActivity.this);
                toast.setView(layout);
                toast.setDuration(Toast.LENGTH_LONG);
                TextView toastText = layout.findViewById(R.id.textViewToast);
                toastText.setText(displayText);

                toast.show();
            }
        });

        Button recordbutton = findViewById(R.id.recordbutton);

        recordbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                String exists = sharedPreferences.getString("locationUri", "none");
                if(exists.equals("none")){
                    return;
                }else {
                    Log.i("RECORD","recording");
                }


            }
        });
        Uri locationUri = Uri.parse(sharedPreferences.getString("locationUri", "none"));


    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("onSTART", "u on startu");
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {


            float lightValue = event.values[0]; // Get the light value in lux


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