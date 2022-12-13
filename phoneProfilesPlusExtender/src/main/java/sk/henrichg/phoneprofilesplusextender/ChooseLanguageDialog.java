package sk.henrichg.phoneprofilesplusextender;

import android.content.Intent;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;

class ChooseLanguageDialog
{
    final AlertDialog mDialog;
    final MainActivity activity;

    final ListView listView;
    final TextView help;

    final ArrayList<Language> languages;

    ChooseLanguageDialog(MainActivity activity)
    {
        this.activity = activity;

        languages = new ArrayList<>();

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setTitle(R.string.extender_menu_choose_language);
        dialogBuilder.setCancelable(true);
        dialogBuilder.setNegativeButton(android.R.string.cancel, null);

        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_choose_language, null);
        dialogBuilder.setView(layout);

        mDialog = dialogBuilder.create();

        mDialog.setOnShowListener(dialog -> {
//                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                if (positive != null) positive.setAllCaps(false);
//                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                if (negative != null) negative.setAllCaps(false);

            doShow();
        });

        listView = layout.findViewById(R.id.choose_language_dlg_listview);
        help = layout.findViewById(R.id.choose_language_dlg_help);

        listView.setOnItemClickListener((parent, item, position, id) -> {
            ChooseLanguageAdapter.ViewHolder viewHolder = (ChooseLanguageAdapter.ViewHolder) item.getTag();
            if (viewHolder != null)
                viewHolder.radioButton.setChecked(true);
            doOnItemSelected(position);
        });

        String str1 = activity.getString(R.string.extender_application_translations);
        String str2 = str1 + " " + PPPEApplication.CROWDIN_URL + " \u21D2";
        Spannable sbt = new SpannableString(str2);
        //sbt.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, str1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(ds.linkColor);    // you can use custom color
                //ds.bgColor = Color.GRAY;
                ds.setUnderlineText(false);    // this remove the underline
            }

            @Override
            public void onClick(@NonNull View textView) {
                String url = PPPEApplication.CROWDIN_URL;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                try {
                    activity.startActivity(Intent.createChooser(i, activity.getString(R.string.extender_web_browser_chooser)));
                } catch (Exception e) {
                    PPPEApplication.recordException(e);
                }
            }
        };
        sbt.setSpan(clickableSpan, str1.length()+1, str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //sbt.setSpan(new UnderlineSpan(), str1.length()+1, str2.length(), 0);
        help.setText(sbt);
        help.setMovementMethod(LinkMovementMethod.getInstance());

    }

    private void doShow() {
        String storedLanguage = LocaleHelper.getLanguage(activity.getApplicationContext());
        String storedCountry = LocaleHelper.getCountry(activity.getApplicationContext());
        String storedScript = LocaleHelper.getScript(activity.getApplicationContext());

        final String[] languageValues = activity.getResources().getStringArray(R.array.chooseLanguageValues);

        for (String languageValue : languageValues) {
            Language language = new Language();
            if (languageValue.equals("[sys]")) {
                language.language = languageValue;
                language.country = "";
                language.script = "";
                language.name = activity.getString(R.string.extender_menu_choose_language_system_language);
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

        if (LocaleHelper.getIsSetSystemLanguage(activity.getApplicationContext())) {
            activity.selectedLanguage = 0;
//            Log.e("ChooseLanguageDialog.doShow", "is set system languauge");
        } else {
            for (int i = 0; i < languages.size(); i++) {
                Language language = languages.get(i);
                String sLanguage = language.language;
                String country = language.country;
                String script = language.script;

                if (sLanguage.equals(storedLanguage) &&
                        storedCountry.isEmpty() &&
                        storedScript.isEmpty()) {
                    activity.selectedLanguage = i;
                    break;
                }
                if (sLanguage.equals(storedLanguage) &&
                        country.equals(storedCountry) &&
                        storedScript.isEmpty()) {
                    activity.selectedLanguage = i;
                    break;
                }
                if (sLanguage.equals(storedLanguage) &&
                        storedCountry.isEmpty() &&
                        script.equals(storedScript)) {
                    activity.selectedLanguage = i;
                    break;
                }
                if (sLanguage.equals(storedLanguage) &&
                        country.equals(storedCountry) &&
                        script.equals(storedScript)) {
                    activity.selectedLanguage = i;
                    break;
                }
            }
        }

        ChooseLanguageAdapter chooseLanguageAdapter = new ChooseLanguageAdapter(this, activity, languageNameChoices);
        listView.setAdapter(chooseLanguageAdapter);
        listView.setSelection(activity.selectedLanguage);

    }

    void doOnItemSelected(int position)
    {
        activity.selectedLanguage = position;
        LocaleHelper.setIsSetSystemLanguage(activity.getApplicationContext(), position == 0);

        Language language = languages.get(activity.selectedLanguage);
        activity.defaultLanguage = language.language;
        activity.defaultCountry = language.country;
        activity.defaultScript = language.script;

        LocaleHelper.setLocale(activity.getApplicationContext(),
                activity.defaultLanguage, activity.defaultCountry, activity.defaultScript, true);

        MainActivity.reloadActivity(activity, false);
        mDialog.dismiss();

        //PPApplication.updateGUI(true, false, activity);
    }

    void show() {
        if (!activity.isFinishing())
            mDialog.show();
    }

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

}
