package sk.henrichg.phoneprofilesplusextender;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

class FromPhoneProfilesPlusBroadcastReceiver extends BroadcastReceiver {

    @SuppressLint("LongLogTag")
    @Override
    public void onReceive(Context context, Intent intent) {
        if ((intent == null) || (intent.getAction() == null))
            return;

        //Log.e("PPPEAccessibilityService", "received broadcast action="+intent.getAction());

        if (intent.getAction().equals(PPPEAccessibilityService.ACTION_FORCE_STOP_INFO_START)) {
            //PPPEAccessibilityService.forceStopStarted = true;
            Intent scanServiceIntent = new Intent(context, ForceCloseIntentService.class);
            scanServiceIntent.putExtra(ForceCloseIntentService.EXTRA_APPLICATIONS, intent.getStringExtra(ForceCloseIntentService.EXTRA_APPLICATIONS));
            context.startService(scanServiceIntent);
        }

        /*
        if (intent.getAction().equals(PPPEAccessibilityService.ACTION_FORCE_STOP_INFO_STOP)) {
            PPPEAccessibilityService.forceStopStarted = false;
        }
        */
    }
}
