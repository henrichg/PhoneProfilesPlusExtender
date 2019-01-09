package sk.henrichg.phoneprofilesplusextender;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

class FromPhoneProfilesPlusBroadcastReceiver extends BroadcastReceiver {

    @SuppressLint("LongLogTag")
    @Override
    public void onReceive(Context context, Intent intent) {
        if ((intent == null) || (intent.getAction() == null))
            return;

        //Log.e("FromPhoneProfilesPlusBroadcastReceiver.onReceive", "received broadcast action="+intent.getAction());

        if (intent.getAction().equals(PPPEAccessibilityService.ACTION_REGISTER_PPPE_FUNCTION)) {

        }
        else
        if (intent.getAction().equals(PPPEAccessibilityService.ACTION_FORCE_STOP_APPLICATIONS_START)) {
            long profileId = intent.getLongExtra(ForceCloseIntentService.EXTRA_PROFILE_ID, 0);

            Intent scanServiceIntent = new Intent(context, ForceCloseIntentService.class);
            scanServiceIntent.putExtra(ForceCloseIntentService.EXTRA_APPLICATIONS, intent.getStringExtra(ForceCloseIntentService.EXTRA_APPLICATIONS));
            scanServiceIntent.putExtra(ForceCloseIntentService.EXTRA_PROFILE_ID, profileId);
            context.startService(scanServiceIntent);
        }
    }
}
