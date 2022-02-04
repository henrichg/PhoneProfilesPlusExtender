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
import android.telephony.PhoneStateListener;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.List;

@SuppressWarnings("SpellCheckingInspection")
public class PPPEAccessibilityService extends android.accessibilityservice.AccessibilityService {

    static PPPEAccessibilityService instance = null;

    //private static final String SERVICE_ID = "sk.henrichg.phoneprofilesplusextender/.PPPEAccessibilityService";

    static final String ACTION_ACCESSIBILITY_SERVICE_CONNECTED = PPPEApplication.PACKAGE_NAME + ".ACTION_ACCESSIBILITY_SERVICE_CONNECTED";
    private static final String ACTION_ACCESSIBILITY_SERVICE_UNBIND = PPPEApplication.PACKAGE_NAME + ".ACTION_ACCESSIBILITY_SERVICE_UNBIND";

    private static final String ACTION_FOREGROUND_APPLICATION_CHANGED = PPPEApplication.PACKAGE_NAME + ".ACTION_FOREGROUND_APPLICATION_CHANGED";

    private static final String EXTRA_PACKAGE_NAME = PPPEApplication.PACKAGE_NAME + ".package_name";
    private static final String EXTRA_CLASS_NAME = PPPEApplication.PACKAGE_NAME + ".class_name";

    static final String ACTION_FORCE_STOP_APPLICATIONS_START = PPPEApplication.PACKAGE_NAME + ".ACTION_FORCE_STOP_APPLICATIONS_START";
    static final String ACTION_FORCE_STOP_APPLICATIONS_END = PPPEApplication.PACKAGE_NAME + ".ACTION_FORCE_STOP_APPLICATIONS_END";
    static final String ACTION_LOCK_DEVICE = PPPEApplication.PACKAGE_NAME + ".ACTION_LOCK_DEVICE";

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        PPPEApplication.logE("PPPEAccessibilityService.onServiceConnected", "[START]");

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

        if (PPPEApplication.screenOnOffReceiver == null) {
            PPPEApplication.screenOnOffReceiver = new ScreenOnOffBroadcastReceiver();
            IntentFilter intentFilter5 = new IntentFilter();
            intentFilter5.addAction(Intent.ACTION_SCREEN_ON);
            intentFilter5.addAction(Intent.ACTION_SCREEN_OFF);
            intentFilter5.addAction(Intent.ACTION_USER_PRESENT);
            getBaseContext().registerReceiver(PPPEApplication.screenOnOffReceiver, intentFilter5);
        }

        if (PPPEApplication.fromPhoneProfilesPlusBroadcastReceiver == null) {
            PPPEApplication.fromPhoneProfilesPlusBroadcastReceiver = new FromPhoneProfilesPlusBroadcastReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(PPPEApplication.ACTION_ACCESSIBILITY_SERVICE_IS_CONNECTED);
            intentFilter.addAction(PPPEApplication.ACTION_REGISTER_PPPE_FUNCTION);
            intentFilter.addAction(ACTION_FORCE_STOP_APPLICATIONS_START);
            intentFilter.addAction(ACTION_LOCK_DEVICE);
            getBaseContext().registerReceiver(PPPEApplication.fromPhoneProfilesPlusBroadcastReceiver, intentFilter,
                    PPPEApplication.ACCESSIBILITY_SERVICE_PERMISSION, null);
        }

