package sk.henrichg.phoneprofilesplusextender;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

class Permissions {

    static final int PERMISSIONS_REQUEST_CODE = 9091;

    static boolean checkSMSMMSPermissions(Context context) {
        if (PPPEApplication.hasSystemFeature(context.getApplicationContext(), PackageManager.FEATURE_TELEPHONY)) {
            boolean grantedReceiveSMS = ContextCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED;
            boolean grantedReceiveMMS = ContextCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.RECEIVE_MMS) == PackageManager.PERMISSION_GRANTED;

            return grantedReceiveSMS && grantedReceiveMMS;
        }
        else
            return false;
    }

    static boolean checkCallPermissions(Context context) {
        if (PPPEApplication.hasSystemFeature(context.getApplicationContext(), PackageManager.FEATURE_TELEPHONY)) {
            boolean grantedReadPhoneState = ContextCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
            boolean grantedProcessOutgoingCalls = ContextCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.PROCESS_OUTGOING_CALLS) == PackageManager.PERMISSION_GRANTED;
            boolean grantedReadCallLog = ContextCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED;

            return grantedReadPhoneState && grantedProcessOutgoingCalls && grantedReadCallLog;
        }
        else
            return false;
    }

    static void grantSMSMMSPermissions(Activity activity) {
        //boolean showRequestReceiveSMS = ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.RECEIVE_SMS);
        //boolean showRequestReceiveMMS = ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.RECEIVE_MMS);

        //if (showRequestReceiveSMS || showRequestReceiveMMS) {
            String[] permArray = new String[2];
            permArray[0] = Manifest.permission.RECEIVE_SMS;
            permArray[1] = Manifest.permission.RECEIVE_MMS;

            ActivityCompat.requestPermissions(activity, permArray, PERMISSIONS_REQUEST_CODE);
        //}
    }

    static void grantCallPermissions(Activity activity) {
        //boolean showRequestReadPhoneState = ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_PHONE_STATE);
        //boolean showRequestProcessOutgoingCalls = ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.PROCESS_OUTGOING_CALLS);
        //boolean showRequestReadCallLog = ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_CALL_LOG);

        //if (showRequestReadPhoneState || showRequestProcessOutgoingCalls || showRequestReadCallLog) {
            String[] permArray = new String[3];
            permArray[0] = Manifest.permission.READ_PHONE_STATE;
            permArray[1] = Manifest.permission.PROCESS_OUTGOING_CALLS;
            permArray[2] = Manifest.permission.READ_CALL_LOG;

            ActivityCompat.requestPermissions(activity, permArray, PERMISSIONS_REQUEST_CODE);
        //}
    }

    static void grantNotificationsPermission(final Activity activity) {
        if (Build.VERSION.SDK_INT >= 33) {
            NotificationManager notificationManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                if (!notificationManager.areNotificationsEnabled()) {

                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                    dialogBuilder.setMessage(R.string.extender_notifications_permission_text);
                    //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                    dialogBuilder.setPositiveButton(R.string.extender_enable_notificaitons_button, (dialog, which) -> {

                        boolean ok = false;

                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                        intent.putExtra(Settings.EXTRA_APP_PACKAGE, PPPEApplication.PACKAGE_NAME);

                        if (MainActivity.activityIntentExists(intent, activity.getApplicationContext())) {
                            try {
                                activity.startActivity(intent);
                                ok = true;
                            } catch (Exception e) {
                                PPPEApplication.recordException(e);
                            }
                        }
                        if (!ok) {
                            AlertDialog.Builder dialogBuilder1 = new AlertDialog.Builder(activity);
                            dialogBuilder1.setMessage(R.string.extender_setting_screen_not_found_alert);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder1.setPositiveButton(android.R.string.ok, null);
                            AlertDialog _dialog = dialogBuilder1.create();

//                                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                                    @Override
//                                    public void onShow(DialogInterface dialog) {
//                                        Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                                        if (positive != null) positive.setAllCaps(false);
//                                        Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                                        if (negative != null) negative.setAllCaps(false);
//                                    }
//                                });

                            if (!activity.isFinishing())
                                _dialog.show();
                        }

                    });
                    //dialogBuilder.setNegativeButton(R.string.extender_dont_enable_notificaitons_button, null);

                    AlertDialog dialog = dialogBuilder.create();
                    dialog.setCancelable(false);
                    dialog.setCanceledOnTouchOutside(false);

                    if (!activity.isFinishing())
                        dialog.show();
                }
            }
        }
    }

}
