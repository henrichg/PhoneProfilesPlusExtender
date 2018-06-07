package sk.henrichg.phoneprofilesplusextender;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int RESULT_ACCESSIBILITY_SETTINGS = 1900;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (/*(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) &&*/ (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            //w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // create our manager instance after the content view is set
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            // enable status bar tint
            tintManager.setStatusBarTintEnabled(true);
            // set a custom tint color for status bar
            tintManager.setStatusBarTintColor(ContextCompat.getColor(getBaseContext(), R.color.colorPrimaryDark));
        }

        TextView text = findViewById(R.id.activity_main_application_version);
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            text.setText(getString(R.string.extender_about_application_version) + " " + pInfo.versionName + " (" + pInfo.versionCode + ")");
        } catch (Exception e) {
            text.setText("");
        }

        String str1 = getString(R.string.extender_accessibility_service_profile_force_stop_applications);
        String str2;
        if (PPPEAccessibilityService.isEnabled(getApplicationContext()))
            str2 = str1 + " " + getString(R.string.extender_accessibility_service_enabled);
        else
            str2 = str1 + " " + getString(R.string.extender_accessibility_service_disabled);
        Spannable sbt = new SpannableString(str2);
        text = findViewById(R.id.activity_main_accessibility_service_force_stop_application);
        sbt.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), str1.length()+1, str2.length(), 0);
        text.setText(sbt);

        str1 = getString(R.string.extender_accessibility_service_event_sensor_applications_orientation);
        if (PPPEAccessibilityService.isEnabled(getApplicationContext()))
            str2 = str1 + " " + getString(R.string.extender_accessibility_service_enabled);
        else
            str2 = str1 + " " + getString(R.string.extender_accessibility_service_disabled);
        sbt = new SpannableString(str2);
        text = findViewById(R.id.activity_main_accessibility_service_event_sensor_applications_orientation);
        sbt.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), str1.length()+1, str2.length(), 0);
        text.setText(sbt);

        final Activity activity = this;
        Button accessibilityButton = findViewById(R.id.activity_main_accessibility_service_button);
        accessibilityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MainActivity.activityActionExists(Settings.ACTION_ACCESSIBILITY_SETTINGS, activity)) {
                    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    startActivityForResult(intent, RESULT_ACCESSIBILITY_SETTINGS);
                } else {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                    dialogBuilder.setMessage(R.string.extender_setting_screen_not_found_alert);
                    //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                    dialogBuilder.setPositiveButton(android.R.string.ok, null);
                    dialogBuilder.show();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_ACCESSIBILITY_SETTINGS)
            reloadActivity(this/*, true*/);
    }

    private static boolean activityActionExists(@SuppressWarnings("SameParameterValue") String action,
                                                Context context) {
        try {
            final Intent intent = new Intent(action);
            List<ResolveInfo> activities = context.getApplicationContext().getPackageManager().queryIntentActivities(intent, 0);
            return activities.size() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private static void reloadActivity(Activity activity/*, boolean newIntent*/)
    {
        /*if (newIntent)
        {
            final Activity _activity = activity;
            new Handler(activity.getMainLooper()).post(new Runnable() {

                @Override
                public void run() {
                    try {
                        Intent intent = _activity.getIntent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        _activity.finish();
                        _activity.overridePendingTransition(0, 0);

                        _activity.startActivity(intent);
                        _activity.overridePendingTransition(0, 0);
                    } catch (Exception ignored) {}
                }
            });
        }
        else*/
            activity.recreate();
    }

}
