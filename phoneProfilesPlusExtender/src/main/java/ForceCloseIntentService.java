import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.Settings;

import java.util.List;

import sk.henrichg.phoneprofilesplusextender.PPPEAccessibilityService;

class ForceCloseIntentService extends IntentService {

    static final String EXTRA_APPLICATIONS = "extra_applications";
    
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
            
            String[] splits = applications.split("\\|");
            for (String split : splits) {
                String packageName = getPackageName(split);

                intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                //intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse("package:" + packageName));
                if (activityIntentExists(intent, this)) {
                    startActivity(intent);
                    PPPEAccessibilityService.sleep(500);
                }
            }

            PPPEAccessibilityService.sleep(5000);

            PPPEAccessibilityService.forceStopStarted = false;
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
