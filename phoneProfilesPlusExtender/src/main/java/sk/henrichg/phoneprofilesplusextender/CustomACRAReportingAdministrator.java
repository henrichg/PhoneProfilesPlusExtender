package sk.henrichg.phoneprofilesplusextender;

import android.annotation.SuppressLint;
import android.app.RemoteServiceException;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.DeadSystemException;
import android.os.DeadSystemRuntimeException;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.pm.PackageInfoCompat;

import com.google.auto.service.AutoService;

import org.acra.builder.ReportBuilder;
import org.acra.config.CoreConfiguration;
import org.acra.config.ReportingAdministrator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeoutException;

// Custom ACRA ReportingAdministrator
// https://github.com/ACRA/acra/wiki/Custom-Extensions

/** @noinspection ExtractMethodRecommender*/
@SuppressWarnings("unused")
@AutoService(ReportingAdministrator.class)
public class CustomACRAReportingAdministrator implements ReportingAdministrator {

    static final String CRASH_FILENAME = "crash.txt";

    public CustomACRAReportingAdministrator() {
//        Log.e("CustomACRAReportingAdministrator constructor", "xxxx");
    }

    @Override
    public boolean shouldStartCollecting(@NonNull final Context context,
                                         @NonNull CoreConfiguration config,
                                         @NonNull ReportBuilder reportBuilder) {

//        Log.e("CustomACRAReportingAdministrator.shouldStartCollecting", "xxxx");

        final Throwable _exception = reportBuilder.getException();
        final Thread _thread = reportBuilder.getUncaughtExceptionThread();

        if (_exception == null)
            return true;

        try {
            if (PPPEApplication.crashIntoFile) {
                final Context appContext = context.getApplicationContext();
                Runnable runnable = () -> {
                    long actualVersionCode = 0;
                    try {
                        PackageInfo pInfo = appContext.getPackageManager().getPackageInfo(appContext.getPackageName(), 0);
                        //actualVersionCode = pInfo.versionCode;
                        actualVersionCode = PackageInfoCompat.getLongVersionCode(pInfo);
                    } catch (Exception ignored) {}

                    StackTraceElement[] arr = _exception.getStackTrace();
                    StringBuilder report = new StringBuilder(_exception.toString());

                    report.append(StringConstants.STR_DOUBLE_NEWLINE);

                    report.append("----- App version code: ").append(actualVersionCode).append(StringConstants.STR_DOUBLE_NEWLINE);

                    /*
                    for (StackTraceElement anArr : arr) {
                        report.append("    ").append(anArr.toString()).append("\n");
                    }
                    report.append("-------------------------------\n\n");
                    */

                    report.append("--------- Stack trace ---------").append(StringConstants.STR_DOUBLE_NEWLINE);
                    for (StackTraceElement anArr : arr) {
                        report.append("    ").append(anArr.toString()).append(StringConstants.CHAR_NEW_LINE);
                    }
                    report.append(StringConstants.CHAR_NEW_LINE);

                    // If the exception was thrown in a background thread inside
                    // AsyncTask, then the actual exception can be found with getCause
                    Throwable cause = _exception.getCause();
                    if (cause != null) {
                        report.append("-------------------------------").append(StringConstants.STR_DOUBLE_NEWLINE);
                        report.append("--------- Cause ---------------").append(StringConstants.STR_DOUBLE_NEWLINE);
                        report.append(cause).append(StringConstants.STR_DOUBLE_NEWLINE);
                        arr = cause.getStackTrace();
                        for (StackTraceElement anArr : arr) {
                            report.append("    ").append(anArr.toString()).append(StringConstants.CHAR_NEW_LINE);
                        }
                    }
                    report.append("-------------------------------").append(StringConstants.STR_DOUBLE_NEWLINE);

                    logIntoFile(appContext, "E", "CustomACRAReportingAdministrator", report.toString());
                };
                PPPEApplication.createBasicExecutorPool();
                PPPEApplication.basicExecutorPool.submit(runnable);
            }
        } catch (Exception ee) {
            //Log.e("CustomACRAReportingAdministrator.uncaughtException", Log.getStackTraceString(ee));
        }

        if (_exception instanceof TimeoutException) {
            if ((_thread != null) && _thread.getName().equals("FinalizerWatchdogDaemon"))
                return false;
        }

        if (_exception.getClass().getSimpleName().equals("CannotDeliverBroadcastException") &&
                (_exception instanceof RemoteServiceException)) {
            // ignore but not exist exception
            // android.app.RemoteServiceException$CannotDeliverBroadcastException: can't deliver broadcast
            // https://stackoverflow.com/questions/72902856/cannotdeliverbroadcastexception-only-on-pixel-devices-running-android-12
//            Log.e("CustomACRAReportingAdministrator.shouldStartCollecting", "CannotDeliverBroadcastException");
            return false;
        }

        if (_exception instanceof DeadSystemException) {
//            Log.e("CustomACRAReportingAdministrator.shouldStartCollecting", "DeadSystemException");
            // Exit app. This restarts PPP
            System.exit(2);
            return false;
        }

        if (Build.VERSION.SDK_INT >= 33) {
            if (_exception instanceof DeadSystemRuntimeException) {
//                Log.e("CustomACRAReportingAdministrator.shouldStartCollecting", "DeadSystemRuntimeException");
                // Exit app. This restarts PPP
                System.exit(2);
                return false;
            }
        }

        if (_exception instanceof RuntimeException) {
            String stackTrace = Log.getStackTraceString(_exception);
            if (stackTrace.contains("android.os.DeadSystemException")) {
                // Exit app. This restarts PPP
                System.exit(2);
                return false;
            }
            if (stackTrace.contains("android.os.DeadSystemRuntimeException")) {
                // Exit app. This restarts PPP
                System.exit(2);
                return false;
            }
        }

/*
        // this is only for debuging, how is handled ignored exceptions
        if (_exception instanceof java.lang.RuntimeException) {
            if (_exception.getMessage() != null) {
                if (_exception.getMessage().equals("Test Crash")) {
//                    Log.e("CustomACRAReportingAdministrator.shouldStartCollecting", "RuntimeException: Test Crash");
                    // Exit app. This restarts PPP
                    System.exit(2);
                    return false;
                }
                if (_exception.getMessage().equals("Test non-fatal exception")) {
//                    Log.e("CustomACRAReportingAdministrator.shouldStartCollecting", "RuntimeException: Test non-fatal exception");
                    return false;
                }
            }
        }
*/

        return true;
    }

