package com.facedetection.utils;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.util.Log;


//import com.accurascan.accuraemirates.sdk.BuildConfig;

import com.accurascan.accuraemirates.BuildConfig;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class AccuraFaceMatchLog {

    private static boolean FM_DEBUG = BuildConfig.DEBUG;

    public static boolean isDEBUG() {
        return FM_DEBUG;
    }

    public static void setPrintLogs(boolean DEBUG) {
        AccuraFaceMatchLog.FM_DEBUG = DEBUG;
    }

    public static void loge(String tag, String s) {
//        logToFile(tag, s);
        if (isDEBUG()) logToFile("AccuraLog." + tag, "" + s);
    }

    public static void refreshLogfile(Context context) {
        if (!isDEBUG()){
            Log.e(AccuraFaceMatchLog.class.getSimpleName(), "Please enable logs before call AccuraFaceMatchLog.refreshLogfile(context)");
            return;
        }
        // Refresh the data so it can seen when the device is plugged in a
        // computer. You may have to unplug and replug to see the latest
        // changes
        if (context != null) {
            try {
                File logFile = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS), "AccuraLog.txt");

                if (!logFile.exists()) if (!logFile.createNewFile()) return;

                MediaScannerConnection.scanFile(context,
                        new String[]{logFile.toString()},
                        null,
                        null);
            } catch (IOException e) {
            }

        }
    }
    /**
     * Gets a stamp containing the current date and time to write to the log.
     * @return The stamp for the current date and time.
     */
    private static String getDateTimeStamp()
    {
        Date dateNow = Calendar.getInstance().getTime();
        // My locale, so all the log files have the same date and time format
        return (DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, Locale.UK).format(dateNow));
    }

    /**
     * Writes a message to the log file on the device.
     * @param logMessageTag A tag identifying a group of log messages.
     * @param logMessage The message to add to the log.
     */
    private static void logToFile(String logMessageTag, String logMessage)
    {
        try
        {
            // Gets the log file from the root of the primary storage. If it does
            // not exist, the file is created.
            File logFile = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS), "AccuraLog.txt");
            if (!logFile.exists())
                logFile.createNewFile();
            // Write the message to the log with a timestamp
            BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true));
            writer.write(String.format("%1s [%2s]:%3s\r\n", getDateTimeStamp(), logMessageTag, logMessage));
            writer.close();
            // Refresh the data so it can seen when the device is plugged in a
            // computer. You may have to unplug and replug to see the latest
            // changes
//            if (context != null) {
//                MediaScannerConnection.scanFile(context,
//                        new String[] { logFile.toString() },
//                        null,
//                        null);
//            }

        }
        catch (IOException e)
        {
        }
    }
}
