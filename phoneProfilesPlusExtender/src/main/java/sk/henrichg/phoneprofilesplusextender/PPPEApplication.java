package sk.henrichg.phoneprofilesplusextender;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.content.ContextCompat;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.config.CoreConfigurationBuilder;
import org.acra.config.MailSenderConfigurationBuilder;
import org.acra.config.NotificationConfigurationBuilder;
import org.acra.data.StringFormat;
import org.lsposed.hiddenapibypass.HiddenApiBypass;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.Collator;
import java.util.concurrent.ExecutorService;

/** @noinspection ExtractMethodRecommender*/
public class PPPEApplication extends Application {

    private static volatile PPPEApplication instance;

    static final String PACKAGE_NAME = "sk.henrichg.phoneprofilesplusextender";

    static final String APPLICATION_PREFS_NAME = "phone_profiles_plus_extender_preferences";

    static final String CROWDIN_URL = "https://crowdin.com/project/phoneprofilesplus";

    static final String INTENT_DATA_PACKAGE = "package:";
    static final String EXTRA_PKG_NAME = "extra_pkgname";

    //static final int pid = Process.myPid();
    //static final int uid = Process.myUid();

    @SuppressWarnings("PointlessBooleanExpression")
    static final boolean logIntoLogCat = true && BuildConfig.DEBUG;
    // TODO: SET IT TO FALSE FOR RELEASE VERSION!!!
    static final boolean logIntoFile = false;
    @SuppressWarnings("PointlessBooleanExpression")
    static final boolean crashIntoFile = true && BuildConfig.DEBUG;
    static final String logFilterTags = ""
                                                //+"|PPPEAccessibilityService"
                                                //+"|SMSBroadcastReceiver"

                                                //+"|PhoneCallReceiver"
                                                //+"|PPPEPhoneStateListener"

                                                //+ "|MainActivity"
                                                //+ "|FromPhoneProfilesPlusBroadcastReceiver"

                                                //+"|[BROADCAST_TO_PPP]"
                                                //+"[MEMORY_LEAK]"
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

    static final String EXTRA_APPLICATIONS = "extra_applications";
    static final String EXTRA_PROFILE_ID = "profile_id";
    static final String EXTRA_SCROLL_TO = "extra_main_activity_scroll_to";
    static final String EXTRA_BLOCK_PROFILE_EVENT_ACTION = "extra_block_profile_event_actions";

    static final String ACTION_CALL_RECEIVED = PPPEApplication.PACKAGE_NAME + ".ACTION_CALL_RECEIVED";
    //private static final String EXTRA_SERVICE_PHONE_EVENT = PPPEApplication.PACKAGE_NAME + ".service_phone_event";
    static final String EXTRA_CALL_EVENT_TYPE = PPPEApplication.PACKAGE_NAME + ".call_event_type";
    static final String EXTRA_PHONE_NUMBER = PPPEApplication.PACKAGE_NAME + ".phone_number";
    static final String EXTRA_EVENT_TIME = PPPEApplication.PACKAGE_NAME + ".event_time";
    static final String EXTRA_SIM_SLOT = PPPEApplication.PACKAGE_NAME + ".sim_slot";

    static volatile boolean HAS_FEATURE_TELEPHONY = false;

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

    static boolean screenOffReceived = false;

    static volatile Collator collator = null;

    @Override
    public void onCreate() {
        super.onCreate();

        // This is required : https://www.acra.ch/docs/Troubleshooting-Guide#applicationoncreate
        if (ACRA.isACRASenderServiceProcess()) {
            Log.e("################# PPPEApplication.onCreate", "ACRA.isACRASenderServiceProcess()");
            return;
        }

//        PPPEApplicationStatic.logE("[MEMORY_LEAK] PPPEApplication.onCreate", "xxxx");


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
//        PPPEApplicationStatic.logE("[MEMORY_LEAK] PPPEApplication.onCreate", "xxxx (2)");

        PPPEApplicationStatic.createGrantPermissionNotificationChannel(this, true);

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

        PPPEApplicationStatic.createBasicExecutorPool();

        PackageManager packageManager = getPackageManager();
        HAS_FEATURE_TELEPHONY = hasSystemFeature(packageManager, PackageManager.FEATURE_TELEPHONY);

        try {
            PPPEApplicationStatic.setCustomKey("DEBUG", BuildConfig.DEBUG);
        } catch (Exception ignored) {}

//        PPPEApplication.logE("[BROADCAST_TO_PPP] PPPEApplication.onCreate", "xxxx");
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

    static PPPEApplication getInstance() {
        //synchronized (PPApplication.phoneProfilesServiceMutex) {
        return instance;
        //}
    }

    @Override
    protected void attachBaseContext(Context base) {
        //super.attachBaseContext(base);
        super.attachBaseContext(LocaleHelper.onAttach(base));
        //Reflection.unseal(base);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            HiddenApiBypass.addHiddenApiExemptions("L");
        }

        collator = PPPEApplicationStatic.getCollator();

        // This is required : https://www.acra.ch/docs/Troubleshooting-Guide#applicationoncreate
        if (ACRA.isACRASenderServiceProcess()) {
            Log.e("################# PPPEApplication.attachBaseContext", "ACRA.isACRASenderServiceProcess()");
            return;
        }

        String packageVersion = "";
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(PPPEApplication.PACKAGE_NAME, 0);
            packageVersion = " - v" + pInfo.versionName + " (" + PPPEApplicationStatic.getVersionCode(pInfo) + ")";
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
            miuiRom1 = !line.isEmpty();
            input.close();

            if (!miuiRom1) {
                p = Runtime.getRuntime().exec("getprop ro.miui.ui.version.name");
                input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
                line = input.readLine();
                miuiRom2 = !line.isEmpty();
                input.close();
            }

            if (!miuiRom1 && !miuiRom2) {
                p = Runtime.getRuntime().exec("getprop ro.miui.internal.storage");
                input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
                line = input.readLine();
                miuiRom3 = !line.isEmpty();
                input.close();
            }

        } catch (Exception ex) {
            //Log.e("PPPEApplication.isMIUIROM", Log.getStackTraceString(ex));
            PPPEApplicationStatic.recordException(ex);
        }

        return miuiRom1 || miuiRom2 || miuiRom3;
    }

    //--------------------------------------------------------------

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

    /** @noinspection SameParameterValue*/
    private boolean hasSystemFeature(PackageManager packageManager, String feature) {
        try {
            return packageManager.hasSystemFeature(feature);
        } catch (Exception e) {
            return false;
        }
    }

}
