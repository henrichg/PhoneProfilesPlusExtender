package sk.henrichg.phoneprofilesplusextender;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.pm.PackageInfoCompat;

import org.acra.ACRA;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Executors;

import me.drakeet.support.toast.ToastCompat;

class PPPEApplicationStatic {

    static void createBasicExecutorPool() {
        if (PPPEApplication.basicExecutorPool == null)
            PPPEApplication.basicExecutorPool = Executors.newCachedThreadPool();
    }

    static private void resetLog()
    {
        /*File sd = Environment.getExternalStorageDirectory();
        File exportDir = new File(sd, EXPORT_PATH);
        if (!(exportDir.exists() && exportDir.isDirectory()))
            //noinspection ResultOfMethodCallIgnored
            exportDir.mkdirs();*/

        File path = PPPEApplication.getInstance().getApplicationContext().getExternalFilesDir(null);
        File logFile = new File(path, PPPEApplication.LOG_FILENAME);
        //noinspection ResultOfMethodCallIgnored
        logFile.delete();
    }

    /** @noinspection SameParameterValue*/
    static private void logIntoFile(String type, String tag, String text)
    {
        if (!PPPEApplication.logIntoFile)
            return;

        if (PPPEApplication.getInstance() == null)
            return;

        try
        {
            File path = PPPEApplication.getInstance().getApplicationContext().getExternalFilesDir(null);

            /*// warnings when logIntoFile == false
            File sd = Environment.getExternalStorageDirectory();
            File exportDir = new File(sd, EXPORT_PATH);
            if (!(exportDir.exists() && exportDir.isDirectory()))
                //noinspection ResultOfMethodCallIgnored
                exportDir.mkdirs();

            File logFile = new File(sd, EXPORT_PATH + "/" + LOG_FILENAME);*/

            File logFile = new File(path, PPPEApplication.LOG_FILENAME);

            if (logFile.length() > 1024 * 10000)
                resetLog();

            if (!logFile.exists())
            {
                //noinspection ResultOfMethodCallIgnored
                logFile.createNewFile();
            }

            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            String log = "";
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat sdf = new SimpleDateFormat("d.MM.yy HH:mm:ss:S");
            String time = sdf.format(Calendar.getInstance().getTimeInMillis());
            log = log + time + " [ " + type + " ] [ " + tag + " ]"+StringConstants.STR_COLON_WITH_SPACE + text;
            buf.append(log);
            buf.newLine();
            buf.flush();
            buf.close();
        }
        catch (IOException ignored) {
            //Log.e("PPPEApplication.logIntoFile", Log.getStackTraceString(e));
        }
    }

