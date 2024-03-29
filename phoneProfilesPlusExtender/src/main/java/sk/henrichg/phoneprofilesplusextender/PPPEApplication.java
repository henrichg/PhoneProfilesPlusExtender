package sk.henrichg.phoneprofilesplusextender;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.pm.PackageInfoCompat;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.config.CoreConfigurationBuilder;
import org.acra.config.MailSenderConfigurationBuilder;
import org.acra.config.NotificationConfigurationBuilder;
import org.acra.data.StringFormat;
import org.lsposed.hiddenapibypass.HiddenApiBypass;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.drakeet.support.toast.ToastCompat;

//import com.google.firebase.crashlytics.FirebaseCrashlytics;

//import com.llew.huawei.verifier.LoadedApkHuaWei;

//import com.google.firebase.analytics.FirebaseAnalytics;
//import com.github.anrwatchdog.ANRError;
//import com.github.anrwatchdog.ANRWatchDog;

public class PPPEApplication extends Application {

    private static PPPEApplication instance;

    static final String PACKAGE_NAME = "sk.henrichg.phoneprofilesplusextender";

    static final String APPLICATION_PREFS_NAME = "phone_profiles_plus_extender_preferences";

    static final String CROWDIN_URL = "https://crowdin.com/project/phoneprofilesplus";

    static final String INTENT_DATA_PACKAGE = "package:";
    static final String EXTRA_PKG_NAME = "extra_pkgname";

    //static final int pid = Process.myPid();
    //static final int uid = Process.myUid();

    @SuppressWarnings("PointlessBooleanExpression")
    private static final boolean logIntoLogCat = true && BuildConfig.DEBUG;
    // TODO: SET IT TO FALSE FOR RELEASE VERSION!!!
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
    static final boolean deviceIsXiaomi = isXiaomi();
    static final boolean deviceIsOnePlus = isOnePlus();
    static final boolean romIsMIUI = isMIUIROM();

    // for new log.txt and crash.txt is in /Android/data/sk.henrichg.phoneprofilesplusextender/files
    //public static final String EXPORT_PATH = "/PhoneProfilesPlusExtender";
    static final String LOG_FILENAME = "log.txt";

    static final String GRANT_PERMISSION_NOTIFICATION_CHANNEL = "phoneProfilesPlusExtender_grant_permission";
    static final int GRANT_PERMISSIONS_SMS_NOTIFICATION_ID = 101;
    static final String GRANT_PERMISSIONS_SMS_NOTIFICATION_TAG = PACKAGE_NAME+"_GRANT_PROFILE_PERMISSIONS_SMS_NOTIFICATION";
    static final int GRANT_PERMISSIONS_CALL_NOTIFICATION_ID = 102;
    static final String GRANT_PERMISSIONS_CALL_NOTIFICATION_TAG = PACKAGE_NAME+"_GRANT_PROFILE_PERMISSIONS_CALL_NOTIFICATION";

    static final String ACCESSIBILITY_SERVICE_PERMISSION = PPPEApplication.PACKAGE_NAME + ".ACCESSIBILITY_SERVICE_PERMISSION";

    static final String ACTION_PPPEXTENDER_STARTED = PPPEApplication.PACKAGE_NAME + ".ACTION_PPPEXTENDER_STARTED";
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

    static final String EXTRA_BLOCK_PROFILE_EVENT_ACTION = "extra_block_profile_event_actions";

    //@SuppressWarnings("SpellCheckingInspection")
    //static private FirebaseAnalytics mFirebaseAnalytics;

    static boolean registeredForceStopApplicationsFunctionPP = true;
    static boolean registeredForceStopApplicationsFunctionPPP = true;
    static boolean registeredForegroundApplicationFunctionPPP = true;
    static boolean registeredSMSFunctionPPP = true;
    static boolean registeredCallFunctionPPP = true;
    static boolean registeredLockDeviceFunctionPP = true;
    static boolean registeredLockDeviceFunctionPPP = true;

    static volatile ExecutorService basicExecutorPool = null;

