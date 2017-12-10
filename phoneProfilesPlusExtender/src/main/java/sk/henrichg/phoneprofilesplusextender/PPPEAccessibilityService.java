package sk.henrichg.phoneprofilesplusextender;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

import java.util.List;

public class PPPEAccessibilityService extends android.accessibilityservice.AccessibilityService {

    private static final String SERVICE_ID = "sk.henrichg.phoneprofilesplusextender/.PPPEAccessibilityService";

    private static final String ACTION_FOREGROUND_APPLICATION_CHANGED = "sk.henrichg.phoneprofilesplusextender.ACTION_FOREGROUND_APPLICATION_CHANGED";
    private static final String ACTION_ACCESSIBILITY_SERVICE_UNBIND = "sk.henrichg.phoneprofilesplusextender.ACTION_ACCESSIBILITY_SERVICE_UNBIND";
    private static final String ACCESSIBILITY_SERVICE_PERMISSION = "sk.henrichg.phoneprofilesplusextender.ACCESSIBILITY_SERVICE_PERMISSION";

    private static final String EXTRA_PACKAGE_NAME = "sk.henrichg.phoneprofilesplus.package_name";
    private static final String EXTRA_CLASS_NAME = "sk.henrichg.phoneprofilesplus.class_name";

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        //Configure these here for compatibility with API 13 and below.
        AccessibilityServiceInfo config = new AccessibilityServiceInfo();
        config.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        config.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

        //Just in case this helps
        config.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;

        setServiceInfo(config);
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        //final Context context = getApplicationContext();
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

            try {
                ComponentName componentName = new ComponentName(
                        event.getPackageName().toString(),
                        event.getClassName().toString()
                );

                ActivityInfo activityInfo = tryGetActivity(componentName);
                boolean isActivity = activityInfo != null;
                if (isActivity) {
                    //Log.d("PPPEAccessibilityService", "currentActivity="+componentName.flattenToShortString());

                    Intent intent = new Intent(ACTION_FOREGROUND_APPLICATION_CHANGED);
                    intent.putExtra(EXTRA_PACKAGE_NAME, event.getPackageName().toString());
                    intent.putExtra(EXTRA_CLASS_NAME, event.getClassName().toString());
                    sendBroadcast(intent, ACCESSIBILITY_SERVICE_PERMISSION);
                }
            } catch (Exception e) {
                Log.e("PPPEAccessibilityService.onAccessibilityEvent", e.toString());
            }
        }
    }

    private ActivityInfo tryGetActivity(ComponentName componentName) {
        try {
            return getPackageManager().getActivityInfo(componentName, 0);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    public boolean onUnbind(Intent intent) {
        //Log.d("PPPEAccessibilityService", "onUnbind");

        //final Context context = getApplicationContext();

        Intent _intent = new Intent(ACTION_ACCESSIBILITY_SERVICE_UNBIND);
        sendBroadcast(_intent, ACCESSIBILITY_SERVICE_PERMISSION);

        return super.onUnbind(intent);
    }

    @SuppressLint("LongLogTag")
    static boolean isEnabled(Context context) {
        AccessibilityManager manager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (manager != null) {
            List<AccessibilityServiceInfo> runningServices =
                    manager.getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK);

            for (AccessibilityServiceInfo service : runningServices) {
                Log.d("PPPEAccessibilityService.isAccessibilityServiceEnabled", "serviceId="+service.getId());
                if (SERVICE_ID.equals(service.getId())) {
                    Log.d("PPPEAccessibilityService.isAccessibilityServiceEnabled", "true");
                    return true;
                }
            }
            Log.d("PPPEAccessibilityService.isAccessibilityServiceEnabled", "false");
            return false;
        }
        Log.d("PPPEAccessibilityService.isAccessibilityServiceEnabled", "false");
        return false;
    }

}
