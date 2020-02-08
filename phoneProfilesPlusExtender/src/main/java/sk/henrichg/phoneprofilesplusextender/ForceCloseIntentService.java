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

import com.crashlytics.android.Crashlytics;

import java.util.List;

public class ForceCloseIntentService extends IntentService {

    static final String EXTRA_APPLICATIONS = "extra_applications";
    static final String EXTRA_PROFILE_ID = "profile_id";

    static boolean screenOffReceived = false;

    private static final List<Long> profileIdList = new java.util.ArrayList<>();
    private static int forceStopApplicationsStartCount = 0;


    public ForceCloseIntentService()
    {
        super("ForceCloseIntentService");

        // if enabled is true, onStartCommand(Intent, int, int) will return START_REDELIVER_INTENT,
        // so if this process dies before onHandleIntent(Intent) returns, the process will be restarted
        // and the intent redelivered. If multiple Intents have been sent, only the most recent one
        // is guaranteed to be redelivered.
        // -- but restarted service has intent == null??
        //setIntentRedelivery(true);
    }

    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        long profileId = intent.getLongExtra(ForceCloseIntentService.EXTRA_PROFILE_ID, 0);
        if (profileId != 0) {
            ForceCloseIntentService.profileIdList.add(profileId);
            ++ForceCloseIntentService.forceStopApplicationsStartCount;
        }

        String applications = intent.getStringExtra(EXTRA_APPLICATIONS);

        if (!(applications.isEmpty() || (applications.equals("-")))) {

            PPPEAccessibilityService.forceStopStarted = true;
            //Log.e("ForceCloseIntentService", "forceStopStarted=true");

            startForceStopActivity();

            String[] splits = applications.split("\\|");
            for (String split : splits) {
                if (screenOffReceived)
                    break;

                boolean keyguardLocked = true;
                KeyguardManager kgMgr = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
                if (kgMgr != null)
                    keyguardLocked = kgMgr.isKeyguardLocked();

                boolean isScreenOn = false;
                PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
                if (pm != null)
                    isScreenOn = PPPEApplication.isScreenOn(pm);

                if (!keyguardLocked && isScreenOn) {
                    // start App info only if keyguard is not locked and screen is on
                    String packageName = getPackageName(split);
                    if (isAppRunning(packageName)) {
                        Intent appInfoIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        //appInfoIntent.addCategory(Intent.CATEGORY_DEFAULT);
                        appInfoIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        appInfoIntent.setData(Uri.parse("package:" + packageName));
                        if (activityIntentExists(appInfoIntent, this)) {
                            startForceStopActivity();
                            if (ForceStopActivity.instance != null) {
                                try {
                                    PPPEAccessibilityService.applicationForceClosed = false;
                                    //ForceStopActivity.instance.appInfoClosed = false;
                                    ForceStopActivity.instance.startActivityForResult(appInfoIntent, 100);
                                    waitForApplicationForceClosed();
                                    //waitForAppInfoEnd();
                                    //ForceStopActivity.instance.finishActivity(100);
                                } catch (Exception e) {
                                    Log.e("ForceCloseIntentService.onHandleIntent", Log.getStackTraceString(e));
                                    Crashlytics.logException(e);
                                }
                            }
                        }
                    }
                }
            }

            --forceStopApplicationsStartCount;

            PPPEAccessibilityService.forceStopStarted = false;
            //Log.e("ForceCloseIntentService", "forceStopStarted=false");
        }

        if (forceStopApplicationsStartCount <= 0) {
            if (ForceStopActivity.instance != null) {
                try {
                    ForceStopActivity.instance.finish();
                } catch (Exception ignored) {}
                ForceStopActivity.instance = null;
            }

            if (PPPEApplication.registeredForceStopApplicationsFunctionPP ||
                    PPPEApplication.registeredForceStopApplicationsFunctionPPP) {
                for (long _profileId : profileIdList) {
                    Intent _intent = new Intent(PPPEAccessibilityService.ACTION_FORCE_STOP_APPLICATIONS_END);
                    _intent.putExtra(EXTRA_PROFILE_ID, _profileId);
                    sendBroadcast(_intent, PPPEAccessibilityService.ACCESSIBILITY_SERVICE_PERMISSION);
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
            if (shortcut.equals("(s)")) {
                return packageName.substring(3);
            }
            if (shortcut.equals("(i)"))
                return "";
            return packageName;
        }
        else
            return "";
    }

    private boolean activityIntentExists(Intent intent, Context context) {
        try {
            List<ResolveInfo> activities = context.getApplicationContext().getPackageManager().queryIntentActivities(intent, 0);
            return activities.size() > 0;
        } catch (Exception e) {
            //Log.e("ForceCloseIntentService.activityIntentExists", Log.getStackTraceString(e));
            //Crashlytics.logException(e);
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

    private static boolean isSTOPPED(ApplicationInfo pkgInfo) {
        return ((pkgInfo.flags & ApplicationInfo.FLAG_STOPPED) != 0);
    }

    private void waitForApplicationForceClosed()
    {
        long start = SystemClock.uptimeMillis();
        do {
            if ((ForceStopActivity.instance == null) || (PPPEAccessibilityService.applicationForceClosed))
                break;
            //try { Thread.sleep(100); } catch (InterruptedException e) { }
            SystemClock.sleep(100);
        } while (SystemClock.uptimeMillis() - start < 5000);
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

    private void startForceStopActivity() {
        if (ForceStopActivity.instance == null) {
            Intent forceStopActivityIntent = new Intent(this, ForceStopActivity.class);
            forceStopActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //forceStopActivityIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            forceStopActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(forceStopActivityIntent);
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
