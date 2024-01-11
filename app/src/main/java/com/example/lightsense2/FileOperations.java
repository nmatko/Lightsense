package com.example.lightsense2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import androidx.documentfile.provider.DocumentFile;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileOperations {

    private FileOperations(){

    }

    public static void renameFile(Context context, DocumentFile file, String newFileName) {
        if (!newFileName.contains(".json")){
            newFileName = newFileName + ".json";
        }
        if (file != null && file.exists()) {
            // Create a new file with the desired name in the same directory
            DocumentFile renamedFile = file.getParentFile().createFile("application/json", newFileName);
            Log.i("NEWFILE", file.getName());
            if (renamedFile != null) {
                // Copy content from the original file to the renamed file
                try {
                    InputStream inputStream = context.getContentResolver().openInputStream(file.getUri());
                    OutputStream outputStream = context.getContentResolver().openOutputStream(renamedFile.getUri());

                    byte[] buffer = new byte[1024];
                    int bytesRead;

                    while ((bytesRead = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, bytesRead);
                    }

                    inputStream.close();
                    outputStream.close();

                    // Delete the original file
                    Log.i("DELETE", "deleting old file " + file.getName());
                    file.delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public static DocumentFile createJSON(SharedPreferences sharedPreferences, Context context) throws IOException {
        Uri jsonUri = Uri.parse(sharedPreferences.getString("locationUri", "none")+"/lightsense");
        Log.i("JSONuri", String.valueOf(jsonUri));
        DocumentFile parentDir = DocumentFile.fromTreeUri(context,jsonUri );
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
                ParcelFileDescriptor parcelFileDescriptor = context.getContentResolver().openFileDescriptor(jsonFile.getUri(), "w");
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
    public static   void appendJsonData(Context context,String light, String timestamp, DocumentFile currJson) {
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
    public static JSONArray loadExistingJsonContent(Context context, Uri fileUri) throws IOException, JSONException {
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
