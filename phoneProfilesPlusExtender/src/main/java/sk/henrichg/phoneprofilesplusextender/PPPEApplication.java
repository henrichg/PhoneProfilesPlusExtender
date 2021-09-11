package sk.henrichg.phoneprofilesplusextender;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.content.pm.PackageInfoCompat;

import org.acra.ACRA;
import org.acra.config.CoreConfigurationBuilder;
import org.acra.config.MailSenderConfigurationBuilder;
import org.acra.config.NotificationConfigurationBuilder;
import org.acra.data.StringFormat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;

//import com.google.firebase.crashlytics.FirebaseCrashlytics;

//import com.llew.huawei.verifier.LoadedApkHuaWei;

//import com.google.firebase.analytics.FirebaseAnalytics;
//import com.github.anrwatchdog.ANRError;
//import com.github.anrwatchdog.ANRWatchDog;

@SuppressWarnings("WeakerAccess")
public class PPPEApplication extends Application {

    private static PPPEApplication instance;

    static final String PACKAGE_NAME = "sk.henrichg.phoneprofilesplusextender";

    @SuppressWarnings("PointlessBooleanExpression")
    private static final boolean logIntoLogCat = true && BuildConfig.DEBUG;
    // TODO: DISABLE IT FOR RELEASE VERSION!!!
    static final boolean logIntoFile = false;
    @SuppressWarnings("PointlessBooleanExpression")
    static final boolean crashIntoFile = true && BuildConfig.DEBUG;
    private static final String logFilterTags = ""
                                                //+"|PPPEAccessibilityService"
                                                //+"|SMSBroadcastReceiver"

                                                //+"|PhoneCallReceiver"
                                                //+"|PPPEPhoneStateListener"

                                                //+ "|MainActivity"
                                                //+ "|FromPhoneProfilesPlusBroadcastReceiver"
            ;

    static final boolean deviceIsOppo = isOppo();
    static final boolean deviceIsRealme = isRealme();
    static final boolean deviceIsHuawei = isHuawei();
    static final boolean deviceIsSamsung = isSamsung();

    // for new log.txt and crash.txt is in /Android/data/sk.henrichg.phoneprofilesplusextender/files
    //public static final String EXPORT_PATH = "/PhoneProfilesPlusExtender";
    private static final String LOG_FILENAME = "log.txt";

    static final String ACCESSIBILITY_SERVICE_PERMISSION = PPPEApplication.PACKAGE_NAME + ".ACCESSIBILITY_SERVICE_PERMISSION";

    static final String ACTION_ACCESSIBILITY_SERVICE_IS_CONNECTED = PPPEApplication.PACKAGE_NAME + ".ACTION_ACCESSIBILITY_SERVICE_IS_CONNECTED";
    static final String ACTION_REGISTER_PPPE_FUNCTION = PPPEApplication.PACKAGE_NAME + ".ACTION_REGISTER_PPPE_FUNCTION";

    static final String EXTRA_REGISTRATION_APP = "registration_app";
    static final String EXTRA_REGISTRATION_TYPE = "registration_type";
    static final int REGISTRATION_TYPE_FORCE_STOP_APPLICATIONS_REGISTER = 1;
    static final int REGISTRATION_TYPE_FORCE_STOP_APPLICATIONS_UNREGISTER = -1;
    static final int REGISTRATION_TYPE_FOREGROUND_APPLICATION_REGISTER = 2;
    static final int REGISTRATION_TYPE_FOREGROUND_APPLICATION_UNREGISTER = -2;
    static final int REGISTRATION_TYPE_SMS_REGISTER = 3;
    static final int REGISTRATION_TYPE_SMS_UNREGISTER = -3;
    static final int REGISTRATION_TYPE_CALL_REGISTER = 4;
    static final int REGISTRATION_TYPE_CALL_UNREGISTER = -4;
    static final int REGISTRATION_TYPE_LOCK_DEVICE_REGISTER = 5;
    static final int REGISTRATION_TYPE_LOCK_DEVICE_UNREGISTER = -5;

    //@SuppressWarnings("SpellCheckingInspection")
    //static private FirebaseAnalytics mFirebaseAnalytics;

    static boolean registeredForceStopApplicationsFunctionPP = true;
    static boolean registeredForceStopApplicationsFunctionPPP = true;
    static boolean registeredForegroundApplicationFunctionPPP = true;
    static boolean registeredSMSFunctionPPP = true;
    static boolean registeredCallFunctionPPP = true;
    static boolean registeredLockDeviceFunctionPP = true;
    static boolean registeredLockDeviceFunctionPPP = true;