    private static boolean logContainsFilterTag(String tag)
    {
        boolean contains = false;
        String[] splits = PPPEApplication.logFilterTags.split(StringConstants.STR_SPLIT_REGEX);
        for (String split : splits) {
            if (tag.contains(split)) {
                contains = true;
                break;
            }
        }
        return contains;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    static private boolean logEnabled() {
        return (PPPEApplication.logIntoLogCat || PPPEApplication.logIntoFile);
    }

    /*
    static public void logI(String tag, String text)
    {
        if (!logEnabled())
            return;

        if (logContainsFilterTag(tag))
        {
            //if (logIntoLogCat) Log.i(tag, text);
            if (logIntoLogCat) Log.i(tag, "[ "+tag+" ]" +StringConstants.STR_COLON_WITH_SPACE + text);
            logIntoFile("I", tag, text);
        }
    }
    */

    /*
    static public void logW(String tag, String text)
    {
        if (!logEnabled())
            return;

        if (logContainsFilterTag(tag))
        {
            //if (logIntoLogCat) Log.w(tag, text);
            if (logIntoLogCat) Log.w(tag, "[ "+tag+" ]" +StringConstants.STR_COLON_WITH_SPACE + text);
            logIntoFile("W", tag, text);
        }
    }
    */

    @SuppressWarnings("unused")
    static void logE(String tag, String text)
    {
        if (!logEnabled())
            return;

        if (logContainsFilterTag(tag))
        {
            //if (logIntoLogCat) Log.e(tag, text);
            if (PPPEApplication.logIntoLogCat) Log.e(tag, "[ "+tag+" ]" +StringConstants.STR_COLON_WITH_SPACE + text);
            logIntoFile("E", tag, text);
        }
    }

    /*
    static void logD(String tag, String text)
    {
        if (!logEnabled())
            return;

        if (logContainsFilterTag(tag))
        {
            //if (logIntoLogCat) Log.d(tag, text);
            if (logIntoLogCat) Log.d(tag, "[ "+tag+" ]" +StringConstants.STR_COLON_WITH_SPACE + text);
            logIntoFile("D", tag, text);
        }
    }
    */

    static void recordException(Throwable ex) {
        try {
            //FirebaseCrashlytics.getInstance().recordException(ex);
            ACRA.getErrorReporter().handleException(ex);
        } catch (Exception ignored) {}
    }

    /*
    static void logToACRA(String s) {
        try {
            //FirebaseCrashlytics.getInstance().log(s);
            ACRA.getErrorReporter().putCustomData("Log", s);
        } catch (Exception ignored) {}
    }
    */

    /*
    static void setCustomKey(String key, int value) {
        try {
            //FirebaseCrashlytics.getInstance().setCustomKey(key, value);
            ACRA.getErrorReporter().putCustomData(key, String.valueOf(value));
        } catch (Exception ignored) {}
    }
    */

    /*
    static void setCustomKey(String key, String value) {
        try {
            //FirebaseCrashlytics.getInstance().setCustomKey(key, value);
            ACRA.getErrorReporter().putCustomData(key, value);
        } catch (Exception ignored) {}
    }
    */

    @SuppressWarnings("SameParameterValue")
    static void setCustomKey(String key, boolean value) {
        try {
            //FirebaseCrashlytics.getInstance().setCustomKey(key, value);
            ACRA.getErrorReporter().putCustomData(key, String.valueOf(value));
        } catch (Exception ignored) {}
    }

    static boolean isIgnoreBatteryOptimizationEnabled(Context appContext) {
        PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
        try {
            if (pm != null) {
                return pm.isIgnoringBatteryOptimizations(PPPEApplication.PACKAGE_NAME);
            }
        } catch (Exception ignore) {
            return false;
        }
        return false;
    }

    static boolean isScreenOn(PowerManager powerManager) {
        return powerManager.isInteractive();
    }

    static int getVersionCode(PackageInfo pInfo) {
        //return pInfo.versionCode;
        return (int) PackageInfoCompat.getLongVersionCode(pInfo);
    }

    /** @noinspection ExtractMethodRecommender*/
    static void createGrantPermissionNotificationChannel(Context context, boolean forceChange) {
        //if (Build.VERSION.SDK_INT >= 26) {
        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context.getApplicationContext());
            if ((!forceChange) && (notificationManager.getNotificationChannel(PPPEApplication.GRANT_PERMISSION_NOTIFICATION_CHANNEL) != null))
                return;

            // The user-visible name of the channel.
            CharSequence name = context.getString(R.string.extender_notification_channel_grant_permission);
            // The user-visible description of the channel.
            String description = context.getString(R.string.extender_notification_channel_grant_permission_description);

            NotificationChannel channel = new NotificationChannel(PPPEApplication.GRANT_PERMISSION_NOTIFICATION_CHANNEL, name, NotificationManager.IMPORTANCE_HIGH);

            // Configure the notification channel.
            //channel.setImportance(importance);
            channel.setDescription(description);
            channel.enableLights(true);
            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            //channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            //channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            channel.setBypassDnd(true);

            notificationManager.createNotificationChannel(channel);
        } catch (Exception e) {
            PPPEApplicationStatic.recordException(e);
        }
        //}
    }

    static void showToast(final Context context, final String text,
                          @SuppressWarnings("SameParameterValue") final int length) {
        final Context appContext = context.getApplicationContext();
        Handler handler = new Handler(context.getApplicationContext().getMainLooper());
        handler.post(() -> {
//                PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=PPApplication.showToast");
            try {
                LocaleHelper.setApplicationLocale(appContext);

                //ToastCompat msg = ToastCompat.makeText(appContext, text, length);
                ToastCompat msg = ToastCompat.makeCustom(appContext,
                        R.layout.toast_layout, R.drawable.toast_background,
                        R.id.custom_toast_message, text,
                        length);
                //Toast msg = Toast.makeText(appContext, text, length);
                msg.show();
            } catch (Exception ignored) {
                //PPApplication.recordException(e);
            }
        });
    }

    static Collator getCollator()
    {
        Locale appLocale;

        // application locale
        appLocale = Locale.getDefault();

        // get collator for application locale
        return Collator.getInstance(appLocale);
    }

}
