
package com.liquid.control.fragments;

import java.io.File;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.text.Spannable;
import android.widget.EditText;

import com.liquid.control.R;
import com.liquid.control.SettingsPreferenceFragment;
import com.liquid.control.util.CMDProcessor;
import com.liquid.control.util.Helpers;
import com.liquid.control.util.SystemRootTools;

public class UserInterface extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String PREF_CRT_ON = "crt_on";
    private static final String PREF_CRT_OFF = "crt_off";
    private static final String PREF_CUSTOM_CARRIER_LABEL = "custom_carrier_label";
    private static final String PREF_IME_SWITCHER = "ime_switcher";
    private static final String PREF_LONGPRESS_TO_KILL = "longpress_to_kill";
    private static final String PREF_LONGPRESS_APP_TASKER = "longpress_app_tasker";
    private static final String PREF_ROTATION_ANIMATION = "rotation_animation_delay";
    private static final String PREF_180 = "rotate_180";
    private static final String PREF_DISABLE_SCREENSHOT_SOUND = "screenshot_sound";

    CheckBoxPreference mAllow180Rotation;
    ListPreference mAnimationRotationDelay;
    CheckBoxPreference mCrtOffAnimation;
    CheckBoxPreference mCrtOnAnimation;
    Preference mCustomLabel;
    CheckBoxPreference mDisableBootAnimation;
    CheckBoxPreference mDisableBugMailer;
    CheckBoxPreference mHorizontalAppSwitcher;
    CheckBoxPreference mLongPressToKill;
    CheckBoxPreference mShowImeSwitcher;
    CheckBoxPreference mLongPressAppTasker;
    CheckBoxPreference mDisableScreenshotSound;

    String mCustomLabelText = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_ui);
        PreferenceScreen prefs = getPreferenceScreen();

        mCrtOffAnimation = (CheckBoxPreference) findPreference(PREF_CRT_OFF);
        mCrtOffAnimation.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.CRT_OFF_ANIMATION, 1) == 1);

        mCrtOnAnimation = (CheckBoxPreference) findPreference(PREF_CRT_ON);
        mCrtOnAnimation.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.CRT_ON_ANIMATION, 0) == 1);

        mShowImeSwitcher = (CheckBoxPreference) findPreference(PREF_IME_SWITCHER);
        mShowImeSwitcher.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.SHOW_STATUSBAR_IME_SWITCHER, 0) == 1);

        mCustomLabel = findPreference(PREF_CUSTOM_CARRIER_LABEL);
        updateCustomLabelTextSummary();

        mLongPressToKill = (CheckBoxPreference) findPreference(PREF_LONGPRESS_TO_KILL);
        mLongPressToKill.setChecked(Settings.Secure.getInt(getActivity().getContentResolver(),
                Settings.Secure.KILL_APP_LONGPRESS_BACK, 0) == 1);

        mAnimationRotationDelay = (ListPreference) findPreference(PREF_ROTATION_ANIMATION);
        mAnimationRotationDelay.setOnPreferenceChangeListener(this);
        mAnimationRotationDelay.setValue(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.ACCELEROMETER_ROTATION_SETTLE_TIME,
                200) + "");

        mAllow180Rotation = (CheckBoxPreference) findPreference(PREF_180);
        mAllow180Rotation.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.ACCELEROMETER_ROTATION_ANGLES, (1 | 2 | 8)) == (1 | 2 | 4 | 8));

        mHorizontalAppSwitcher = (CheckBoxPreference) findPreference("horizontal_recents_task_panel");
        mHorizontalAppSwitcher.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.HORIZONTAL_RECENTS_TASK_PANEL, 0) == 1);

        mDisableBootAnimation = (CheckBoxPreference) findPreference("disable_bootanimation");
        mDisableBootAnimation.setChecked(!new File("/system/media/bootanimation.zip").exists());

        mDisableBugMailer = (CheckBoxPreference) findPreference("disable_bugmailer");
        mDisableBugMailer.setChecked(!new File("/system/bin/bugmailer.sh").exists());

        /* DISABLED TILL WE HAVE FRAMEWORKS SUPPORT
        mLongPressAppTasker = (CheckBoxPreference) findPreference("longpress_app_tasker");
        mLongPressAppTasker.setChecked(Settings.Secure.getInt(getActivity().getContentResolver(),
                Settings.Secure.LONGPRESS_APP_TASKER_INTENT, 0) == 1));
        */

        mDisableScreenshotSound = (CheckBoxPreference) findPreference(PREF_DISABLE_SCREENSHOT_SOUND);
        mDisableScreenshotSound.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.SCREENSHOT_CAMERA_SOUND, 0) == 1);

        //TODO: summarys in ics shouldn't be dynamic; only exception is dialog input events
        // summary should be true if checked and false if unchecked
        if (mDisableBootAnimation.isChecked())
            mDisableBootAnimation.setSummary(R.string.disable_bootanimation_summary);

        if (!getResources().getBoolean(com.android.internal.R.bool.config_enableCrtAnimations)) {
            prefs.removePreference((PreferenceGroup) findPreference("crt"));
        } else {
            // can't get this working in ICS just yet
            ((PreferenceGroup) findPreference("crt")).removePreference(mCrtOnAnimation);
        }

        // update summeries that should be dynamic
        updateListPrefs();
    }

    private void updateCustomLabelTextSummary() {
        mCustomLabelText = Settings.System.getString(getActivity().getContentResolver(),
                Settings.System.CUSTOM_CARRIER_LABEL);
        if (mCustomLabelText == null) {
            mCustomLabel
                    .setSummary("Custom label currently not set. Once you specify a custom one, there's no way back without doing a data wipe.");
        } else {
            mCustomLabel.setSummary(mCustomLabelText);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mCrtOffAnimation) {
            boolean checked = ((CheckBoxPreference) preference).isChecked();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.CRT_OFF_ANIMATION, checked ? 1 : 0);
            return true;
        } else if (preference == mCrtOnAnimation) {
            boolean checked = ((CheckBoxPreference) preference).isChecked();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.CRT_ON_ANIMATION, checked ? 1 : 0);
            return true;
        } else if (preference == mShowImeSwitcher) {
            boolean checked = ((CheckBoxPreference) preference).isChecked();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.SHOW_STATUSBAR_IME_SWITCHER, checked ? 1 : 0);
            return true;
        } else if (preference == mCustomLabel) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
            alert.setTitle("Custom Carrier Label");
            alert.setMessage("Please enter a new one!");

            // Set an EditText view to get user input
            final EditText input = new EditText(getActivity());
            input.setText(mCustomLabelText != null ? mCustomLabelText : "");
            alert.setView(input);

            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String value = ((Spannable) input.getText()).toString();
                    Settings.System.putString(getActivity().getContentResolver(),
                            Settings.System.CUSTOM_CARRIER_LABEL, value);
                    updateCustomLabelTextSummary();
                }
            });

            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                }
            });

            alert.show();
        } else if (preference == mLongPressToKill) {
            boolean checked = ((CheckBoxPreference) preference).isChecked();
            Settings.Secure.putInt(getActivity().getContentResolver(),
                    Settings.Secure.KILL_APP_LONGPRESS_BACK, checked ? 1 : 0);
            return true;
        } else if (preference == mAllow180Rotation) {
            boolean checked = ((CheckBoxPreference) preference).isChecked();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.ACCELEROMETER_ROTATION_ANGLES, checked ? (1 | 2 | 4 | 8)
                            : (1 | 2 | 8));
            return true;
        } else if (preference == mHorizontalAppSwitcher) {
            boolean checked = ((CheckBoxPreference) preference).isChecked();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.HORIZONTAL_RECENTS_TASK_PANEL, checked ? 1
                            : 0);
            restartSystemUI();
            return true;
        } else if (preference == mDisableBootAnimation) {
            boolean checked = ((CheckBoxPreference) preference).isChecked();
            if (checked) {
                Helpers.getMount("rw");
                new CMDProcessor().su
                        .runWaitFor("mv /system/media/bootanimation.zip /system/media/bootanimation.unicorn");
                Helpers.getMount("ro");
                preference.setSummary(R.string.disable_bootanimation_summary);
            } else {
                Helpers.getMount("rw");
                new CMDProcessor().su
                        .runWaitFor("mv /system/media/bootanimation.unicorn /system/media/bootanimation.zip");
                Helpers.getMount("ro");
            }
        } else if (preference == mDisableBugMailer) {
            boolean checked = ((CheckBoxPreference) preference).isChecked();
            if (checked) {
                Helpers.getMount("rw");
                new CMDProcessor().su
                        .runWaitFor("mv /system/bin/bugmailer.sh /system/bin/bugmailer.sh.liquid");
                Helpers.getMount("ro");
            } else {
                Helpers.getMount("rw");
                new CMDProcessor().su
                        .runWaitFor("mv /system/bin/bugmailer.sh.liquid /system/bin/bugmailer.sh");
                Helpers.getMount("ro");
            }
        } else if (preference == mDisableScreenshotSound) {
            boolean checked = ((CheckBoxPreference) preference).isChecked();
            Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.SCREENSHOT_CAMERA_SOUND, checked ? 1 : 0);
        }
        /* DISABLED TILL WE SUPPORT WITH FRAMEWORKS
          else if (preference == mLongPressAppTasker) {
            boolean checked = ((CheckBoxPreference) preference).isChecked();
            Settings.Secure.putInt(getActivity().getContentResolver(),
                    Settings.Secure.LONGPRESS_APP_TASKER_INTENT, checked ? 1 : 0);
            return true;
        } */

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean handled = false;
        if (preference == mAnimationRotationDelay) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.ACCELEROMETER_ROTATION_SETTLE_TIME,
                    Integer.parseInt((String) newValue));
            handled = true;
        }

        //update our dynamic values and return if we handled
        updateListPrefs();
        return handled;
    }

    private void restartSystemUI() {
        mContext.sendBroadcast(new Intent(SystemRootTools.ACTION_RESTART_SYSTEMUI));
    }

    public static void addButton(Context context, String key) {
        ArrayList<String> enabledToggles = Navbar
                .getButtonsStringArray(context);
        enabledToggles.add(key);
        Navbar.setButtonsFromStringArray(context, enabledToggles);
    }

    public static void removeButton(Context context, String key) {
        ArrayList<String> enabledToggles = Navbar
                .getButtonsStringArray(context);
        enabledToggles.remove(key);
        Navbar.setButtonsFromStringArray(context, enabledToggles);
    }

    private void updateListPrefs() {
        int mRotate = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.ACCELEROMETER_ROTATION_SETTLE_TIME, 200);
        mAnimationRotationDelay.setSummary(String.format("Current: %s", mRotate));
    }
}