    static FromPhoneProfilesPlusBroadcastReceiver fromPhoneProfilesPlusBroadcastReceiver = null;
    static ScreenOnOffBroadcastReceiver screenOnOffReceiver = null;
    static SMSBroadcastReceiver smsBroadcastReceiver = null;
    static SMSBroadcastReceiver mmsBroadcastReceiver = null;
    static PhoneCallReceiver phoneCallReceiver = null;
    static SimStateChangedBroadcastReceiver simStateChangedBroadcastReceiver = null;

    static PPPEPhoneStateListener phoneStateListenerSIM1 = null;
    static PPPEPhoneStateListener phoneStateListenerSIM2 = null;
    static PPPEPhoneStateListener phoneStateListenerDefaul = null;

    static TelephonyManager telephonyManagerSIM1 = null;
    static TelephonyManager telephonyManagerSIM2 = null;
    static TelephonyManager telephonyManagerDefault = null;

    static boolean forceStopStarted = false;
    static boolean applicationForceClosed = false;
    static boolean forceStopPerformed = false;

    @Override
    public void onCreate() {
        super.onCreate();

        if (ACRA.isACRASenderServiceProcess())
            return;

/*        CoreConfigurationBuilder builder = new CoreConfigurationBuilder(this)
                .setBuildConfigClass(BuildConfig.class)
                .setReportFormat(StringFormat.KEY_VALUE_LIST);
        //builder.getPluginConfigurationBuilder(ToastConfigurationBuilder.class)
        //        .setResText(R.string.acra_toast_text)
        //        .setEnabled(true);
        builder.getPluginConfigurationBuilder(NotificationConfigurationBuilder.class)
                .setResChannelName(R.string.extender_notification_channel_crash_report)
                .setResChannelImportance(NotificationManager.IMPORTANCE_DEFAULT)
                .setResIcon(R.drawable.ic_exclamation_notify)
                .setResTitle(R.string.extender_acra_notification_title)
                .setResText(R.string.extender_acra_notification_text)
                .setResSendButtonIcon(0)
                .setResDiscardButtonIcon(0)
                .setSendOnClick(true)
                .setEnabled(true);
        builder.getPluginConfigurationBuilder(MailSenderConfigurationBuilder.class)
                .setMailTo("henrich.gron@gmail.com")
                .setResSubject(R.string.extender_acra_email_subject_text)
                .setResBody(R.string.extender_acra_email_body_text)
                .setReportAsFile(true)
                .setReportFileName("crash_report.txt")
                .setEnabled(true);

        ACRA.init(this, builder);

        // don't schedule anything in crash reporter process
        if (ACRA.isACRASenderServiceProcess())
            return;
*/
        instance = this;

        if (checkAppReplacingState())
            return;

        ///////////////////////////////////////////
        // Bypass Android's hidden API restrictions
        // https://github.com/tiann/FreeReflection
        if (Build.VERSION.SDK_INT >= 28) {
            try {
                Method forName = Class.class.getDeclaredMethod("forName", String.class);
                Method getDeclaredMethod = Class.class.getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);

                Class<?> vmRuntimeClass = (Class<?>) forName.invoke(null, "dalvik.system.VMRuntime");
                Method getRuntime = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "getRuntime", null);
                Method setHiddenApiExemptions = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "setHiddenApiExemptions", new Class[]{String[].class});

                Object vmRuntime = getRuntime.invoke(null);

