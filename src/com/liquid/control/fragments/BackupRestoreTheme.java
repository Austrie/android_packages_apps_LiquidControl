/*
 * Copyright (C) 2012 The LiquidSmoothROMs Project
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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.Spannable;
import android.util.Log;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.liquid.control.R;
import com.liquid.control.SettingsPreferenceFragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.ClassCastException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;


public class BackupRestoreTheme extends SettingsPreferenceFragment {

    private static final String TAG = "BRT";
    private static final boolean DEBUG = true;
    private static final boolean CLASS_DEBUG = false;
    private static final String BLANK = "";
    private static final String BACKUP_PREF = "backup";
    private static final String RESTORE_PREF = "restore";
    private static final String THEME_EXILED_PREF = "theme_exiled";
    private static final int EXILED = 1;
    private static String CONFIG_FILENAME = null;
    private static final String MESSAGE_TO_HEAD_FILE = "These values can be modified by hand but be careful as you can throw unforseen exceptions";
    private static final String PATH_TO_CONFIGS = "/sdcard/LiquidControl/";
    private static final String PATH_TO_VALUES = "/sdcard/LiquidControl/backup";
    private static boolean success = false;
    private final String OPEN_FILENAME = "open_filepath";

    // to hold our lists
    String[] array;
    ArrayList<String> stringSettingsArray = new ArrayList<String>();
    ArrayList<String> intSettingsArray = new ArrayList<String>();
    ArrayList<String> floatSettingsArray = new ArrayList<String>();

    PreferenceScreen mBackup;
    PreferenceScreen mRestore;
    PreferenceScreen mExiledThemer;

    Properties mStringProps = new Properties();
    Properties mIntProps = new Properties();
    Properties mFloatProps = new Properties();
    Properties mNameHolder = new Properties();

    @Override
    public void onCreate(Bundle didOrientationChange) {
        super.onCreate(didOrientationChange);

        addPreferencesFromResource(R.xml.backup_restore_theme);
        PreferenceScreen prefs = getPreferenceScreen();
        mBackup = (PreferenceScreen) prefs.findPreference(BACKUP_PREF);
        mRestore = (PreferenceScreen) prefs.findPreference(RESTORE_PREF);
        mExiledThemer = (PreferenceScreen) prefs.findPreference(THEME_EXILED_PREF);
        setupArrays();
    }

    private boolean runBackup() {
        if (DEBUG) Log.d(TAG, "runBackup has been called");
        // setup the edit text dialog
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle(getString(R.string.backup_alert_title));
        alert.setMessage(getString(R.string.backup_alert_message));

        // name the config file
        final EditText input = new EditText(getActivity());
        success = false;
        input.setText(CONFIG_FILENAME != null ? CONFIG_FILENAME : "");
        alert.setView(input);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = ((Spannable) input.getText()).toString();

                // I'm sure there is some way to check for bad chars in filename
                if (value != null || value != "" || !value.contains("!") || !value.contains("@") ||
                        !value.contains("#") || !value.contains("$") || !value.contains("%") ||
                        !value.contains("^") || !value.contains("&") || !value.contains("*") ||
                        !value.contains("(") || !value.contains(")") || !value.contains(" ")) {
                    String string_setting = null;
                    int int_setting;
                    float float_setting;
                    for (final String liquid_string_setting : stringSettingsArray) {
                        try {
                            string_setting = Settings.System.getString(getActivity().getContentResolver(), liquid_string_setting);
                            mStringProps.setProperty(liquid_string_setting, string_setting);
                            Log.d(TAG, String.format("Strings: {%s} returned value {%s}", liquid_string_setting, string_setting));
                        } catch (ClassCastException cce) {
                            if (CLASS_DEBUG) cce.printStackTrace();
                        }
                    }

                    for (final String liquid_int_setting : intSettingsArray) {
                        Log.d(TAG, "looking for int value of: " + liquid_int_setting);
                        try {
                            int int_ = Settings.System.getInt(getActivity().getContentResolver(), liquid_int_setting);
                            mIntProps.setProperty(liquid_int_setting, String.format("%d", int_));
                            if (DEBUG) Log.d(TAG, "ints: {" + liquid_int_setting + "} returned value {" + int_ + "}");
                        } catch (SettingNotFoundException notFound) {
                            if (CLASS_DEBUG) notFound.printStackTrace();
                        } catch (ClassCastException cce) {
                            if (CLASS_DEBUG) cce.printStackTrace();
                        }
                    }

                    for (final String liquid_float_setting : floatSettingsArray) {
                        Log.d(TAG, "looking for float value of: " + liquid_float_setting);
                        try {
                            float float_ = Settings.System.getFloat(getActivity().getContentResolver(), liquid_float_setting);
                            mFloatProps.setProperty(liquid_float_setting, String.format("%f", float_));
                            if (DEBUG) Log.d(TAG, "floats:  {" + liquid_float_setting + "} returned value {" + float_ + "}");
                        } catch (SettingNotFoundException notFound) {
                            if (DEBUG) notFound.printStackTrace();
                        } catch (ClassCastException cce) {
                            if (CLASS_DEBUG) cce.printStackTrace();
                        }
                    }

                    // be sure we have the directories we need or everything fails
                    File makeDirs = new File(PATH_TO_VALUES);
                    if (!makeDirs.exists()) {
                        if (!makeDirs.mkdirs()) {
                            Log.d(TAG, "failed to create the required directories");
                        }
                    }

                    if (mStringProps != null) {
                        try {
                            File storeStringFile = new File(String.format("/sdcard/LiquidControl/backup/string_%s", value));
                            mStringProps.store(new FileOutputStream(storeStringFile), null);
                            if (DEBUG) Log.d(TAG, "Does storeStringFile exist? " + storeStringFile.exists());
                            success = true;
                        } catch (FileNotFoundException fnfe) {
                            fnfe.printStackTrace();
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                    } else {
                        if (DEBUG) Log.d(TAG, "mStringProps was null");
                    }

                    if (mIntProps != null) {
                        try {
                            File storeIntFile = new File(String.format("/sdcard/LiquidControl/backup/int_%s", value));
                            mIntProps.store(new FileOutputStream(storeIntFile), null);
                            if (DEBUG) Log.d(TAG, "Does storeIntFile exist? " + storeIntFile.exists());
                            success = true;
                        } catch (FileNotFoundException fnfe) {
                            fnfe.printStackTrace();
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                    } else {
                        if (DEBUG) Log.d(TAG, "mIntProps was null");
                    }

                    if (mFloatProps != null) {
                        try {
                            File storeFloatFile = new File(String.format("/sdcard/LiquidControl/backup/float_%s", value));
                            mFloatProps.store(new FileOutputStream(storeFloatFile), null);
                            if (DEBUG) Log.d(TAG, "Does storeFloatFile exist? " + storeFloatFile.exists());
                            success = true;
                        } catch (FileNotFoundException fnfe) {
                            fnfe.printStackTrace();
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                    } else {
                        if (DEBUG) Log.d(TAG, "mFloatProps was null");
                    }

                    // we save the real settings in /sdcard/LiquidControl/backup/
                    // and a name place holder in /sdcard/LiquidControl
                    mNameHolder.setProperty(value, value);
                    try {
                        File nameSpaceFile = new File(String.format("/sdcard/LiquidControl/%s", value));
                        mNameHolder.store(new FileOutputStream(nameSpaceFile), null);
                        if (DEBUG) Log.d(TAG, "Does nameSpaceFile exist? " + nameSpaceFile.exists());
                    } catch (FileNotFoundException fnfe) {
                        fnfe.printStackTrace();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            }
        });
        alert.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // no one cares
            }
        });

        alert.show();
        return success;
    }

    private void runRestore() {
        // call the file picker then apply in the result
        Intent open_file = new Intent(mContext, com.liquid.control.tools.FilePicker.class);
        open_file.putExtra(OPEN_FILENAME, BLANK);
        open_file.putExtra("action", false);
        open_file.putExtra("path", PATH_TO_CONFIGS);
        startActivityForResult(open_file, 1);
    }

    private boolean applyTheme(int theme) {
        // TODO: duh
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen prefScreen,
            Preference pref) {
        if (pref == mBackup) {
            if (DEBUG) Log.d(TAG, "calling backup method");
            return runBackup();
        } else if (pref == mRestore) {
            if (DEBUG) Log.d(TAG, "calling restore method");
            // we don't boolean this one because we must involve another class
            runRestore();
            return true;
        } else if (pref == mExiledThemer) {
            if (DEBUG) Log.d(TAG, "calling backup method");
            return applyTheme(EXILED);
        } //TODO: we should also have a complete return to fresh wipe
        return super.onPreferenceTreeClick(prefScreen, pref);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO this prob should be given to a Handler() as to be async because this makes the system freak out
        if (DEBUG) Log.d(TAG, "requestCode=" + requestCode + "	resultCode=" + resultCode + "	Intent data=" + data);
        setupArrays();
        if (requestCode == 1) {
            try {
                final String open_data_string = data.getStringExtra(OPEN_FILENAME);
                Log.d(TAG, String.format("extra open data found: %s", open_data_string));

                // determine the name to be used for opening saved config files
                File nameSpaceFile = new File(open_data_string);
                File testDirectories = new File(PATH_TO_VALUES);
                final String userSuppliedFilename = nameSpaceFile.getName();
                if (DEBUG) {
                    Log.d(TAG, String.format("userSuppliedFilename=%s for nameSpaceFile=%s", userSuppliedFilename, nameSpaceFile));
                    Log.d(TAG, "Do our directories exist? " + testDirectories.isDirectory());
                }

                // TODO/XXX should we consider filesystem space? our configs are very small (we don't save drawables, yet)
                //    and our sdcard is 16gb so for now we won't worry about it

                // first the strings
                try{
                    String filename_strings = String.format("%s/LiquidControl/backup/string_%s",
                            Environment.getExternalStorageDirectory(), userSuppliedFilename);
                    File configStringFile = new File(filename_strings);
                    if (DEBUG) Log.d(TAG, String.format("Strings config file {%s}	Exists? %s	CanRead? %s",
                            configStringFile.getPath(), configStringFile.exists(), configStringFile.canRead()));
                    FileReader stringReader = new FileReader(configStringFile);
                    mStringProps.load(stringReader);
                    for (String stringPropCheck : stringSettingsArray) {
                        // null is returned if no setting is found
                        if ((String) mStringProps.get(stringPropCheck) != null) {
                            if (DEBUG) Log.d(TAG, String.format("String Property found: %s	value: %s",
                                    stringPropCheck, (String) mStringProps.get(stringPropCheck)));
                            try {
                                Settings.System.putString(mContext.getContentResolver(), stringPropCheck,
                                        (String) mStringProps.get(stringPropCheck));
                            } catch (NumberFormatException nfe) {
                                if (DEBUG) nfe.printStackTrace();
                            }
                        }
                    }
                } catch (IOException e) {
                    if (DEBUG) e.printStackTrace();
                }

                // next ints
                try{
                    String filename_ints = String.format("%s/LiquidControl/backup/int_%s",
                            Environment.getExternalStorageDirectory(), userSuppliedFilename);
                    File configIntFile = new File(filename_ints);
                    if (DEBUG) Log.d(TAG, String.format("Ints config file {%s}	Exists? %s	CanRead? %s",
                            configIntFile.getPath(), configIntFile.exists(), configIntFile.canRead()));
                    FileReader intsReader = new FileReader(configIntFile);
                    mIntProps.load(intsReader);
                    for (String intPropCheck : intSettingsArray) {
                        // null is returned if no setting is found
                        if ((String) mIntProps.get(intPropCheck) != null) {
                            if (DEBUG) Log.d(TAG, String.format("Int property found: %s	value: %s",
                                    intPropCheck, (String) mIntProps.get(intPropCheck)));
                            // TODO lots of deferencing going on here is this hurting performace?
                            try {
                                Settings.System.putInt(mContext.getContentResolver(), intPropCheck,
                                        Integer.parseInt((String) mIntProps.get(intPropCheck)));
                            } catch  (NumberFormatException nfe) {
                                if (DEBUG) nfe.printStackTrace();
                            }
                        }
                    }
                } catch (IOException e) {
                    if (DEBUG) e.printStackTrace();
                }

                // last floats
                try{
                    String filename_floats = String.format("%s/LiquidControl/backup/float_%s",
                            Environment.getExternalStorageDirectory(), userSuppliedFilename);
                    File configFloatFile = new File(filename_floats);
                    if (DEBUG) Log.d(TAG, String.format("Floats config file {%s} 	Exists? %s	CanRead? %s",
                            configFloatFile.getPath(), configFloatFile.exists(), configFloatFile.canRead()));
                    if (DEBUG) Log.d(TAG, "{" + filename_floats + "}");
                    FileReader floatReader = new FileReader(configFloatFile);
                    mFloatProps.load(floatReader);
                    for (String floatPropCheck : floatSettingsArray) {
                        // null is returned if no setting is found
                        if ((String) mFloatProps.get(floatPropCheck) != null) {
                            if (DEBUG) Log.d(TAG, String.format("Float property found: %s	value: %s",
                                    floatPropCheck, (String) mFloatProps.get(floatPropCheck)));
                            try {
                                Settings.System.putFloat(mContext.getContentResolver(), floatPropCheck,
                                        Float.parseFloat((String) mFloatProps.get(floatPropCheck)));
                            } catch  (NumberFormatException nfe) {
                                if (DEBUG) nfe.printStackTrace();
                            }
                        }
                    }
                } catch (IOException e) {
                    if (DEBUG) e.printStackTrace();
                }
            } catch (NullPointerException npe) {
                // let the user know and move on
                Toast.makeText(mContext, "no file was returned", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                if (DEBUG) e.printStackTrace();
            }
        } else {
            // request code wasn't what we sent
            Log.wtf(TAG, "This shouldn't ever happen ...shit is fucked up");
        }
    }

    private void setupArrays() {
        // be sure we start fresh each time we load
        stringSettingsArray.clear();
        intSettingsArray.clear();
        floatSettingsArray.clear();

        /* XXX These data sets are a pain to maintain so PLEASE KEEP UP TODATE!!! XXX */
        // Strings first
        // UserInterface
        stringSettingsArray.add(Settings.System.CUSTOM_CARRIER_LABEL);
        // StatusBarToggles
        stringSettingsArray.add(Settings.System.STATUSBAR_TOGGLES);
        // Misc
        stringSettingsArray.add(Settings.System.WIDGET_BUTTONS);

        // ints next
        // UserInterface
        intSettingsArray.add(Settings.System.ACCELEROMETER_ROTATION_ANGLES);
        intSettingsArray.add(Settings.System.HORIZONTAL_RECENTS_TASK_PANEL);
        intSettingsArray.add(Settings.System.CRT_OFF_ANIMATION);
        intSettingsArray.add(Settings.System.SCREENSHOT_CAMERA_SOUND);
        intSettingsArray.add(Settings.System.SHOW_STATUSBAR_IME_SWITCHER);
        intSettingsArray.add(Settings.Secure.KILL_APP_LONGPRESS_BACK);
        intSettingsArray.add(Settings.System.ACCELEROMETER_ROTATION_SETTLE_TIME);
        // Navbar
        intSettingsArray.add(Settings.System.MENU_LOCATION);
        intSettingsArray.add(Settings.System.MENU_VISIBILITY);
        intSettingsArray.add(Settings.System.NAVIGATION_BAR_TINT);
        intSettingsArray.add(Settings.System.NAVIGATION_BAR_BACKGROUND_COLOR);
        intSettingsArray.add(Settings.System.NAVIGATION_BAR_HOME_LONGPRESS);
        //intSettingsArray.add(Settings.System.NAVIGATION_BAR_GLOW_DURATION); TODO String[] can't be backed up yet
        intSettingsArray.add(Settings.System.NAVIGATION_BAR_WIDTH);
        intSettingsArray.add(Settings.System.NAVIGATION_BAR_HEIGHT);
        // Lockscreen
        intSettingsArray.add(Settings.System.LOCKSCREEN_CUSTOM_TEXT_COLOR);
        intSettingsArray.add(Settings.System.LOCKSCREEN_LAYOUT);
        intSettingsArray.add(Settings.System.LOCKSCREEN_ENABLE_MENU_KEY);
        intSettingsArray.add(Settings.Secure.LOCK_SCREEN_LOCK_USER_OVERRIDE);
        intSettingsArray.add(Settings.System.SHOW_LOCK_BEFORE_UNLOCK);
        intSettingsArray.add(Settings.System.LOCKSCREEN_BATTERY);
        intSettingsArray.add(Settings.System.VOLUME_WAKE_SCREEN);
        intSettingsArray.add(Settings.System.VOLUME_MUSIC_CONTROLS);
        intSettingsArray.add(Settings.System.LOCKSCREEN_HIDE_NAV);
        intSettingsArray.add(Settings.System.LOCKSCREEN_LANDSCAPE);
        intSettingsArray.add(Settings.System.LOCKSCREEN_QUICK_UNLOCK_CONTROL);
        intSettingsArray.add(Settings.System.ENABLE_FAST_TORCH);
        // Powermenu
        intSettingsArray.add(Settings.System.POWER_DIALOG_SHOW_AIRPLANE);
        intSettingsArray.add(Settings.System.POWER_DIALOG_SHOW_EASTEREGG);
        intSettingsArray.add(Settings.System.POWER_DIALOG_SHOW_FLASHLIGHT);
        intSettingsArray.add(Settings.System.POWER_DIALOG_SHOW_HIDENAVBAR);
        intSettingsArray.add(Settings.System.POWER_DIALOG_SHOW_POWERSAVER);
        intSettingsArray.add(Settings.System.POWER_DIALOG_SHOW_PROFILES);
        intSettingsArray.add(Settings.System.POWER_DIALOG_SHOW_SCREENSHOT);
        // Powersaver
        intSettingsArray.add(Settings.Secure.POWER_SAVER_MODE);
        intSettingsArray.add(Settings.Secure.POWER_SAVER_DATA_MODE);
        intSettingsArray.add(Settings.Secure.POWER_SAVER_DATA_DELAY);
        intSettingsArray.add(Settings.Secure.POWER_SAVER_SYNC_MODE);
        intSettingsArray.add(Settings.Secure.POWER_SAVER_SYNC_INTERVAL);
        intSettingsArray.add(Settings.Secure.POWER_SAVER_WIFI_MODE);
        intSettingsArray.add(Settings.Secure.POWER_SAVER_SYNC_DATA_MODE);
        intSettingsArray.add(Settings.Secure.POWER_SAVER_SYNC_MOBILE_PREFERENCE);
        //Led
        intSettingsArray.add(Settings.System.NOTIFICATION_LIGHT_OFF);
        intSettingsArray.add(Settings.System.NOTIFICATION_LIGHT_ON);
        intSettingsArray.add(Settings.Secure.LED_SCREEN_ON);
        intSettingsArray.add(Settings.System.NOTIFICATION_LIGHT_COLOR);
        // StatusBarGeneral
        intSettingsArray.add(Settings.System.STATUSBAR_SHOW_DATE);
        intSettingsArray.add(Settings.System.STATUSBAR_DATE_FORMAT);
        intSettingsArray.add(Settings.System.STATUSBAR_REMOVE_AOSP_SETTINGS_LINK);
        intSettingsArray.add(Settings.System.STATUSBAR_SETTINGS_BEHAVIOR);
        intSettingsArray.add(Settings.System.STATUSBAR_QUICKTOGGLES_AUTOHIDE);
        intSettingsArray.add(Settings.System.STATUSBAR_DATE_BEHAVIOR);
        intSettingsArray.add(Settings.System.STATUS_BAR_BRIGHTNESS_TOGGLE);
        intSettingsArray.add(Settings.System.STATUSBAR_REMOVE_LIQUIDCONTROL_LINK);
        intSettingsArray.add(Settings.Secure.ADB_ICON);
        intSettingsArray.add(Settings.System.STATUSBAR_WINDOWSHADE_USER_BACKGROUND);
        intSettingsArray.add(Settings.System.STATUSBAR_UNEXPANDED_COLOR);
        intSettingsArray.add(Settings.System.STATUS_BAR_LAYOUT);
        intSettingsArray.add(Settings.System.STATUSBAR_WINDOWSHADE_HANDLE_IMAGE);
        // StatusBarToggles
        intSettingsArray.add(Settings.System.STATUSBAR_TOGGLES_USE_BUTTONS);
        intSettingsArray.add(Settings.System.STATUSBAR_TOGGLES_BRIGHTNESS_LOC);
        intSettingsArray.add(Settings.System.STATUSBAR_TOGGLES_STYLE);
        // StatusBarClock
        intSettingsArray.add(Settings.System.STATUSBAR_CLOCK_STYLE);
        intSettingsArray.add(Settings.System.STATUSBAR_CLOCK_AM_PM_STYLE);
        intSettingsArray.add(Settings.System.STATUSBAR_SHOW_ALARM);
        intSettingsArray.add(Settings.System.STATUSBAR_CLOCK_COLOR);
        intSettingsArray.add(Settings.System.STATUSBAR_CLOCK_WEEKDAY);
        // StatusBarBattery
        intSettingsArray.add(Settings.System.STATUSBAR_BATTERY_ICON);
        intSettingsArray.add(Settings.System.STATUSBAR_BATTERY_BAR);
        intSettingsArray.add(Settings.System.STATUSBAR_BATTERY_BAR_STYLE);
        intSettingsArray.add(Settings.System.STATUSBAR_BATTERY_BAR_ANIMATE);
        intSettingsArray.add(Settings.System.STATUSBAR_BATTERY_BAR_THICKNESS);
        intSettingsArray.add(Settings.System.STATUSBAR_BATTERY_BAR_COLOR);
        // StatusBarSignal
        intSettingsArray.add(Settings.System.STATUSBAR_SIGNAL_TEXT);
        intSettingsArray.add(Settings.System.STATUSBAR_SIGNAL_TEXT_COLOR);
        intSettingsArray.add(Settings.System.STATUSBAR_SIXBAR_SIGNAL);
        // Misc
        intSettingsArray.add(Settings.System.EXPANDED_VIEW_WIDGET);
        intSettingsArray.add(Settings.System.IS_TABLET);

        // floats next
        // Navbar
        floatSettingsArray.add(Settings.System.NAVIGATION_BAR_BUTTON_ALPHA);
        // StatusBarGeneral
        floatSettingsArray.add(Settings.System.STATUSBAR_EXPANDED_BOTTOM_ALPHA);
        floatSettingsArray.add(Settings.System.STATUSBAR_UNEXPANDED_ALPHA);
    }
}