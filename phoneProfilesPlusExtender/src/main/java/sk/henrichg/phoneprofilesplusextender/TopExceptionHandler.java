package sk.henrichg.phoneprofilesplusextender;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeoutException;

class TopExceptionHandler implements Thread.UncaughtExceptionHandler {

    private final Thread.UncaughtExceptionHandler defaultUEH;
    private final Context applicationContext;
    private final long actualVersionCode;

    static final String CRASH_FILENAME = "crash.txt";

    TopExceptionHandler(Context applicationContext, long actualVersionCode) {
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        this.applicationContext = applicationContext;
        this.actualVersionCode = actualVersionCode;
    }

    @SuppressWarnings("StringConcatenationInLoop")
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e)
    {
        if (PPPEApplication.crashIntoFile) {
            StackTraceElement[] arr = e.getStackTrace();
            String report = e/*.toString()*/ + "\n\n";

            report += "----- App version code: " + actualVersionCode + "\n\n";

            for (StackTraceElement anArr : arr) {
                report += "    " + anArr.toString() + "\n";
            }
            report += "-------------------------------\n\n";

            report += "--------- Stack trace ---------\n\n";
            for (StackTraceElement anArr : arr) {
                report += "    " + anArr.toString() + "\n";
            }
            report += "-------------------------------\n\n";

            // If the exception was thrown in a background thread inside
            // AsyncTask, then the actual exception can be found with getCause
            report += "--------- Cause ---------------\n\n";
            Throwable cause = e.getCause();
            if (cause != null) {
                report += cause/*.toString()*/ + "\n\n";
                arr = cause.getStackTrace();
                for (StackTraceElement anArr : arr) {
                    report += "    " + anArr.toString() + "\n";
                }
            }
            report += "-------------------------------\n\n";

            logIntoFile("E", "TopExceptionHandler", report);
        }

        if (defaultUEH != null) {
            //noinspection StatementWithEmptyBody
            if (t.getName().equals("FinalizerWatchdogDaemon") && (e instanceof TimeoutException)) {
                // ignore these exceptions
                // java.util.concurrent.TimeoutException: com.android.internal.os.BinderInternal$GcWatcher.finalize() timed out after 10 seconds
                // https://stackoverflow.com/a/55999687/2863059
            }
            else {
                //Delegates to Android's error handling
                defaultUEH.uncaughtException(t, e);
            }
        }
        else
            //Prevents the service/app from freezing
            System.exit(2);
    }

    @SuppressWarnings("SameParameterValue")
    @SuppressLint("SimpleDateFormat")
    private void logIntoFile(String type, String tag, String text)
    {
        if (PPPEApplication.crashIntoFile) {
            try {
                File path = applicationContext.getExternalFilesDir(null);

                /*File sd = Environment.getExternalStorageDirectory();
                File exportDir = new File(sd, PPPEApplication.EXPORT_PATH);
                if (!(exportDir.exists() && exportDir.isDirectory()))
                    //noinspection ResultOfMethodCallIgnored
                    exportDir.mkdirs();

                File logFile = new File(sd, PPPEApplication.EXPORT_PATH + "/" + CRASH_FILENAME);*/

                File logFile = new File(path, CRASH_FILENAME);

                if (logFile.length() > 1024 * 10000)
                    resetLog();

                if (!logFile.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    logFile.createNewFile();
                }

                //BufferedWriter for performance, true to set append to file flag
                BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
                String log = "";
                SimpleDateFormat sdf = new SimpleDateFormat("d.MM.yy HH:mm:ss:S");
                String time = sdf.format(Calendar.getInstance().getTimeInMillis());
                log = log + time + "--" + type + "-----" + tag + "------" + text;
                buf.append(log);
                buf.newLine();
                buf.flush();
                buf.close();
            } catch (IOException ignored) {
            }
        }
    }

    private void resetLog()
    {
        /*File sd = Environment.getExternalStorageDirectory();
        File exportDir = new File(sd, PPPEApplication.EXPORT_PATH);
        if (!(exportDir.exists() && exportDir.isDirectory()))
            //noinspection ResultOfMethodCallIgnored
            exportDir.mkdirs();

        File logFile = new File(sd, PPPEApplication.EXPORT_PATH + "/" + CRASH_FILENAME);*/

        File path = applicationContext.getExternalFilesDir(null);
        File logFile = new File(path, CRASH_FILENAME);

        //noinspection ResultOfMethodCallIgnored
        logFile.delete();
    }

}
