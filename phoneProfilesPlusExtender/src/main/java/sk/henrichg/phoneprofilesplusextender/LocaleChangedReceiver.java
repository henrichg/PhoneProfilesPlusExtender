package sk.henrichg.phoneprofilesplusextender;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LocaleChangedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if ((intent != null) && (intent.getAction() != null) && intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED)) {
            PPPEApplication.collator = PPPEApplication.getCollator();
            PPPEApplication.createGrantPermissionNotificationChannel(context.getApplicationContext(), true);
        }
    }

}
