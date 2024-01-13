package com.example.lightsense2;

import android.animation.AnimatorSet;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Message;
import android.provider.DocumentsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.documentfile.provider.DocumentFile;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import android.os.Handler;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;


public class LoggingActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor lightSensor;

    View bottomNavigationView;
    Uri locationUri;
    Uri writeJson;

    OutputStream outputStream;


    int jsonCount = 0;
    private static final String PREF_NAME = "MyPrefs";
    private SharedPreferences sharedPreferences;

    private int clickCnt = 0;

    private Handler handler;
    private boolean timerRunning;
    private long elapsedTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logging_light);
        bottomNavigationView = findViewById(R.id.bottom_nav);
        jsonCount = -1;
        LinkedList<DocumentFile> newJSONS = new LinkedList<>();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        ImageButton measureButton = bottomNavigationView.findViewById(R.id.measurebutton);

        LinearLayout logCon = findViewById(R.id.logcontainer);
        LayoutTransition layoutTransition = new LayoutTransition();

        ObjectAnimator appearingAnimator = ObjectAnimator.ofFloat(null, "alpha", 0f, 1f);

        layoutTransition.setDuration(400);
        layoutTransition.setAnimator(LayoutTransition.APPEARING, appearingAnimator);
        layoutTransition.setAnimator(LayoutTransition.CHANGE_DISAPPEARING, null);

        logCon.setLayoutTransition(layoutTransition);


        measureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoggingActivity.this, MeasureActivity.class);
                startActivity(intent);
            }
        });
        ImageButton homebutton = bottomNavigationView.findViewById(R.id.homebutton);

        homebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoggingActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        ImageButton informationbtn = findViewById(R.id.informationbutton);
        informationbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

                String displayText = "";
                String deviceName = sharedPreferences.getString("deviceName", "exists");
                String deviceType = sharedPreferences.getString("deviceType", "exists");
                String deviceVendor = sharedPreferences.getString("deviceVendor", "exists");

                displayText = displayText + "Device name: " + deviceName + "\n";
                displayText = displayText + "Device type: " + deviceType + "\n";
                displayText = displayText + "Device vendor: " + deviceVendor + "\n";
                System.out.println(displayText);
                toaster(displayText, 1);
            }
        });

        Button recordbutton = findViewById(R.id.recordbutton);
        String hexColor = "#4e348b";
        int color = Color.parseColor(hexColor);
        recordbutton.setBackgroundColor(color);
        recordbutton.setTextColor(Color.WHITE);
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                updateTimerText(recordbutton);
                return true;
            }
        });


        recordbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickCnt % 2 == 0) {
                    locationUri = Uri.parse(sharedPreferences.getString("locationUri", "none"));
                    if (locationUri.toString().equals("none")) {
                        toaster("Please set the preffered directory for storing recordings", 1);
                    }
                    clickCnt++;
                    jsonCount++;
                    recordbutton.setBackgroundColor(Color.RED);
                    recordbutton.setTextColor(Color.WHITE);
                    SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

                    try {
                        DocumentFile currJson = FileOperations.createJSON(sharedPreferences, LoggingActivity.this);
                        writeJson = currJson.getUri();
                        newJSONS.add(currJson);
                        Log.i("JSONcr", currJson.getUri().toString());
                    } catch (IOException e) {
                        Log.e("JSON", "failed to create");
                        throw new RuntimeException(e);
                    }
                    startTimer(recordbutton);
                    Log.i("RECORD", "recording");
                } else {
                    clickCnt++;
                    String hexColor = "#4e348b";
                    int color = Color.parseColor(hexColor);
                    recordbutton.setText("Record");
                    recordbutton.setTextColor(Color.WHITE);
                    recordbutton.setBackgroundColor(color);

                    displayFile(writeJson);
                    stopTimer(recordbutton);
                }


            }
        });


        listFiles(loadFiles());
    }

    private void displayFile(Uri uri) {
        Log.i("DISP", "displaying file " + uri.toString());
        DocumentFile file = DocumentFile.fromSingleUri(LoggingActivity.this, uri);
        Log.i("DISP", "file name " + file.getName());
        LinearLayout logContainer = findViewById(R.id.logcontainer);


        LayoutInflater inflater = LayoutInflater.from(LoggingActivity.this);
        View templateLogView = inflater.inflate(R.layout.template_log, logContainer, false);

        ImageButton openfolder = templateLogView.findViewById(R.id.openfolder);

        openfolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileExplorer(locationUri);
            }
        });
        ImageButton deletefile = templateLogView.findViewById(R.id.deletefile);

        deletefile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (file != null && file.exists()) {
                    String delname = file.getName();
                    Log.i("DEL", "attempting to delete");
                    if (file.delete()) { // Delete the file
                        toaster("File " + delname + " has successfully been deleted", 0);
                        //fadeOutToSide(templateLogView);
                        //logContainer.removeView(templateLogView);
                        fadeOutToSideAndRemove(templateLogView);
                    }
                }
            }
        });

        EditText editText = templateLogView.findViewById(R.id.filename);
        editText.setText(file.getName());

        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String newName = editText.getText().toString();
                    if (!newName.isEmpty() && !newName.equals(file.getName())) {
                        if (FileOperations.renameFile(getApplicationContext(), file.getUri(), newName)) {
                            toaster("File renamed to " + newName, 0);
                        }

                    } else {
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
                if (editable.toString().contains(".json") && !editable.toString().isEmpty() && !editable.toString().equals(file.getName()) && editable.length() > 5) {
                    if (FileOperations.renameFile(getApplicationContext(), file.getUri(), editable.toString())) {
                        toaster("File renamed to " + editable.toString(), 0);
                    }
                }
            }
        });
        Log.i("RECORD", "finished recording");
        logContainer.addView(templateLogView);


    }

    private void fadeOutToSideAndRemove(View slidingView) {
        // Create ObjectAnimators for alpha and translationX properties
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(slidingView, "alpha", 1f, 0f);
        ObjectAnimator slideOut = ObjectAnimator.ofFloat(slidingView, "translationX", 0, slidingView.getWidth());

        // Combine the animations into an AnimatorSet
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(fadeOut, slideOut);
        animatorSet.setDuration(700); // Set the duration of the animation in milliseconds

        // Set up a listener to remove the view when the animation ends
        animatorSet.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                super.onAnimationEnd(animation);

                // Remove the view from its parent
                ViewGroup parentView = (ViewGroup) slidingView.getParent();
                if (parentView != null) {
                    parentView.removeView(slidingView);
                }
            }
        });

        // Start the animation
        animatorSet.start();
    }

    private void startTimer(Button button) {
        timerRunning = true;
        button.setText("Recording");
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (timerRunning) {
                    try {
                        Thread.sleep(500);
                        elapsedTime += 500;
                        handler.sendEmptyMessage(0);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void stopTimer(Button button) {
        timerRunning = false;
        button.setText("Record");
        elapsedTime = 0;
    }

    private void updateTimerText(Button button) {
        int seconds = (int) (elapsedTime / 1000) % 60;
        int minutes = (int) ((elapsedTime / (1000 * 60)) % 60);
        int hours = (int) ((elapsedTime / (1000 * 60 * 60)) % 24);
        String timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        if (timerRunning) {
            button.setText(timeFormatted);
        }

    }

    public void toaster(String msg, int length) {
        LayoutInflater inflater = getLayoutInflater();

        View layout = inflater.inflate(R.layout.custom_toast_layout, findViewById(R.id.customtoast));

        layout.setBackgroundColor(Color.parseColor("#4e348b"));

        Toast toast = new Toast(LoggingActivity.this);
        toast.setView(layout);
        toast.setDuration(length);
        TextView toastText = layout.findViewById(R.id.textViewToast);
        toastText.setText(msg);

        toast.show();

    }

    protected LinkedList<DocumentFile> loadFiles() {
// Get the number of child views

        Uri locUri = Uri.parse(sharedPreferences.getString("locationUri", "none"));
        DocumentFile folder = DocumentFile.fromTreeUri(this, locUri);
        LinkedList<DocumentFile> linkedFiles = new LinkedList<>();

        // Check if the folder exists and is a directory
        if (folder != null && folder.isDirectory()) {
            // Get the list of files in the folder
            DocumentFile[] files = folder.listFiles();
            Arrays.sort(files, Comparator.comparing(DocumentFile::getName));

            linkedFiles.addAll(Arrays.asList(files));

            Log.i("FILES", "loading " + files.length + " files");
        }
        return linkedFiles;
    }

    protected void listFiles(LinkedList<DocumentFile> files) {


        LinearLayout logContainer = findViewById(R.id.logcontainer);
        int childCount = logContainer.getChildCount();

// Iterate through each child and remove it
        for (int i = 0; i < childCount; i++) {
            View childView = logContainer.getChildAt(i);
            logContainer.removeView(childView);
        }
        if (files != null && files.size() > 0) {
            for (DocumentFile file : files) {
                // Add each file to the list
                if (file.getName().contains(".json")) {

                    LayoutInflater inflater = LayoutInflater.from(LoggingActivity.this);
                    View templateLogView = inflater.inflate(R.layout.template_log, logContainer, false);

                    EditText editText = templateLogView.findViewById(R.id.filename);
                    editText.setText(file.getName());

                    editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                        @Override
                        public void onFocusChange(View v, boolean hasFocus) {
                            if (!hasFocus) {
                                String newName = editText.getText().toString();

                                if (!newName.isEmpty() && !newName.equals(file.getName())) {
                                    if (FileOperations.renameFile(LoggingActivity.this, file.getUri(), newName)) {
                                        toaster("File renamed to " + newName, 0);
                                    }
                                } else {
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
                            if (editable.toString().contains(".json") && !editable.toString().isEmpty() && !editable.toString().equals(file.getName())) {
                                if (FileOperations.renameFile(getApplicationContext(), file.getUri(), editable.toString())) {
                                    toaster("File renamed to " + editable.toString(), 0);
                                }


                            }
                        }
                    });

                    ImageButton openfolder = templateLogView.findViewById(R.id.openfolder);
                    openfolder.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openFileExplorer(file.getUri());
                            Log.i("OPEN", "opening file" + file.getUri());
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
                                if (filetodel.delete()) { // Delete the file
                                    toaster("File " + delname + " has successfully been deleted", 0);
                                    //logContainer.removeView(templateLogView);
                                    fadeOutToSideAndRemove(templateLogView);

                                }
                            }
                        }
                    });

                    logContainer.addView(templateLogView);
                } else {
                    file.delete();
                }
            }
        } else {

            toaster("You have no recordings to be displayed. Press record to start a recording.", 1);
        }

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

            if (clickCnt % 2 == 1) {
                Log.i("LIGHT", String.valueOf(event.values[0]));

                LocalDateTime currentDateTime = LocalDateTime.now();

                // Define a formatter to format the date and time
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                // Format the current date and time using the formatter
                String formattedDateTime = currentDateTime.format(formatter);
                Log.i("LOG", formattedDateTime);

                // Now 'date' contains the date/time in a human-readable format
                // You can format it as needed for display or logging
                Log.i("LIGHT", formattedDateTime);

                float lightValue = event.values[0];

                if (writeJson != null) {
                    FileOperations.appendJsonData(getApplicationContext(), String.valueOf(lightValue), formattedDateTime, writeJson);

                    Log.i("WRITE", "successfull writing");

                }
            } else {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        Log.e("OUTPUT", "greska u outputu");
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