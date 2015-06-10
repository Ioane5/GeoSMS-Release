package com.steps.geosms;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.ioane.sharvadze.geosms.R;
import com.steps.geosms.utils.Utils;


public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                    onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private static void showOpenSourceLicenses(Context context){
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(R.string.open_source_licenses);

        WebView wv = new WebView(context);
        wv.loadUrl("file:///android_asset/licenses.html");

        wv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);

                return true;
            }
        });

        alert.setView(wv);
        alert.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        alert.show();
    }
    /**
     * A placeholder fragment containing a simple view.
     */
    public static class SettingsFragment extends PreferenceFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener{

        @SuppressWarnings("unused")
        private static final String TAG = SettingsFragment.class.getSimpleName();

        public SettingsFragment() {}

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);


            Preference licenses = getPreferenceManager().findPreference("open_source_licenses");
            licenses.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    showOpenSourceLicenses(getActivity());
                    return true;
                }
            });

            Preference notifications = getPreferenceManager().findPreference("notifications");
            if(!Utils.isDefaultSmsApp(getActivity().getApplicationContext()))
                notifications.setEnabled(false);


            Preference language = getPreferenceManager().findPreference("pref_language");
            language.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    ListPreference lp = (ListPreference)preference;
                    if(TextUtils.equals((String)newValue,"0")){
                        setLocale("ka");
                    }else{
                        setLocale(null);
                    }
                    return true;
                }
            });

            MyPreferencesManager.getWebSmsPreferences(getActivity().getBaseContext()).
                    registerOnSharedPreferenceChangeListener(this);

            getPreferenceManager().setSharedPreferencesMode(MODE_PRIVATE);
            SharedPreferences preferences = getPreferenceManager().getSharedPreferences();
            onSharedPreferenceChanged(preferences,MyPreferencesManager.WEBSMS_USERNAME);
            onSharedPreferenceChanged(preferences,MyPreferencesManager.WEBSMS_NAME);
        }


        public void setLocale(final String lang) {
            android.support.v7.app.AlertDialog.Builder builder =
                    new android.support.v7.app.AlertDialog.Builder(getActivity());
            builder.setPositiveButton(R.string.restart, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Activity activity = getActivity();
                        MyApplication.updateLanguage(activity, lang);

                        Intent i = activity.getPackageManager()
                                .getLaunchIntentForPackage(activity.getPackageName());
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        activity.finish();
                        startActivity(i);
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    }
                }).setTitle(R.string.restart_request_title)
                  .setMessage(getString(R.string.restart_app_request)).show();
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Preference pref = findPreference(key);
            if (key.equals(MyPreferencesManager.WEBSMS_USERNAME)) {
                pref.setSummary(sharedPreferences.getString(key, ""));
            }else if(key.equals(MyPreferencesManager.WEBSMS_NAME)){
                ListPreference listPreference = (ListPreference)pref;
                int index = listPreference.findIndexOfValue(sharedPreferences.getString(key,"-1"));

                pref.setSummary(index >= 0
                        ? listPreference.getEntries()[index]
                        : null);
            }
        }
    }
}
