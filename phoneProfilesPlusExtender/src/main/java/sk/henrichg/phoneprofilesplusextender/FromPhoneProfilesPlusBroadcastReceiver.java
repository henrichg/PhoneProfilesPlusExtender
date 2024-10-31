package sk.henrichg.phoneprofilesplusextender;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

class FromPhoneProfilesPlusBroadcastReceiver extends BroadcastReceiver {

    @SuppressLint({"LongLogTag", "InlinedApi"})
    @Override
    public void onReceive(Context context, Intent intent) {
//        PPPEApplicationStatic.logE("[MEMORY_LEAK] FromPhoneProfilesPlusBroadcastReceiver.onReceive", "xxxxxxxxxxx");

        if ((intent == null) || (intent.getAction() == null))
            return;

//        PPPEApplication.logE("FromPhoneProfilesPlusBroadcastReceiver.onReceive", "received broadcast action="+intent.getAction());

        final Context appContext = context.getApplicationContext();

        String action = intent.getAction();
//        PPPEApplicationStatic.logE("[MEMORY_LEAK] FromPhoneProfilesPlusBroadcastReceiver.onReceive", "action="+action);
        if (action.equals(PPPEApplication.ACTION_REGISTER_PPPE_FUNCTION)) {
            String registrationApplication = intent.getStringExtra(PPPEApplication.EXTRA_REGISTRATION_APP);
            int registrationType = intent.getIntExtra(PPPEApplication.EXTRA_REGISTRATION_TYPE, 0);

//            PPPEApplicationStatic.logE("[MEMORY_LEAK] FromPhoneProfilesPlusBroadcastReceiver.onReceive", "registrationApplication="+registrationApplication);
//            PPPEApplicationStatic.logE("[MEMORY_LEAK] FromPhoneProfilesPlusBroadcastReceiver.onReceive", "registrationType="+registrationType);

            if ((registrationApplication != null) && registrationApplication.equals(StringConstants.PHONE_PROFILES)) {
                switch (registrationType) {
                    case PPPEApplication.REGISTRATION_TYPE_FORCE_STOP_APPLICATIONS_REGISTER:
                        PPPEApplication.registeredForceStopApplicationsFunctionPP = true;
                        break;
                    case PPPEApplication.REGISTRATION_TYPE_FORCE_STOP_APPLICATIONS_UNREGISTER:
                        PPPEApplication.registeredForceStopApplicationsFunctionPP = false;
                        break;
                    case PPPEApplication.REGISTRATION_TYPE_LOCK_DEVICE_REGISTER:
                        PPPEApplication.registeredLockDeviceFunctionPP = true;
                        break;
                    case PPPEApplication.REGISTRATION_TYPE_LOCK_DEVICE_UNREGISTER:
                        PPPEApplication.registeredLockDeviceFunctionPP = false;
                        break;
                }
            }
            if ((registrationApplication != null) && registrationApplication.equals(StringConstants.PHONE_PROFILES_PLUS)) {
                switch (registrationType) {
                    case PPPEApplication.REGISTRATION_TYPE_FORCE_STOP_APPLICATIONS_REGISTER:
                        PPPEApplication.registeredForceStopApplicationsFunctionPPP = true;
                        break;
                    case PPPEApplication.REGISTRATION_TYPE_FORCE_STOP_APPLICATIONS_UNREGISTER:
                        PPPEApplication.registeredForceStopApplicationsFunctionPPP = false;
                        break;
                    case PPPEApplication.REGISTRATION_TYPE_FOREGROUND_APPLICATION_REGISTER:
                        PPPEApplication.registeredForegroundApplicationFunctionPPP = true;
                        break;
                    case PPPEApplication.REGISTRATION_TYPE_FOREGROUND_APPLICATION_UNREGISTER:
                        PPPEApplication.registeredForegroundApplicationFunctionPPP = false;
                        break;
                    case PPPEApplication.REGISTRATION_TYPE_SMS_REGISTER:
                        PPPEApplication.registeredSMSFunctionPPP = true;
                        if (!Permissions.checkSMSMMSPermissions(appContext)) {
                            showPermissionNotification(appContext,
                                    context.getString(R.string.extender_notification_permission_title),
                                    context.getString(R.string.extender_notification_sms_mms_permission_text),
                                    PPPEApplication.GRANT_PERMISSIONS_SMS_NOTIFICATION_ID,
                                    PPPEApplication.GRANT_PERMISSIONS_SMS_NOTIFICATION_TAG,
                                    R.id.activity_main_permissions_event_sensor_sms_mms);
                        }
                        break;
                    case PPPEApplication.REGISTRATION_TYPE_SMS_UNREGISTER:
                        PPPEApplication.registeredSMSFunctionPPP = false;
                        break;
                    case PPPEApplication.REGISTRATION_TYPE_CALL_REGISTER:
                        PPPEApplication.registeredCallFunctionPPP = true;
                        if (!Permissions.checkCallPermissions(appContext)) {
                            showPermissionNotification(appContext,
                                    context.getString(R.string.extender_notification_permission_title),
                                    context.getString(R.string.extender_notification_call_permission_text),
                                    PPPEApplication.GRANT_PERMISSIONS_CALL_NOTIFICATION_ID,
                                    PPPEApplication.GRANT_PERMISSIONS_CALL_NOTIFICATION_TAG,
                                    R.id.activity_main_permissions_event_sensor_call);
                        }
                        break;
                    case PPPEApplication.REGISTRATION_TYPE_CALL_UNREGISTER:
                        PPPEApplication.registeredCallFunctionPPP = false;
                        break;
                    case PPPEApplication.REGISTRATION_TYPE_LOCK_DEVICE_REGISTER:
                        PPPEApplication.registeredLockDeviceFunctionPPP = true;
                        break;
                    case PPPEApplication.REGISTRATION_TYPE_LOCK_DEVICE_UNREGISTER:
                        PPPEApplication.registeredLockDeviceFunctionPPP = false;
                        break;
                }
            }
            //PPPEApplication.logE("FromPhoneProfilesPlusBroadcastReceiver.onReceive", "PPPEApplication.registeredCallFunctionPPP="+PPPEApplication.registeredCallFunctionPPP);
        }
        //else
        //if (action.equals(PPPEApplication.ACTION_ACCESSIBILITY_SERVICE_IS_CONNECTED)) {
            // send answer to PPP
//            PPPEApplication.logE("FromPhoneProfilesPlusBroadcastReceiver.onReceive", "PPPEApplication.ACTION_PPPEXTENDER_IS_RUNNING");
        //    Intent sendIntent = new Intent(PPPEAccessibilityService.ACTION_ACCESSIBILITY_SERVICE_CONNECTED);
        //    context.sendBroadcast(sendIntent, PPPEApplication.ACCESSIBILITY_SERVICE_PERMISSION);
        //}
        else
        if (action.equals(PPPEAccessibilityService.ACTION_FORCE_STOP_APPLICATIONS_START)) {
            if (!intent.getBooleanExtra(PPPEApplication.EXTRA_BLOCK_PROFILE_EVENT_ACTION, false)) {
                long profileId = intent.getLongExtra(PPPEApplication.EXTRA_PROFILE_ID, 0);
//                PPPEApplication.logE("FromPhoneProfilesPlusBroadcastReceiver.onReceive", "profileId="+profileId);
//                PPPEApplication.logE("FromPhoneProfilesPlusBroadcastReceiver.onReceive", "applications="+intent.getStringExtra(ForceCloseIntentService.EXTRA_APPLICATIONS));

                if (PPPEAccessibilityService.instance != null) {
                    Intent scanServiceIntent = new Intent(appContext, ForceCloseIntentService.class);
                    scanServiceIntent.putExtra(PPPEApplication.EXTRA_APPLICATIONS, intent.getStringExtra(PPPEApplication.EXTRA_APPLICATIONS));
                    scanServiceIntent.putExtra(PPPEApplication.EXTRA_PROFILE_ID, profileId);
                    appContext.startService(scanServiceIntent);
                }
            }
        }
        else
        if (action.equals(PPPEAccessibilityService.ACTION_LOCK_DEVICE)) {
            if (!intent.getBooleanExtra(PPPEApplication.EXTRA_BLOCK_PROFILE_EVENT_ACTION, false)) {
                if (PPPEApplication.registeredLockDeviceFunctionPP ||
                        PPPEApplication.registeredLockDeviceFunctionPPP) {
                    if ((Build.VERSION.SDK_INT >= 28) && (PPPEAccessibilityService.instance != null)) {
//                        PPPEApplication.logE("FromPhoneProfilesPlusBroadcastReceiver.onReceive", "lock device");
                        PPPEAccessibilityService.instance.performGlobalAction(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN);
                    }
                }
            }
        }
    }

