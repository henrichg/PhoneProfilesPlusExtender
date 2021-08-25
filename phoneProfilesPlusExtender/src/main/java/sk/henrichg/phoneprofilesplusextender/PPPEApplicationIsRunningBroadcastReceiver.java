package sk.henrichg.phoneprofilesplusextender;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

class PPPEApplicationIsRunningBroadcastReceiver extends BroadcastReceiver {

    private static final String ACTION_PPPEXTENDER_IS_RUNNING_ANSWER = PPPEApplication.PACKAGE_NAME + ".ACTION_PPPEXTENDER_IS_RUNNING_ANSWER";

    @SuppressLint({"LongLogTag", "InlinedApi"})
    @Override
    public void onReceive(Context context, Intent intent) {
        if ((intent == null) || (intent.getAction() == null))
            return;

        //PPPEApplication.logE("PPPEApplicationIsRunningBroadcastReceiver.onReceive", "received broadcast action="+intent.getAction());

        String action = intent.getAction();
        if (action.equals(PPPEApplication.ACTION_PPPEXTENDER_IS_RUNNING)) {
            // send answer to PPP
//            PPPEApplication.logE("FromPhoneProfilesPlusBroadcastReceiver.onReceive", "PPPEApplication.ACTION_PPPEXTENDER_IS_RUNNING");
            if (PPPEAccessibilityService.instance != null) {
                Intent sendIntent = new Intent(ACTION_PPPEXTENDER_IS_RUNNING_ANSWER);
                context.sendBroadcast(sendIntent, PPPEApplication.ACCESSIBILITY_SERVICE_PERMISSION);
            }
        }
    }
}
