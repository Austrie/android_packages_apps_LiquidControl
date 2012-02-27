
package com.liquid.control.fragments;

import android.content.Context;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.widget.Toast;

import com.liquid.control.R;
import com.liquid.control.SettingsPreferenceFragment;
import com.liquid.control.widgets.SeekBarPreference;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class StatusBarGeneral extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "LiquidControl :StatusBarGeneral";
    private static final boolean DEBUG = true;
    private static final String PREF_SHOW_DATE = "show_date";
    private static final String PREF_DATE_FORMAT = "date_format";
    private static final String PREF_SETTINGS_BUTTON_BEHAVIOR = "settings_behavior";
    private static final String PREF_AUTO_HIDE_TOGGLES = "auto_hide_toggles";
    private static final String PREF_DATE_BEHAVIOR = "date_behavior";
    private static final String PREF_BRIGHTNESS_TOGGLE = "status_bar_brightness_toggle";
    private static final String PREF_SHOW_AOSP = "show_aosp_settings";
    private static final String PREF_SHOW_LIQUIDCONTROL = "show_liquid_control";
    private static final String PREF_ADB_ICON = "adb_icon";
    private static final String PREF_WINDOWSHADE_COLOR = "statusbar_windowshade_background_color";
    private static final String PREF_STATUSBAR_ALPHA = "statusbar_alpha";
    private static final String PREF_STATUSBAR_UNEXPANDED_ALPHA = "statusbar_unexpanded_alpha";
    private static final String PREF_STATUSBAR_UNEXPANDED_COLOR = "statusbar_unexpanded_color";
    private static String STATUSBAR_COLOR_SUMMARY_HOLDER;

    CheckBoxPreference mShowDate;
    ListPreference mDateFormat;
    CheckBoxPreference mShowAospSettings;
    CheckBoxPreference mDefaultSettingsButtonBehavior;
    CheckBoxPreference mAutoHideToggles;
    CheckBoxPreference mDateBehavior;
    CheckBoxPreference mStatusBarBrightnessToggle;
    CheckBoxPreference mShowLiquidControl;
    CheckBoxPreference mAdbIcon;
    ColorPickerPreference mWindowshadeBackground;
    SeekBarPreference mStatusbarAlpha;
    SeekBarPreference mStatusbarUnexpandedAlpha;
    ColorPickerPreference mStatusbarUnexpandedColor;
    Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity().getApplicationContext();

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_statusbar_general);

        // Load preferences
        mShowDate = (CheckBoxPreference) findPreference(PREF_SHOW_DATE);
        mDateFormat = (ListPreference) findPreference(PREF_DATE_FORMAT);
        mDateFormat.setOnPreferenceChangeListener(this);
        mDefaultSettingsButtonBehavior = (CheckBoxPreference) findPreference(PREF_SETTINGS_BUTTON_BEHAVIOR);
        mAutoHideToggles = (CheckBoxPreference) findPreference(PREF_AUTO_HIDE_TOGGLES);
        mDateBehavior = (CheckBoxPreference) findPreference(PREF_DATE_BEHAVIOR);
        mStatusBarBrightnessToggle = (CheckBoxPreference) findPreference(PREF_BRIGHTNESS_TOGGLE);
        mShowAospSettings = (CheckBoxPreference) findPreference(PREF_SHOW_AOSP);
        mShowLiquidControl = (CheckBoxPreference) findPreference(PREF_SHOW_LIQUIDCONTROL);
        mAdbIcon = (CheckBoxPreference) findPreference(PREF_ADB_ICON);
        mWindowshadeBackground = (ColorPickerPreference) findPreference(PREF_WINDOWSHADE_COLOR);
        mWindowshadeBackground.setOnPreferenceChangeListener(this);
        mStatusbarAlpha = (SeekBarPreference) findPreference(PREF_STATUSBAR_ALPHA);
        mStatusbarAlpha.setOnPreferenceChangeListener(this);
        mStatusbarUnexpandedAlpha = (SeekBarPreference) findPreference(PREF_STATUSBAR_UNEXPANDED_ALPHA);
        mStatusbarUnexpandedAlpha.setOnPreferenceChangeListener(this);
        mStatusbarUnexpandedColor = (ColorPickerPreference) findPreference(PREF_STATUSBAR_UNEXPANDED_COLOR);
        mStatusbarUnexpandedColor.setOnPreferenceChangeListener(this);

        // make sure the settings are updated
        updateSettings();

        if (mTablet) {
            PreferenceScreen prefs = getPreferenceScreen();
            prefs.removePreference(mStatusBarBrightnessToggle);
            prefs.removePreference(mAutoHideToggles);
            prefs.removePreference(mDefaultSettingsButtonBehavior);
        }
    }

    private void updateSettings() {
        mShowDate.setChecked(Settings.System.getInt(mContext
                .getContentResolver(), Settings.System.STATUSBAR_SHOW_DATE, 0) == 1);

        mDefaultSettingsButtonBehavior.setChecked(Settings.System.getInt(mContext
                .getContentResolver(), Settings.System.STATUSBAR_SETTINGS_BEHAVIOR, 0) == 1);

        mAutoHideToggles.setChecked(Settings.System.getInt(mContext
                .getContentResolver(), Settings.System.STATUSBAR_QUICKTOGGLES_AUTOHIDE, 0) == 1);

        mDateBehavior.setChecked(Settings.System.getInt(mContext
                .getContentResolver(), Settings.System.STATUSBAR_DATE_BEHAVIOR, 0) == 1);

        mStatusBarBrightnessToggle.setChecked(Settings.System.getInt(mContext
                .getContentResolver(), Settings.System.STATUS_BAR_BRIGHTNESS_TOGGLE, 0) == 1);

        mShowAospSettings.setChecked(Settings.System.getInt(mContext
                .getContentResolver(), Settings.System.STATUSBAR_REMOVE_AOSP_SETTINGS_LINK, 0) == 1);

        mShowLiquidControl.setChecked(Settings.System.getInt(mContext
                .getContentResolver(), Settings.System.STATUSBAR_REMOVE_LIQUIDCONTROL_LINK, 0) == 1);

        mAdbIcon.setChecked(Settings.Secure.getInt(getActivity().getContentResolver(),
                Settings.Secure.ADB_ICON, 1) == 1);

        // update the Date format summary
        int dFormat = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.STATUSBAR_DATE_FORMAT, 0);
        String date = null;
        String displayFormat = "Current: %s";
        switch (dFormat) {
            case 0:
                // default, February 14, 2012
                date = "Febuary 14, 2012";
            break;
            case 1:
                // Tuesday February 14, 2012
                date = "Tuesday February 14, 2012";
            break;
            case 2:
                // Tues February 14, 2012
                date = "Tues February 14, 2012";
            break;
            case 3:
                // Tuesday
                date = "Tuesday";
            break;
            case 4:
                // day 45 of 2012
                date = "day 45 of 2012";
            break;
            case 5:
                // Tues Feb 14
                date = "Tues Feb 14";
            break;
        }
        mDateFormat.setSummary(String.format(displayFormat, date));

        float expandedAlpha = Settings.System.getFloat(getActivity()
                .getContentResolver(), Settings.System.STATUSBAR_EXPANDED_BOTTOM_ALPHA, 1f);
        mStatusbarAlpha.setInitValue((int) (expandedAlpha * 100));
        mStatusbarAlpha.setSummary(String.format("%f", expandedAlpha * 100));

        try {
            int expandedColor = Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_EXPANDED_BACKGROUND_COLOR);
            // I'm blanking on a better way to setSummary
            String summary = String.format("%d", expandedColor);
            mWindowshadeBackground.setSummary(summary);
        } catch (SettingNotFoundException snfe) {
            // just let it go
        }

        float unexpandedAlpha = Settings.System.getFloat(getActivity()
                .getContentResolver(), Settings.System.STATUSBAR_UNEXPANDED_ALPHA, 1f);
        mStatusbarUnexpandedAlpha.setInitValue((int) (unexpandedAlpha * 100));
        mStatusbarUnexpandedAlpha.setSummary(String.format("%f", unexpandedAlpha * 100));

        try {
            int unexpandedColor = Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_UNEXPANDED_COLOR);
            // I'm blanking on a better way to setSummary
            String summary = String.format("%d", unexpandedColor);
            mWindowshadeBackground.setSummary(summary);
        } catch (SettingNotFoundException snfe) {
            // just let it go
        }
    }

    public boolean onPreferenceChange(Preference pref, Object newValue) {
        boolean success = false;

        if (pref == mDateFormat) {
            int val0 = Integer.parseInt((String) newValue);
            if (DEBUG) Log.d(TAG, "led on time new value: " + val0);
            success = Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_DATE_FORMAT, val0);
        } else if (pref == mStatusbarAlpha) {
            float val1 = Float.parseFloat((String) newValue);
            if (DEBUG) Log.d(TAG, "value:" + val1 / 100 + "    raw:" + val1);
            success = Settings.System.putFloat(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_EXPANDED_BOTTOM_ALPHA, val1 / 100);
        } else if (pref == mWindowshadeBackground) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String.valueOf(newValue)));
            pref.setSummary(hex);

            int intHex = ColorPickerPreference.convertToColorInt(hex);
            success =Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_EXPANDED_BACKGROUND_COLOR, intHex);

            if (DEBUG) Log.d(TAG, String.format("new color hex value: %d", intHex));
        } else if (pref == mStatusbarUnexpandedAlpha) {
            float val2 = Float.parseFloat((String) newValue);
            if (DEBUG) Log.d(TAG, "value:" + val2 / 100 + "    raw:" + val2);
            success = Settings.System.putFloat(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_UNEXPANDED_ALPHA, val2 / 100);
        } else if (pref == mStatusbarUnexpandedColor) {
            String statusbar_hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String.valueOf(newValue)));
            pref.setSummary(statusbar_hex);

            int intHex = ColorPickerPreference.convertToColorInt(statusbar_hex);
            success = Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_UNEXPANDED_COLOR, intHex);
            if (DEBUG) Log.d(TAG, "color value int:" + intHex);
        }

        // update checkboxes and return success=true:false
        updateSettings();
        return success;
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
        } else if (preference == mShowDate) {
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.STATUSBAR_SHOW_DATE,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