    static FromPhoneProfilesPlusBroadcastReceiver fromPhoneProfilesPlusBroadcastReceiver = null;
    static ScreenOnOffBroadcastReceiver screenOnOffReceiver = null;
    static SMSBroadcastReceiver smsBroadcastReceiver = null;
    static SMSBroadcastReceiver mmsBroadcastReceiver = null;
    static PhoneCallReceiver phoneCallReceiver = null;
    static SimStateChangedBroadcastReceiver simStateChangedBroadcastReceiver = null;

    static PPPEPhoneStateListener phoneStateListenerSIM1 = null;
    static PPPEPhoneStateListener phoneStateListenerSIM2 = null;
    static PPPEPhoneStateListener phoneStateListenerDefault = null;

    static TelephonyManager telephonyManagerSIM1 = null;
    static TelephonyManager telephonyManagerSIM2 = null;
    static TelephonyManager telephonyManagerDefault = null;

    static boolean forceStopStarted = false;
    static boolean applicationForceClosed = false;
    static boolean forceStopPerformed = false;

    static volatile String latestApplicationPackageName;
    static volatile String getLatestApplicationClassName;

    static volatile Collator collator = null;

    @Override
    public void onCreate() {
        super.onCreate();

        // This is required : https://www.acra.ch/docs/Troubleshooting-Guide#applicationoncreate
        if (ACRA.isACRASenderServiceProcess()) {
            Log.e("################# PPPEApplication.onCreate", "ACRA.isACRASenderServiceProcess()");
            return;
        }

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

        //Log.e("##### PPPEApplication.onCreate", "Start  uid="+uid);

        PPPEApplication.createGrantPermissionNotificationChannel(this, true);

        Log.e("##### PPPEApplication.onCreate", "after cerate notification channel");

        ////////////////////////////////////////////////////////////////////////////////////
        // Bypass Android's hidden API restrictions
        // !!! WARNING - this is required also for android.jar from android-hidden-api !!!
        // https://github.com/tiann/FreeReflection
        /*if (Build.VERSION.SDK_INT >= 28) {
            try {
                Method forName = Class.class.getDeclaredMethod("forName", String.class);
                Method getDeclaredMethod = Class.class.getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);

                Class<?> vmRuntimeClass = (Class<?>) forName.invoke(null, "dalvik.system.VMRuntime");
                Method getRuntime = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "getRuntime", null);
                Method setHiddenApiExemptions = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "setHiddenApiExemptions", new Class[]{String[].class});

                if (getRuntime != null) {
                    Object vmRuntime = getRuntime.invoke(null);
                    if (setHiddenApiExemptions != null)
                        setHiddenApiExemptions.invoke(vmRuntime, new Object[]{new String[]{"L"}});
                }
            } catch (Exception e) {
                //Log.e("PPPEApplication.onCreate", Log.getStackTraceString(e));
                PPPEApplication.recordException(e);
            }
        }*/
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

        PPPEApplication.createBasicExecutorPool();

        try {
            PPPEApplication.setCustomKey("DEBUG", BuildConfig.DEBUG);
        } catch (Exception ignored) {}

        Intent sendIntent = new Intent(ACTION_PPPEXTENDER_STARTED);
        sendBroadcast(sendIntent, PPPEApplication.ACCESSIBILITY_SERVICE_PERMISSION);
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
        //super.attachBaseContext(base);
        super.attachBaseContext(LocaleHelper.onAttach(base));
        //Reflection.unseal(base);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            HiddenApiBypass.addHiddenApiExemptions("L");
        }

        collator = getCollator();

        // This is required : https://www.acra.ch/docs/Troubleshooting-Guide#applicationoncreate
        if (ACRA.isACRASenderServiceProcess()) {
            Log.e("################# PPPEApplication.attachBaseContext", "ACRA.isACRASenderServiceProcess()");
            return;
        }