                setHiddenApiExemptions.invoke(vmRuntime, new Object[]{new String[]{"L"}});
            } catch (Exception e) {
                Log.e("PPApplication.onCreate", Log.getStackTraceString(e));
                PPPEApplication.recordException(e);
            }
        }
        //////////////////////////////////////////

        // Fix for FC: java.lang.IllegalArgumentException: register too many Broadcast Receivers
        //LoadedApkHuaWei.hookHuaWeiVerifier(this);

        /*
        // set up ANR-WatchDog
        ANRWatchDog anrWatchDog = new ANRWatchDog();
        //anrWatchDog.setReportMainThreadOnly();
        anrWatchDog.setANRListener(new ANRWatchDog.ANRListener() {
            @Override
            public void onAppNotResponding(ANRError error) {
                Crashlytics.getInstance().core.logException(error);
            }
        });
        anrWatchDog.start();
        */

        try {
            PPPEApplication.setCustomKey("DEBUG", BuildConfig.DEBUG);
        } catch (Exception ignored) {}

        //if (BuildConfig.DEBUG) {
        long actualVersionCode = 0;
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            //actualVersionCode = pInfo.versionCode;
            actualVersionCode = PackageInfoCompat.getLongVersionCode(pInfo);
        } catch (Exception e) {
            Log.e("PPPEApplication.onCreate", Log.getStackTraceString(e));
            PPPEApplication.recordException(e);
        }
        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(getApplicationContext(), actualVersionCode));
        //}

    }

    // workaround for: java.lang.NullPointerException: Attempt to invoke virtual method
    // 'android.content.res.AssetManager android.content.res.Resources.getAssets()' on a null object reference
    // https://issuetracker.google.com/issues/36972466
    @SuppressLint("LongLogTag")
    private boolean checkAppReplacingState() {
        if (getResources() == null) {
            Log.w("PPPEApplication.onCreate", "app is replacing...kill");
            android.os.Process.killProcess(android.os.Process.myPid());
            return true;
        }
        return false;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        if (ACRA.isACRASenderServiceProcess()) {
            Log.e("##### PPPEApplication.attachBaseContext", "ACRA.isACRASenderServiceProcess()");
            return;
        }

        String packageVersion = "";
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(PPPEApplication.PACKAGE_NAME, 0);
            packageVersion = " - v" + pInfo.versionName + " (" + PPPEApplication.getVersionCode(pInfo) + ")";
        } catch (Exception e) {
            PPPEApplication.recordException(e);
        }

        String body;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
            body = getString(R.string.extender_acra_email_body_device) + " " +
                    Settings.Global.getString(getContentResolver(), Settings.Global.DEVICE_NAME) +
                    " (" + Build.MODEL + ")" + " \n";
        else {
            String manufacturer = Build.MANUFACTURER;
            String model = Build.MODEL;
            if (model.startsWith(manufacturer))
                body = getString(R.string.extender_acra_email_body_device) + " " + model + " \n";
            else
                body = getString(R.string.extender_acra_email_body_device) + " " + manufacturer + " " + model + " \n";
        }
        body = body + getString(R.string.extender_acra_email_body_android_version) + " " + Build.VERSION.RELEASE + " \n\n";
        body = body + getString(R.string.extender_acra_email_body_text);

        Log.e("##### PPPEApplication.attachBaseContext", "ACRA inittialization");
        CoreConfigurationBuilder builder = new CoreConfigurationBuilder(this)
                .withBuildConfigClass(BuildConfig.class)
                .withReportFormat(StringFormat.KEY_VALUE_LIST);
        /*builder.getPluginConfigurationBuilder(ToastConfigurationBuilder.class)
                .setResText(R.string.acra_toast_text)
                .setEnabled(true);*/
        builder.getPluginConfigurationBuilder(NotificationConfigurationBuilder.class)
                .withResChannelName(R.string.extender_notification_channel_crash_report)
                .withResChannelImportance(NotificationManager.IMPORTANCE_DEFAULT)
                .withResIcon(R.drawable.ic_exclamation_notify)
                .withResTitle(R.string.extender_acra_notification_title)
                .withResText(R.string.extender_acra_notification_text)
                .withResSendButtonIcon(0)
                .withResDiscardButtonIcon(0)
                .withSendOnClick(true)
                .withEnabled(true);
        builder.getPluginConfigurationBuilder(MailSenderConfigurationBuilder.class)
                .withMailTo("henrich.gron@gmail.com")
                .withSubject("PhoneProfilesPlusExtender" + packageVersion + " - " + getString(R.string.extender_acra_email_subject_text))
                .withBody(body)
                .withReportAsFile(true)
                .withReportFileName("crash_report.txt")
                .withEnabled(true);

        //ACRA.DEV_LOGGING = true;

        ACRA.init(this, builder);
    }

    //--------------------------------------------------------------

    private static boolean isOppo() {
        return Build.BRAND.equalsIgnoreCase("oppo") ||
                Build.MANUFACTURER.equalsIgnoreCase("oppo") ||
                Build.FINGERPRINT.toLowerCase().contains("oppo");
    }

    private static boolean isRealme() {
        return Build.BRAND.equalsIgnoreCase("realme") ||
                Build.MANUFACTURER.equalsIgnoreCase("realme") ||
                Build.FINGERPRINT.toLowerCase().contains("realme");
    }

    private static boolean isHuawei() {
        return Build.BRAND.equalsIgnoreCase("huawei") ||
                Build.MANUFACTURER.equalsIgnoreCase("huawei") ||
                Build.FINGERPRINT.toLowerCase().contains("huawei");
    }

    private static boolean isSamsung() {
        return Build.BRAND.equalsIgnoreCase("samsung") ||
                Build.MANUFACTURER.equalsIgnoreCase("samsung") ||
                Build.FINGERPRINT.toLowerCase().contains("samsung");
    }

    //--------------------------------------------------------------

    static private void resetLog()
    {
        /*File sd = Environment.getExternalStorageDirectory();
        File exportDir = new File(sd, EXPORT_PATH);
        if (!(exportDir.exists() && exportDir.isDirectory()))
            //noinspection ResultOfMethodCallIgnored
            exportDir.mkdirs();*/

        File path = instance.getApplicationContext().getExternalFilesDir(null);
        File logFile = new File(path, LOG_FILENAME);
        //noinspection ResultOfMethodCallIgnored
        logFile.delete();
    }

    @SuppressLint("SimpleDateFormat")
    static private void logIntoFile(String type, String tag, String text)
    {
        if (!logIntoFile)
            return;

        if (instance == null)
            return;

        try
        {
            File path = instance.getApplicationContext().getExternalFilesDir(null);

            /*// warnings when logIntoFile == false
            File sd = Environment.getExternalStorageDirectory();
            File exportDir = new File(sd, EXPORT_PATH);
            if (!(exportDir.exists() && exportDir.isDirectory()))
                //noinspection ResultOfMethodCallIgnored
                exportDir.mkdirs();

            File logFile = new File(sd, EXPORT_PATH + "/" + LOG_FILENAME);*/

            File logFile = new File(path, LOG_FILENAME);

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
            SimpleDateFormat sdf = new SimpleDateFormat("d.MM.yy HH:mm:ss:S");
            String time = sdf.format(Calendar.getInstance().getTimeInMillis());
            log = log + time + "--" + type + "-----" + tag + "------" + text;
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
        String[] splits = logFilterTags.split("\\|");
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
        return (logIntoLogCat || logIntoFile);
    }

    @SuppressWarnings("unused")
    static public void logI(String tag, String text)
    {
        if (!logEnabled())
            return;

        if (logContainsFilterTag(tag))
        {
            if (logIntoLogCat) Log.i(tag, text);
            logIntoFile("I", tag, text);
        }
    }

    @SuppressWarnings("unused")
    static public void logW(String tag, String text)
    {
        if (!logEnabled())
            return;

        if (logContainsFilterTag(tag))
        {
            if (logIntoLogCat) Log.w(tag, text);
            logIntoFile("W", tag, text);
        }
    }

    @SuppressWarnings("unused")
    static public void logE(String tag, String text)
    {
        if (!logEnabled())
            return;

        if (logContainsFilterTag(tag))
        {
            if (logIntoLogCat) Log.e(tag, text);
            logIntoFile("E", tag, text);
        }
    }

    @SuppressWarnings("unused")
    static public void logD(String tag, String text)
    {
        if (!logEnabled())
            return;

        if (logContainsFilterTag(tag))
        {
            if (logIntoLogCat) Log.d(tag, text);
            logIntoFile("D", tag, text);
        }
    }

    // Firebase Crashlytics -------------------------------------------------------------------------

    static void recordException(Throwable ex) {
        try {
            //FirebaseCrashlytics.getInstance().recordException(ex);
            ACRA.getErrorReporter().handleException(ex);
        } catch (Exception ignored) {}
    }

    @SuppressWarnings("unused")
    static void logToCrashlytics(String s) {
        try {
            //FirebaseCrashlytics.getInstance().log(s);
            ACRA.getErrorReporter().putCustomData("Log", s);
        } catch (Exception ignored) {}
    }

    @SuppressWarnings("unused")
    static void setCustomKey(String key, int value) {
        try {
            //FirebaseCrashlytics.getInstance().setCustomKey(key, value);
            ACRA.getErrorReporter().putCustomData(key, String.valueOf(value));
        } catch (Exception ignored) {}
    }

    @SuppressWarnings("unused")
    static void setCustomKey(String key, String value) {
        try {
            //FirebaseCrashlytics.getInstance().setCustomKey(key, value);
            ACRA.getErrorReporter().putCustomData(key, value);
        } catch (Exception ignored) {}
    }

    @SuppressWarnings("SameParameterValue")
    static void setCustomKey(String key, boolean value) {
        try {
            //FirebaseCrashlytics.getInstance().setCustomKey(key, value);
            ACRA.getErrorReporter().putCustomData(key, String.valueOf(value));
        } catch (Exception ignored) {}
    }

    // Google Analytics ----------------------------------------------------------------------------

    /*
    static void logAnalyticsEvent(String itemId, String itemName, String contentType) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, itemId);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, itemName);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, contentType);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }
    */

    //---------------------------------------------------------------------------------------------

    static boolean hasSystemFeature(Context context, @SuppressWarnings("SameParameterValue") String feature) {
        try {
            PackageManager packageManager = context.getPackageManager();
            return packageManager.hasSystemFeature(feature);
        } catch (Exception e) {
            return false;
        }
    }

    static boolean isIgnoreBatteryOptimizationEnabled(Context appContext) {
        PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
        try {
            if (pm != null) {
                return pm.isIgnoringBatteryOptimizations(PACKAGE_NAME);
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

}
