
package com.liquid.control.fragments;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

import com.liquid.control.R;

public class PowerMenu extends PreferenceFragment {

    private static final String PREF_FULLSCREEN = "show_fullscreen";
    private static final String PREF_POWER_SAVER = "show_power_saver";
    private static final String PREF_SCREENSHOT = "show_screenshot";
    private static final String PREF_EASTER_EGG = "show_easter_egg";

    CheckBoxPreference mShowFullscreen;
    CheckBoxPreference mShowPowerSaver;
    CheckBoxPreference mShowScreenShot;
    CheckBoxPreference mShowEasterEgg;

    @Override
    public void onPause() {
        // no one cares let it go
        super.onPause();
    }

    @Override
    public void onResume() {
        updateToggles();
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_powermenu);

        int powerSaverVal = 0;
        mShowPowerSaver = (CheckBoxPreference) findPreference(PREF_POWER_SAVER);

        try {
            powerSaverVal = Settings.Secure.getInt(getActivity()
                    .getContentResolver(), Settings.Secure.POWER_SAVER_MODE);
        } catch (SettingNotFoundException e) {
            //TODO FIX
            //mShowPowerSaver.setEnabled(false);
            mShowPowerSaver.setSummary("Enable power saver before you can see it in the menu.");
        }

        mShowPowerSaver.setChecked(powerSaverVal == 1);

        mShowScreenShot = (CheckBoxPreference) findPreference(PREF_SCREENSHOT);
        mShowScreenShot.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.POWER_DIALOG_SHOW_SCREENSHOT, 1) == 1);

        mShowEasterEgg = (CheckBoxPreference) findPreference(PREF_EASTER_EGG);
        mShowEasterEgg.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.POWER_DIALOG_SHOW_EASTER_EGG, 0) == 1);

        // Check if we are in fullscreen mode if so we freeze the setEnabled state
        // so user doesn't accidentally completely disable normal mode
        boolean mInFullscreenMode = false;
        mInFullscreenMode = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.POWER_DIALOG_FULLSCREEN, 0) == 1;

        // now that we know what state we are in lets act accordingly
        mShowFullscreen = (CheckBoxPreference) findPreference(PREF_FULLSCREEN);
        mShowFullscreen.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.POWER_DIALOG_SHOW_FULLSCREEN, 0) == 1);
        mShowFullscreen.setEnabled(mInFullscreenMode == false);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        boolean handled = false;
        if (preference == mShowPowerSaver) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.POWER_DIALOG_SHOW_POWER_SAVER,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            handled = true;
        } else if (preference == mShowScreenShot) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.POWER_DIALOG_SHOW_SCREENSHOT,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            handled = true;
        } else if (preference == mShowEasterEgg) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.POWER_DIALOG_SHOW_EASTER_EGG,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            handled = true;
        } else if (preference == mShowFullscreen) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.POWER_DIALOG_SHOW_FULLSCREEN,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            handled = true;
        }
        updateToggles();
        if (handled) return true;
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public void updateToggles() {
        // any time we do anything we call this
        int powerSaverVal = 0;
        mShowPowerSaver = (CheckBoxPreference) findPreference(PREF_POWER_SAVER);
        try {
            powerSaverVal = Settings.Secure.getInt(getActivity()
                    .getContentResolver(), Settings.Secure.POWER_SAVER_MODE);
        } catch (SettingNotFoundException e) {
            mShowPowerSaver.setSummary("Enable power saver before you can see it in the menu.");
        }
        mShowPowerSaver.setChecked(powerSaverVal == 1);
        mShowScreenShot.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.POWER_DIALOG_SHOW_SCREENSHOT, 1) == 1);
        mShowEasterEgg.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.POWER_DIALOG_SHOW_EASTER_EGG, 0) == 1);
        boolean mInFullscreenMode = false;
        mInFullscreenMode = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.POWER_DIALOG_FULLSCREEN, 0) == 1;
        mShowFullscreen.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.POWER_DIALOG_SHOW_FULLSCREEN, 0) == 1);
        mShowFullscreen.setEnabled(mInFullscreenMode == false);
    }
}

