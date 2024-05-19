package sk.henrichg.phoneprofilesplusextender;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Handles broadcasts related to SIM card state changes.
 * <p>
 * Possible states that are received here are:
 * <p>
 * Documented:
 * ABSENT
 * NETWORK_LOCKED
 * PIN_REQUIRED
 * PUK_REQUIRED
 * READY
 * UNKNOWN
 * <p>
 * Undocumented:
 * NOT_READY (ICC interface is not ready, e.g. radio is off or powering on)
 * CARD_IO_ERROR (three consecutive times there was a SIM IO error)
 * IMSI (ICC IMSI is ready in property)
 * LOADED (all ICC records, including IMSI, are loaded)
 * <p>
 * Note: some of these are not documented in
 * <a href="https://developer.android.com/reference/android/telephony/TelephonyManager.html">...</a>
 * but they can be found deeper in the source code, namely in com.android.internal.telephony.IccCardConstants.
 */
public class SimStateChangedBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPPEApplication.logE("[IN_BROADCAST] SimStateChangedBroadcastReceiver.onReceive", "xxx");

        if (intent == null)
            return;

        PPPEApplication.logE("[MEMORY_LEAK] SimStateChangedBroadcastReceiver.onReceive", "xxxx");

        final Context appContext = context.getApplicationContext();
        //final Intent _intent = intent;

        PPPEAccessibilityService.registerPhoneStateListener(false, appContext);
        try{ Thread.sleep(1000); }catch(InterruptedException ignored){ }
        PPPEAccessibilityService.registerPhoneStateListener(true, appContext);

    }

}
