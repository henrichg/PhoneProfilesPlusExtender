package sk.henrichg.phoneprofilesplusextender;

import android.app.IntentService;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import java.util.List;

public class ForceCloseIntentService extends IntentService {

    static final String EXTRA_APPLICATIONS = "extra_applications";

    static boolean screenOffReceived = false;

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

        String applications = intent.getStringExtra(EXTRA_APPLICATIONS);

        if (!(applications.isEmpty() || (applications.equals("-")))) {

            PPPEAccessibilityService.forceStopStarted = true;
            //Log.e("ForceCloseIntentService", "forceStopStarted=true");

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
                    isScreenOn = pm.isScreenOn();

                if (!keyguardLocked && isScreenOn) {
                    // start App info only if keyguard is not locked and screen is on
                    String packageName = getPackageName(split);
                    intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    //intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setData(Uri.parse("package:" + packageName));
                    if (activityIntentExists(intent, this)) {
                        startActivity(intent);
                        PPPEAccessibilityService.sleep(3000);
                    }
                }
            }

            PPPEAccessibilityService.forceStopStarted = false;
            //Log.e("ForceCloseIntentService", "forceStopStarted=false");
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
            return false;
        }
    }
    
}