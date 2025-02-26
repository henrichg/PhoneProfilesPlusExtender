package sk.henrichg.phoneprofilesplusextender;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.Handler;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

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

    /** @noinspection SameParameterValue*/
    static void setCustomDialogTitle(Context context, AlertDialog.Builder dialogBuilder,
                                     boolean showSubtitle, CharSequence _title, CharSequence _subtitle) {
        //String s = _title.toString();
        //if (s.startsWith(StringConstants.CHAR_BULLET +" "))
        //    _title = TextUtils.replace(_title, new String[]{StringConstants.CHAR_BULLET +" "}, new CharSequence[]{""});

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        //noinspection IfStatementWithIdenticalBranches
        if (showSubtitle) {
            @SuppressLint("InflateParams")
            View titleView = layoutInflater.inflate(R.layout.custom_dialog_title_wtih_subtitle, null);
            TextView titleText = titleView.findViewById(R.id.custom_dialog_title);
            //noinspection DataFlowIssue
            titleText.setText(_title);
            TextView subtitleText = titleView.findViewById(R.id.custom_dialog_subtitle);
            //noinspection DataFlowIssue
            subtitleText.setText(_subtitle);
            dialogBuilder.setCustomTitle(titleView);
        } else {
            @SuppressLint("InflateParams")
            View titleView = layoutInflater.inflate(R.layout.custom_dialog_title_wtihout_subtitle, null);
            TextView titleText = titleView.findViewById(R.id.custom_dialog_title);
            //noinspection DataFlowIssue
            titleText.setText(_title);
            dialogBuilder.setCustomTitle(titleView);
        }
    }

}
