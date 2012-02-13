
package com.liquid.control.fragments;

import android.content.Context;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;

import com.liquid.control.R;
import com.liquid.control.SettingsPreferenceFragment;

public class StatusBarGeneral extends SettingsPreferenceFragment {

    private static final String PREF_SETTINGS_BUTTON_BEHAVIOR = "settings_behavior";
    private static final String PREF_AUTO_HIDE_TOGGLES = "auto_hide_toggles";
    private static final String PREF_DATE_BEHAVIOR = "date_behavior";
    private static final String PREF_BRIGHTNESS_TOGGLE = "status_bar_brightness_toggle";
    private static final String PREF_SHOW_AOSP = "show_aosp_settings";
    private static final String PREF_SHOW_LIQUIDCONTROL = "show_liquid_control";
    private static final String PREF_ADB_ICON = "adb_icon";

    CheckBoxPreference mShowAospSettings;
    CheckBoxPreference mDefaultSettingsButtonBehavior;
    CheckBoxPreference mAutoHideToggles;
    CheckBoxPreference mDateBehavior;
    CheckBoxPreference mStatusBarBrightnessToggle;
    CheckBoxPreference mShowLiquidControl;
    CheckBoxPreference mAdbIcon;
    Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity().getApplicationContext();

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_statusbar_general);

        mDefaultSettingsButtonBehavior = (CheckBoxPreference) findPreference(PREF_SETTINGS_BUTTON_BEHAVIOR);
        mDefaultSettingsButtonBehavior.setChecked(Settings.System.getInt(mContext
                .getContentResolver(), Settings.System.STATUSBAR_SETTINGS_BEHAVIOR, 0) == 1);

        mAutoHideToggles = (CheckBoxPreference) findPreference(PREF_AUTO_HIDE_TOGGLES);
        mAutoHideToggles.setChecked(Settings.System.getInt(mContext
                .getContentResolver(), Settings.System.STATUSBAR_QUICKTOGGLES_AUTOHIDE, 0) == 1);

        mDateBehavior = (CheckBoxPreference) findPreference(PREF_DATE_BEHAVIOR);
        mDateBehavior.setChecked(Settings.System.getInt(mContext
                .getContentResolver(), Settings.System.STATUSBAR_DATE_BEHAVIOR, 0) == 1);

        mStatusBarBrightnessToggle = (CheckBoxPreference) findPreference(PREF_BRIGHTNESS_TOGGLE);
        mStatusBarBrightnessToggle.setChecked(Settings.System.getInt(mContext
                .getContentResolver(), Settings.System.STATUS_BAR_BRIGHTNESS_TOGGLE, 0) == 1);

        mShowAospSettings = (CheckBoxPreference) findPreference(PREF_SHOW_AOSP);
        mShowAospSettings.setChecked(Settings.System.getInt(mContext
                .getContentResolver(), Settings.System.STATUSBAR_REMOVE_AOSP_SETTINGS_LINK, 0) == 1);

        mShowLiquidControl = (CheckBoxPreference) findPreference(PREF_SHOW_LIQUIDCONTROL);
        mShowLiquidControl.setChecked(Settings.System.getInt(mContext
                .getContentResolver(), Settings.System.STATUSBAR_REMOVE_LIQUIDCONTROL_LINK, 0) == 1);

        mAdbIcon = (CheckBoxPreference) findPreference(PREF_ADB_ICON);
        mAdbIcon.setChecked(Settings.Secure.getInt(getActivity().getContentResolver(),
                Settings.Secure.ADB_ICON, 1) == 1);

        if (mTablet) {
            PreferenceScreen prefs = getPreferenceScreen();
            prefs.removePreference(mStatusBarBrightnessToggle);
            prefs.removePreference(mAutoHideToggles);
            prefs.removePreference(mDefaultSettingsButtonBehavior);
        }    
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mDefaultSettingsButtonBehavior) {
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.STATUSBAR_SETTINGS_BEHAVIOR,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;
        } else if (preference == mAutoHideToggles) {
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.STATUSBAR_QUICKTOGGLES_AUTOHIDE,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;
        } else if (preference == mDateBehavior) {
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.STATUSBAR_DATE_BEHAVIOR,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;
        } else if (preference == mStatusBarBrightnessToggle) {
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.STATUS_BAR_BRIGHTNESS_TOGGLE,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;
        } else if (preference == mShowAospSettings) {
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.STATUSBAR_REMOVE_AOSP_SETTINGS_LINK,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;
        } else if (preference == mShowLiquidControl) {
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.STATUSBAR_REMOVE_LIQUIDCONTROL_LINK,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;
        } else if (preference == mAdbIcon) {
            boolean checked = ((CheckBoxPreference) preference).isChecked();
            Settings.Secure.putInt(getActivity().getContentResolver(),
                    Settings.Secure.ADB_ICON, checked ? 1 : 0);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
