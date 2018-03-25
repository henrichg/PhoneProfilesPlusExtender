package sk.henrichg.phoneprofilesplusextender;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

class FromPhoneProfilesPlusBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ((intent == null) || (intent.getAction() == null))
            return;

        if (intent.getAction().equals(PPPEAccessibilityService.APP_INFO_OPENED_BROADCAST)) {
            PPPEAccessibilityService.appInfoOpened = true;
        }
    }
}
