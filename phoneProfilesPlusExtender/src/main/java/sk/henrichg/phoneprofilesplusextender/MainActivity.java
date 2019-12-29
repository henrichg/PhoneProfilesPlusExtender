package sk.henrichg.phoneprofilesplusextender;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.pm.PackageInfoCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int RESULT_ACCESSIBILITY_SETTINGS = 1900;
    private static final int RESULT_PERMISSIONS_SETTINGS = 1901;

    private final BroadcastReceiver refreshGUIBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive( Context context, Intent intent ) {
            MainActivity.this.displayAccessibilityServiceStatus();
            PPPEApplication.logE("MainActivity.refreshGUIBroadcastReceiver", "xxx");
        }
    };

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        setContentView(R.layout.activity_main);

        PPPEApplication.logE("MainActivity.onCreated", "xxx");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setElevation(0);
        }

        TextView text = findViewById(R.id.activity_main_application_version);
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            text.setText(getString(R.string.extender_about_application_version) + " " + pInfo.versionName +
                                        " (" + PackageInfoCompat.getLongVersionCode(pInfo) + ")");
        } catch (Exception e) {
            text.setText("");
        }

        displayAccessibilityServiceStatus();

        if (Build.VERSION.SDK_INT >= 23) {
            final Activity activity = this;
            String str1;
            String str2;
            Spannable sbt;

            str1 = getString(R.string.extender_permissions_event_sensor_sms_mms);
            if (Permissions.checkSMSMMSPermissions(activity))
                str2 = str1 + " " + getString(R.string.extender_permissions_granted);
            else
                str2 = str1 + " " + getString(R.string.extender_permissions_not_granted);
            sbt = new SpannableString(str2);
            text = findViewById(R.id.activity_main_permissions_event_sensor_sms_mms);
            sbt.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), str1.length() + 1, str2.length(), 0);
            text.setText(sbt);

            if (Build.VERSION.SDK_INT < 28)
                str1 = getString(R.string.extender_permissions_event_sensor_call);
            else
                str1 = getString(R.string.extender_permissions_event_sensor_call_28);
            if (Permissions.checkCallPermissions(activity))
                str2 = str1 + " " + getString(R.string.extender_permissions_granted);
            else
                str2 = str1 + " " + getString(R.string.extender_permissions_not_granted);
            sbt = new SpannableString(str2);
            text = findViewById(R.id.activity_main_permissions_event_sensor_call);
            sbt.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), str1.length() + 1, str2.length(), 0);
            text.setText(sbt);

            Button permissionsButton = findViewById(R.id.activity_main_sms_permissions_button);
            permissionsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Permissions.checkSMSMMSPermissions(activity)) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        //intent.addCategory(Intent.CATEGORY_DEFAULT);
                        intent.setData(Uri.parse("package:"+getPackageName()));
                        if (MainActivity.activityIntentExists(intent, activity)) {
                            startActivityForResult(intent, RESULT_PERMISSIONS_SETTINGS);
                        } else {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                            dialogBuilder.setMessage(R.string.extender_setting_screen_not_found_alert);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
                            dialogBuilder.show();
                        }
                    }
                    else
                        Permissions.grantSMSMMSPermissions(activity);
                }
            });
            permissionsButton = findViewById(R.id.activity_main_call_permissions_button);
            permissionsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Permissions.checkCallPermissions(activity)) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        //intent.addCategory(Intent.CATEGORY_DEFAULT);
                        intent.setData(Uri.parse("package:"+getPackageName()));
                        if (MainActivity.activityIntentExists(intent, activity)) {
                            startActivityForResult(intent, RESULT_PERMISSIONS_SETTINGS);
                        } else {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                            dialogBuilder.setMessage(R.string.extender_setting_screen_not_found_alert);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
                            dialogBuilder.show();
                        }
                    }
                    else
                        Permissions.grantCallPermissions(activity);
                }
            });
        }
        else {
            text = findViewById(R.id.activity_main_permissions_event_sensor_sms_mms);
            text.setVisibility(View.GONE);
            text = findViewById(R.id.activity_main_permissions_event_sensor_call);
            text.setVisibility(View.GONE);
            Button permissionsButton = findViewById(R.id.activity_main_sms_permissions_button);
            permissionsButton.setVisibility(View.GONE);
            permissionsButton = findViewById(R.id.activity_main_call_permissions_button);
            permissionsButton.setVisibility(View.GONE);
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(refreshGUIBroadcastReceiver,
                new IntentFilter(PPPEApplication.PACKAGE_NAME + ".RefreshGUIBroadcastReceiver"));

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_ACCESSIBILITY_SETTINGS)
            reloadActivity(this/*, true*/);
        if (requestCode == RESULT_PERMISSIONS_SETTINGS)
            reloadActivity(this/*, true*/);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (requestCode == Permissions.PERMISSIONS_REQUEST_CODE) {
            boolean allGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_DENIED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                reloadActivity(this/*, true*/);
            } else {
                //if (!onlyNotification) {
                Context context = getApplicationContext();
                Toast msg = Toast.makeText(context,
                        context.getResources().getString(R.string.extender_app_name) + ": " +
                                context.getResources().getString(R.string.extender_toast_permissions_not_granted),
                        Toast.LENGTH_SHORT);
                msg.show();
                //}
                reloadActivity(this/*, true*/);
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void displayAccessibilityServiceStatus() {
        String str1 = getString(R.string.extender_accessibility_service_profile_force_stop_applications);
        String str2;
        if (PPPEAccessibilityService.isEnabled(getApplicationContext()))
            str2 = str1 + " " + getString(R.string.extender_accessibility_service_enabled);
        else
            str2 = str1 + " " + getString(R.string.extender_accessibility_service_disabled);
        Spannable sbt = new SpannableString(str2);
        TextView text = findViewById(R.id.activity_main_accessibility_service_profile_force_stop_application);
        sbt.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), str1.length()+1, str2.length(), 0);
        text.setText(sbt);

        str1 = getString(R.string.extender_accessibility_service_profile_lock_device);
        if (PPPEAccessibilityService.isEnabled(getApplicationContext()))
            str2 = str1 + " " + getString(R.string.extender_accessibility_service_enabled);
        else
            str2 = str1 + " " + getString(R.string.extender_accessibility_service_disabled);
        sbt = new SpannableString(str2);
        text = findViewById(R.id.activity_main_accessibility_service_profile_lock_device);
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

        str1 = getString(R.string.extender_accessibility_service_event_sensor_sms_mms);
        if (PPPEAccessibilityService.isEnabled(getApplicationContext()))
            str2 = str1 + " " + getString(R.string.extender_accessibility_service_enabled);
        else
            str2 = str1 + " " + getString(R.string.extender_accessibility_service_disabled);
        sbt = new SpannableString(str2);
        text = findViewById(R.id.activity_main_accessibility_service_event_sensor_sms_mms);
        sbt.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), str1.length()+1, str2.length(), 0);
        text.setText(sbt);

        str1 = getString(R.string.extender_accessibility_service_event_sensor_call);
        if (PPPEAccessibilityService.isEnabled(getApplicationContext()))
            str2 = str1 + " " + getString(R.string.extender_accessibility_service_enabled);
        else
            str2 = str1 + " " + getString(R.string.extender_accessibility_service_disabled);
        sbt = new SpannableString(str2);
        text = findViewById(R.id.activity_main_accessibility_service_event_sensor_call);
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

    private static boolean activityIntentExists(Intent intent, Context context) {
        try {
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