    private void showPermissionNotification(Context appContext, String title, String text,
                                                    int notificationID, String notificationTag,
                                                    int scrollTo) {
        //noinspection UnnecessaryLocalVariable
        String nTitle = title;
        //noinspection UnnecessaryLocalVariable
        String nText = text;
        PPPEApplicationStatic.createGrantPermissionNotificationChannel(appContext, false);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(appContext, PPPEApplication.GRANT_PERMISSION_NOTIFICATION_CHANNEL)
                .setColor(ContextCompat.getColor(appContext, R.color.errorColor))
                .setSmallIcon(R.drawable.ic_pppe_notification/*icic_exclamation_notify*/) // notification icon
                .setLargeIcon(BitmapFactory.decodeResource(appContext.getResources(), R.drawable.ic_exclamation_notification))
                .setContentTitle(nTitle) // title for notification
                .setContentText(nText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(nText))
                .setAutoCancel(true); // clear notification after click

        Intent intent = new Intent(appContext, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        intent.putExtra(PPPEApplication.EXTRA_SCROLL_TO, scrollTo);

        @SuppressLint("UnspecifiedImmutableFlag")
        PendingIntent pi = PendingIntent.getActivity(appContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        mBuilder.setCategory(NotificationCompat.CATEGORY_RECOMMENDATION);
        mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        mBuilder.setOnlyAlertOnce(true);

        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(appContext);
        try {
            mNotificationManager.notify(notificationTag, notificationID, mBuilder.build());
        } catch (SecurityException en) {
            Log.e("FromPhoneProfilesPlusBroadcastReceiver.showPermissionNotification", Log.getStackTraceString(en));
        } catch (Exception e) {
            //Log.e("FromPhoneProfilesPlusBroadcastReceiver.showPermissionNotification", Log.getStackTraceString(e));
            PPPEApplicationStatic.recordException(e);
        }
    }

}
