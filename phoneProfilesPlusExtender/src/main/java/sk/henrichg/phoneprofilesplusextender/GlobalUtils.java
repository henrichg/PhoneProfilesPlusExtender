package sk.henrichg.phoneprofilesplusextender;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.Handler;
import android.util.TypedValue;

import java.util.List;

class GlobalUtils {

    static boolean activityActionExists(@SuppressWarnings("SameParameterValue") String action,
                                                Context context) {
        try {
            final Intent intent = new Intent(action);
            List<ResolveInfo> activities = context.getApplicationContext().getPackageManager().queryIntentActivities(intent, 0);
            return !activities.isEmpty();
        } catch (Exception e) {
            //Log.e("MainActivity.activityActionExists", Log.getStackTraceString(e));
            //PPPEApplicationStatic.recordException(e);
            return false;
        }
    }

    static boolean activityIntentExists(Intent intent, Context context) {
        try {
            List<ResolveInfo> activities = context.getApplicationContext().getPackageManager().queryIntentActivities(intent, 0);
            return !activities.isEmpty();
        } catch (Exception e) {
            //Log.e("MainActivity.activityIntentExists", Log.getStackTraceString(e));
            //PPPEApplicationStatic.recordException(e);
            return false;
        }
    }

    static void reloadActivity(Activity activity,
                               @SuppressWarnings("SameParameterValue") boolean newIntent)
    {
        if (newIntent)
        {
            final Activity _activity = activity;
            new Handler(activity.getMainLooper()).post(() -> {
                try {
                    @SuppressLint("UnsafeIntentLaunch")
                    Intent intent = _activity.getIntent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    _activity.finish();
                    _activity.overridePendingTransition(0, 0);

                    _activity.startActivity(intent);
                    _activity.overridePendingTransition(0, 0);
                } catch (Exception ignored) {}
            });
        }
        else
            activity.recreate();
    }

    static int sip(float sp) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, Resources.getSystem().getDisplayMetrics()));
    }

}
