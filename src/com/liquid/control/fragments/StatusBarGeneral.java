/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.liquid.control.fragments;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Toast;

import com.liquid.control.R;
import com.liquid.control.SettingsPreferenceFragment;
import com.liquid.control.widgets.SeekBarPreference;

import java.io.IOException;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class StatusBarGeneral extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

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
    private static final String PREF_TEST_NOTICE = "test_notice";
    private static final String PREF_STATUSBAR_UNEXPANDED_COLOR = "statusbar_unexpanded_color";
    private static final String PREF_LAYOUT = "status_bar_layout";
    private static String STATUSBAR_COLOR_SUMMARY_HOLDER;

    private static final String TEST_SHORT = "Alpha Test";
    private static final String TEST_TITLE = "Test Notice";
    private static final String TEST_MESSAGE = "Sent to test notification alpha";

    /* Default Color Schemes */
    private static final float STATUSBAR_EXPANDED_ALPHA_DEFAULT = 0.7f; //TODO update
    private static final int STATUSBAR_EXPANDED_COLOR_DEFAULT = 0xFF000000; //TODO update
    private static final int STATUSBAR_UNEXPANDED_COLOR_DEFAULT = 0xFF000000; //TODO update

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
    PreferenceScreen mTestNotification;
    ColorPickerPreference mStatusbarUnexpandedColor;
    ListPreference mLayout;
    NotificationManager mNoticeManager;
    Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity().getApplicationContext();
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
        mLayout = (ListPreference) findPreference(PREF_LAYOUT);
        mLayout.setOnPreferenceChangeListener(this);
        mLayout.setValue(Integer.toString(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUS_BAR_LAYOUT, 0)));

        mTestNotification = (PreferenceScreen) findPreference(PREF_TEST_NOTICE);
        mTestNotification.setOnPreferenceClickListener(
                new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent();
                mNoticeManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                mNoticeManager.cancelAll();
                Notification testNote = new Notification(R.mipmap.ic_launcher, TEST_SHORT,
                        System.currentTimeMillis());
                testNote.setLatestEventInfo(mContext, TEST_TITLE,
                        TEST_MESSAGE, PendingIntent.getActivity(mContext,
                        0, intent, PendingIntent.FLAG_CANCEL_CURRENT));
                mNoticeManager.notify(0, testNote);
                return true;
            }
        });

        if (mTablet) {
            PreferenceScreen prefs = getPreferenceScreen();
            prefs.removePreference(mStatusBarBrightnessToggle);
            prefs.removePreference(mAutoHideToggles);
            prefs.removePreference(mDefaultSettingsButtonBehavior);
        }

        setHasOptionsMenu(true);
        updateSettings();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.statusbar_general, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.reset:
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.STATUSBAR_SHOW_DATE, 0);
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.STATUSBAR_REMOVE_AOSP_SETTINGS_LINK, 0);
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.STATUSBAR_REMOVE_LIQUIDCONTROL_LINK, 0);
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.STATUSBAR_DATE_FORMAT, 0);
                Settings.System.putFloat(getActivity().getContentResolver(),
                        Settings.System.STATUSBAR_EXPANDED_BOTTOM_ALPHA, STATUSBAR_EXPANDED_ALPHA_DEFAULT);
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.STATUSBAR_EXPANDED_BACKGROUND_COLOR, STATUSBAR_EXPANDED_COLOR_DEFAULT);
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.STATUSBAR_UNEXPANDED_COLOR, STATUSBAR_UNEXPANDED_COLOR_DEFAULT);

                updateSettings();
                return true;
            default:
                return super.onContextItemSelected(item);
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
        } else if (pref == mLayout) {
            int val = Integer.parseInt((String) newValue);
            success = Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_LAYOUT, val);
        }

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

