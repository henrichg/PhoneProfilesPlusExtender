package sk.henrichg.phoneprofilesplusextender;

import android.annotation.SuppressLint;
import android.app.RemoteServiceException;
import android.content.Context;
import android.os.DeadSystemException;

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
    public void uncaughtException(@NonNull Thread _thread, @NonNull Throwable _exception)
    {
        if (PPPEApplication.crashIntoFile) {
            StackTraceElement[] arr = _exception.getStackTrace();
            String report = _exception/*.toString()*/ + "\n\n";

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
            Throwable cause = _exception.getCause();
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

//        Log.e("TopExceptionHandler.uncaughtException", "defaultUEH="+defaultUEH);

        if (defaultUEH != null) {
//            Log.e("TopExceptionHandler.uncaughtException", "(2)");

            boolean ignore = false;
            if (_thread.getName().equals("FinalizerWatchdogDaemon") && (_exception instanceof TimeoutException)) {
                // ignore these exceptions
                // java.util.concurrent.TimeoutException: com.android.internal.os.BinderInternal$GcWatcher.finalize() timed out after 10 seconds
                // https://stackoverflow.com/a/55999687/2863059
                ignore = true;
            }
//            Log.e("TopExceptionHandler.uncaughtException", "(2x)");
            if (_exception instanceof DeadSystemException) {
                // ignore these exceptions
                // these are from dead of system for example:
                // java.lang.RuntimeException: Unable to create service
                // androidx.work.impl.background.systemjob.SystemJobService:
                // java.lang.RuntimeException: android.os.DeadSystemException
                ignore = true;
            }
//            Log.e("TopExceptionHandler.uncaughtException", "(2y)");
            if (_exception.getClass().getSimpleName().equals("CannotDeliverBroadcastException") &&
                    (_exception instanceof RemoteServiceException)) {
                // ignore but not exist exception
                // android.app.RemoteServiceException$CannotDeliverBroadcastException: can't deliver broadcast
                // https://stackoverflow.com/questions/72902856/cannotdeliverbroadcastexception-only-on-pixel-devices-running-android-12
                ignore = true;
            }
//            Log.e("TopExceptionHandler.uncaughtException", "(2z)");

            // this is only for debuging, how is handled ignored exceptions
//            if (_exception instanceof java.lang.RuntimeException) {
//                if (_exception.getMessage() != null) {
//                    if (_exception.getMessage().equals("Test Crash"))
//                        ignore = true;
//                    else
//                    if (_exception.getMessage().equals("Test non-fatal exception"))
//                        ignore = true;
//                }
//            }

//            Log.e("TopExceptionHandler.uncaughtException", "ignore="+ignore);

            if (!ignore) {
                //Delegates to Android's error handling
//                Log.e("TopExceptionHandler.uncaughtException", "(3)");
                defaultUEH.uncaughtException(_thread, _exception);
//                Log.e("TopExceptionHandler.uncaughtException", "(4)");
            } else
                //Prevents the service/app from freezing
                System.exit(2);
        }
        else
            //Prevents the service/app from freezing
            System.exit(2);

//        Log.e("TopExceptionHandler.uncaughtException", "end");
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
