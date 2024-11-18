package sk.henrichg.phoneprofilesplusextender;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.content.pm.PackageInfoCompat;
import androidx.core.view.MenuCompat;

public class MainActivity extends AppCompatActivity
                                implements RefreshGUIMainActivityListener
{

    private static final int RESULT_ACCESSIBILITY_SETTINGS = 1900;
    static final int RESULT_PERMISSIONS_SETTINGS = 1901;
    private static final int RESULT_BATTERY_OPTIMIZATION_SETTINGS = 1902;

    int selectedLanguage = 0;
    String defaultLanguage = "";
    String defaultCountry = "";
    String defaultScript = "";

    private int scrollTo = 0;

    @Override
    public void refreshGUIFromListener() {
        //PPPEApplication.logE("MainActivity.refreshGUIBroadcastReceiver", "xxx (2)");
        displayAccessibilityServiceStatus();
    }

    static private class RefreshGUIBroadcastReceiver extends BroadcastReceiver {

        private final RefreshGUIMainActivityListener listener;

        public RefreshGUIBroadcastReceiver(RefreshGUIMainActivityListener listener){
            this.listener = listener;
        }

        @Override
        public void onReceive( Context context, Intent intent ) {
            //PPPEApplication.logE("MainActivity.refreshGUIBroadcastReceiver", "xxx (1)");
//            PPPEApplicationStatic.logE("[MEMORY_LEAK] MainActivity.refreshGUIBroadcastReceiver.onReceive", "xxxxxx");
            listener.refreshGUIFromListener();
        }
    }
    private RefreshGUIBroadcastReceiver refreshGUIBroadcastReceiver;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        int miuiVersion = -1;
//        if (PPPEApplication.deviceIsXiaomi && PPPEApplication.romIsMIUI) {
//            String[] splits = Build.VERSION.INCREMENTAL.split("\\.");
//            miuiVersion = Integer.parseInt(splits[0].substring(1));
//        }

        //if (PPPEApplication.deviceIsOnePlus)
        //    setTheme(R.style.AppTheme_noRipple);
        //else
        //if (PPPEApplication.deviceIsXiaomi && PPPEApplication.romIsMIUI && miuiVersion >= 14)
        //    setTheme(R.style.AppTheme_noRipple);
        //else
            setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);

