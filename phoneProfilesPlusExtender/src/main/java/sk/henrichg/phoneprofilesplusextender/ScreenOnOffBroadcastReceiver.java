package sk.henrichg.phoneprofilesplusextender;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScreenOnOffBroadcastReceiver extends BroadcastReceiver {

    @SuppressLint("LongLogTag")
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null)
            return;

        final String action = intent.getAction();

        if ((action != null) && action.equals(Intent.ACTION_SCREEN_ON)) {
            //Log.e("ScreenOnOffBroadcastReceiver.onReceive","ACTION_SCREEN_ON");
            ForceCloseIntentService.screenOffReceived = false;
        } else if ((action != null) && action.equals(Intent.ACTION_SCREEN_OFF)) {
            //Log.e("ScreenOnOffBroadcastReceiver.onReceive","ACTION_SCREEN_OFF");
            ForceCloseIntentService.screenOffReceived = true;

            if (PPPEApplication.forceStopStarted) {
                // simulate home button click
                Intent startMain = new Intent(Intent.ACTION_MAIN);
                startMain.addCategory(Intent.CATEGORY_HOME);
                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(startMain);
            }

        } else if ((action != null) && action.equals(Intent.ACTION_USER_PRESENT)) {
            //Log.e("ScreenOnOffBroadcastReceiver.onReceive","ACTION_USER_PRESENT");
            ForceCloseIntentService.screenOffReceived = false;
        }
    }
}
