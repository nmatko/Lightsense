package com.example.lightsense2;
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
import androidx.appcompat.app.AppCompatActivity;
import android.provider.DocumentsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.documentfile.provider.DocumentFile;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LoggingActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor lightSensor;
    Boolean record = false;
    View bottomNavigationView;
    Uri locationUri;
    int colorCnt = 0;
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


                String displayText="";
                String deviceName = sharedPreferences.getString("deviceName", "exists");
                String deviceType = sharedPreferences.getString("deviceType", "exists");
                String deviceVendor = sharedPreferences.getString("deviceVendor", "exists");

                displayText = displayText + "Device name: "+ deviceName + "\n";
                displayText = displayText + "Device type: " + deviceType+ "\n";
                displayText = displayText + "Device vendor: " + deviceVendor + "\n";
                System.out.println(displayText);
                toaster(displayText);
            }
        });

        Button recordbutton = findViewById(R.id.recordbutton);

        recordbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickCnt%2==0){
                    locationUri = Uri.parse(sharedPreferences.getString("locationUri", "none"));
                    if(locationUri.toString().equals("none")){
                        toaster("Please set the preffered directory for storing recordings");
                    }
                    clickCnt++;
                    recordbutton.setText("Recording");
                    recordbutton.setBackgroundColor(Color.RED);
                    SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

                    try {
                        currJson = FileOperations.createJSON(sharedPreferences, getApplicationContext());
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

                    LinearLayout logContainer = findViewById(R.id.logcontainer);

                    LayoutInflater inflater = LayoutInflater.from(LoggingActivity.this);
                    View templateLogView = inflater.inflate(R.layout.template_log, logContainer, false);

                    ImageButton openfolder = templateLogView.findViewById(R.id.openfolder);

                    openfolder.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openFileExplorer(currJson.getUri());
                        }
                    });
                    ImageButton deletefile = templateLogView.findViewById(R.id.deletefile);

                    deletefile.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (currJson != null && currJson.exists()) {
                                String delname = currJson.getName();
                                Log.i("DEL", "attempting to delete");
                                if(currJson.delete()) { // Delete the file
                                    toaster("File " + delname + " has successfully been deleted");
                                    Intent intent = new Intent(LoggingActivity.this, LoggingActivity.class);
                                    startActivity(intent);
                                }
                            }
                        }
                    });

                    EditText editText =  templateLogView.findViewById(R.id.filename);
                    editText.setText(currJson.getName());

                    editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                        @Override
                        public void onFocusChange(View v, boolean hasFocus) {
                            if (!hasFocus){
                                String newName = editText.getText().toString();
                                Log.i("CURR", currJson.getName());
                                if (!newName.isEmpty() && !newName.equals(currJson.getName())) {
                                    FileOperations.renameFile(getApplicationContext(), currJson, newName);
                                    Intent intent = new Intent(LoggingActivity.this, LoggingActivity.class);
                                    startActivity(intent);
                                }else{
                                    editText.setText(currJson.getName());
                                }
                            }
                        }
                    });
                    editText.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
                            // Called to notify you that the characters within `start` and `start + before` are about to be replaced
                        }
                        @Override
                        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                            // Called to notify you that somewhere within `start` and `start + before` the text has been replaced with new text having length `count`
                        }

                        @Override
                        public void afterTextChanged(Editable editable) {
                            // Called to notify you that the characters within the `Editable` have changed
                            if (editable.toString().contains(".json") && !editable.toString().isEmpty() && !editable.toString().equals(currJson.getName()) && editable.length()>5){
                                FileOperations.renameFile(getApplicationContext(), currJson, editable.toString());
                                Intent intent = new Intent(LoggingActivity.this, LoggingActivity.class);
                                startActivity(intent);

                            }
                        }
                    });

                    logContainer.addView(templateLogView);
                    Log.i("RECORD","finished recording");
                }



            }
        });
        Uri locationUri = Uri.parse(sharedPreferences.getString("locationUri", "none"));

        List<DocumentFile> fileList = new ArrayList<>();

        // Get the external storage directory URI

        DocumentFile folder = DocumentFile.fromTreeUri(this, locationUri);


        // Check if the folder exists and is a directory
        if (folder != null && folder.isDirectory()) {
            // Get the list of files in the folder
            DocumentFile[] files = folder.listFiles();
            if (files != null && files.length > 0) {
                for (DocumentFile file : files) {
                    // Add each file to the list
                    if (file.getName().contains(".json")){
                    LinearLayout logContainer = findViewById(R.id.logcontainer);

                    LayoutInflater inflater = LayoutInflater.from(LoggingActivity.this);
                    View templateLogView = inflater.inflate(R.layout.template_log, logContainer, false);

                    EditText editText =  templateLogView.findViewById(R.id.filename);
                    editText.setText(file.getName());

                    editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                        @Override
                        public void onFocusChange(View v, boolean hasFocus) {
                            if (!hasFocus){
                                String newName = editText.getText().toString();
                                Log.i("OLDFILE", file.getParentFile().getName());
                                if (!newName.isEmpty() && !newName.equals(file.getName())) {
                                    FileOperations.renameFile(getApplicationContext(), file, newName);
                                    Intent intent = new Intent(LoggingActivity.this, LoggingActivity.class);
                                    startActivity(intent);
                                }else{
                                    editText.setText(file.getName());
                                }
                            }
                        }
                    });
                        editText.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
                                // Called to notify you that the characters within `start` and `start + before` are about to be replaced
                            }
                            @Override
                            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                                // Called to notify you that somewhere within `start` and `start + before` the text has been replaced with new text having length `count`
                            }

                            @Override
                            public void afterTextChanged(Editable editable) {
                                // Called to notify you that the characters within the `Editable` have changed
                                if (editable.toString().contains(".json") && !editable.toString().isEmpty() && !editable.toString().equals(file.getName())){
                                    FileOperations.renameFile(getApplicationContext(), file, editable.toString());
                                    Intent intent = new Intent(LoggingActivity.this, LoggingActivity.class);
                                    startActivity(intent);

                                }
                            }
                        });

                    ImageButton openfolder = templateLogView.findViewById(R.id.openfolder);
                    openfolder.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openFileExplorer(file.getUri());
                            Log.i("OPEN","opening file" + file.getUri().toString());
                        }
                    });
                        ImageButton deletefile = templateLogView.findViewById(R.id.deletefile);

                        deletefile.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (file != null && file.exists()) {
                                    DocumentFile filetodel = DocumentFile.fromSingleUri(LoggingActivity.this, file.getUri());
                                    String delname = filetodel.getName();
                                    Log.i("DEL", "attempting to delete");
                                    if(filetodel.delete()) { // Delete the file
                                        toaster("File " + delname + " has successfully been deleted");
                                        Intent intent = new Intent(LoggingActivity.this, LoggingActivity.class);
                                        startActivity(intent);
                                    }
                                }
                            }
                        });

                    logContainer.addView(templateLogView);
                    }
                }
            }else {
                currJson = null;
                toaster("You have no recordings to be displayed. Press record to start a recording.");
            }

        }


    }
    public void toaster(String msg){
        LayoutInflater inflater = getLayoutInflater();

        View layout = inflater.inflate(R.layout.custom_toast_layout, findViewById(R.id.customtoast));

        layout.setBackgroundColor(Color.parseColor("#4e348b"));

        Toast toast = new Toast(LoggingActivity.this);
        toast.setView(layout);
        toast.setDuration(Toast.LENGTH_LONG);
        TextView toastText = layout.findViewById(R.id.textViewToast);
        toastText.setText(msg);

        toast.show();

    }

    private void openFileExplorer(Uri uri) {
        // Convert the URI string to a URI object

        // Create an intent to open the file explorer with the specified URI
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri);

        // Start the file explorer activity
        startActivityForResult(intent, 0); // You can use a different request code if needed
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
                        FileOperations.appendJsonData(getApplicationContext(),String.valueOf(lightValue),formattedDateTime, currJson);

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


}