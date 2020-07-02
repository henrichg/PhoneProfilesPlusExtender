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
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.List;

public class PPPEAccessibilityService extends android.accessibilityservice.AccessibilityService {

    static PPPEAccessibilityService instance = null;

    private static final String SERVICE_ID = "sk.henrichg.phoneprofilesplusextender/.PPPEAccessibilityService";

    static final String ACCESSIBILITY_SERVICE_PERMISSION = PPPEApplication.PACKAGE_NAME + ".ACCESSIBILITY_SERVICE_PERMISSION";

    private static final String ACTION_ACCESSIBILITY_SERVICE_CONNECTED = PPPEApplication.PACKAGE_NAME + ".ACTION_ACCESSIBILITY_SERVICE_CONNECTED";
    private static final String ACTION_FOREGROUND_APPLICATION_CHANGED = PPPEApplication.PACKAGE_NAME + ".ACTION_FOREGROUND_APPLICATION_CHANGED";
    private static final String ACTION_ACCESSIBILITY_SERVICE_UNBIND = PPPEApplication.PACKAGE_NAME + ".ACTION_ACCESSIBILITY_SERVICE_UNBIND";

    private static final String EXTRA_PACKAGE_NAME = PPPEApplication.PACKAGE_NAME + ".package_name";
    private static final String EXTRA_CLASS_NAME = PPPEApplication.PACKAGE_NAME + ".class_name";

    static final String ACTION_FORCE_STOP_APPLICATIONS_START = PPPEApplication.PACKAGE_NAME + ".ACTION_FORCE_STOP_APPLICATIONS_START";
    static final String ACTION_FORCE_STOP_APPLICATIONS_END = PPPEApplication.PACKAGE_NAME + ".ACTION_FORCE_STOP_APPLICATIONS_END";
    static final String ACTION_LOCK_DEVICE = PPPEApplication.PACKAGE_NAME + ".ACTION_LOCK_DEVICE";

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

        //PPPEApplication.logE("PPPEAccessibilityService.onServiceConnected", "xxx");

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

        Intent refreshIntent = new Intent(PPPEApplication.PACKAGE_NAME + ".RefreshGUIBroadcastReceiver");
        LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(refreshIntent);