        String packageVersion = "";
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(PPPEApplication.PACKAGE_NAME, 0);
            packageVersion = " - v" + pInfo.versionName + " (" + PPPEApplication.getVersionCode(pInfo) + ")";
        } catch (Exception ignored) {
        }

        String body;
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
            body = getString(R.string.extender_acra_email_body_device) + " " +
                    Settings.Global.getString(getContentResolver(), Settings.Global.DEVICE_NAME) +
                    " (" + Build.MODEL + ")" + StringConstants.STR_NEWLINE_WITH_SPACE;
        /*else {
            String manufacturer = Build.MANUFACTURER;
            String model = Build.MODEL;
            if (model.startsWith(manufacturer))
                body = getString(R.string.extender_acra_email_body_device) + " " + model + " \n";
            else
                body = getString(R.string.extender_acra_email_body_device) + " " + manufacturer + " " + model + " \n";
        }*/
        body = body + getString(R.string.extender_acra_email_body_android_version) + " " + Build.VERSION.RELEASE + StringConstants.STR_DOUBLE_NEWLINE_WITH_SPACE;
        body = body + getString(R.string.extender_acra_email_body_text);

        Log.e("##### PPPEApplication.attachBaseContext", "ACRA inittialization");

        ReportField[] reportContent = new ReportField[] {
                ReportField.REPORT_ID,
                ReportField.ANDROID_VERSION,
                ReportField.APP_VERSION_CODE,
                ReportField.APP_VERSION_NAME,
                ReportField.PHONE_MODEL,
                ReportField.PRODUCT,
                //ReportField.APPLICATION_LOG,
                ReportField.AVAILABLE_MEM_SIZE,
                ReportField.BRAND,
                ReportField.BUILD,
                //BUILD_CONFIG !!! must be removed because in it is also encrypt_contacts_key, encrypt_contacts_salt
                ReportField.CRASH_CONFIGURATION,
                ReportField.TOTAL_MEM_SIZE,
                ReportField.USER_APP_START_DATE,
                ReportField.USER_CRASH_DATE,

                ReportField.CUSTOM_DATA,
                ReportField.STACK_TRACE,
                ReportField.LOGCAT,

                ReportField.SHARED_PREFERENCES,

                ReportField.DEVICE_FEATURES,
                //ReportField.DEVICE_ID
                ReportField.DISPLAY,
                //DROPBOX
                //ReportField.DUMPSYS_MEMINFO,
                ReportField.ENVIRONMENT,
                //ReportField.FILE_PATH,
                ReportField.INITIAL_CONFIGURATION,
                //ReportField.INSTALLATION_ID,
                //ReportField.IS_SILENT,
                //ReportField.MEDIA_CODEC_LIST,
                //ReportField.PACKAGE_NAME,
                //ReportField.RADIOLOG,
                ReportField.SETTINGS_GLOBAL,
                ReportField.SETTINGS_SECURE,
                ReportField.SETTINGS_SYSTEM,
                //STACK_TRACE_HASH
                //ReportField.THREAD_DETAILS,
                //ReportField.USER_COMMENT,
                //ReportField.USER_EMAIL,
                //ReportField.USER_IP,
                ReportField.EVENTSLOG
        };

        CoreConfigurationBuilder builder = new CoreConfigurationBuilder()
                .withBuildConfigClass(BuildConfig.class)
                .withReportFormat(StringFormat.KEY_VALUE_LIST)
                .withReportContent(reportContent)
                ;

        builder.withPluginConfigurations(
                new NotificationConfigurationBuilder()
                        .withChannelName(getString(R.string.extender_notification_channel_crash_report))
                        .withChannelImportance(NotificationManager.IMPORTANCE_HIGH)
                        .withResIcon(R.drawable.ic_pppe_notification)
                        .withTitle(/*"!!! " +*/ getString(R.string.extender_acra_notification_title))
                        .withText(getString(R.string.extender_acra_notification_text))
                        .withResSendButtonIcon(0)
                        .withResDiscardButtonIcon(0)
                        .withSendOnClick(true)
                        .withColor(ContextCompat.getColor(base, R.color.error_color))
                        .withEnabled(true)
                        .build(),
                new MailSenderConfigurationBuilder()
                        .withMailTo(StringConstants.AUTHOR_EMAIL)
                        .withSubject(StringConstants.PHONE_PROFILES_PLUS_EXTENDER + packageVersion + " - " + getString(R.string.extender_acra_email_subject_text))
                        .withBody(body)
                        .withReportAsFile(true)
                        .withReportFileName("crash_report.txt")
                        .withEnabled(false) // must be false because of custom report sender
                        .build()
        );

        ACRA.DEV_LOGGING = false;

        ACRA.init(this, builder);

        /*
        //if (BuildConfig.DEBUG) {
        long actualVersionCode = 0;
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            //actualVersionCode = pInfo.versionCode;
            actualVersionCode = PackageInfoCompat.getLongVersionCode(pInfo);
        } catch (Exception ignored) {}

        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(base, actualVersionCode));
        //}
        */

    }

    //--------------------------------------------------------------

    private static boolean isOppo() {
        final String OPPO = "oppo";
        return Build.BRAND.equalsIgnoreCase(OPPO) ||
                Build.MANUFACTURER.equalsIgnoreCase(OPPO) ||
                Build.FINGERPRINT.toLowerCase().contains(OPPO);
    }

    private static boolean isRealme() {
        final String REALME = "realme";
        return Build.BRAND.equalsIgnoreCase(REALME) ||
                Build.MANUFACTURER.equalsIgnoreCase(REALME) ||
                Build.FINGERPRINT.toLowerCase().contains(REALME);
    }

    private static boolean isHuawei() {
        final String HUAWEI = "huawei";
        return Build.BRAND.equalsIgnoreCase(HUAWEI) ||
                Build.MANUFACTURER.equalsIgnoreCase(HUAWEI) ||
                Build.FINGERPRINT.toLowerCase().contains(HUAWEI);
    }

    private static boolean isSamsung() {
        final String SAMSUNG = "samsung";
        return Build.BRAND.equalsIgnoreCase(SAMSUNG) ||
                Build.MANUFACTURER.equalsIgnoreCase(SAMSUNG) ||
                Build.FINGERPRINT.toLowerCase().contains(SAMSUNG);
    }

    private static boolean isXiaomi() {
        final String XIOMI = "xiaomi";
        return Build.BRAND.equalsIgnoreCase(XIOMI) ||
                Build.MANUFACTURER.equalsIgnoreCase(XIOMI) ||
                Build.FINGERPRINT.toLowerCase().contains(XIOMI);
    }

    private static boolean isOnePlus() {
        final String ONEPLUS = "oneplus";
        return Build.BRAND.equalsIgnoreCase(ONEPLUS) ||
                Build.MANUFACTURER.equalsIgnoreCase(ONEPLUS) ||
                Build.FINGERPRINT.toLowerCase().contains(ONEPLUS);
    }

    private static boolean isMIUIROM() {
        boolean miuiRom1 = false;
        boolean miuiRom2 = false;
        boolean miuiRom3 = false;

        String line;
        BufferedReader input;
        try {
            java.lang.Process p = Runtime.getRuntime().exec("getprop ro.miui.ui.version.code");
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            miuiRom1 = line.length() != 0;
            input.close();

            if (!miuiRom1) {
                p = Runtime.getRuntime().exec("getprop ro.miui.ui.version.name");
                input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
                line = input.readLine();
                miuiRom2 = line.length() != 0;
                input.close();
            }

            if (!miuiRom1 && !miuiRom2) {
                p = Runtime.getRuntime().exec("getprop ro.miui.internal.storage");
                input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
                line = input.readLine();
                miuiRom3 = line.length() != 0;
                input.close();
            }

        } catch (Exception ex) {
            //Log.e("PPPEApplication.isMIUIROM", Log.getStackTraceString(ex));
            PPPEApplication.recordException(ex);
        }

        return miuiRom1 || miuiRom2 || miuiRom3;
    }

    static void createBasicExecutorPool() {
        if (PPPEApplication.basicExecutorPool == null)
            PPPEApplication.basicExecutorPool = Executors.newCachedThreadPool();
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

    /** @noinspection SameParameterValue*/
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
        String[] splits = logFilterTags.split(StringConstants.STR_SPLIT_REGEX);
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
    static public void logE(String tag, String text)
    {
        if (!logEnabled())
            return;

        if (logContainsFilterTag(tag))
        {
            //if (logIntoLogCat) Log.e(tag, text);
            if (logIntoLogCat) Log.e(tag, "[ "+tag+" ]" +StringConstants.STR_COLON_WITH_SPACE + text);
            logIntoFile("E", tag, text);
        }
    }

    /*
    static public void logD(String tag, String text)
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

    // ACRA -------------------------------------------------------------------------

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
                PPPEApplication.recordException(e);
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
