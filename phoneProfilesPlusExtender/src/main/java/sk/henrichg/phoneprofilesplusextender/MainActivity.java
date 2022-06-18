package sk.henrichg.phoneprofilesplusextender;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.pm.PackageInfoCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int RESULT_ACCESSIBILITY_SETTINGS = 1900;
    private static final int RESULT_PERMISSIONS_SETTINGS = 1901;
    private static final int RESULT_BATTERY_OPTIMIZATION_SETTINGS = 1902;

    private final BroadcastReceiver refreshGUIBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive( Context context, Intent intent ) {
            MainActivity.this.displayAccessibilityServiceStatus();
            //PPPEApplication.logE("MainActivity.refreshGUIBroadcastReceiver", "xxx");
        }
    };

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        setContentView(R.layout.activity_main);

        //PPPEApplication.logE("MainActivity.onCreated", "xxx");

        if (getSupportActionBar() != null) {
            //getSupportActionBar().setHomeButtonEnabled(false);
            //getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle(R.string.extender_app_name);
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

        text = findViewById(R.id.activity_main_application_releases);
        String str1 = getString(R.string.extender_application_releases);
        String str2 = str1 + " https://github.com/henrichg/PhoneProfilesPlusExtender/releases" + " \u21D2";
        Spannable sbt = new SpannableString(str2);
        sbt.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setColor(ds.linkColor);    // you can use custom color
                ds.setUnderlineText(false);    // this remove the underline
            }

            @Override
            public void onClick(@NonNull View textView) {
                String url = "https://github.com/henrichg/PhoneProfilesPlusExtender/releases";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                try {
                    startActivity(Intent.createChooser(i, getString(R.string.extender_web_browser_chooser)));
                } catch (Exception ignored) {}
            }
        };
        sbt.setSpan(clickableSpan, str1.length()+1, str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //sbt.setSpan(new UnderlineSpan(), str1.length()+1, str2.length(), 0);
        text.setText(sbt);
        text.setMovementMethod(LinkMovementMethod.getInstance());

        displayAccessibilityServiceStatus();
        displayPermmisionsGrantStatus();

        LocalBroadcastManager.getInstance(this).registerReceiver(refreshGUIBroadcastReceiver,
                new IntentFilter(PPPEApplication.PACKAGE_NAME + ".RefreshGUIBroadcastReceiver"));
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        if (Build.VERSION.SDK_INT >= 33) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                if (!notificationManager.areNotificationsEnabled()) {

                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                    dialogBuilder.setMessage(R.string.extender_notifications_permission_text);
                    //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                    dialogBuilder.setPositiveButton(R.string.extender_enable_notificaitons_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            boolean ok = false;

                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                            intent.putExtra(Settings.EXTRA_APP_PACKAGE, PPPEApplication.PACKAGE_NAME);

                            if (activityIntentExists(intent, getApplicationContext())) {
                                try {
                                    startActivity(intent);
                                    ok = true;
                                } catch (Exception e) {
                                    PPPEApplication.recordException(e);
                                }
                            }
                            if (!ok) {
                                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
                                dialogBuilder.setMessage(R.string.extender_setting_screen_not_found_alert);
                                //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                                dialogBuilder.setPositiveButton(android.R.string.ok, null);
                                AlertDialog _dialog = dialogBuilder.create();

//                                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                                    @Override
//                                    public void onShow(DialogInterface dialog) {
//                                        Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                                        if (positive != null) positive.setAllCaps(false);
//                                        Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                                        if (negative != null) negative.setAllCaps(false);
//                                    }
//                                });

                                if (!isFinishing())
                                    _dialog.show();
                            }

                        }
                    });
                    //dialogBuilder.setNegativeButton(R.string.extender_dont_enable_notificaitons_button, null);

                    AlertDialog dialog = dialogBuilder.create();
                    dialog.setCancelable(false);
                    dialog.setCanceledOnTouchOutside(false);

                    if (!isFinishing())
                        dialog.show();
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_ACCESSIBILITY_SETTINGS)
            reloadActivity(this/*, true*/);
        if (requestCode == RESULT_PERMISSIONS_SETTINGS)
            reloadActivity(this/*, true*/);
        if (requestCode == RESULT_BATTERY_OPTIMIZATION_SETTINGS)
            reloadActivity(this/*, true*/);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // If request is cancelled, the result arrays are empty.
        if (requestCode == Permissions.PERMISSIONS_REQUEST_CODE) {
            boolean allGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_DENIED) {
                    allGranted = false;
                    break;
                }
            }
            if (!allGranted) {
                //if (!onlyNotification) {
                Context context = getApplicationContext();
                Toast msg = Toast.makeText(context,
                        context.getString(R.string.extender_app_name) + ": " +
                                context.getString(R.string.extender_toast_permissions_not_granted),
                        Toast.LENGTH_SHORT);
                msg.show();
                //}
            }
            reloadActivity(this/*, true*/);

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @SuppressLint("SetTextI18n")
    private void displayAccessibilityServiceStatus() {
        TextView text = findViewById(R.id.activity_main_accessibility_service_profile_force_stop_application);
        String str1 = getString(R.string.extender_accessibility_service_profile_force_stop_applications);
        /*String str2;
        if (PPPEAccessibilityService.isEnabled(getApplicationContext()))
            str2 = str1 + " [ " + getString(R.string.extender_accessibility_service_enabled) + " ]";
        else
            str2 = str1 + " [ " + getString(R.string.extender_accessibility_service_disabled) + " ]";
        Spannable sbt = new SpannableString(str2);
        sbt.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), str1.length()+1, str2.length(), 0);
        text.setText(sbt);*/
        text.setText(str1);

        text = findViewById(R.id.activity_main_accessibility_service_profile_lock_device);
        str1 = getString(R.string.extender_accessibility_service_profile_lock_device);
        /*if (PPPEAccessibilityService.isEnabled(getApplicationContext()))
            str2 = str1 + " [ " + getString(R.string.extender_accessibility_service_enabled) + " ]";
        else
            str2 = str1 + " [ " + getString(R.string.extender_accessibility_service_disabled) + " ]";
        sbt = new SpannableString(str2);
        sbt.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), str1.length()+1, str2.length(), 0);
        text.setText(sbt);*/
        text.setText(str1);

        text = findViewById(R.id.activity_main_accessibility_service_event_sensor_applications_orientation);
        str1 = getString(R.string.extender_accessibility_service_event_sensor_applications_orientation);
        /*if (PPPEAccessibilityService.isEnabled(getApplicationContext()))
            str2 = str1 + " [ " + getString(R.string.extender_accessibility_service_enabled) + " ]";
        else
            str2 = str1 + " [ " + getString(R.string.extender_accessibility_service_disabled) + " ]";
        sbt = new SpannableString(str2);
        sbt.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), str1.length()+1, str2.length(), 0);
        text.setText(sbt);*/
        text.setText(str1);

        text = findViewById(R.id.activity_main_accessibility_service_event_sensor_sms_mms);
        if (PPPEApplication.hasSystemFeature(getApplicationContext(), PackageManager.FEATURE_TELEPHONY)) {
            str1 = getString(R.string.extender_accessibility_service_event_sensor_sms_mms);
            /*if (PPPEAccessibilityService.isEnabled(getApplicationContext()))
                str2 = str1 + " [ " + getString(R.string.extender_accessibility_service_enabled) + " ]";
            else
                str2 = str1 + " [ " + getString(R.string.extender_accessibility_service_disabled) + " ]";
            sbt = new SpannableString(str2);
            sbt.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), str1.length()+1, str2.length(), 0);
            text.setText(sbt);*/
            text.setText(str1);
        } else
            text.setVisibility(View.GONE);

        text = findViewById(R.id.activity_main_accessibility_service_event_sensor_call);
        if (PPPEApplication.hasSystemFeature(getApplicationContext(), PackageManager.FEATURE_TELEPHONY)) {
            str1 = getString(R.string.extender_accessibility_service_event_sensor_call);
            /*if (PPPEAccessibilityService.isEnabled(getApplicationContext()))
                str2 = str1 + " [ " + getString(R.string.extender_accessibility_service_enabled) + " ]";
            else
                str2 = str1 + " [ " + getString(R.string.extender_accessibility_service_disabled) + " ]";
            sbt = new SpannableString(str2);
            sbt.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), str1.length()+1, str2.length(), 0);
            text.setText(sbt);*/
            text.setText(str1);
        } else
            text.setVisibility(View.GONE);

        text = findViewById(R.id.activity_main_accessibility_service_status);
        if (PPPEAccessibilityService.isEnabled(getApplicationContext()))
            text.setText("[ " + getString(R.string.extender_accessibility_service_enabled) + " ]");
        else
            text.setText("[ " + getString(R.string.extender_accessibility_service_disabled) + " ]");

        final Activity activity = this;
        Button accessibilityButton = findViewById(R.id.activity_main_accessibility_service_button);
        accessibilityButton.setOnClickListener(view -> {
            if (MainActivity.activityActionExists(Settings.ACTION_ACCESSIBILITY_SETTINGS, activity)) {
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                //noinspection deprecation
                startActivityForResult(intent, RESULT_ACCESSIBILITY_SETTINGS);
            } else {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                dialogBuilder.setMessage(R.string.extender_setting_screen_not_found_alert);
                //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                dialogBuilder.setPositiveButton(android.R.string.ok, null);
                dialogBuilder.show();
            }
        });
    }

    @SuppressLint({"SetTextI18n", "BatteryLife"})
    private void displayPermmisionsGrantStatus() {
        final Activity activity = this;

        TextView text;
        String str1;

        boolean displayPopupWindowsInBackground = false;
        if (Build.VERSION.SDK_INT >= 30) {
            if (PPPEApplication.deviceIsXiaomi) {
                displayPopupWindowsInBackground = true;
            }
        }
        text = findViewById(R.id.activity_main_permission_popup_windows_in_background);
        if (displayPopupWindowsInBackground) {
            str1 = getString(R.string.extender_permissions_popup_windows_in_background);
            text.setText(str1);
        } else {
            text.setVisibility(View.GONE);
        }

        if (PPPEApplication.hasSystemFeature(getApplicationContext(), PackageManager.FEATURE_TELEPHONY)) {
            text = findViewById(R.id.activity_main_permissions_event_sensor_sms_mms);
            str1 = getString(R.string.extender_permissions_event_sensor_sms_mms);
            /*if (Permissions.checkSMSMMSPermissions(activity))
                str2 = str1 + " [ " + getString(R.string.extender_permissions_granted) + " ]";
            else
                str2 = str1 + " [ " + getString(R.string.extender_permissions_not_granted) + " ]";
            sbt = new SpannableString(str2);
            sbt.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), str1.length() + 1, str2.length(), 0);
            text.setText(sbt);*/
            text.setText(str1);

            text = findViewById(R.id.activity_main_sms_permissions_status);
            if (Permissions.checkSMSMMSPermissions(activity))
                text.setText("[ " + getString(R.string.extender_permissions_granted) + " ]");
            else
                text.setText("[ " + getString(R.string.extender_permissions_not_granted) + " ]");
        }
        else {
            text = findViewById(R.id.activity_main_permissions_event_sensor_sms_mms);
            text.setVisibility(View.GONE);
            text = findViewById(R.id.activity_main_sms_permissions_status);
            text.setVisibility(View.GONE);
        }

        if (PPPEApplication.hasSystemFeature(getApplicationContext(), PackageManager.FEATURE_TELEPHONY)) {
            text = findViewById(R.id.activity_main_permissions_event_sensor_call);
            if (Build.VERSION.SDK_INT < 28)
                str1 = getString(R.string.extender_permissions_event_sensor_call);
            else
                str1 = getString(R.string.extender_permissions_event_sensor_call_28);
            /*if (Permissions.checkCallPermissions(activity))
                str2 = str1 + " [ " + getString(R.string.extender_permissions_granted) + " ]";
            else
                str2 = str1 + " [ " + getString(R.string.extender_permissions_not_granted) + " ]";
            sbt = new SpannableString(str2);
            sbt.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), str1.length() + 1, str2.length(), 0);
            text.setText(sbt);*/
            text.setText(str1);

            text = findViewById(R.id.activity_main_call_permissions_status);
            if (Permissions.checkCallPermissions(activity))
                text.setText("[ " + getString(R.string.extender_permissions_granted) + " ]");
            else
                text.setText("[ " + getString(R.string.extender_permissions_not_granted) + " ]");
        }
        else {
            text = findViewById(R.id.activity_main_permissions_event_sensor_call);
            text.setVisibility(View.GONE);
            text = findViewById(R.id.activity_main_call_permissions_status);
            text.setVisibility(View.GONE);
        }

        text = findViewById(R.id.activity_main_battery_optimization);
        str1 = getString(R.string.extender_battery_optimization_text);
        /*if (PPPEApplication.isIgnoreBatteryOptimizationEnabled(activity.getApplicationContext()))
            str2 = str1 + " [ " + getString(R.string.extender_battery_optimization_not_optimized) + " ]";
        else
            str2 = str1 + " [ " + getString(R.string.extender_battery_optimization_optimized) + " ]";
        sbt = new SpannableString(str2);
        sbt.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), str1.length() + 1, str2.length(), 0);
        text.setText(sbt);*/
        text.setText(str1);

        text = findViewById(R.id.activity_main_battery_optimization_status);
        if (PPPEApplication.isIgnoreBatteryOptimizationEnabled(activity.getApplicationContext()))
            text.setText("[ " + getString(R.string.extender_battery_optimization_not_optimized) + " ]");
        else
            text.setText("[ " + getString(R.string.extender_battery_optimization_optimized) + " ]");

        Button permissionsButton = findViewById(R.id.activity_main_sms_permissions_button);
        if (PPPEApplication.hasSystemFeature(getApplicationContext(), PackageManager.FEATURE_TELEPHONY)) {
            permissionsButton.setOnClickListener(view -> {
                if (Permissions.checkSMSMMSPermissions(activity)) {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    //intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    if (MainActivity.activityIntentExists(intent, activity)) {
                        //noinspection deprecation
                        startActivityForResult(intent, RESULT_PERMISSIONS_SETTINGS);
                    } else {
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                        dialogBuilder.setMessage(R.string.extender_setting_screen_not_found_alert);
                        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                        dialogBuilder.setPositiveButton(android.R.string.ok, null);
                        dialogBuilder.show();
                    }
                } else
                    Permissions.grantSMSMMSPermissions(activity);
            });
        }
        else
            permissionsButton.setVisibility(View.GONE);

        permissionsButton = findViewById(R.id.activity_main_call_permissions_button);
        if (PPPEApplication.hasSystemFeature(getApplicationContext(), PackageManager.FEATURE_TELEPHONY)) {
            permissionsButton.setOnClickListener(view -> {
                if (Permissions.checkCallPermissions(activity)) {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    //intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    if (MainActivity.activityIntentExists(intent, activity)) {
                        //noinspection deprecation
                        startActivityForResult(intent, RESULT_PERMISSIONS_SETTINGS);
                    } else {
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                        dialogBuilder.setMessage(R.string.extender_setting_screen_not_found_alert);
                        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                        dialogBuilder.setPositiveButton(android.R.string.ok, null);
                        dialogBuilder.show();
                    }
                } else
                    Permissions.grantCallPermissions(activity);
            });
        }
        else
            permissionsButton.setVisibility(View.GONE);

        Button batteryOptimizationButton = findViewById(R.id.activity_main_battery_optimization_button);
        batteryOptimizationButton.setOnClickListener(view -> {

            String packageName = PPPEApplication.PACKAGE_NAME;
            Intent intent;

            PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
            if (pm.isIgnoringBatteryOptimizations(packageName)) {
                intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            }
            else {
                //    DO NOT USE IT, CHANGE IS NOT DISPLAYED IN SYSTEM SETTINGS
                //    But in ONEPLUS it IS ONLY SOLUTION !!!
                intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
            }
            //intent.addCategory(Intent.CATEGORY_DEFAULT);
            if (MainActivity.activityIntentExists(intent, activity)) {
                //noinspection deprecation
                startActivityForResult(intent, RESULT_BATTERY_OPTIMIZATION_SETTINGS);
            } else {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                dialogBuilder.setMessage(R.string.extender_setting_screen_not_found_alert);
                //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                dialogBuilder.setPositiveButton(android.R.string.ok, null);
                dialogBuilder.show();
            }
        });

        Button popupWindowsInBackgroundButton = findViewById(R.id.activity_main_popup_windows_in_background_button);
        if (displayPopupWindowsInBackground) {
            popupWindowsInBackgroundButton.setOnClickListener(view -> {
                Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                intent.setClassName("com.miui.securitycenter",
                        "com.miui.permcenter.permissions.PermissionsEditorActivity");
                intent.putExtra("extra_pkgname", PPPEApplication.PACKAGE_NAME);
                //intent.addCategory(Intent.CATEGORY_DEFAULT);
                if (MainActivity.activityIntentExists(intent, activity)) {
                    //noinspection deprecation
                    startActivity(intent);
                } else {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                    dialogBuilder.setMessage(R.string.extender_setting_screen_not_found_alert);
                    //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                    dialogBuilder.setPositiveButton(android.R.string.ok, null);
                    dialogBuilder.show();
                }
            });
        }
        else
            popupWindowsInBackgroundButton.setVisibility(View.GONE);
    }

    private static boolean activityActionExists(@SuppressWarnings("SameParameterValue") String action,
                                                Context context) {
        try {
            final Intent intent = new Intent(action);
            List<ResolveInfo> activities = context.getApplicationContext().getPackageManager().queryIntentActivities(intent, 0);
            return activities.size() > 0;
        } catch (Exception e) {
            //Log.e("MainActivity.activityActionExists", Log.getStackTraceString(e));
            //PPPEApplication.recordException(e);
            return false;
        }
    }

    private static boolean activityIntentExists(Intent intent, Context context) {
        try {
            List<ResolveInfo> activities = context.getApplicationContext().getPackageManager().queryIntentActivities(intent, 0);
            return activities.size() > 0;
        } catch (Exception e) {
            //Log.e("MainActivity.activityIntentExists", Log.getStackTraceString(e));
            //PPPEApplication.recordException(e);
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
