package sk.henrichg.phoneprofilesplusextender;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

class FromPhoneProfilesPlusBroadcastReceiver extends BroadcastReceiver {

    @SuppressLint({"LongLogTag", "InlinedApi"})
    @Override
    public void onReceive(Context context, Intent intent) {
        if ((intent == null) || (intent.getAction() == null))
            return;

//        PPPEApplication.logE("FromPhoneProfilesPlusBroadcastReceiver.onReceive", "received broadcast action="+intent.getAction());

        String action = intent.getAction();
        if (action.equals(PPPEApplication.ACTION_REGISTER_PPPE_FUNCTION)) {
            String registrationApplication = intent.getStringExtra(PPPEApplication.EXTRA_REGISTRATION_APP);
            int registrationType = intent.getIntExtra(PPPEApplication.EXTRA_REGISTRATION_TYPE, 0);

            //PPPEApplication.logE("FromPhoneProfilesPlusBroadcastReceiver.onReceive", "registrationApplication="+registrationApplication);
            //PPPEApplication.logE("FromPhoneProfilesPlusBroadcastReceiver.onReceive", "registrationType="+registrationType);

            if (registrationApplication.equals("PhoneProfiles")) {
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
            if (registrationApplication.equals("PhoneProfilesPlus")) {
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
                        if (!Permissions.checkSMSMMSPermissions(context)) {
                            showPermissionNotification(context,
                                    context.getString(R.string.extender_notification_permission_title),
                                    context.getString(R.string.extender_notification_sms_mms_permission_text),
                                    PPPEApplication.GRANT_PERMISSIONS_SMS_NOTIFICATION_ID,
                                    PPPEApplication.GRANT_PERMISSIONS_SMS_NOTIFICATION_TAG);
                        }
                        break;
                    case PPPEApplication.REGISTRATION_TYPE_SMS_UNREGISTER:
                        PPPEApplication.registeredSMSFunctionPPP = false;
                        break;
                    case PPPEApplication.REGISTRATION_TYPE_CALL_REGISTER:
                        PPPEApplication.registeredCallFunctionPPP = true;
                        if (!Permissions.checkCallPermissions(context)) {
                            showPermissionNotification(context,
                                    context.getString(R.string.extender_notification_permission_title),
                                    context.getString(R.string.extender_notification_call_permission_text),
                                    PPPEApplication.GRANT_PERMISSIONS_CALL_NOTIFICATION_ID,
                                    PPPEApplication.GRANT_PERMISSIONS_CALL_NOTIFICATION_TAG);
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
                    case PPPEApplication.REGISTRATION_TYPE_PUT_SETTINGS_PARAMETER_REGISTER:
                        PPPEApplication.registeredPutSettingsParameterFunctionPPP = true;
                        break;
                    case PPPEApplication.REGISTRATION_TYPE_PUT_SETTINGS_PARAMETER_UNREGISTER:
                        PPPEApplication.registeredPutSettingsParameterFunctionPPP = false;
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
                long profileId = intent.getLongExtra(ForceCloseIntentService.EXTRA_PROFILE_ID, 0);
//                PPPEApplication.logE("FromPhoneProfilesPlusBroadcastReceiver.onReceive", "profileId="+profileId);
//                PPPEApplication.logE("FromPhoneProfilesPlusBroadcastReceiver.onReceive", "applications="+intent.getStringExtra(ForceCloseIntentService.EXTRA_APPLICATIONS));

                if (PPPEAccessibilityService.instance != null) {
                    Intent scanServiceIntent = new Intent(PPPEAccessibilityService.instance, ForceCloseIntentService.class);
                    scanServiceIntent.putExtra(ForceCloseIntentService.EXTRA_APPLICATIONS, intent.getStringExtra(ForceCloseIntentService.EXTRA_APPLICATIONS));
                    scanServiceIntent.putExtra(ForceCloseIntentService.EXTRA_PROFILE_ID, profileId);
                    PPPEAccessibilityService.instance.startService(scanServiceIntent);
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
        else
        if (action.equals(PPPEAccessibilityService.ACTION_PUT_SETTINGS_PARAMETER)) {
            if (!intent.getBooleanExtra(PPPEApplication.EXTRA_BLOCK_PROFILE_EVENT_ACTION, false)) {
                if (PPPEApplication.registeredPutSettingsParameterFunctionPPP) {
                    if (PPPEAccessibilityService.instance != null) {
                        PPPEApplication.logE("FromPhoneProfilesPlusBroadcastReceiver.onReceive", "put settins parameter");
                        // TODO tu spracuj broadcast z PPP na nastavenie Settings

                        String type = intent.getStringExtra(PPPEAccessibilityService.EXTRA_PUT_SETTINGS_PARAMETER_TYPE);
                        String name = intent.getStringExtra(PPPEAccessibilityService.EXTRA_PUT_SETTINGS_PARAMETER_NAME);
                        String value = intent.getStringExtra(PPPEAccessibilityService.EXTRA_PUT_SETTINGS_PARAMETER_VALUE);

                        if (((type != null) && !type.isEmpty()) &&
                            ((name != null) && !name.isEmpty()) &&
                            ((value != null) && !value.isEmpty())) {
                            ContentResolver contentResolver = context.getContentResolver();
                            try {
                                ContentValues contentValues = new ContentValues(2);
                                contentValues.put("name", name);
                                contentValues.put("value", value);
                                // settingsType : "system", "secure", "global"
                                contentResolver.insert(Uri.parse("content://settings/" + type), contentValues);
                            } catch (Exception e) {
                                Log.e("FromPhoneProfilesPlusBroadcastReceiver.onReceive", Log.getStackTraceString(e));
                            }
                        }
                    }
                }
            }
        }
    }

    static private void showPermissionNotification(Context context, String title, String text,
                                                    int notificationID, String notificationTag) {
        //noinspection UnnecessaryLocalVariable
        String nTitle = title;
        //noinspection UnnecessaryLocalVariable
        String nText = text;
        PPPEApplication.createGrantPermissionNotificationChannel(context);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, PPPEApplication.GRANT_PERMISSION_NOTIFICATION_CHANNEL)
                .setColor(ContextCompat.getColor(context, R.color.notificationDecorationColor))
                .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                .setContentTitle(nTitle) // title for notification
                .setContentText(nText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(nText))
                .setAutoCancel(true); // clear notification after click

        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        @SuppressLint("UnspecifiedImmutableFlag")
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        mBuilder.setCategory(NotificationCompat.CATEGORY_RECOMMENDATION);
        mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        mBuilder.setOnlyAlertOnce(true);

        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(context);
        try {
            mNotificationManager.notify(notificationTag, notificationID, mBuilder.build());
        } catch (Exception e) {
            //Log.e("IgnoreBatteryOptimizationNotification.showNotification", Log.getStackTraceString(e));
            PPPEApplication.recordException(e);
        }
    }

}
