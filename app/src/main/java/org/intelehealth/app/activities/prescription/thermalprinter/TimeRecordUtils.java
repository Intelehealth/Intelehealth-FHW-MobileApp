package org.intelehealth.app.activities.prescription.thermalprinter;

import android.util.Log;

public class TimeRecordUtils {
    public synchronized static void record(String describe, long timemills) {
        Log.e("Fu", timemills + "\t" + describe);
    }
}
