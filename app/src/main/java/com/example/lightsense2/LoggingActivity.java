package com.example.lightsense2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.lightsense2.databinding.ActivityLoggingLightBinding;

public class LoggingActivity extends AppCompatActivity {


    View bottomNavigationView;
    MeasureHome measureHome = new MeasureHome();
    LogFragment logFragment = new LogFragment();
    HomeFragment home = new HomeFragment();
    private static final String PREF_NAME = "MyPrefs";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logging_light);
        bottomNavigationView = findViewById(R.id.bottom_nav);

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



    }


}