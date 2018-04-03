package sk.henrichg.phoneprofilesplusextender;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

class FromPhoneProfilesPlusBroadcastReceiver extends BroadcastReceiver {

    @SuppressLint("LongLogTag")
    @Override
    public void onReceive(Context context, Intent intent) {
        if ((intent == null) || (intent.getAction() == null))
            return;

        //Log.e("PPPEAccessibilityService", "received broadcast action="+intent.getAction());

        if (intent.getAction().equals(PPPEAccessibilityService.ACTION_FORCE_STOP_START)) {
            //PPPEAccessibilityService.forceStopStarted = true;
            Intent scanServiceIntent = new Intent(context, ForceCloseIntentService.class);
            scanServiceIntent.putExtra(ForceCloseIntentService.EXTRA_APPLICATIONS, intent.getStringExtra(ForceCloseIntentService.EXTRA_APPLICATIONS));
            context.startService(scanServiceIntent);
        }

        if (intent.getAction().equals(PPPEAccessibilityService.ACTION_CHANGE_LANGUAGE)) {
            String language = intent.getStringExtra(PPPEAccessibilityService.EXTRA_LANGUAGE);
            if (language.isEmpty())
                language = "system";
            SharedPreferences preferences = context.getApplicationContext().getSharedPreferences(PPPEApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(PPPEApplication.PREF_APPLICATION_LANGUAGE, language);
            editor.apply();
        }

    }
}
