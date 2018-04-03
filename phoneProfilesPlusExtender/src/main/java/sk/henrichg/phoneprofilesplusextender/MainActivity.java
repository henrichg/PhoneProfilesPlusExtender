package sk.henrichg.phoneprofilesplusextender;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int RESULT_ACCESSIBILITY_SETTINGS = 1900;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setLanguage(getBaseContext());

        setContentView(R.layout.activity_main);

        TextView text = findViewById(R.id.activity_main_application_version);
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            text.setText(getString(R.string.about_application_version) + " " + pInfo.versionName + " (" + pInfo.versionCode + ")");
        } catch (Exception e) {
            text.setText("");
        }

        String str1 = getString(R.string.exend_accessibility_service);
        String str2;
        if (PPPEAccessibilityService.isEnabled(getApplicationContext()))
            str2 = str1 + " " + getString(R.string.exend_accessibility_service_enabled);
        else
            str2 = str1 + " " + getString(R.string.exend_accessibility_service_disabled);
        Spannable sbt = new SpannableString(str2);
        text = findViewById(R.id.activity_main_exend_accessibility_service);
        final Activity activity = this;
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                if (MainActivity.activityActionExists(Settings.ACTION_ACCESSIBILITY_SETTINGS, activity)) {
                    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    startActivityForResult(intent, RESULT_ACCESSIBILITY_SETTINGS);
                } else {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                    dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                    //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                    dialogBuilder.setPositiveButton(android.R.string.ok, null);
                    dialogBuilder.show();
                }
            }
        };
        sbt.setSpan(clickableSpan, str1.length()+1, str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        sbt.setSpan(new UnderlineSpan(), str1.length()+1, str2.length(), 0);
        text.setText(sbt);
        text.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_ACCESSIBILITY_SETTINGS)
            reloadActivity(this/*, true*/);
    }

    private static boolean activityActionExists(String action, Context context) {
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

    // https://stackoverflow.com/questions/40221711/android-context-getresources-updateconfiguration-deprecated
    // but my solution working also in Android 8.1
    private static void setLanguage(Context context)//, boolean restart)
    {
        //if (android.os.Build.VERSION.SDK_INT < 24) {

        SharedPreferences preferences = context.getApplicationContext().getSharedPreferences(PPPEApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        String lang = preferences.getString(PPPEApplication.PREF_APPLICATION_LANGUAGE, "system");

        Locale appLocale;

        if (!lang.equals("system")) {
            String[] langSplit = lang.split("-");
            if (langSplit.length == 1)
                appLocale = new Locale(lang);
            else
                appLocale = new Locale(langSplit[0], langSplit[1]);
        } else {
            //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            //    appLocale = Resources.getSystem().getConfiguration().getLocales().get(0);
            //else
            appLocale = Resources.getSystem().getConfiguration().locale;
        }

        Locale.setDefault(appLocale);
        Configuration appConfig = new Configuration();
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        //    appConfig.setLocale(appLocale);
        //else
        appConfig.locale = appLocale;

        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        //    Context context  = context.createConfigurationContext(appConfig);
        //else
        context.getResources().updateConfiguration(appConfig, context.getResources().getDisplayMetrics());
        //}

        // collator for application locale sorting
        //collator = getCollator(context);
    }

    /*
    private static Collator getCollator(Context context)
    {
        //if (android.os.Build.VERSION.SDK_INT < 24) {
        // get application Locale
        String lang = ApplicationPreferences.applicationLanguage(context);
        Locale appLocale;
        if (!lang.equals("system")) {
            String[] langSplit = lang.split("-");
            if (langSplit.length == 1)
                appLocale = new Locale(lang);
            else
                appLocale = new Locale(langSplit[0], langSplit[1]);
        } else {
            //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //    appLocale = Resources.getSystem().getConfiguration().getLocales().get(0);
            //} else {
            appLocale = Resources.getSystem().getConfiguration().locale;
            //}
        }
        // get collator for application locale
        return Collator.getInstance(appLocale);
        //}
        //else {
        //    //Log.d("GlobalGUIRoutines.getCollator", java.util.Locale.getDefault().toString());
        //    return Collator.getInstance();
        //}
    }
    */

}
