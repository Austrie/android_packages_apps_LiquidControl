
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

    private static final String PREF_AIRPLANE = "show_airplane";
    private static final String PREF_EASTEREGG = "show_easteregg";
    private static final String PREF_FLASHLIGHT = "show_flashlight";
    private static final String PREF_FULLSCREEN = "show_fullscreen";
    private static final String PREF_POWERSAVER = "show_powersaver";
    private static final String PREF_SCREENSHOT = "show_screenshot";

    CheckBoxPreference mShowAirplane;
    CheckBoxPreference mShowEasteregg;
    CheckBoxPreference mShowFlashlight;
    CheckBoxPreference mShowFullscreen;
    CheckBoxPreference mShowPowersaver;
    CheckBoxPreference mShowScreenshot;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_powermenu);

        mShowAirplane = (CheckBoxPreference) findPreference(PREF_AIRPLANE);
        mShowAirplane.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.POWER_DIALOG_SHOW_AIRPLANE, 0) == 1);

        mShowEasteregg = (CheckBoxPreference) findPreference(PREF_EASTEREGG);
        mShowEasteregg.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.POWER_DIALOG_SHOW_EASTEREGG, 0) == 1);

        mShowFlashlight = (CheckBoxPreference) findPreference(PREF_FLASHLIGHT);
        mShowFlashlight.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.POWER_DIALOG_SHOW_FLASHLIGHT, 0) == 1);

        mShowFullscreen = (CheckBoxPreference) findPreference(PREF_FULLSCREEN);
        mShowFullscreen.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.POWER_DIALOG_SHOW_FULLSCREEN, 0) == 1);

        mShowPowersaver = (CheckBoxPreference) findPreference(PREF_POWERSAVER);
        mShowPowersaver.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.POWER_DIALOG_SHOW_POWERSAVER, 0) == 1);

        mShowScreenshot = (CheckBoxPreference) findPreference(PREF_SCREENSHOT);
        mShowScreenshot.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.POWER_DIALOG_SHOW_SCREENSHOT, 1) == 1);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, 
        Preference preference) {

        if (preference == mShowAirplane) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.POWER_DIALOG_SHOW_AIRPLANE,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;
        } else if (preference == mShowEasteregg) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.POWER_DIALOG_SHOW_EASTEREGG,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;
        } else if (preference == mShowFlashlight) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.POWER_DIALOG_SHOW_FLASHLIGHT,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;
        } else if (preference == mShowFullscreen) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.POWER_DIALOG_SHOW_FULLSCREEN,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;
        } else if (preference == mShowPowersaver) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.POWER_DIALOG_SHOW_POWERSAVER,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;
        } else if (preference == mShowScreenshot) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.POWER_DIALOG_SHOW_SCREENSHOT,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}

