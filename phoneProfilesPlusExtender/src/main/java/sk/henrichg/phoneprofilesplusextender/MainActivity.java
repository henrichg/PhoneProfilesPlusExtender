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
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.view.Menu;
import android.view.MenuItem;
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

    int selectedLanguage = 0;
    String defaultLanguage = "";
    String defaultCountry = "";
    String defaultScript = "";

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
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);

        if (Build.VERSION.SDK_INT >= 28) {
            menu.setGroupDividerEnabled(true);
        }

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean ret = super.onPrepareOptionsMenu(menu);

        MenuItem menuItem;

        menuItem = menu.findItem(R.id.menu_debug);
        if (menuItem != null) {
            menuItem.setVisible(DebugVersion.enabled);
            menuItem.setEnabled(DebugVersion.enabled);
        }

        return ret;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        /*if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        else*/
        if (itemId == R.id.menu_choose_language) {
            ChooseLanguageDialog chooseLanguageDialog = new ChooseLanguageDialog(this);
            chooseLanguageDialog.show();
            return true;

            /*
            String storedLanguage = LocaleHelper.getLanguage(getApplicationContext());
            String storedCountry = LocaleHelper.getCountry(getApplicationContext());
            String storedScript = LocaleHelper.getScript(getApplicationContext());
//            Log.e("MainActivity.onOptionsItemSelected", "storedLanguage="+storedLanguage);
//            Log.e("MainActivity.onOptionsItemSelected", "storedCountry="+storedCountry);
//            Log.e("MainActivity.onOptionsItemSelected", "storedScript="+storedScript);

            final String[] languageValues = getResources().getStringArray(R.array.chooseLanguageValues);
            ArrayList<Language> languages = new ArrayList<>();

            for (String languageValue : languageValues) {
                Language language = new Language();
                if (languageValue.equals("[sys]")) {
                    language.language = languageValue;
                    language.country = "";
                    language.script = "";
                    language.name = getString(R.string.extender_menu_choose_language_system_language);
                } else {
                    String[] splits = languageValue.split("-");
                    String sLanguage = splits[0];
                    String country = "";
                    if (splits.length >= 2)
                        country = splits[1];
                    String script = "";
                    if (splits.length >= 3)
                        script = splits[2];

                    Locale loc = null;
                    if (country.isEmpty() && script.isEmpty())
                        loc = new Locale.Builder().setLanguage(sLanguage).build();
                    if (!country.isEmpty() && script.isEmpty())
                        loc = new Locale.Builder().setLanguage(sLanguage).setRegion(country).build();
                    if (country.isEmpty() && !script.isEmpty())
                        loc = new Locale.Builder().setLanguage(sLanguage).setScript(script).build();
                    if (!country.isEmpty() && !script.isEmpty())
                        loc = new Locale.Builder().setLanguage(sLanguage).setRegion(country).setScript(script).build();

                    language.language = sLanguage;
                    language.country = country;
                    language.script = script;
                    language.name = loc.getDisplayName(loc);
                    language.name = language.name.substring(0, 1).toUpperCase(loc) + language.name.substring(1);
                }
                languages.add(language);
            }

            languages.sort(new LanguagesComparator());

            final String[] languageNameChoices = new String[languages.size()];
            for(int i = 0; i < languages.size(); i++) languageNameChoices[i] = languages.get(i).name;

            for (int i = 0; i < languages.size(); i++) {
                Language language = languages.get(i);
                String sLanguage = language.language;
                String country = language.country;
                String script = language.script;

                if (sLanguage.equals(storedLanguage) &&
                        storedCountry.isEmpty() &&
                        storedScript.isEmpty()) {
                    selectedLanguage = i;
                    break;
                }
                if (sLanguage.equals(storedLanguage) &&
                        country.equals(storedCountry) &&
                        storedScript.isEmpty()) {
                    selectedLanguage = i;
                    break;
                }
                if (sLanguage.equals(storedLanguage) &&
                        storedCountry.isEmpty() &&
                        script.equals(storedScript)) {
                    selectedLanguage = i;
                    break;
                }
                if (sLanguage.equals(storedLanguage) &&
                        country.equals(storedCountry) &&
                        script.equals(storedScript)) {
                    selectedLanguage = i;
                    break;
                }
            }

            //Log.e("MainActivity.onOptionsItemSelected", "defualt language="+Locale.getDefault().getDisplayLanguage());
            // this is list of locales by order in system settings. Index 0 = default locale in system
            //LocaleListCompat locales = ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration());
            //for (int i = 0; i < locales.size(); i++) {
            //    Log.e("MainActivity.onOptionsItemSelected", "language="+locales.get(i).getDisplayLanguage());
            //}

            AlertDialog chooseLanguageDialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.extender_menu_choose_language)
                    .setCancelable(true)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setSingleChoiceItems(languageNameChoices, selectedLanguage, (dialog, which) -> {
                        selectedLanguage = which;

                        Language language = languages.get(selectedLanguage);
                        defaultLanguage = language.language;
                        defaultCountry = language.country;
                        defaultScript = language.script;

//                        Log.e("MainActivity.onOptionsItemSelected", "defaultLanguage="+defaultLanguage);
//                        Log.e("MainActivity.onOptionsItemSelected", "defaultCountry="+defaultCountry);
//                        Log.e("MainActivity.onOptionsItemSelected", "defaultScript="+defaultScript);

                        LocaleHelper.setLocale(getApplicationContext(),
                                defaultLanguage, defaultCountry, defaultScript, true);

                        reloadActivity(this, false);
                        dialog.dismiss();

                        LocaleHelper.setApplicationLocale(getApplicationContext());
                    })
                    .create();

//                    dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                        @Override
//                        public void onShow(DialogInterface dialog) {
//                            Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                            if (positive != null) positive.setAllCaps(false);
//                            Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                            if (negative != null) negative.setAllCaps(false);
//                        }
//                    });

            chooseLanguageDialog.show();

            return true;
            */
        }
        else
        if (DebugVersion.debugMenuItems(itemId, this))
            return true;
        else
            return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        Permissions.grantNotificationsPermission(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_ACCESSIBILITY_SETTINGS)
            reloadActivity(this, false);
        if (requestCode == RESULT_PERMISSIONS_SETTINGS)
            reloadActivity(this, false);
        if (requestCode == RESULT_BATTERY_OPTIMIZATION_SETTINGS)
            reloadActivity(this, false);
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
                PPPEApplication.showToast(getApplicationContext(),
                        getString(R.string.extender_app_name) + ": " +
                                getString(R.string.extender_toast_permissions_not_granted),
                        Toast.LENGTH_SHORT);
                //}
            }
            reloadActivity(this, false);

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

        text = findViewById(R.id.activity_main_call_permissions_status);
        if (Permissions.checkCallPermissions(activity))
            text.setText("[ " + getString(R.string.extender_permissions_granted) + " ]");
        else
            text.setText("[ " + getString(R.string.extender_permissions_not_granted) + " ]");

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

    static boolean activityIntentExists(Intent intent, Context context) {
        try {
            List<ResolveInfo> activities = context.getApplicationContext().getPackageManager().queryIntentActivities(intent, 0);
            return activities.size() > 0;
        } catch (Exception e) {
            //Log.e("MainActivity.activityIntentExists", Log.getStackTraceString(e));
            //PPPEApplication.recordException(e);
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

    /*
    private static class Language {
        String language;
        String country;
        String script;
        String name;
    }

    private static class LanguagesComparator implements Comparator<Language> {

        public int compare(Language lhs, Language rhs) {
            return PPPEApplication.collator.compare(lhs.name, rhs.name);
        }
    }
    */

}
