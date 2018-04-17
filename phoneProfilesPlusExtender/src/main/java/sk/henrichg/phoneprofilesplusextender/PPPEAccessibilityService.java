package sk.henrichg.phoneprofilesplusextender;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

public class PPPEAccessibilityService extends android.accessibilityservice.AccessibilityService {

    private static final String SERVICE_ID = "sk.henrichg.phoneprofilesplusextender/.PPPEAccessibilityService";

    static final String ACCESSIBILITY_SERVICE_PERMISSION = "sk.henrichg.phoneprofilesplusextender.ACCESSIBILITY_SERVICE_PERMISSION";

    private static final String ACTION_FOREGROUND_APPLICATION_CHANGED = "sk.henrichg.phoneprofilesplusextender.ACTION_FOREGROUND_APPLICATION_CHANGED";
    private static final String ACTION_ACCESSIBILITY_SERVICE_UNBIND = "sk.henrichg.phoneprofilesplusextender.ACTION_ACCESSIBILITY_SERVICE_UNBIND";

    private static final String EXTRA_PACKAGE_NAME = "sk.henrichg.phoneprofilesplusextender.package_name";
    private static final String EXTRA_CLASS_NAME = "sk.henrichg.phoneprofilesplusextender.class_name";

    static final String ACTION_FORCE_STOP_START = "sk.henrichg.phoneprofilesplusextender.ACTION_FORCE_STOP_START";
    static final String ACTION_FORCE_STOP_END = "sk.henrichg.phoneprofilesplusextender.ACTION_FORCE_STOP_END";

    private FromPhoneProfilesPlusBroadcastReceiver fromPhoneProfilesPlusBroadcastReceiver = null;
    private ScreenOnOffBroadcastReceiver screenOnOffReceiver = null;

    static boolean forceStopStarted = false;
    //static boolean forceCloseButtonClicked = false;


    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        /*
        //Configure these here for compatibility with API 13 and below.
        AccessibilityServiceInfo config = new AccessibilityServiceInfo();
        config.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        config.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

        //Just in case this helps
        config.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS |
                        AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;

        setServiceInfo(config);
        */


        screenOnOffReceiver = new ScreenOnOffBroadcastReceiver();
        IntentFilter intentFilter5 = new IntentFilter();
        intentFilter5.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter5.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter5.addAction(Intent.ACTION_USER_PRESENT);
        getBaseContext().registerReceiver(screenOnOffReceiver, intentFilter5);

        fromPhoneProfilesPlusBroadcastReceiver = new FromPhoneProfilesPlusBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_FORCE_STOP_START);
        getBaseContext().registerReceiver(fromPhoneProfilesPlusBroadcastReceiver, intentFilter,
                            ACCESSIBILITY_SERVICE_PERMISSION, null);
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        //final Context context = getApplicationContext();
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

            try {
                // for foreground application change
                ComponentName componentName = new ComponentName(
                        event.getPackageName().toString(),
                        event.getClassName().toString()
                );

                ActivityInfo activityInfo = tryGetActivity(componentName);
                boolean isActivity = activityInfo != null;
                if (isActivity) {
                    //Log.e("PPPEAccessibilityService", "currentActivity="+componentName.flattenToShortString());

                    Intent intent = new Intent(ACTION_FOREGROUND_APPLICATION_CHANGED);
                    intent.putExtra(EXTRA_PACKAGE_NAME, event.getPackageName().toString());
                    intent.putExtra(EXTRA_CLASS_NAME, event.getClassName().toString());
                    sendBroadcast(intent, ACCESSIBILITY_SERVICE_PERMISSION);
                }
                //////////////////

                //Log.e("PPPEAccessibilityService", "forceStopStarted="+forceStopStarted);
                //Log.e("PPPEAccessibilityService", "event.getClassName()="+event.getClassName());
                if (forceStopStarted) {
                    //Log.e("PPPEAccessibilityService", "forceStopStarted");
                    // force stop is started in PPP
                    AccessibilityNodeInfo nodeInfo = event.getSource();
                    if (nodeInfo != null) {
                        List<AccessibilityNodeInfo> list;
                        if (event.getClassName().equals("com.android.settings.applications.InstalledAppDetailsTop")) {
                            //forceCloseButtonClicked = false;
                            list = nodeInfo.findAccessibilityNodeInfosByViewId("com.android.settings:id/right_button");
                            for (AccessibilityNodeInfo node : list) {
                                if (node.isEnabled()) {
                                    //Log.e("PPPEAccessibilityService", "forceCloseButtonClicked");
                                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                    //forceCloseButtonClicked = true;
                                }
                                else
                                    performGlobalAction(GLOBAL_ACTION_BACK);
                            }
                        } else if (event.getClassName().equals("android.app.AlertDialog")) {
                            //forceCloseButtonClicked = false;
                            list = nodeInfo.findAccessibilityNodeInfosByViewId("android:id/button1");
                            //Log.e("PPPEAccessibilityService", "android:id/button1 list.size()="+list.size());
                            for (final AccessibilityNodeInfo node : list) {
                                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                sleep(100);
                                performGlobalAction(GLOBAL_ACTION_BACK);
                            }
                        }
                    }
                }
                //////////////////

            } catch (Exception e) {
                Log.e("PPPEAccessibilityService.onAccessibilityEvent", Log.getStackTraceString(e));
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

        getBaseContext().unregisterReceiver(fromPhoneProfilesPlusBroadcastReceiver);
        getBaseContext().unregisterReceiver(screenOnOffReceiver);

        return super.onUnbind(intent);
    }

    @SuppressLint("LongLogTag")
    static boolean isEnabled(Context context) {
        AccessibilityManager manager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (manager != null) {
            List<AccessibilityServiceInfo> runningServices =
                    manager.getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK);

            for (AccessibilityServiceInfo service : runningServices) {
                if (service != null) {
                    //Log.d("PPPEAccessibilityService.isAccessibilityServiceEnabled", "serviceId=" + service.getId());
                    if (SERVICE_ID.equals(service.getId())) {
                        //Log.d("PPPEAccessibilityService.isAccessibilityServiceEnabled", "true");
                        return true;
                    }
                }
            }
            //Log.d("PPPEAccessibilityService.isAccessibilityServiceEnabled", "false");
            return false;
        }
        //Log.d("PPPEAccessibilityService.isAccessibilityServiceEnabled", "false");
        return false;
    }

    static public void sleep(long ms) {
        /*long start = SystemClock.uptimeMillis();
        do {
            SystemClock.sleep(100);
        } while (SystemClock.uptimeMillis() - start < ms);*/
        //SystemClock.sleep(ms);
        try{ Thread.sleep(ms); }catch(InterruptedException ignored){ }
    }

}
