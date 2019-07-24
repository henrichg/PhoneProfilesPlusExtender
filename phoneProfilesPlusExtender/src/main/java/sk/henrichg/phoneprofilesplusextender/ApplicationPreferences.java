package sk.henrichg.phoneprofilesplusextender;

import android.content.Context;
import android.content.SharedPreferences;

class ApplicationPreferences {

    static private SharedPreferences preferences = null;

    static final String PREF_APPLICATION_THEME = "applicationTheme";

    static SharedPreferences getSharedPreferences(Context context) {
        if (preferences == null)
            preferences = context.getApplicationContext().getSharedPreferences(PPPEApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences;
    }

    static String applicationTheme(Context context/*, boolean useNightMode*/) {
        //noinspection UnnecessaryLocalVariable
        String applicationTheme = getSharedPreferences(context).getString(PREF_APPLICATION_THEME, "white");
        /*if (applicationTheme.equals("night_mode") && useNightMode) {
            int nightModeFlags =
                    context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            switch (nightModeFlags) {
                case Configuration.UI_MODE_NIGHT_YES:
                    applicationTheme = "dark";
                    break;
                case Configuration.UI_MODE_NIGHT_NO:
                    applicationTheme = "white"; //getSharedPreferences(context).getString(PREF_APPLICATION_NIGHT_MODE_OFF_THEME, "white");
                    break;
                case Configuration.UI_MODE_NIGHT_UNDEFINED:
                    applicationTheme = "white"; //getSharedPreferences(context).getString(PREF_APPLICATION_NIGHT_MODE_OFF_THEME, "white");
                    break;
            }
        }*/
        return applicationTheme;
    }

}