        if (PPPEApplication.hasSystemFeature(getApplicationContext(), PackageManager.FEATURE_TELEPHONY)) {
            if (PPPEApplication.smsBroadcastReceiver == null) {
                //PPPEApplication.logE("PPPEAccessibilityService.onServiceConnected", "sms receiver");
                PPPEApplication.smsBroadcastReceiver = new SMSBroadcastReceiver();
                IntentFilter intentFilter21 = new IntentFilter();
                intentFilter21.addAction(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
                intentFilter21.setPriority(Integer.MAX_VALUE);
                getBaseContext().registerReceiver(PPPEApplication.smsBroadcastReceiver, intentFilter21);
            }

            if (PPPEApplication.mmsBroadcastReceiver == null) {
                //PPPEApplication.logE("PPPEAccessibilityService.onServiceConnected", "mms receiver");
                PPPEApplication.mmsBroadcastReceiver = new SMSBroadcastReceiver();
                IntentFilter intentFilter22;
                intentFilter22 = IntentFilter.create(Telephony.Sms.Intents.WAP_PUSH_RECEIVED_ACTION, "application/vnd.wap.mms-message");
                intentFilter22.setPriority(Integer.MAX_VALUE);
                getBaseContext().registerReceiver(PPPEApplication.mmsBroadcastReceiver, intentFilter22);
            }

            registerPhoneStateListener(true, getBaseContext());

            if (PPPEApplication.phoneCallReceiver == null) {
                PPPEApplication.phoneCallReceiver = new PhoneCallReceiver();
                IntentFilter intentFilter6 = new IntentFilter();
                // not needed for unlink volumes and event Call sensor
                intentFilter6.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
                getBaseContext().registerReceiver(PPPEApplication.phoneCallReceiver, intentFilter6);
            }

            if (PPPEApplication.simStateChangedBroadcastReceiver == null) {
                PPPEApplication.simStateChangedBroadcastReceiver = new SimStateChangedBroadcastReceiver();
                IntentFilter intentFilter10 = new IntentFilter();
                //noinspection deprecation
                intentFilter10.addAction("android.intent.action.SIM_STATE_CHANGED");
                getBaseContext().registerReceiver(PPPEApplication.simStateChangedBroadcastReceiver, intentFilter10);
            }

        }

        Intent refreshIntent = new Intent(PPPEApplication.PACKAGE_NAME + ".RefreshGUIBroadcastReceiver");
        LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(refreshIntent);

        Intent sendIntent = new Intent(ACTION_ACCESSIBILITY_SERVICE_CONNECTED);
        sendBroadcast(sendIntent, PPPEApplication.ACCESSIBILITY_SERVICE_PERMISSION);

        PPPEApplication.logE("PPPEAccessibilityService.onServiceConnected", "[END]");

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
                            sendBroadcast(intent, PPPEApplication.ACCESSIBILITY_SERVICE_PERMISSION);
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
                if (PPPEApplication.forceStopStarted) {
                    //PPPEApplication.logE("PPPEAccessibilityService.onAccessibilityEvent", "in forceStopStarted");
                    // force stop is started in PPP
                    AccessibilityNodeInfo nodeInfo;
                    try {
                        nodeInfo = event.getSource();
                    } catch (Exception e) {
                        nodeInfo = null;
                    }
                    if (nodeInfo != null) {
                        List<AccessibilityNodeInfo> list;
                        if (event.getClassName().equals("com.android.settings.applications.InstalledAppDetailsTop")) {
                            //PPPEApplication.logE("PPPEAccessibilityService.onAccessibilityEvent", "App info opened");
                            //forceCloseButtonClicked = false;
                            //if (Build.VERSION.SDK_INT <= 22) {
                            //    list = nodeInfo.findAccessibilityNodeInfosByViewId("com.android.settings:id/left_button");
                            //    PPPEApplication.logE("PPPEAccessibilityService.onAccessibilityEvent", "com.android.settings:id/left_button list="+list.size());
                            //}
                            //else
                            if (Build.VERSION.SDK_INT >= 29) {
                                if (PPPEApplication.deviceIsOppo || PPPEApplication.deviceIsRealme)
                                    list = nodeInfo.findAccessibilityNodeInfosByViewId("com.android.settings:id/middle_button");
                                else
                                if (PPPEApplication.deviceIsHuawei)
                                    list = nodeInfo.findAccessibilityNodeInfosByViewId("com.android.settings:id/right_button");
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
                                    PPPEApplication.forceStopPerformed = true;
                                }
                                else {
                                    PPPEApplication.applicationForceClosed = true;
                                    PPPEApplication.forceStopPerformed = false;
                                    /*if (ForceStopActivity.instance != null)
                                        ForceStopActivity.instance.finishActivity(100);
                                    else
                                        performGlobalAction(GLOBAL_ACTION_BACK);*/
                                }
                            }
                        } else
                        if (PPPEApplication.forceStopPerformed// ||
                            //event.getClassName().equals("android.app.AlertDialog") ||
                            //event.getClassName().equals("androidx.appcompat.app.AlertDialog")
                            ) {
                            //PPPEApplication.logE("PPPEAccessibilityService.onAccessibilityEvent", "Alert opened");
                            //forceCloseButtonClicked = false;
                            if ((PPPEApplication.deviceIsSamsung) && (Build.VERSION.SDK_INT >= 30))
                                list = nodeInfo.findAccessibilityNodeInfosByViewId("android:id/button1");
                            else
                            if ((PPPEApplication.deviceIsSamsung) && (Build.VERSION.SDK_INT >= 29))
                                list = nodeInfo.findAccessibilityNodeInfosByViewId("com.android.settings:id/button1");
                            else
                                list = nodeInfo.findAccessibilityNodeInfosByViewId("android:id/button1");
                            //PPPEApplication.logE("PPPEAccessibilityService.onAccessibilityEvent", "android:id/button1 list.size()="+list.size());
                            for (final AccessibilityNodeInfo node : list) {
                                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                PPPEApplication.applicationForceClosed = true;
                                PPPEApplication.forceStopPerformed = false;
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

/*        if (PPPEApplication.logIntoFile) {
            // TODO  this is only for testing, for increase support of devices !!! Comment for production version !!!
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
        }*/
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
        PPPEApplication.logE("PPPEAccessibilityService.onInterrupt", "xxx");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        //Log.d("PPPEAccessibilityService", "onUnbind");
        PPPEApplication.logE("PPPEAccessibilityService.onUnbind", "[START]");

        //final Context context = getApplicationContext();

        // for event sensors: Applications and Orientation
        Intent _intent = new Intent(ACTION_ACCESSIBILITY_SERVICE_UNBIND);
        sendBroadcast(_intent);//, ACCESSIBILITY_SERVICE_PERMISSION);

        // for event Call sensor
        Intent sendIntent = new Intent(PPPEPhoneStateListener.ACTION_CALL_RECEIVED);
        //sendIntent.putExtra(PPPEPhoneStateListener.EXTRA_SERVICE_PHONE_EVENT, servicePhoneEvent);
        sendIntent.putExtra(PPPEPhoneStateListener.EXTRA_CALL_EVENT_TYPE, PPPEPhoneStateListener.CALL_EVENT_SERVICE_UNBIND);
        sendIntent.putExtra(PPPEPhoneStateListener.EXTRA_PHONE_NUMBER, "");
        sendIntent.putExtra(PPPEPhoneStateListener.EXTRA_EVENT_TIME, 0);
        sendIntent.putExtra(PPPEPhoneStateListener.EXTRA_SIM_SLOT, 0);
        sendBroadcast(sendIntent);//, PPPEAccessibilityService.ACCESSIBILITY_SERVICE_PERMISSION);

        if (PPPEApplication.fromPhoneProfilesPlusBroadcastReceiver != null) {
            try {
                getBaseContext().unregisterReceiver(PPPEApplication.fromPhoneProfilesPlusBroadcastReceiver);
                PPPEApplication.fromPhoneProfilesPlusBroadcastReceiver = null;
            } catch (Exception e) {
                PPPEApplication.fromPhoneProfilesPlusBroadcastReceiver = null;
            }
        }
        if (PPPEApplication.screenOnOffReceiver != null) {
            try {
                getBaseContext().unregisterReceiver(PPPEApplication.screenOnOffReceiver);
                PPPEApplication.screenOnOffReceiver = null;
            } catch (Exception e) {
                PPPEApplication.screenOnOffReceiver = null;
            }
        }
        if (PPPEApplication.smsBroadcastReceiver != null) {
            try {
                getBaseContext().unregisterReceiver(PPPEApplication.smsBroadcastReceiver);
                PPPEApplication.smsBroadcastReceiver = null;
            } catch (Exception e) {
                PPPEApplication.smsBroadcastReceiver = null;
            }
        }
        if (PPPEApplication.mmsBroadcastReceiver != null) {
            try {
                getBaseContext().unregisterReceiver(PPPEApplication.mmsBroadcastReceiver);
                PPPEApplication.mmsBroadcastReceiver = null;
            } catch (Exception e) {
                PPPEApplication.mmsBroadcastReceiver = null;
            }
        }
        registerPhoneStateListener(false, this);
        if (PPPEApplication.phoneCallReceiver != null) {
            try {
                getBaseContext().unregisterReceiver(PPPEApplication.phoneCallReceiver);
                PPPEApplication.phoneCallReceiver = null;
            } catch (Exception e) {
                PPPEApplication.phoneCallReceiver = null;
            }
        }
        if (PPPEApplication.simStateChangedBroadcastReceiver != null) {
            try {
                getBaseContext().unregisterReceiver(PPPEApplication.simStateChangedBroadcastReceiver);
                PPPEApplication.simStateChangedBroadcastReceiver = null;
            } catch (Exception e) {
                PPPEApplication.simStateChangedBroadcastReceiver = null;
            }
        }

        instance = null;

        PPPEApplication.logE("PPPEAccessibilityService.onUnbind", "[END]");

        return super.onUnbind(intent);
    }

    static void registerPhoneStateListener(boolean register, Context context) {
        if (!register) {
            if (PPPEApplication.phoneStateListenerSIM1 != null) {
                try {
                    if (PPPEApplication.telephonyManagerSIM1 != null)
                        PPPEApplication.telephonyManagerSIM1.listen(PPPEApplication.phoneStateListenerSIM1, PhoneStateListener.LISTEN_NONE);
                    PPPEApplication.phoneStateListenerSIM1 = null;
                    PPPEApplication.telephonyManagerSIM1 = null;
                } catch (Exception ignored) {
                }
            }
            if (PPPEApplication.phoneStateListenerSIM2 != null) {
                try {
                    if (PPPEApplication.telephonyManagerSIM2 != null)
                        PPPEApplication.telephonyManagerSIM2.listen(PPPEApplication.phoneStateListenerSIM2, PhoneStateListener.LISTEN_NONE);
                    PPPEApplication.phoneStateListenerSIM2 = null;
                    PPPEApplication.telephonyManagerSIM2 = null;
                } catch (Exception ignored) {
                }
            }
            if (PPPEApplication.phoneStateListenerDefaul != null) {
                try {
                    if (PPPEApplication.telephonyManagerDefault != null)
                        PPPEApplication.telephonyManagerDefault.listen(PPPEApplication.phoneStateListenerDefaul, PhoneStateListener.LISTEN_NONE);
                    PPPEApplication.phoneStateListenerDefaul = null;
                    PPPEApplication.telephonyManagerDefault = null;
                } catch (Exception ignored) {
                }
            }
        }
        else {
            PPPEApplication.telephonyManagerDefault = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
            if (PPPEApplication.telephonyManagerDefault != null) {
                int simCount = PPPEApplication.telephonyManagerDefault.getSimCount();
                if (simCount > 1) {
                    SubscriptionManager mSubscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                    //SubscriptionManager.from(appContext);
                    if (mSubscriptionManager != null) {
//                        PPPEApplication.logE("PhoneProfilesService.registerAllTheTimeRequiredSystemReceivers", "mSubscriptionManager != null");
                        List<SubscriptionInfo> subscriptionList = null;
                        try {
                            // Loop through the subscription list i.e. SIM list.
                            subscriptionList = mSubscriptionManager.getActiveSubscriptionInfoList();
//                            PPPEApplication.logE("PhoneProfilesService.registerAllTheTimeRequiredSystemReceivers", "subscriptionList=" + subscriptionList);
                        } catch (SecurityException e) {
                            //PPApplication.recordException(e);
                        }
                        if (subscriptionList != null) {
//                            PPPEApplication.logE("PhoneProfilesService.registerAllTheTimeRequiredSystemReceivers", "subscriptionList.size()=" + subscriptionList.size());
                            for (int i = 0; i < subscriptionList.size(); i++) {
                                // Get the active subscription ID for a given SIM card.
                                SubscriptionInfo subscriptionInfo = subscriptionList.get(i);
//                                PPPEApplication.logE("PhoneProfilesService.registerAllTheTimeRequiredSystemReceivers", "subscriptionInfo=" + subscriptionInfo);
                                if (subscriptionInfo != null) {
                                    int subscriptionId = subscriptionInfo.getSubscriptionId();
                                    if (i == 0) {
                                        if (PPPEApplication.telephonyManagerSIM1 == null) {
//                                            PPPEApplication.logE("PhoneProfilesService.registerAllTheTimeRequiredSystemReceivers", "subscriptionId=" + subscriptionId);
                                            //noinspection ConstantConditions
                                            PPPEApplication.telephonyManagerSIM1 = PPPEApplication.telephonyManagerDefault.createForSubscriptionId(subscriptionId);
                                            PPPEApplication.phoneStateListenerSIM1 = new PPPEPhoneStateListener(subscriptionInfo, context);
                                            PPPEApplication.telephonyManagerSIM1.listen(PPPEApplication.phoneStateListenerSIM1, PhoneStateListener.LISTEN_CALL_STATE);
                                        }
                                    }
                                    if (i == 1) {
                                        if (PPPEApplication.telephonyManagerSIM2 == null) {
//                                            PPPEApplication.logE("PhoneProfilesService.registerAllTheTimeRequiredSystemReceivers", "subscriptionId=" + subscriptionId);
                                            //noinspection ConstantConditions
                                            PPPEApplication.telephonyManagerSIM2 = PPPEApplication.telephonyManagerDefault.createForSubscriptionId(subscriptionId);
                                            PPPEApplication.phoneStateListenerSIM2 = new PPPEPhoneStateListener(subscriptionInfo, context);
                                            PPPEApplication.telephonyManagerSIM2.listen(PPPEApplication.phoneStateListenerSIM2, PhoneStateListener.LISTEN_CALL_STATE);
                                        }
                                    }
                                }
//                                else
//                                    PPPEApplication.logE("PhoneProfilesService.registerAllTheTimeRequiredSystemReceivers", "subscriptionInfo == null");
                            }
                        }
//                        else
//                            PPPEApplication.logE("PhoneProfilesService.registerAllTheTimeRequiredSystemReceivers", "subscriptionList == null");
                    }
//                    else
//                        PPPEApplication.logE("PhoneProfilesService.registerAllTheTimeRequiredSystemReceivers", "mSubscriptionManager == null");
                }
                else {
                    PPPEApplication.phoneStateListenerDefaul = new PPPEPhoneStateListener(null, context);
                    PPPEApplication.telephonyManagerDefault.listen(PPPEApplication.phoneStateListenerDefaul, PhoneStateListener.LISTEN_CALL_STATE);
                }
            }
        }
    }

    @SuppressLint("LongLogTag")
    static boolean isEnabled(Context context) {
        AccessibilityManager manager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (manager != null) {
            List<AccessibilityServiceInfo> runningServices =
                    manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);

            for (AccessibilityServiceInfo service : runningServices) {
                if (service != null) {
                    //Log.d("PPPEAccessibilityService.isAccessibilityServiceEnabled", "serviceId=" + service.getId());
                    try {
                        if (service.getId().contains(PPPEApplication.PACKAGE_NAME)) {
                            //PPApplication.logE("PPPExtenderBroadcastReceiver.isAccessibilityServiceEnabled", "true");
                            return true;
                        }
/*
                        if (service.packageNames != null) {
                            for (String packageName : service.packageNames) {
                                if (PPApplication.EXTENDER_ACCESSIBILITY_PACKAGE_NAME.equals(packageName)) {
                                    //PPApplication.logE("PPPExtenderBroadcastReceiver.isAccessibilityServiceEnabled", "true");
                                    return true;
                                }
                            }
                        }
 */
                    } catch (Exception ignored) {}
                }
            }
            //Log.d("PPPEAccessibilityService.isAccessibilityServiceEnabled", "false");
            return false;
        }
        //Log.d("PPPEAccessibilityService.isAccessibilityServiceEnabled", "false");
        return false;
    }

}
