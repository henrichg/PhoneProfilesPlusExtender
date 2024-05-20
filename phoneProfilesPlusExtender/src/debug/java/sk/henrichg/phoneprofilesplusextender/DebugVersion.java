package sk.henrichg.phoneprofilesplusextender;

import android.app.Activity;
import android.content.Intent;

class DebugVersion {
    static final boolean enabled = true;

    static boolean debugMenuItems(int menuItem, Activity activity) {

        if (menuItem == R.id.menu_test_crash) {
            throw new RuntimeException("Test Crash");
            //return true;
        }
        else
        if (menuItem == R.id.menu_test_nonFatal) {
            try {
                throw new RuntimeException("Test non-fatal exception");
            } catch (Exception e) {
                // You must relaunch PPP to get this exception in Firebase console:
                //
                // Crashlytics processes exceptions on a dedicated background thread, so the performance
                // impact to your app is minimal. To reduce your users’ network traffic, Crashlytics batches
                // logged exceptions together and sends them the next time the app launches.
                //
                // Crashlytics only stores the most recent 8 exceptions in a given app session. If your app
                // throws more than 8 exceptions in a session, older exceptions are lost.
                PPPEApplicationStatic.recordException(e);
            }
            return true;
        }
        else
        if (menuItem == R.id.menu_show_log_file) {
            Intent intentLaunch = new Intent(activity.getApplicationContext(), LogCrashActivity.class);
            activity.startActivity(intentLaunch);

            return true;
        }
        else
            return false;
    }

}
