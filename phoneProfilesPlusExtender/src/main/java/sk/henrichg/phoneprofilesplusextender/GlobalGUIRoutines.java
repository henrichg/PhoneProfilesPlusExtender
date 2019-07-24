package sk.henrichg.phoneprofilesplusextender;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Handler;
import androidx.appcompat.app.AppCompatDelegate;

import static android.os.Looper.getMainLooper;

class GlobalGUIRoutines {

    private static void switchNightMode(Context appContext) {
        switch (ApplicationPreferences.applicationTheme(appContext/*, false*/)) {
            case "white":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            //case "night_mode":
            //    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            //    break;
        }
    }

    static void switchNightMode(final Context appContext, @SuppressWarnings("SameParameterValue") boolean useHandler) {
        if (useHandler) {
            new Handler(getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    try {
                        switchNightMode(appContext);
                    } catch (Exception ignored) {}
                }
            });
        }
        else
            switchNightMode(appContext);
    }

    static void reloadActivity(final Activity activity, @SuppressWarnings("SameParameterValue") boolean newIntent)
    {
        if (activity == null)
            return;

        if (newIntent)
        {
            new Handler(activity.getMainLooper()).post(new Runnable() {

                @Override
                public void run() {
                    try {
                        Intent intent = activity.getIntent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        activity.finish();
                        activity.overridePendingTransition(0, 0);

                        activity.startActivity(intent);
                        activity.overridePendingTransition(0, 0);
                    } catch (Exception ignored) {}
                }
            });
        }
        else
            activity.recreate();
    }

    @SuppressWarnings("SameParameterValue")
    static int dpToPx(int dp)
    {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

}