//        PPPEApplicationStatic.logE("[MEMORY_LEAK] MainActivity.onCreate", "xxxxxx");

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        setContentView(R.layout.activity_main);

        //PPPEApplication.logE("MainActivity.onCreated", "xxx");

        if (getSupportActionBar() != null) {
            //getSupportActionBar().setHomeButtonEnabled(false);
            //getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle(R.string.extender_app_name);
            getSupportActionBar().setElevation(0);
        }

        Intent intent = getIntent();
        scrollTo = intent.getIntExtra(PPPEApplication.EXTRA_SCROLL_TO, 0);

        TextView text = findViewById(R.id.activity_main_application_version);
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            //noinspection DataFlowIssue
            text.setText(getString(R.string.extender_about_application_version) + " " + pInfo.versionName +
                                        " (" + PackageInfoCompat.getLongVersionCode(pInfo) + ")");
        } catch (Exception e) {
            //noinspection DataFlowIssue
            text.setText("");
        }

        text = findViewById(R.id.activity_main_application_releases);
        String str1 = getString(R.string.extender_application_releases);
        String str2 = str1 + " https://github.com/henrichg/PhoneProfilesPlusExtender/releases" + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW;
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
        //noinspection DataFlowIssue
        text.setText(sbt);
        text.setMovementMethod(LinkMovementMethod.getInstance());

        displayAccessibilityServiceStatus();
        displayPermmisionsGrantStatus();

        refreshGUIBroadcastReceiver = new RefreshGUIBroadcastReceiver(this);
        int receiverFlags = 0;
        if (Build.VERSION.SDK_INT >= 34)
            receiverFlags = RECEIVER_NOT_EXPORTED;
        getApplicationContext().registerReceiver(refreshGUIBroadcastReceiver,
                new IntentFilter(PPPEAccessibilityService.ACTION_REFRESH_GUI_BROADCAST_RECEIVER), receiverFlags);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        if (DebugVersion.enabled)
            getMenuInflater().inflate(R.menu.main_menu_debug, menu);

        MenuCompat.setGroupDividerEnabled(menu, true);

        return true;
    }

    @SuppressLint("AlwaysShowAction")
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean ret = super.onPrepareOptionsMenu(menu);

        MenuItem menuItem;

        menuItem = menu.findItem(R.id.menu_support);
        if (menuItem != null)
        {
            SubMenu subMenu = menuItem.getSubMenu();
            if (subMenu != null) {
                Drawable triangle = ContextCompat.getDrawable(this, R.drawable.ic_submenu_triangle);
                if (triangle != null) {
                    triangle.setTint(ContextCompat.getColor(this, R.color.activitySecondaryTextColor));
                    SpannableString headerTitle = new SpannableString("    " + menuItem.getTitle());
                    triangle.setBounds(0,
                            GlobalUtils.sip(1),
                            GlobalUtils.sip(10.5f),
                            GlobalUtils.sip(8.5f));
                    headerTitle.setSpan(new ImageSpan(triangle, ImageSpan.ALIGN_BASELINE), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    //headerTitle.setSpan(new ImageSpan(this, R.drawable.ic_submenu_triangle, DynamicDrawableSpan.ALIGN_BASELINE), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    subMenu.setHeaderTitle(headerTitle);
                }
            }
        }

        if (DebugVersion.enabled) {
            menuItem = menu.findItem(R.id.menu_debug);
            if (menuItem != null) {
                SubMenu subMenu = menuItem.getSubMenu();
                if (subMenu != null) {
                    Drawable triangle = ContextCompat.getDrawable(this, R.drawable.ic_submenu_triangle);
                    if (triangle != null) {
                        triangle.setTint(ContextCompat.getColor(this, R.color.activitySecondaryTextColor));
                        SpannableString headerTitle = new SpannableString("    " + menuItem.getTitle());
                        triangle.setBounds(0,
                                GlobalUtils.sip(1),
                                GlobalUtils.sip(10.5f),
                                GlobalUtils.sip(8.5f));
                        headerTitle.setSpan(new ImageSpan(triangle, ImageSpan.ALIGN_BASELINE), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        //headerTitle.setSpan(new ImageSpan(this, R.drawable.ic_submenu_triangle, DynamicDrawableSpan.ALIGN_BASELINE), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        subMenu.setHeaderTitle(headerTitle);
                    }
                }
            }
        }
        menuItem = menu.findItem(R.id.menu_discord);
        if (menuItem != null)
        {
            SubMenu subMenu = menuItem.getSubMenu();
            if (subMenu != null) {
                Drawable triangle = ContextCompat.getDrawable(this, R.drawable.ic_submenu_triangle);
                if (triangle != null) {
                    triangle.setTint(ContextCompat.getColor(this, R.color.activitySecondaryTextColor));
                    SpannableString headerTitle = new SpannableString("    " + menuItem.getTitle());
                    triangle.setBounds(0,
                            GlobalUtils.sip(1),
                            GlobalUtils.sip(10.5f),
                            GlobalUtils.sip(8.5f));
                    headerTitle.setSpan(new ImageSpan(triangle, ImageSpan.ALIGN_BASELINE), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    //headerTitle.setSpan(new ImageSpan(this, R.drawable.ic_submenu_triangle, DynamicDrawableSpan.ALIGN_BASELINE), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    subMenu.setHeaderTitle(headerTitle);
                }
            }
        }

        //onNextLayout(editorToolbar, this::showTargetHelps);

        return ret;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

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
        }
        else
        if (itemId == R.id.menu_email_to_author) {
            intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse(StringConstants.INTENT_DATA_MAIL_TO_COLON)); // only email apps should handle this
            String[] email = {StringConstants.AUTHOR_EMAIL};
            intent.putExtra(Intent.EXTRA_EMAIL, email);
            String packageVersion = "";
            try {
                PackageInfo pInfo = getPackageManager().getPackageInfo(PPPEApplication.PACKAGE_NAME, 0);
                packageVersion = " - v" + pInfo.versionName + " (" + PPPEApplicationStatic.getVersionCode(pInfo) + ")";
            } catch (Exception e) {
                PPPEApplicationStatic.recordException(e);
            }
            intent.putExtra(Intent.EXTRA_SUBJECT, StringConstants.PHONE_PROFILES_PLUS_EXTENDER + packageVersion + " - " + getString(R.string.extender_support_subject));
            intent.putExtra(Intent.EXTRA_TEXT, getEmailBodyText());
            try {
                startActivity(Intent.createChooser(intent, getString(R.string.extender_email_chooser)));
            } catch (Exception e) {
                PPPEApplicationStatic.recordException(e);
            }

            return true;
        }
        else
        if (itemId == R.id.menu_xda_developers) {
            String url = PPPEApplication.XDA_DEVELOPERS_PPP_URL;
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            try {
                startActivity(Intent.createChooser(intent, getString(R.string.extender_web_browser_chooser)));
            } catch (Exception e) {
                PPPEApplicationStatic.recordException(e);
            }
            return true;
        }
        else
        if (itemId == R.id.menu_discord_server) {
            String url = PPPEApplication.DISCORD_SERVER_URL;
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            try {
                startActivity(Intent.createChooser(intent, getString(R.string.extender_web_browser_chooser)));
            } catch (Exception e) {
                PPPEApplicationStatic.recordException(e);
            }
            return true;
        }
        else
        if (itemId == R.id.menu_discord_invitation) {
            String url = PPPEApplication.DISCORD_INVITATION_URL;
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            try {
                startActivity(Intent.createChooser(intent, getString(R.string.extender_web_browser_chooser)));
            } catch (Exception e) {
                PPPEApplicationStatic.recordException(e);
            }
            return true;
        }
//        else
//        if (itemId == R.id.menu_twitter) {
//            String url = PPPEApplication.TWITTER_URL;
//            intent = new Intent(Intent.ACTION_VIEW);
//            intent.setData(Uri.parse(url));
//            try {
//                startActivity(Intent.createChooser(intent, getString(R.string.extender_web_browser_chooser)));
//            } catch (Exception e) {
//                PPPEApplicationStatic.recordException(e);
//            }
//            return true;
//        }
        else
        if (itemId == R.id.menu_reddit) {
            String url = PPPEApplication.REDDIT_URL;
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            try {
                startActivity(Intent.createChooser(intent, getString(R.string.extender_web_browser_chooser)));
            } catch (Exception e) {
                PPPEApplicationStatic.recordException(e);
            }
            return true;
        }
        else
        if (itemId == R.id.menu_bluesky) {
            String url = PPPEApplication.BLUESKY_URL;
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            try {
                startActivity(Intent.createChooser(intent, getString(R.string.extender_web_browser_chooser)));
            } catch (Exception e) {
                PPPEApplicationStatic.recordException(e);
            }
            return true;
        }
        else
        if (itemId == R.id.menu_mastodon) {
            String url = PPPEApplication.MASTODON_URL;
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            try {
                startActivity(Intent.createChooser(intent, getString(R.string.extender_web_browser_chooser)));
            } catch (Exception e) {
                PPPEApplicationStatic.recordException(e);
            }
            return true;
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

//        PPPEApplicationStatic.logE("[MEMORY_LEAK] MainActivity.onStart", "xxxxxx");

        Permissions.grantNotificationsPermission(this);

        if (scrollTo != 0) {
            final ScrollView scrollView = findViewById(R.id.activity_main_scroll_view);
            final View viewToScroll = findViewById(scrollTo);
            if (viewToScroll != null) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
//                        PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=ImportantInfoHelpFragment.onViewCreated (2)");
                    //noinspection DataFlowIssue
                    scrollView.scrollTo(0, viewToScroll.getTop());
                }, 200);

                scrollTo = 0;
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        PPPEApplicationStatic.logE("[MEMORY_LEAK] MainActivity.onDestroy", "xxxxxxxxx");

        try {
            getApplicationContext().unregisterReceiver(refreshGUIBroadcastReceiver);
//            PPPEApplicationStatic.logE("[MEMORY_LEAK] MainActivity.onDestroy", "unregister refreshGUIBroadcastReceiver");
        } catch (Exception ignored) {}
        refreshGUIBroadcastReceiver = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
//        PPPEApplicationStatic.logE("[MEMORY_LEAK] MainActivity.onActivityResult", "xxxxxxxxx");

        if (requestCode == RESULT_ACCESSIBILITY_SETTINGS)
            GlobalUtils.reloadActivity(this, false);
        if (requestCode == RESULT_PERMISSIONS_SETTINGS)
            GlobalUtils.reloadActivity(this, false);
        if (requestCode == RESULT_BATTERY_OPTIMIZATION_SETTINGS)
            GlobalUtils.reloadActivity(this, false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        PPPEApplicationStatic.logE("[MEMORY_LEAK] MainActivity.onRequestPermissionsResult", "xxxxxxxxx");

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
                PPPEApplicationStatic.showToast(getApplicationContext(),
                        getString(R.string.extender_app_name) + StringConstants.STR_COLON_WITH_SPACE +
                                getString(R.string.extender_toast_permissions_not_granted),
                        Toast.LENGTH_SHORT);
                //}
            }
            GlobalUtils.reloadActivity(this, false);

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @SuppressLint("SetTextI18n")
    private void displayAccessibilityServiceStatus() {
        TextView text = findViewById(R.id.activity_main_accessibility_service_profile_force_stop_application);
        String str1 = StringConstants.TAG_LIST_START_FIRST_ITEM_HTML + getString(R.string.extender_accessibility_service_profile_force_stop_applications) + StringConstants.TAG_LIST_END_LAST_ITEM_HTML;
        //noinspection DataFlowIssue
        text.setText(StringFormatUtils.fromHtml(str1, true, false, false, 0, 0, true));

        text = findViewById(R.id.activity_main_accessibility_service_profile_lock_device);
        str1 = StringConstants.TAG_LIST_START_FIRST_ITEM_HTML +getString(R.string.extender_accessibility_service_profile_lock_device) + StringConstants.TAG_LIST_END_LAST_ITEM_HTML;
        //noinspection DataFlowIssue
        text.setText(StringFormatUtils.fromHtml(str1, true, false, false, 0, 0, true));

        text = findViewById(R.id.activity_main_accessibility_service_event_sensor_applications_orientation);
        str1 = StringConstants.TAG_LIST_START_FIRST_ITEM_HTML +getString(R.string.extender_accessibility_service_event_sensor_applications_orientation) + StringConstants.TAG_LIST_END_LAST_ITEM_HTML;
        //noinspection DataFlowIssue
        text.setText(StringFormatUtils.fromHtml(str1, true, false, false, 0, 0, true));

        text = findViewById(R.id.activity_main_accessibility_service_event_sensor_sms_mms);
        if (PPPEApplication.HAS_FEATURE_TELEPHONY) {
            str1 = StringConstants.TAG_LIST_START_FIRST_ITEM_HTML +getString(R.string.extender_accessibility_service_event_sensor_sms_mms) + StringConstants.TAG_LIST_END_LAST_ITEM_HTML;
            //noinspection DataFlowIssue
            text.setText(StringFormatUtils.fromHtml(str1, true, false, false, 0, 0, true));
        } else
            //noinspection DataFlowIssue
            text.setVisibility(View.GONE);

        text = findViewById(R.id.activity_main_accessibility_service_event_sensor_call);
        if (PPPEApplication.HAS_FEATURE_TELEPHONY) {
            str1 = StringConstants.TAG_LIST_START_FIRST_ITEM_HTML +getString(R.string.extender_accessibility_service_event_sensor_call) + StringConstants.TAG_LIST_END_LAST_ITEM_HTML;
            //noinspection DataFlowIssue
            text.setText(StringFormatUtils.fromHtml(str1, true, false, false, 0, 0, true));
        } else
            //noinspection DataFlowIssue
            text.setVisibility(View.GONE);

        text = findViewById(R.id.activity_main_accessibility_service_status);
        if (PPPEAccessibilityService.isEnabled(getApplicationContext()))
            //noinspection DataFlowIssue
            text.setText("[ " + getString(R.string.extender_accessibility_service_enabled) + " ]");
        else
            //noinspection DataFlowIssue
            text.setText("[ " + getString(R.string.extender_accessibility_service_disabled) + " ]");

        final Activity activity = this;
        Button accessibilityButton = findViewById(R.id.activity_main_accessibility_service_button);
        //noinspection DataFlowIssue
        accessibilityButton.setOnClickListener(view -> {
            if (GlobalUtils.activityActionExists(Settings.ACTION_ACCESSIBILITY_SETTINGS, activity)) {
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

        if (Build.VERSION.SDK_INT >= 33) {
            text = findViewById(R.id.activity_main_accessibility_service_app_info);
            TextView text2 = findViewById(R.id.activity_main_accessibility_service_app_info_2);
            Button appInfoButton = findViewById(R.id.activity_main_accessibility_service_app_info_button);
            View divider = findViewById(R.id.activity_main_divider_app_info);
            if (!PPPEAccessibilityService.isEnabled(getApplicationContext())) {
                str1 = StringConstants.TAG_LIST_START_FIRST_ITEM_HTML + getString(R.string.extender_accessibility_service_disabled_app_info_1) + StringConstants.TAG_DOUBLE_BREAK_HTML;
                str1 = str1 + getString(R.string.extender_accessibility_service_disabled_app_info_2) + StringConstants.TAG_BREAK_HTML;
                str1 = str1 + getString(R.string.extender_accessibility_service_disabled_app_info_3) + StringConstants.TAG_BREAK_HTML;
                str1 = str1 + getString(R.string.extender_accessibility_service_disabled_app_info_4) + StringConstants.TAG_DOUBLE_BREAK_HTML;
                str1 = str1 + StringConstants.TAG_BOLD_START_HTML + getString(R.string.extender_accessibility_service_disabled_app_info_5) + StringConstants.TAG_BREAK_HTML;
                str1 = str1 + getString(R.string.extender_accessibility_service_disabled_app_info_6) + StringConstants.TAG_BOLD_END_HTML;
                str1 = str1 + StringConstants.TAG_LIST_END_LAST_ITEM_HTML;
                //noinspection DataFlowIssue
                text.setText(StringFormatUtils.fromHtml(str1, true, false, false, 0, 0, true));
                text.setVisibility(View.VISIBLE);

                str1 = getString(R.string.extender_accessibility_service_disabled_app_info_7);
                String str2 = str1 + " https://apt.izzysoft.de/fdroid/index/apk/com.looker.droidify" + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW;
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
                        String url = "https://apt.izzysoft.de/fdroid/index/apk/com.looker.droidify";
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        try {
                            startActivity(Intent.createChooser(i, getString(R.string.extender_web_browser_chooser)));
                        } catch (Exception ignored) {}
                    }
                };
                sbt.setSpan(clickableSpan, str1.length()+1, str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                //sbt.setSpan(new UnderlineSpan(), str1.length()+1, str2.length(), 0);
                //noinspection DataFlowIssue
                text2.setText(sbt);
                text2.setMovementMethod(LinkMovementMethod.getInstance());
                text2.setVisibility(View.VISIBLE);
                //noinspection DataFlowIssue
                divider.setVisibility(View.VISIBLE);
                //noinspection DataFlowIssue
                appInfoButton.setVisibility(View.VISIBLE);
                appInfoButton.setOnClickListener(view -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    //intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.setData(Uri.parse(PPPEApplication.INTENT_DATA_PACKAGE+PPPEApplication.PACKAGE_NAME));
                    if (GlobalUtils.activityIntentExists(intent, activity)) {
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
            else {
                //noinspection DataFlowIssue
                text.setVisibility(View.GONE);
                //noinspection DataFlowIssue
                text2.setVisibility(View.GONE);
                //noinspection DataFlowIssue
                divider.setVisibility(View.GONE);
                //noinspection DataFlowIssue
                appInfoButton.setVisibility(View.GONE);
            }
        }
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
        View divider = findViewById(R.id.activity_main_divider_popup_window);
        if (displayPopupWindowsInBackground) {
            str1 = getString(R.string.extender_permissions_popup_windows_in_background);
            //noinspection DataFlowIssue
            text.setText(str1);
            text.setVisibility(View.VISIBLE);
            //noinspection DataFlowIssue
            divider.setVisibility(View.VISIBLE);
        } else {
            //noinspection DataFlowIssue
            text.setVisibility(View.GONE);
            //noinspection DataFlowIssue
            divider.setVisibility(View.GONE);
        }

        if (PPPEApplication.HAS_FEATURE_TELEPHONY) {
            text = findViewById(R.id.activity_main_permissions_event_sensor_sms_mms);
            str1 = StringConstants.TAG_LIST_START_FIRST_ITEM_HTML +getString(R.string.extender_permissions_event_sensor_sms_mms) + StringConstants.TAG_LIST_END_LAST_ITEM_HTML;
            //noinspection DataFlowIssue
            text.setText(StringFormatUtils.fromHtml(str1, true, false, false, 0, 0, true));

            text = findViewById(R.id.activity_main_sms_permissions_status);
            if (Permissions.checkSMSMMSPermissions(activity)) {
                //noinspection DataFlowIssue
                text.setTextColor(ContextCompat.getColor(this, R.color.activityNormalTextColor));
                text.setText("[ " + getString(R.string.extender_permissions_granted) + " ]");
            }
            else {
                if (scrollTo == R.id.activity_main_permissions_event_sensor_sms_mms)
                    //noinspection DataFlowIssue
                    text.setTextColor(ContextCompat.getColor(this, R.color.errorColor));
                else
                    //noinspection DataFlowIssue
                    text.setTextColor(ContextCompat.getColor(this, R.color.activityNormalTextColor));
                text.setText("[ " + getString(R.string.extender_permissions_not_granted) + " ]");
            }
        }
        else {
            text = findViewById(R.id.activity_main_permissions_event_sensor_sms_mms);
            //noinspection DataFlowIssue
            text.setVisibility(View.GONE);
            text = findViewById(R.id.activity_main_sms_permissions_status);
            //noinspection DataFlowIssue
            text.setVisibility(View.GONE);
        }

        if (PPPEApplication.HAS_FEATURE_TELEPHONY) {
            text = findViewById(R.id.activity_main_permissions_event_sensor_call);
            if (Build.VERSION.SDK_INT < 28)
                str1 = StringConstants.TAG_LIST_START_FIRST_ITEM_HTML +getString(R.string.extender_permissions_event_sensor_call) + StringConstants.TAG_LIST_END_LAST_ITEM_HTML;
            else
                str1 = StringConstants.TAG_LIST_START_FIRST_ITEM_HTML +getString(R.string.extender_permissions_event_sensor_call_28) + StringConstants.TAG_LIST_END_LAST_ITEM_HTML;
            //noinspection DataFlowIssue
            text.setText(StringFormatUtils.fromHtml(str1, true, false, false, 0, 0, true));

            text = findViewById(R.id.activity_main_call_permissions_status);
            if (Permissions.checkCallPermissions(activity)) {
                //noinspection DataFlowIssue
                text.setTextColor(ContextCompat.getColor(this, R.color.activityNormalTextColor));
                text.setText("[ " + getString(R.string.extender_permissions_granted) + " ]");
            }
            else {
                if (scrollTo == R.id.activity_main_permissions_event_sensor_call)
                    //noinspection DataFlowIssue
                    text.setTextColor(ContextCompat.getColor(this, R.color.errorColor));
                else
                    //noinspection DataFlowIssue
                    text.setTextColor(ContextCompat.getColor(this, R.color.activityNormalTextColor));
                text.setText("[ " + getString(R.string.extender_permissions_not_granted) + " ]");
            }
        }
        else {
            text = findViewById(R.id.activity_main_permissions_event_sensor_call);
            //noinspection DataFlowIssue
            text.setVisibility(View.GONE);
            text = findViewById(R.id.activity_main_call_permissions_status);
            //noinspection DataFlowIssue
            text.setVisibility(View.GONE);
        }

        text = findViewById(R.id.activity_main_call_permissions_status);
        if (Permissions.checkCallPermissions(activity))
            //noinspection DataFlowIssue
            text.setText("[ " + getString(R.string.extender_permissions_granted) + " ]");
        else
            //noinspection DataFlowIssue
            text.setText("[ " + getString(R.string.extender_permissions_not_granted) + " ]");

        text = findViewById(R.id.activity_main_battery_optimization);
        str1 = StringConstants.TAG_LIST_START_FIRST_ITEM_HTML +getString(R.string.extender_battery_optimization_text) + StringConstants.TAG_LIST_END_LAST_ITEM_HTML;
        //noinspection DataFlowIssue
        text.setText(StringFormatUtils.fromHtml(str1, true, false, false, 0, 0, true));

        text = findViewById(R.id.activity_main_battery_optimization_status);
        if (PPPEApplicationStatic.isIgnoreBatteryOptimizationEnabled(activity.getApplicationContext()))
            //noinspection DataFlowIssue
            text.setText("[ " + getString(R.string.extender_battery_optimization_not_optimized) + " ]");
        else
            //noinspection DataFlowIssue
            text.setText("[ " + getString(R.string.extender_battery_optimization_optimized) + " ]");

        Button permissionsButton = findViewById(R.id.activity_main_sms_permissions_button);
        if (PPPEApplication.HAS_FEATURE_TELEPHONY) {
            //noinspection DataFlowIssue
            permissionsButton.setOnClickListener(view -> {
                if (Permissions.checkSMSMMSPermissions(activity)) {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    //intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.setData(Uri.parse(PPPEApplication.INTENT_DATA_PACKAGE + getPackageName()));
                    if (GlobalUtils.activityIntentExists(intent, activity)) {
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
            //noinspection DataFlowIssue
            permissionsButton.setVisibility(View.GONE);

        permissionsButton = findViewById(R.id.activity_main_call_permissions_button);
        if (PPPEApplication.HAS_FEATURE_TELEPHONY) {
            //noinspection DataFlowIssue
            permissionsButton.setOnClickListener(view -> {
                if (Permissions.checkCallPermissions(activity)) {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    //intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.setData(Uri.parse(PPPEApplication.INTENT_DATA_PACKAGE + getPackageName()));
                    if (GlobalUtils.activityIntentExists(intent, activity)) {
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
            //noinspection DataFlowIssue
            permissionsButton.setVisibility(View.GONE);

        Button batteryOptimizationButton = findViewById(R.id.activity_main_battery_optimization_button);
        //noinspection DataFlowIssue
        batteryOptimizationButton.setOnClickListener(view -> {

            String packageName = PPPEApplication.PACKAGE_NAME;
            Intent intent;

            PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
            if ((pm != null) && pm.isIgnoringBatteryOptimizations(packageName)) {
                intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            }
            else {
                //    DO NOT USE IT, CHANGE IS NOT DISPLAYED IN SYSTEM SETTINGS
                //    But in ONEPLUS it IS ONLY SOLUTION !!!
                intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse(PPPEApplication.INTENT_DATA_PACKAGE + packageName));
            }
            //intent.addCategory(Intent.CATEGORY_DEFAULT);
            if (GlobalUtils.activityIntentExists(intent, activity)) {
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
            //noinspection DataFlowIssue
            popupWindowsInBackgroundButton.setOnClickListener(view -> {
                Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                intent.setClassName("com.miui.securitycenter",
                        "com.miui.permcenter.permissions.PermissionsEditorActivity");
                intent.putExtra(PPPEApplication.EXTRA_PKG_NAME, PPPEApplication.PACKAGE_NAME);
                //intent.addCategory(Intent.CATEGORY_DEFAULT);
                if (GlobalUtils.activityIntentExists(intent, activity)) {
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
            //noinspection DataFlowIssue
            popupWindowsInBackgroundButton.setVisibility(View.GONE);
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

    String getEmailBodyText() {
        String body;
        body = getString(R.string.extender_acra_email_body_device) + " " +
                Settings.Global.getString(getContentResolver(), Settings.Global.DEVICE_NAME) +
                " (" + Build.MODEL + ")" + StringConstants.STR_NEWLINE_WITH_SPACE;
        body = body + getString(R.string.extender_acra_email_body_android_version) + " " + Build.VERSION.RELEASE + StringConstants.STR_DOUBLE_NEWLINE_WITH_SPACE;
        return body;
    }

}
