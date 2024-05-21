package sk.henrichg.phoneprofilesplusextender;

import android.app.IntentService;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;

import java.util.List;

public class ForceCloseIntentService extends IntentService {

    private static final List<Long> profileIdList = new java.util.ArrayList<>();
    private static int forceStopApplicationsStartCount = 0;

    public ForceCloseIntentService()
    {
        super("ForceCloseIntentService");

//        PPPEApplicationStatic.logE("[MEMORY_LEAK] ForceCloseIntentService (constructor)", "xxxx");

        // if enabled is true, onStartCommand(Intent, int, int) will return START_REDELIVER_INTENT,
        // so if this process dies before onHandleIntent(Intent) returns, the process will be restarted
        // and the intent redelivered. If multiple Intents have been sent, only the most recent one
        // is guaranteed to be redelivered.
        // -- but restarted service has intent == null??
        //setIntentRedelivery(true);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }

//        PPPEApplicationStatic.logE("[MEMORY_LEAK] ForceCloseIntentService.onHandleIntent", "xxxx");

        long profileId = intent.getLongExtra(PPPEApplication.EXTRA_PROFILE_ID, 0);
        //Log.e("ForceCloseIntentService.onHandleIntent", "profileId="+profileId);

        if (profileId != 0) {
            ForceCloseIntentService.profileIdList.add(profileId);
            ++ForceCloseIntentService.forceStopApplicationsStartCount;
        }

        String applications = intent.getStringExtra(PPPEApplication.EXTRA_APPLICATIONS);
        //Log.e("ForceCloseIntentService.onHandleIntent", "applications="+applications);

        if (!(applications.isEmpty() || (applications.equals("-")))) {

            PPPEApplication.forceStopStarted = true;
            //Log.e("ForceCloseIntentService.onHandleIntent", "forceStopStarted=true");

            startForceStopActivity();

            String[] splits = applications.split(StringConstants.STR_SPLIT_REGEX);
            for (String split : splits) {
                if (PPPEApplication.screenOffReceived)
                    break;

                Context appContext = getApplicationContext();

                boolean keyguardLocked = true;
                KeyguardManager kgMgr = (KeyguardManager) appContext.getSystemService(Context.KEYGUARD_SERVICE);
                if (kgMgr != null)
                    keyguardLocked = kgMgr.isKeyguardLocked();

                boolean isScreenOn = false;
                PowerManager pm = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                if (pm != null)
                    isScreenOn = PPPEApplicationStatic.isScreenOn(pm);

                if (!keyguardLocked && isScreenOn) {
                    // start App info only if keyguard is not locked and screen is on
                    String packageName = getPackageName(split);
                    if (isAppRunning(packageName)) {
                        Intent appInfoIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        //appInfoIntent.addCategory(Intent.CATEGORY_DEFAULT);
                        appInfoIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        appInfoIntent.setData(Uri.parse(PPPEApplication.INTENT_DATA_PACKAGE + packageName));
                        if (activityIntentExists(appInfoIntent, this)) {
                            //Log.e("ForceCloseIntentService.onHandleIntent", "activity intent exists");
                            startForceStopActivity();
                            if (ForceStopActivity.instance != null) {
                                try {
                                    PPPEApplication.applicationForceClosed = false;
                                    PPPEApplication.forceStopPerformed = false;
                                    //ForceStopActivity.instance.appInfoClosed = false;
                                    //noinspection deprecation
                                    ForceStopActivity.instance.startActivityForResult(appInfoIntent, 100);
                                    //Log.e("ForceCloseIntentService.onHandleIntent", "App info started");
                                    waitForApplicationForceClosed();
                                    //Log.e("ForceCloseIntentService.onHandleIntent", "after wait");
                                    //waitForAppInfoEnd();
                                    //ForceStopActivity.instance.finishActivity(100);
                                } catch (Exception e) {
                                    Log.e("ForceCloseIntentService.onHandleIntent", Log.getStackTraceString(e));
                                    PPPEApplicationStatic.recordException(e);
                                }
                            }
                        }
                    }
                }
            }

            --forceStopApplicationsStartCount;

            PPPEApplication.forceStopStarted = false;
            //Log.e("ForceCloseIntentService.onHandleIntent", "forceStopStarted=false");

        }

        //Log.e("ForceCloseIntentService.onHandleIntent", "forceStopApplicationsStartCount="+forceStopApplicationsStartCount);

        if (forceStopApplicationsStartCount <= 0) {
            if (ForceStopActivity.instance != null) {
                try {
                    ForceStopActivity.instance.finishAffinity();
                    //Log.e("ForceCloseIntentService.onHandleIntent", "ForceStopActivity finished");
                } catch (Exception ignored) {}
                ForceStopActivity.instance = null;
            }

            //Log.e("ForceCloseIntentService.onHandleIntent", "PPPEApplication.registeredForceStopApplicationsFunctionPP="+PPPEApplication.registeredForceStopApplicationsFunctionPP);

            if (PPPEApplication.registeredForceStopApplicationsFunctionPP ||
                    PPPEApplication.registeredForceStopApplicationsFunctionPPP) {

                //Log.e("ForceCloseIntentServiceo.nHandleIntent", "profileIdList.size()="+profileIdList.size());

                for (long _profileId : profileIdList) {
//                    PPPEApplication.logE("[BROADCAST_TO_PPP] ForceCloseIntentService.onHandleIntent", "xxxx");
                    Intent _intent = new Intent(PPPEAccessibilityService.ACTION_FORCE_STOP_APPLICATIONS_END);
                    _intent.putExtra(PPPEApplication.EXTRA_PROFILE_ID, _profileId);
                    sendBroadcast(_intent, PPPEApplication.ACCESSIBILITY_SERVICE_PERMISSION);
                }
            }
            profileIdList.clear();
            forceStopApplicationsStartCount = 0;
        }
    }