    private void logIntoFile(Context context,
                             @SuppressWarnings("SameParameterValue") String type,
                             @SuppressWarnings("SameParameterValue") String tag,
                             String text)
    {
        try {
            /*File sd = Environment.getExternalStorageDirectory();
            File exportDir = new File(sd, PPApplication.EXPORT_PATH);
            if (!(exportDir.exists() && exportDir.isDirectory()))
                exportDir.mkdirs();

            File logFile = new File(sd, PPApplication.EXPORT_PATH + "/" + CRASH_FILENAME);*/

            File path = context.getExternalFilesDir(null);
            File logFile = new File(path, CRASH_FILENAME);

            if (logFile.length() > 1024 * 10000)
                resetLog(context);

            if (!logFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                logFile.createNewFile();
            }

            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat sdf = new SimpleDateFormat("d.MM.yy HH:mm:ss:S");
            String time = sdf.format(Calendar.getInstance().getTimeInMillis());
            String log = time + "--" + type + "-----" + tag + "------" + text;
            buf.append(log);
            buf.newLine();
            buf.flush();
            buf.close();
        } catch (IOException ee) {
            //Log.e("CustomACRAReportingAdministrator.logIntoFile", Log.getStackTraceString(ee));
        }
    }

    private void resetLog(Context context)
    {
        /*File sd = Environment.getExternalStorageDirectory();
        File exportDir = new File(sd, PPApplication.EXPORT_PATH);
        if (!(exportDir.exists() && exportDir.isDirectory()))
            exportDir.mkdirs();

        File logFile = new File(sd, PPApplication.EXPORT_PATH + "/" + CRASH_FILENAME);*/

        File path = context.getExternalFilesDir(null);
        File logFile = new File(path, CRASH_FILENAME);

        //noinspection ResultOfMethodCallIgnored
        logFile.delete();
    }

}
