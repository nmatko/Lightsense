package com.example.lightsense2;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    View bottomNavigationView;

    Uri locationUri;
    HomeFragment home = new HomeFragment();
    private static final int ACCESS_URI = 1;
    private static final String PREF_NAME = "MyPrefs";


    void setManufacturerInfo() {

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if (lightSensor != null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("deviceName", lightSensor.getName());
            editor.putString("deviceType", lightSensor.getStringType());
            editor.putString("deviceVendor", lightSensor.getVendor());// Store a string value with a key
            editor.apply();
        } else {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("noLightSensor", "This device doesn't have a functioning light sensor.\n Unfortunately you wont be able to use this app.");
            editor.apply();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigationView = findViewById(R.id.bottom_nav);

        setManufacturerInfo();


        ImageButton measureButton = bottomNavigationView.findViewById(R.id.measurebutton);
        measureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getSupportFragmentManager().beginTransaction().replace(R.id.container,measureHome).commit();
                Intent intent = new Intent(MainActivity.this, MeasureActivity.class);
                startActivity(intent);
            }
        });
        ImageButton logbutton = bottomNavigationView.findViewById(R.id.logbutton);

        logbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getSupportFragmentManager().beginTransaction().replace(R.id.container,logFragment).commit();
                SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

                String locuri = sharedPreferences.getString("locationUri", "none");
                if (!locuri.equals("none")) {
                    Intent intent = new Intent(MainActivity.this, LoggingActivity.class);
                    startActivity(intent);
                } else {
                    LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.custom_toast_layout, findViewById(R.id.customtoast));
                    layout.setBackgroundColor(Color.parseColor("#4e348b"));
                    Toast toast = new Toast(MainActivity.this);
                    toast.setView(layout);
                    toast.setDuration(Toast.LENGTH_LONG);
                    TextView toastText = layout.findViewById(R.id.textViewToast);
                    toastText.setText("Please set the prefered directory for storing recordings");

                    toast.show();
                }
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
                layout.setBackgroundColor(Color.parseColor("#4e348b"));
                Log.i("MAIN", "log");
                String displayText = "";
                String deviceName = sharedPreferences.getString("deviceName", "exists");
                String deviceType = sharedPreferences.getString("deviceType", "exists");
                String deviceVendor = sharedPreferences.getString("deviceVendor", "exists");

                displayText = displayText + "Device name: " + deviceName + "\n";
                displayText = displayText + "Device type: " + deviceType + "\n";
                displayText = displayText + "Device vendor: " + deviceVendor + "\n";
                System.out.println(displayText);
                Toast toast = new Toast(MainActivity.this);
                toast.setView(layout);
                toast.setDuration(Toast.LENGTH_LONG);
                TextView toastText = layout.findViewById(R.id.textViewToast);
                toastText.setText(displayText);

                toast.show();


            }
        });
        ImageButton folderbutton = findViewById(R.id.setfolder);

        folderbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDirectory();
            }
        });
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        Log.i("SHAREDPREF", sharedPreferences.getString("locationUri", "none"));


    }

    public void openDirectory() {
        // Choose a directory using the system's file picker.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        // Optionally, specify a URI for the directory that should be opened in
        // the system file picker when it loads.

        startActivityForResult(intent, ACCESS_URI);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        Log.i("RETURN", "U RETURNU");
        if (requestCode == ACCESS_URI
                && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            Uri uri = null;
            Log.i("RESULT", "IN result");
            if (resultData != null) {
                uri = resultData.getData();
                final int takeFlags = resultData.getFlags()
                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                getContentResolver().takePersistableUriPermission(uri, takeFlags);
                locationUri = uri;

                SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("locationUri", locationUri.toString());
                editor.apply();


                Log.i("URI", locationUri.toString());


            } else {
                super.onActivityResult(requestCode, resultCode, resultData);
            }
        }
    }
}