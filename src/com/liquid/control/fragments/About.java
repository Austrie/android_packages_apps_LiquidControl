
package com.liquid.control.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;

import com.liquid.control.SettingsPreferenceFragment;
import com.liquid.control.R;

public class About extends SettingsPreferenceFragment {

    public static final String TAG = "About";

    Preference mSiteUrl;
    Preference mSourceUrl;
    Preference mIrcUrl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.prefs_about);
        mSiteUrl = findPreference("liquid_website");
        mSourceUrl = findPreference("liquid_source");
        mIrcUrl = findPreference("liquid_irc");

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mSiteUrl) {
            launchUrl("http://liquidsmoothroms.com/");
        } else if (preference == mSourceUrl) {
            launchUrl("http://github.com/LiquidSmoothROMs");
        } else if (preference == mIrcUrl) {
            launchUrl("http://webchat.freenode.net/?channels=liquid-toro");
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void launchUrl(String url) {
        Uri uriUrl = Uri.parse(url);
        Intent donate = new Intent(Intent.ACTION_VIEW, uriUrl);
        getActivity().startActivity(donate);
    }
}
