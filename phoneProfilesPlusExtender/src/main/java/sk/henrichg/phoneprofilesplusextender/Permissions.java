package sk.henrichg.phoneprofilesplusextender;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

class Permissions {

    static final int PERMISSIONS_REQUEST_CODE = 9091;

    static boolean checkSMSMMSPermissions(Activity activity) {
        boolean grantedReceiveSMS = ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED;
        boolean grantedReceiveMMS = ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.RECEIVE_MMS) == PackageManager.PERMISSION_GRANTED;

        return grantedReceiveSMS && grantedReceiveMMS;
    }

    static boolean checkCallPermissions(Activity activity) {
        boolean grantedReadPhoneState = ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
        boolean grantedProcessOutgoingCalls = ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.PROCESS_OUTGOING_CALLS) == PackageManager.PERMISSION_GRANTED;
        boolean grantedReadCallLog = ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED;

        return grantedReadPhoneState && grantedProcessOutgoingCalls && grantedReadCallLog;
    }

    static void grantSMSMMSPermissions(Activity activity) {
        boolean showRequestReceiveSMS = ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.RECEIVE_SMS);
        boolean showRequestReceiveMMS = ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.RECEIVE_MMS);

        if (showRequestReceiveSMS || showRequestReceiveMMS) {
            String[] permArray = new String[2];
            permArray[0] = Manifest.permission.RECEIVE_SMS;
            permArray[1] = Manifest.permission.RECEIVE_MMS;

            ActivityCompat.requestPermissions(activity, permArray, PERMISSIONS_REQUEST_CODE);
        }
    }

    static void grantCallPermissions(Activity activity) {
        boolean showRequestReadPhoneState = ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_PHONE_STATE);
        boolean showRequestProcessOutgoingCalls = ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.PROCESS_OUTGOING_CALLS);
        boolean showRequestReadCallLog = ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_CALL_LOG);

        if (showRequestReadPhoneState || showRequestProcessOutgoingCalls || showRequestReadCallLog) {
            String[] permArray = new String[3];
            permArray[0] = Manifest.permission.READ_PHONE_STATE;
            permArray[1] = Manifest.permission.PROCESS_OUTGOING_CALLS;
            permArray[2] = Manifest.permission.READ_CALL_LOG;

            ActivityCompat.requestPermissions(activity, permArray, PERMISSIONS_REQUEST_CODE);
        }
    }

}
