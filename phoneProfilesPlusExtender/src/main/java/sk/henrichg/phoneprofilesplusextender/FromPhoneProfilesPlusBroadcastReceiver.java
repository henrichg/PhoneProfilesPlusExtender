package sk.henrichg.phoneprofilesplusextender;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

class FromPhoneProfilesPlusBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ((intent == null) || (intent.getAction() == null))
            return;

        if (intent.getAction().equals(PPPEAccessibilityService.ACTION_APP_INFO_OPENING)) {
            PPPEAccessibilityService.appInfoOpened = false;
            Intent _intent = new Intent(PPPEAccessibilityService.ACTION_OPEN_APP_INFO);
            context.sendBroadcast(_intent, PPPEAccessibilityService.ACCESSIBILITY_SERVICE_PERMISSION);
        }

        if (intent.getAction().equals(PPPEAccessibilityService.ACTION_APP_INFO_OPENED)) {
            PPPEAccessibilityService.appInfoOpened = true;
        }
    }
}
