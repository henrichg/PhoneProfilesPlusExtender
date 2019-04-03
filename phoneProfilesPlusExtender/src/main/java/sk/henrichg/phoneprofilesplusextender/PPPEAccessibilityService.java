package sk.henrichg.phoneprofilesplusextender;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Telephony;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

public class PPPEAccessibilityService extends android.accessibilityservice.AccessibilityService {

    static PPPEAccessibilityService instance = null;

    private static final String SERVICE_ID = "sk.henrichg.phoneprofilesplusextender/.PPPEAccessibilityService";

    static final String ACCESSIBILITY_SERVICE_PERMISSION = "sk.henrichg.phoneprofilesplusextender.ACCESSIBILITY_SERVICE_PERMISSION";

    private static final String ACTION_ACCESSIBILITY_SERVICE_CONNECTED = "sk.henrichg.phoneprofilesplusextender.ACTION_ACCESSIBILITY_SERVICE_CONNECTED";
    private static final String ACTION_FOREGROUND_APPLICATION_CHANGED = "sk.henrichg.phoneprofilesplusextender.ACTION_FOREGROUND_APPLICATION_CHANGED";
    private static final String ACTION_ACCESSIBILITY_SERVICE_UNBIND = "sk.henrichg.phoneprofilesplusextender.ACTION_ACCESSIBILITY_SERVICE_UNBIND";

    private static final String EXTRA_PACKAGE_NAME = "sk.henrichg.phoneprofilesplusextender.package_name";
    private static final String EXTRA_CLASS_NAME = "sk.henrichg.phoneprofilesplusextender.class_name";

    static final String ACTION_FORCE_STOP_APPLICATIONS_START = "sk.henrichg.phoneprofilesplusextender.ACTION_FORCE_STOP_APPLICATIONS_START";
    static final String ACTION_FORCE_STOP_APPLICATIONS_END = "sk.henrichg.phoneprofilesplusextender.ACTION_FORCE_STOP_APPLICATIONS_END";
    static final String ACTION_LOCK_DEVICE = "sk.henrichg.phoneprofilesplusextender.ACTION_LOCK_DEVICE";

    private FromPhoneProfilesPlusBroadcastReceiver fromPhoneProfilesPlusBroadcastReceiver = null;
    private ScreenOnOffBroadcastReceiver screenOnOffReceiver = null;
    private SMSBroadcastReceiver smsBroadcastReceiver = null;
    private SMSBroadcastReceiver mmsBroadcastReceiver = null;
    private PhoneCallBroadcastReceiver phoneCallBroadcastReceiver = null;

    static boolean forceStopStarted = false;
    static boolean applicationForceClosed = false;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        PPPEApplication.logE("PPPEAccessibilityService.onServiceConnected", "xxx");

        instance = this;

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
        intentFilter.addAction(PPPEApplication.ACTION_REGISTER_PPPE_FUNCTION);
        intentFilter.addAction(ACTION_FORCE_STOP_APPLICATIONS_START);
        intentFilter.addAction(ACTION_LOCK_DEVICE);
        getBaseContext().registerReceiver(fromPhoneProfilesPlusBroadcastReceiver, intentFilter,
                            ACCESSIBILITY_SERVICE_PERMISSION, null);