        Intent sendIntent = new Intent(ACTION_ACCESSIBILITY_SERVICE_CONNECTED);
        sendBroadcast(sendIntent, PPPEAccessibilityService.ACCESSIBILITY_SERVICE_PERMISSION);

    }

    @SuppressLint({"LongLogTag", "SwitchIntDef"})
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        //final Context context = getApplicationContext();
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            //PPPEApplication.logE("PPPEAccessibilityService.onAccessibilityEvent", "AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED");

            if (event.getClassName() == null)
                return;

            // for foreground application change
            try {
                if (event.getPackageName() != null) {
                    ComponentName componentName = new ComponentName(
                            event.getPackageName().toString(),
                            event.getClassName().toString()
                    );

                    ActivityInfo activityInfo = tryGetActivity(componentName);
                    boolean isActivity = activityInfo != null;
                    if (isActivity) {
                        //PPPEApplication.logE("PPPEAccessibilityService.onAccessibilityEvent", "currentActivity=" + componentName.flattenToShortString());
                        if (PPPEApplication.registeredForegroundApplicationFunctionPPP) {
                            Intent intent = new Intent(ACTION_FOREGROUND_APPLICATION_CHANGED);
                            intent.putExtra(EXTRA_PACKAGE_NAME, event.getPackageName().toString());
                            intent.putExtra(EXTRA_CLASS_NAME, event.getClassName().toString());
                            sendBroadcast(intent, ACCESSIBILITY_SERVICE_PERMISSION);
                        }
                    }
                }
            } catch (Exception e) {
                // do not log this exception, package name or class name may be null
                // wor this is not possible to get component name
                Log.e("PPPEAccessibilityService.onAccessibilityEvent", Log.getStackTraceString(e));
                PPPEApplication.recordException(e);
            }
            //////////////////

            try {
                //PPPEApplication.logE("PPPEAccessibilityService.onAccessibilityEvent", "forceStopStarted="+forceStopStarted);
                //PPPEApplication.logE("PPPEAccessibilityService.onAccessibilityEvent", "event.getClassName()="+event.getClassName());
                if (forceStopStarted) {
                    //PPPEApplication.logE("PPPEAccessibilityService.onAccessibilityEvent", "in forceStopStarted");
                    // force stop is started in PPP
                    AccessibilityNodeInfo nodeInfo = event.getSource();
                    if (nodeInfo != null) {
                        List<AccessibilityNodeInfo> list;
                        if (event.getClassName().equals("com.android.settings.applications.InstalledAppDetailsTop")) {
                            PPPEApplication.logE("PPPEAccessibilityService.onAccessibilityEvent", "App info opened");
                            //forceCloseButtonClicked = false;
                            if (Build.VERSION.SDK_INT <= 22) {
                                list = nodeInfo.findAccessibilityNodeInfosByViewId("com.android.settings:id/left_button");
                                //PPPEApplication.logE("PPPEAccessibilityService.onAccessibilityEvent", "com.android.settings:id/left_button list="+list.size());
                            }
                            else
                            if (Build.VERSION.SDK_INT >= 29) {
                                if (PPPEApplication.deviceIsRealme)
                                    list = nodeInfo.findAccessibilityNodeInfosByViewId("com.android.settings:id/middle_button");
                                else
                                    list = nodeInfo.findAccessibilityNodeInfosByViewId("com.android.settings:id/button3");
                                //PPPEApplication.logE("PPPEAccessibilityService.onAccessibilityEvent", "com.android.settings:id/button3="+list.size());
                            }
                            else {
                                if (PPPEApplication.deviceIsOppo || PPPEApplication.deviceIsRealme) {
                                    list = nodeInfo.findAccessibilityNodeInfosByViewId("com.android.settings:id/left_button");
                                } else {
                                    list = nodeInfo.findAccessibilityNodeInfosByViewId("com.android.settings:id/right_button");
                                    //PPPEApplication.logE("PPPEAccessibilityService.onAccessibilityEvent", "com.android.settings:id/right_button="+list.size());
                                    if (list.size() == 0) {
                                        // Samsung Galaxy S10
                                        list = nodeInfo.findAccessibilityNodeInfosByViewId("com.android.settings:id/button2_negative");
                                        //PPPEApplication.logE("PPPEAccessibilityService.onAccessibilityEvent", "com.android.settings:id/button2_negative="+list.size());
                                    }
                                }
                            }
                            for (AccessibilityNodeInfo node : list) {
                                if (node.isEnabled()) {
                                    //PPPEApplication.logE("PPPEAccessibilityService.onAccessibilityEvent", "force close button clicked");
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
                        } else
                        if (event.getClassName().equals("android.app.AlertDialog") ||
                            event.getClassName().equals("androidx.appcompat.app.AlertDialog")) {
                            PPPEApplication.logE("PPPEAccessibilityService.onAccessibilityEvent", "Alert opened");
                            //forceCloseButtonClicked = false;
                            list = nodeInfo.findAccessibilityNodeInfosByViewId("android:id/button1");
                            //PPPEApplication.logE("PPPEAccessibilityService.onAccessibilityEvent", "android:id/button1 list.size()="+list.size());
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

            } catch (Exception e) {
                Log.e("PPPEAccessibilityService.onAccessibilityEvent", Log.getStackTraceString(e));
                PPPEApplication.recordException(e);
            }
        }

        if (PPPEApplication.logIntoFile) {
            PPPEApplication.logE("PPPEAccessibilityService.onAccessibilityEvent", "Build.VERSION.SDK_INT="+Build.VERSION.SDK_INT);
            PPPEApplication.logE("PPPEAccessibilityService.onAccessibilityEvent", "Build.BRAND="+Build.BRAND);
            PPPEApplication.logE("PPPEAccessibilityService.onAccessibilityEvent", "Build.MANUFACTURER="+Build.MANUFACTURER);
            PPPEApplication.logE("PPPEAccessibilityService.onAccessibilityEvent", "Build.FINGERPRINT="+Build.FINGERPRINT);

            PPPEApplication.logE("PPPEAccessibilityService.onAccessibilityEvent", "event.getClassName()="+event.getClassName());

            try {
                switch (event.getEventType()) {
                    //On Gesture events print out the entire view hierarchy!

                    case AccessibilityEvent.TYPE_GESTURE_DETECTION_START:
                        PPPEApplication.logE("PPPEAccessibilityService.onAccessibilityEvent", A11yNodeInfo.wrap(getRootInActiveWindow()).toViewHierarchy());

                    case AccessibilityEvent.TYPE_VIEW_CLICKED:
                        PPPEApplication.logE("PPPEAccessibilityService.onAccessibilityEvent", event.getSource().toString());

                    default: {
                        //The event has different types, for you, you want to look for "action clicked"
                        if (event.getSource() != null) {
                            PPPEApplication.logE("PPPEAccessibilityService.onAccessibilityEvent", A11yNodeInfo.wrap(event.getSource()).toViewHierarchy());
                        }
                    }
                }
            } catch (Exception ignored) {}
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
        sendBroadcast(_intent);//, ACCESSIBILITY_SERVICE_PERMISSION);

        // for event Call sensor
        Intent sendIntent = new Intent(PhoneCallBroadcastReceiver.ACTION_CALL_RECEIVED);
        //sendIntent.putExtra(PhoneCallBroadcastReceiver.EXTRA_SERVICE_PHONE_EVENT, servicePhoneEvent);
        sendIntent.putExtra(PhoneCallBroadcastReceiver.EXTRA_CALL_EVENT_TYPE, PhoneCallBroadcastReceiver.CALL_EVENT_SERVICE_UNBIND);
        sendIntent.putExtra(PhoneCallBroadcastReceiver.EXTRA_PHONE_NUMBER, "");
        sendIntent.putExtra(PhoneCallBroadcastReceiver.EXTRA_EVENT_TIME, 0);
        sendBroadcast(sendIntent);//, PPPEAccessibilityService.ACCESSIBILITY_SERVICE_PERMISSION);

        if (fromPhoneProfilesPlusBroadcastReceiver != null) {
            try {
                getBaseContext().unregisterReceiver(fromPhoneProfilesPlusBroadcastReceiver);
            } catch (Exception ignored) {}
        }
        if (screenOnOffReceiver != null) {
            try {
                getBaseContext().unregisterReceiver(screenOnOffReceiver);
            } catch (Exception ignored) {}
        }
        if (smsBroadcastReceiver != null) {
            try {
                getBaseContext().unregisterReceiver(smsBroadcastReceiver);
            } catch (Exception ignored) {}
        }
        if (mmsBroadcastReceiver != null) {
            try {
                getBaseContext().unregisterReceiver(mmsBroadcastReceiver);
            } catch (Exception ignored) {}
        }
        if (phoneCallBroadcastReceiver != null) {
            try {
                getBaseContext().unregisterReceiver(phoneCallBroadcastReceiver);
            } catch (Exception ignored) {}
        }

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
