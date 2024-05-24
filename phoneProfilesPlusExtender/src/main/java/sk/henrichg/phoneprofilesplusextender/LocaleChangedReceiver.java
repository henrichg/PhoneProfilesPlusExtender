package sk.henrichg.phoneprofilesplusextender;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LocaleChangedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPPEApplicationStatic.logE("[MEMORY_LEAK] LocaleChangedReceiver.onReceive", "xxxx");

        if ((intent != null) && (intent.getAction() != null) && intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED)) {
            PPPEApplication.collator = PPPEApplicationStatic.getCollator();
            PPPEApplicationStatic.createGrantPermissionNotificationChannel(context.getApplicationContext(), true);
        }
    }

}