        if (PPPEApplication.hasSystemFeature(getApplicationContext(), PackageManager.FEATURE_TELEPHONY)) {
            smsBroadcastReceiver = new SMSBroadcastReceiver();
            IntentFilter intentFilter21 = new IntentFilter();
            intentFilter21.addAction(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
            intentFilter21.setPriority(Integer.MAX_VALUE);
            getBaseContext().registerReceiver(smsBroadcastReceiver, intentFilter21);

            mmsBroadcastReceiver = new SMSBroadcastReceiver();
            IntentFilter intentFilter22;
            intentFilter22 = IntentFilter.create(Telephony.Sms.Intents.WAP_PUSH_RECEIVED_ACTION, "application/vnd.wap.mms-message");
            intentFilter22.setPriority(Integer.MAX_VALUE);
            getBaseContext().registerReceiver(mmsBroadcastReceiver, intentFilter22);

            phoneCallBroadcastReceiver = new PhoneCallBroadcastReceiver();
            IntentFilter intentFilter6 = new IntentFilter();
            // not needed for unlink volumes and event Call sensor
            intentFilter6.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
            intentFilter6.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
            getBaseContext().registerReceiver(phoneCallBroadcastReceiver, intentFilter6);
        }

        Intent refreshIntent = new Intent("RefreshGUIBroadcastReceiver");
        LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(refreshIntent);

        Intent sendIntent = new Intent(ACTION_ACCESSIBILITY_SERVICE_CONNECTED);
        sendBroadcast(sendIntent, PPPEAccessibilityService.ACCESSIBILITY_SERVICE_PERMISSION);

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
                    PPPEApplication.logE("PPPEAccessibilityService.onAccessibilityEvent", "currentActivity="+componentName.flattenToShortString());
                    if (PPPEApplication.registeredForegroundApplicationFunctionPPP) {
                        Intent intent = new Intent(ACTION_FOREGROUND_APPLICATION_CHANGED);
                        intent.putExtra(EXTRA_PACKAGE_NAME, event.getPackageName().toString());
                        intent.putExtra(EXTRA_CLASS_NAME, event.getClassName().toString());
                        sendBroadcast(intent, ACCESSIBILITY_SERVICE_PERMISSION);
                    }
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
                            if (Build.VERSION.SDK_INT <= 22)
                                list = nodeInfo.findAccessibilityNodeInfosByViewId("com.android.settings:id/left_button");
                            else
                                list = nodeInfo.findAccessibilityNodeInfosByViewId("com.android.settings:id/right_button");
                            for (AccessibilityNodeInfo node : list) {
                                if (node.isEnabled()) {
                                    //Log.e("PPPEAccessibilityService", "force close button clicked");
                                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                }
                                else {
                                    applicationForceClosed = true;
                                    /*if (ForceStopActivity.instance != null)
                                        ForceStopActivity.instance.finishActivity(100);
                                    else
                                        performGlobalAction(GLOBAL_ACTION_BACK);*/
                                }
                            }
                        } else if (event.getClassName().equals("android.app.AlertDialog")) {
                            //forceCloseButtonClicked = false;
                            list = nodeInfo.findAccessibilityNodeInfosByViewId("android:id/button1");
                            //Log.e("PPPEAccessibilityService", "android:id/button1 list.size()="+list.size());
                            for (final AccessibilityNodeInfo node : list) {
                                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                applicationForceClosed = true;
                                /*sleep(200);
                                if (ForceStopActivity.instance != null)
                                    ForceStopActivity.instance.finishActivity(100);
                                else
                                    performGlobalAction(GLOBAL_ACTION_BACK);*/
                            }
                        }
                    }
                }
                //////////////////

            } catch (Exception ignored) {
                //Log.e("PPPEAccessibilityService.onAccessibilityEvent", Log.getStackTraceString(e));
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
        instance = null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        //Log.d("PPPEAccessibilityService", "onUnbind");

        //final Context context = getApplicationContext();

        // for event sensors: Applications and Orientation
        Intent _intent = new Intent(ACTION_ACCESSIBILITY_SERVICE_UNBIND);
        sendBroadcast(_intent, ACCESSIBILITY_SERVICE_PERMISSION);

        // for event Call sensor
        Intent sendIntent = new Intent(PhoneCallBroadcastReceiver.ACTION_CALL_RECEIVED);
        //sendIntent.putExtra(PhoneCallBroadcastReceiver.EXTRA_SERVICE_PHONE_EVENT, servicePhoneEvent);
        sendIntent.putExtra(PhoneCallBroadcastReceiver.EXTRA_CALL_EVENT_TYPE, PhoneCallBroadcastReceiver.CALL_EVENT_SERVICE_UNBIND);
        sendIntent.putExtra(PhoneCallBroadcastReceiver.EXTRA_PHONE_NUMBER, "");
        sendIntent.putExtra(PhoneCallBroadcastReceiver.EXTRA_EVENT_TIME, 0);
        sendBroadcast(sendIntent, PPPEAccessibilityService.ACCESSIBILITY_SERVICE_PERMISSION);

        if (fromPhoneProfilesPlusBroadcastReceiver != null)
            getBaseContext().unregisterReceiver(fromPhoneProfilesPlusBroadcastReceiver);
        if (screenOnOffReceiver != null)
            getBaseContext().unregisterReceiver(screenOnOffReceiver);
        if (smsBroadcastReceiver != null)
            getBaseContext().unregisterReceiver(smsBroadcastReceiver);
        if (mmsBroadcastReceiver != null)
            getBaseContext().unregisterReceiver(mmsBroadcastReceiver);
        if (phoneCallBroadcastReceiver != null)
            getBaseContext().unregisterReceiver(phoneCallBroadcastReceiver);

        instance = null;

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

}
