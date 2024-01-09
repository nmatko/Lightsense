package com.example.lightsense2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.Context;
import android.content.Intent;

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

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class MainActivity extends AppCompatActivity {
    View bottomNavigationView;

    Uri locationUri;
    MeasureHome measureHome = new MeasureHome();
    LogFragment logFragment = new LogFragment();
    HomeFragment home = new HomeFragment();
    private static final int ACCESS_URI = 1;
    private static final String PREF_NAME = "MyPrefs";


    void setManufacturerInfo(){

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

        getSupportFragmentManager().beginTransaction().replace(R.id.container,home).commit();


        ImageButton measureButton =  bottomNavigationView.findViewById(R.id.measurebutton);
        measureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager().beginTransaction().replace(R.id.container,measureHome).commit();
                Intent intent = new Intent(MainActivity.this, MeasureActivity.class);
                startActivity(intent);
            }
        });
        ImageButton logbutton =  bottomNavigationView.findViewById(R.id.logbutton);

        logbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager().beginTransaction().replace(R.id.container,logFragment).commit();
                Intent intent = new Intent(MainActivity.this, LoggingActivity.class);
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
                Toast toast = new Toast(MainActivity.this);
                toast.setView(layout);
                toast.setDuration(Toast.LENGTH_LONG);
                TextView toastText = layout.findViewById(R.id.textViewToast);
                toastText.setText(displayText);

                toast.show();


            }
        });
        ImageButton folderbutton =  findViewById(R.id.setfolder);

        folderbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDirectory();
            }
        });
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        LocalDateTime currentDateTime = LocalDateTime.now();

        // Define a formatter to format the date and time
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Format the current date and time using the formatter
        String formattedDateTime = currentDateTime.format(formatter);
        Log.i("LOG",formattedDateTime);
        Log.i("SHAREDPREF",sharedPreferences.getString("locationUri","none"));

        String dir = sharedPreferences.getString("locationUri","none");


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
        Log.i("RETURN","U RETURNU");
        if (requestCode == ACCESS_URI
                && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            Uri uri = null;
            Log.i("RESULT","IN result");
            if (resultData != null) {
                uri = resultData.getData();
                locationUri = uri;
                SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("locationUri", locationUri.toString());
                editor.apply();

                DocumentFile pickedDir = DocumentFile.fromTreeUri(this.getBaseContext(), locationUri);
                DocumentFile lightsenseFolder = pickedDir.findFile("lightsense");

                if (pickedDir != null && pickedDir.exists() && pickedDir.isDirectory() && lightsenseFolder == null) {
                    String newDirectoryName = "lightsense"; // Replace with your desired folder name
                    DocumentFile newDir = pickedDir.createDirectory(newDirectoryName);

                    if (newDir != null && newDir.exists() && newDir.isDirectory()) {
                        Log.i("FILE","file created");
                    } else {
                        // Directory creation failed
                        // Handle failure to create directory
                    }
                } else {
                    // Invalid directory or directory doesn't exist
                    // Handle this scenario
                }

                Log.i("URI", locationUri.toString());



            }else {
                super.onActivityResult(requestCode, resultCode, resultData);
            }
        }
    }
}