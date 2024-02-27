package org.intelehealth.ezazi.utilities;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;


import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.ezazi.R;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import org.intelehealth.ezazi.app.AppConstants;

public class FileUtils {
    public static String TAG = FileUtils.class.getSimpleName();

    public static String readFile(String FILENAME, Context context) {
        Log.i(TAG, "Reading from file");

        try {
            File myDir = new File(context.getFilesDir().getAbsolutePath() + File.separator + AppConstants.JSON_FOLDER + File.separator + FILENAME);
            FileInputStream fileIn = new FileInputStream(myDir);
            InputStreamReader InputRead = new InputStreamReader(fileIn);
            final int READ_BLOCK_SIZE = 100;
            char[] inputBuffer = new char[READ_BLOCK_SIZE];
            String s = "";
            int charRead;

            while ((charRead = InputRead.read(inputBuffer)) > 0) {
                String readstring = String.copyValueOf(inputBuffer, 0, charRead);
                s += readstring;
            }
            InputRead.close();
            Log.i("FILEREAD>", s);
            return s;

        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            return null;
        }

    }

    public static String readFileRoot(String FILENAME, Context context) {
        Log.i(TAG, "Reading from file");

        try {
            File myDir = new File(context.getFilesDir().getAbsolutePath() + File.separator + File.separator + FILENAME);
            FileInputStream fileIn = new FileInputStream(myDir);
            InputStreamReader InputRead = new InputStreamReader(fileIn);
            final int READ_BLOCK_SIZE = 100;
            char[] inputBuffer = new char[READ_BLOCK_SIZE];
            String s = "";
            int charRead;

            while ((charRead = InputRead.read(inputBuffer)) > 0) {
                String readstring = String.copyValueOf(inputBuffer, 0, charRead);
                s += readstring;
            }
            InputRead.close();
            Log.i("FILEREAD>", s);
            return s;

        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            return null;
        }

    }

    public static JSONObject encodeJSON(Context context, String fileName) {
        String raw_json = null;
        JSONObject encoded = null;
        try {
            InputStream is = context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            raw_json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        try {
            encoded = new JSONObject(raw_json);
        } catch (JSONException | NullPointerException e) {
            Toast.makeText(context, "config file is missing", Toast.LENGTH_SHORT).show();
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        return encoded;

    }

    public static String getJsonFromAssets(Context context, String fileName) {
        try {
            InputStream is = context.getAssets().open(fileName);
            byte[] bytes = new byte[is.available()];
            is.read(bytes, 0, bytes.length);
            is.close();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            return null;
        }
    }

    public static void writeToFile(String data, Context context, String file) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(file, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public static String getProjectCatchDir(Context context) {
        String main = context.getCacheDir().getAbsolutePath() + File.separator;
        File file = new File(main + context.getResources().getString(R.string.app_name) + File.separator);
        if (!file.exists()) file.mkdirs();
        return file.getAbsolutePath() + File.separator;
    }
}