    private String getPackageName(String value) {
        if (value.length() > 2) {
            String packageName;
            String shortcut;
            String[] splits2 = value.split("/");
            if (splits2.length == 2) {
                shortcut = splits2[0].substring(0, 3);
                packageName = splits2[0];
            }
            else {
                shortcut = value.substring(0, 3);
                packageName = value;
            }
            if (shortcut.equals(StringConstants.SHORTCUT_ID)) {
                return packageName.substring(3);
            }
            if (shortcut.equals(StringConstants.INTENT_ID))
                return "";
            return packageName;
        }
        else
            return "";
    }

    private boolean activityIntentExists(Intent intent, Context context) {
        try {
            List<ResolveInfo> activities = context.getApplicationContext().getPackageManager().queryIntentActivities(intent, 0);
            return !activities.isEmpty();
        } catch (Exception e) {
            //Log.e("ForceCloseIntentService.activityIntentExists", Log.getStackTraceString(e));
            //PPPEApplication.recordException(e);
            return false;
        }
    }

    private boolean isAppRunning(final String packageName) {
        if (packageName.isEmpty())
            return false;
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo packageInfo : packages) {
            if (!isSTOPPED(packageInfo) && packageInfo.packageName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSTOPPED(ApplicationInfo pkgInfo) {
        return ((pkgInfo.flags & ApplicationInfo.FLAG_STOPPED) != 0);
    }

    private void waitForApplicationForceClosed()
    {
        long start = SystemClock.uptimeMillis();
        do {
//            Log.e("ForceCloseIntentService.waitForApplicationForceClosed", "ForceStopActivity.instance="+ForceStopActivity.instance);
//            Log.e("ForceCloseIntentService.waitForApplicationForceClosed", "PPPEApplication.applicationForceClosed="+PPPEApplication.applicationForceClosed);

            if ((ForceStopActivity.instance == null) || (PPPEApplication.applicationForceClosed))
                break;
            //try { Thread.sleep(100); } catch (InterruptedException e) { }
//            Log.e("ForceCloseIntentService.waitForApplicationForceClosed", "in wait");
            SystemClock.sleep(100);
        // TODO  30 seconds is only for testing, for get data to increase PPPE support !!! Comment for production version !!!
        } while ((SystemClock.uptimeMillis() - start) < 5000 /* *30*/);
    }

    /*
    private void waitForAppInfoEnd()
    {
        long start = SystemClock.uptimeMillis();
        do {
            if ((ForceStopActivity.instance == null) || (ForceStopActivity.instance.appInfoClosed))
                break;
            //try { Thread.sleep(100); } catch (InterruptedException e) { }
            SystemClock.sleep(100);
        } while (SystemClock.uptimeMillis() - start < 5000);
    }
    */

    // start activity from service restrictions
    // exceptions is also AccessibilityService
    //https://developer.android.com/guide/components/activities/background-starts
    // BUT NOT IN XIAOMI DEVICES WITH ANDROID 11 !!! WHY ???
    // !!! Must be enabled Apps/Manage apps/PPPE/Other permissions/Display pop-up windows while running in the background
    private void startForceStopActivity() {
//        Log.e("ForceCloseIntentService.startForceStopActivity", "ForceStopActivity.instance="+ForceStopActivity.instance);
        if (ForceStopActivity.instance == null) {
            Intent forceStopActivityIntent = new Intent(PPPEAccessibilityService.instance, ForceStopActivity.class);
            forceStopActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //forceStopActivityIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            forceStopActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            PPPEAccessibilityService.instance.startActivity(forceStopActivityIntent);
            waitForceStopActivityStart();
        }
    }

    private void waitForceStopActivityStart()
    {
        long start = SystemClock.uptimeMillis();
        do {
            if (ForceStopActivity.instance != null)
                break;
            //try { Thread.sleep(100); } catch (InterruptedException e) { }
            SystemClock.sleep(100);
        } while (SystemClock.uptimeMillis() - start < 2000);
    }

}
