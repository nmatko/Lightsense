package com.example.lightsense2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import android.os.ParcelFileDescriptor;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class LoggingActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor lightSensor;
    Boolean record = false;
    View bottomNavigationView;
    Uri locationUri;
    MeasureHome measureHome = new MeasureHome();
    HomeFragment home = new HomeFragment();

    DocumentFile currJson;
    OutputStream outputStream;

    BufferedReader reader;
    private static final int ACCESS_URI = 1;
    private static final String PREF_NAME = "MyPrefs";
    private SharedPreferences sharedPreferences;
    private static final String MAX_VALUE_KEY = "maxValue";
    private static final String MIN_VALUE_KEY = "minValue";
    private int clickCnt = 0;
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
                //getSupportFragmentManager().beginTransaction().replace(R.id.container,measureHome).commit();
                Intent intent = new Intent(LoggingActivity.this, MeasureActivity.class);
                startActivity(intent);
            }
        });
        ImageButton homebutton =  bottomNavigationView.findViewById(R.id.homebutton);

        homebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getSupportFragmentManager().beginTransaction().replace(R.id.container,home).commit();
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
                if (clickCnt%2==0){
                    locationUri = Uri.parse(sharedPreferences.getString("locationUri", "none"));
                    if(locationUri.toString().equals("none")){
                        LayoutInflater inflater = getLayoutInflater();
                        View layout = inflater.inflate(R.layout.custom_toast_layout, findViewById(R.id.customtoast));
                        Toast toast = new Toast(LoggingActivity.this);
                        toast.setView(layout);
                        toast.setDuration(Toast.LENGTH_LONG);
                        TextView toastText = layout.findViewById(R.id.textViewToast);
                        toastText.setText("Please set the preffered directory for storing recordings");

                        toast.show();
                        return;
                    }
                    clickCnt++;
                    recordbutton.setText("Recording");
                    recordbutton.setBackgroundColor(Color.RED);
                    SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

                    try {
                        currJson = createJSON();
                        Log.i("JSONcr",currJson.getUri().toString());
                    } catch (IOException e) {
                        Log.e("JSON", "failed to create");
                        throw new RuntimeException(e);
                    }

                    Log.i("RECORD","recording");
                }else {
                    clickCnt++;
                    String hexColor = "#4e348b"; // This is an example hex color (orange)
                    int color = Color.parseColor(hexColor);
                    recordbutton.setText("Record");
                    recordbutton.setTextColor(Color.WHITE);
                    recordbutton.setBackgroundColor(color);
                    Log.i("RECORD","finished recording");
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

            if (clickCnt%2 == 1) {
                Log.i("LIGHT", String.valueOf(event.values[0]));

                LocalDateTime currentDateTime = LocalDateTime.now();

                // Define a formatter to format the date and time
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                // Format the current date and time using the formatter
                String formattedDateTime = currentDateTime.format(formatter);
                Log.i("LOG",formattedDateTime);

                // Now 'date' contains the date/time in a human-readable format
                // You can format it as needed for display or logging
                Log.i("LIGHT", formattedDateTime);

                float lightValue = event.values[0];

                if (currJson !=null) {
                        appendJsonData(getApplicationContext(),String.valueOf(lightValue),formattedDateTime);

                        Log.i("WRITE", "successfull writing");

                }
            }else {
                if (outputStream !=null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        Log.e("OUTPUT","greska u outputu");
                        throw new RuntimeException(e);
                    }
                }
            }

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
    public DocumentFile createJSON() throws IOException {
        Uri jsonUri = Uri.parse(sharedPreferences.getString("locationUri", "none")+"/lightsense");
        Log.i("JSONuri", String.valueOf(jsonUri));
        DocumentFile parentDir = DocumentFile.fromTreeUri(getApplicationContext(),jsonUri );
        Log.i("JSONDIR",parentDir.getUri().toString());
        LocalDateTime currentDateTime = LocalDateTime.now();

        // Define a formatter to format the date and time
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Format the current date and time using the formatter
        String formattedDateTime = currentDateTime.format(formatter);
        Log.i("LOG",formattedDateTime);

        if (parentDir != null && parentDir.exists() && parentDir.isDirectory()) {
            // Create a new file named "data.json" within the parent directory
            DocumentFile jsonFile = parentDir.createFile("application/json", "recording" + formattedDateTime + ".json");

            if (jsonFile != null) {
                JSONArray jsonArray = new JSONArray();
                ParcelFileDescriptor parcelFileDescriptor = getApplicationContext().getContentResolver().openFileDescriptor(jsonFile.getUri(), "w");
                if (parcelFileDescriptor != null) {
                    OutputStream outputStream = new FileOutputStream(parcelFileDescriptor.getFileDescriptor());
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));

                    // Write the JSON array to the file
                    writer.write(jsonArray.toString());
                    writer.flush();

                    writer.close();
                    outputStream.close();
                    parcelFileDescriptor.close();
                }


                return jsonFile;
            }
        }
        return parentDir;
    }
    public  void appendJsonData(Context context,String light, String timestamp) {
        // Assume 'fileUri' is the URI of the JSON file obtained through SAF
        Uri fileUri = currJson.getUri();

        try {
            // Load existing JSON content from the file
            JSONArray jsonArray = loadExistingJsonContent(context, fileUri);

            // Create new JSON data to append
            JSONObject newData = new JSONObject();
            newData.put("light", light);
            newData.put("timestamp", timestamp);

            // Append the new JSON data to the existing JSON array
            jsonArray.put(newData);

            // Write the updated JSON content back to the file
            writeJsonToFile(context, fileUri, jsonArray);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
    private  JSONArray loadExistingJsonContent(Context context, Uri fileUri) throws IOException, JSONException {
        JSONArray jsonArray = new JSONArray();

        ParcelFileDescriptor parcelFileDescriptor = context.getContentResolver().openFileDescriptor(fileUri, "r");
        if (parcelFileDescriptor != null) {
            FileInputStream inputStream = new FileInputStream(parcelFileDescriptor.getFileDescriptor());

            // Read existing content into JSONArray
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            jsonArray = new JSONArray(sb.toString());

            inputStream.close();
            parcelFileDescriptor.close();
        }

        return jsonArray;
    }
    private static void writeJsonToFile(Context context, Uri fileUri, JSONArray jsonArray) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = context.getContentResolver().openFileDescriptor(fileUri, "w");
        if (parcelFileDescriptor != null) {
            FileOutputStream outputStream = new FileOutputStream(parcelFileDescriptor.getFileDescriptor());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));

            // Write the updated JSON content back to the file
            writer.write(jsonArray.toString());
            writer.flush();

            writer.close();
            outputStream.close();
            parcelFileDescriptor.close();
        }
    }

}