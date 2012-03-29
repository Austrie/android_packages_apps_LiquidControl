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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.Spannable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;

public class BackupRestore extends SettingsPreferenceFragment {

    private static final String TAG = "BackupRestore";
    private static final boolean DEBUG = true;
    private static final boolean CLASS_DEBUG = false;
    private static final String BLANK = "";
    private static final String BACKUP_PREF = "backup";
    private static final String RESTORE_PREF = "restore";
    private static final String THEME_CAT_PREF = "theme_cat";
    private static final String THEME_EXILED_PREF = "theme_exiled";
    private static final String THEME_UNAFFILIATED_PREF = "theme_unaffiliated";
    private static final String THEME_NITROZ_PREF = "theme_nitroz";
    private static final int EXILED = 1;
    private static final int UNAFFILIATED = 2;
    private static final int NITROZ = 3;
    private static String CONFIG_FILENAME = null;
    private static String CONFIG_CHECK_PASS = "/sdcard/LiquidControl/%s and dependant restore files have been created";
    private static final String PATH_TO_CONFIGS = "/sdcard/LiquidControl/";
    private static final String PATH_TO_VALUES = "/sdcard/LiquidControl/backup";
    private static final String PATH_TO_THEMES = "/sdcard/LiquidControl/themes";
    private static boolean success = false;
    private final String OPEN_FILENAME = "open_filepath";
    private final String SAVE_FILENAME = "save_filepath";
    private static boolean FOUND_CLASS = false;
    private static final String MESSAAGE_TO_HEAD_FILE = "~XXX~ BE CAREFUL EDITING BY HAND ~XXX~ you have been warned!";

    // to hold our lists
    String[] array;
    ArrayList<String> settingsArray = new ArrayList<String>();

    PreferenceScreen prefs;
    PreferenceScreen mBackup;
    PreferenceScreen mRestore;
    PreferenceCategory mThemeCat;

    Properties mProperties = new Properties();

    @Override
    public void onCreate(Bundle didOrientationChange) {
        super.onCreate(didOrientationChange);

        addPreferencesFromResource(R.xml.backup_restore);
        prefs = getPreferenceScreen();
        mBackup = (PreferenceScreen) prefs.findPreference(BACKUP_PREF);
        mRestore = (PreferenceScreen) prefs.findPreference(RESTORE_PREF);

        // gain reference to theme category so we can drop our prefs if not found
        mThemeCat = (PreferenceCategory) prefs.findPreference(THEME_CAT_PREF);

        // themes live in the theme category while the theme category lives on the PreferenceScreen
        prefs.removePreference(mThemeCat);
        setupArrays();

        // make required dirs and disable themes if unavailable
        // be sure we have the directories we need or everything fails
        File makeDirs = new File(PATH_TO_VALUES);
        File themersDirs = new File(PATH_TO_THEMES);
        String[] allThemesFound = themersDirs.list();
        if (DEBUG) Log.d(TAG, themersDirs.list().toString());

        if (!makeDirs.exists()) {
            if (!makeDirs.mkdirs()) {
                Log.d(TAG, "failed to create the required directories");
            }
        }

        // add themes if found
        // TODO: read and load these dynamically
        if (allThemesFound != null) {
            prefs.addPreference(mThemeCat);
            for (final String theme_ : allThemesFound) {
                // don't try to load directories as themes
                File tf = new File(PATH_TO_THEMES, theme_);
                if (!tf.isDirectory()) {
                    try {
                        File themeFile = new File(PATH_TO_THEMES, theme_);
                        PreferenceScreen newTheme = getPreferenceManager().createPreferenceScreen(mContext);
                        // use namespace for key
                        newTheme.setKey(theme_);
                        FileReader fReader = new FileReader(themeFile);
                        Properties mThemeProps = new Properties();
                        mThemeProps.load(fReader);
                        // look for some strings to set title and summary in config file
                        String returnedTitle = ((String) mThemeProps.get("title"));
                        String returnedSummary = ((String) mThemeProps.get("summary"));
                        if (returnedTitle != null) newTheme.setTitle(returnedTitle);
                        // use the filename is we have nothing else
                        else newTheme.setTitle(theme_);

                        if (returnedSummary != null) newTheme.setSummary(returnedSummary);
                        else newTheme.setSummary(BLANK);
                        newTheme.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                                @Override
                                public boolean onPreferenceClick(Preference newTheme) {
                                    return restore(theme_, true);
                                }
                        });

                        // now we have all the info lets add our new preference to the screen
                        mThemeCat.addPreference(newTheme);
                    } catch (NullPointerException npe){
                        // theme file was not found this shouldn't happen but just in case
                        npe.printStackTrace();
                    } catch (FileNotFoundException noFile) {
                        if (DEBUG) noFile.printStackTrace();
                    } catch (IOException io) {
                        if (DEBUG) io.printStackTrace();
                    }
                }
            }
        }
    }

    private boolean runBackup(String bkname, String title_text, String summary_text) {
        // for debugging
        FOUND_CLASS = false;

        if (DEBUG) Log.d(TAG, "runBackup has been called: " + bkname);
        String string_setting = null;
        int int_setting;
        float float_setting;

        int foundStrings = 0;
        int foundInts = 0;
        int foundFloats = 0;

        // use army of clones so we don't waste time reading files
        ArrayList<String> stringArray = new ArrayList<String>(settingsArray);
        ArrayList<String> floatArray = new ArrayList<String>(settingsArray);

        if (title_text != null) {
            mProperties.setProperty("title", title_text);
        }
        if (summary_text != null) {
            mProperties.setProperty("summary", summary_text);
        }

        // handle floats first and remove the handled values from stringArray
        for (final String liquid_float_setting : floatArray) {
            // only alpha is kept as a float so don't bother with the rest
            if (liquid_float_setting.contains("alpha")) {
                try {
                    float float_ = Settings.System.getFloat(getActivity().getContentResolver(), liquid_float_setting);
                    mProperties.setProperty(liquid_float_setting, String.format("%f", float_));
                    if (DEBUG) Log.d(TAG, "floats:  {" + liquid_float_setting + "} returned value {" + float_ + "}");
                    stringArray.remove(liquid_float_setting);
                    foundFloats = foundFloats + 1;
                } catch (SettingNotFoundException notFound) {
                    if (CLASS_DEBUG) notFound.printStackTrace();
                } catch (ClassCastException cce) {
                    if (CLASS_DEBUG) cce.printStackTrace();
                } catch (NumberFormatException badFloat) {
                    if (CLASS_DEBUG) badFloat.printStackTrace();
                }
            }
        }

        // strings can almost always be handled so do it last
        for (final String liquid_string_setting : stringArray) {
            try {
                string_setting = Settings.System.getString(getActivity().getContentResolver(), liquid_string_setting);
                try {
                    // it's an int so set it as so
                    int testIsANumber = Integer.valueOf(string_setting);
                    try {
                        testIsANumber = Settings.System.getInt(getActivity().getContentResolver(), liquid_string_setting);
                        mProperties.setProperty(liquid_string_setting, String.format("%d", testIsANumber));
                        foundInts = foundInts + 1;
                        Log.d(TAG, String.format("Ints: {%s} returned value {%s}",
                                liquid_string_setting, string_setting));
                    } catch (SettingNotFoundException noSetting) {
                        // not found
                    }
                } catch (NumberFormatException ne) {
                    // it's a string not a number
                    if (string_setting != null) {
                        mProperties.setProperty(liquid_string_setting, string_setting);
                        foundStrings = foundStrings + 1;
                        Log.d(TAG, String.format("Strings: {%s} returned value {%s}",
                                liquid_string_setting, string_setting));
                    }
                }
                FOUND_CLASS = true;
            } catch (ClassCastException cce) {
                if (CLASS_DEBUG) cce.printStackTrace();
            } catch (NullPointerException npe) {
                npe.printStackTrace();
            }
        }

        if (DEBUG) {
            Log.d(TAG, "How many properties were found and handled? Strings: " + foundStrings
                    + "	Ints: " + foundInts + "	Floats: " + foundFloats);
            Log.d(TAG, "how long are our lists? Strings: " + stringArray.size() + "	Floats: " + floatArray.size());
        }

        if (mProperties != null) {
           try {
               // TODO fix paths
               File storeFile = new File(bkname);
               mProperties.store(new FileOutputStream(storeFile), MESSAAGE_TO_HEAD_FILE);
               if (DEBUG) Log.d(TAG, "Does storeFile exist? " + storeFile.exists() + "	AbsolutPath: " + storeFile.getAbsolutePath());
               success = true;
           } catch (FileNotFoundException fnfe) {
               fnfe.printStackTrace();
           } catch (IOException ioe) {
               ioe.printStackTrace();
           }
        } else {
           if (DEBUG) Log.d(TAG, "mProperties was null");
        }

        // Notify user if files were created correctly
        if (checkConfigFiles(bkname)) {
            Toast.makeText(mContext, String.format(CONFIG_CHECK_PASS,
                    bkname), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext, "We encountered a problem, restore not created",
                    Toast.LENGTH_SHORT).show();
        }

        return success;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen prefScreen,
            Preference pref) {
        if (pref == mBackup) {
            if (DEBUG) Log.d(TAG, "calling backup method");
            saveConfig();
            return true;
        } else if (pref == mRestore) {
            if (DEBUG) Log.d(TAG, "calling restore method");
            // we don't boolean this one because we must involve another class
            runRestore();
            return true;
        }
        //TODO: we should also have a complete return to fresh wipe
        return super.onPreferenceTreeClick(prefScreen, pref);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO this prob should be given to a Handler() as to be async because this makes the system freak out
        if (DEBUG) Log.d(TAG, "requestCode=" + requestCode + "	resultCode=" + resultCode + "	Intent data=" + data);
        if (requestCode == 1) {
            // restore
            try {
                String supplied = data.getStringExtra(OPEN_FILENAME);
                // false because user saved configs are not themes
                restore(supplied, false);
            } catch (NullPointerException np) {
                // user backed out of filepicker just move on
            }
        } else if (requestCode == 2) {
            // save
            try {
                String supplied = data.getStringExtra(SAVE_FILENAME);
                if (supplied.contains("LiquidControl/themes/")) {
                    getUserSuppliedThemeInfo(supplied);
                } else {
                    runBackup(supplied, null, null);
                }
            } catch (NullPointerException np) {
                // user backed out of filepicker nothing to see here
            }
        } else {
            // request code wasn't what we sent
            Log.wtf(TAG, "This shouldn't ever happen ...shit is fucked up");
        }
    }

    private boolean checkConfigFiles(String pathToConfig) {
        File configNameSpace = new File(pathToConfig);
        if (configNameSpace.exists() && configNameSpace.isFile() && configNameSpace.canRead()) {
            if (DEBUG) Log.d(TAG, "config files have been saved for: {" + configNameSpace.getAbsolutePath() + "}");
            return true;
        } else {
            if (DEBUG) Log.d(TAG, "config checks failed for: {" + configNameSpace.getAbsolutePath() + "}");
            return false;
        }
    }

    private void saveConfig() {
        // call the file picker then apply in the result
        Intent save_file = new Intent(mContext, com.liquid.control.tools.FilePicker.class);
        save_file.putExtra(SAVE_FILENAME, BLANK);
        // true because we are saving
        save_file.putExtra("action", true);
        // provide a path to start the user off on
        save_file.putExtra("path", PATH_TO_CONFIGS);
        // let users go where ever they want
        save_file.putExtra("lock_dir", false);
        // result code can be whatever but must match requestCode in onActivityResult
        startActivityForResult(save_file, 2);
    }

    private void runRestore() {
        // call the file picker then apply in the result
        Intent open_file = new Intent(mContext, com.liquid.control.tools.FilePicker.class);
        open_file.putExtra(OPEN_FILENAME, BLANK);
        // false because we are not saving
        open_file.putExtra("action", false);
        // provide a path to start the user off on
        open_file.putExtra("path", PATH_TO_CONFIGS);
        // let users go where ever they want
        open_file.putExtra("lock_dir", false);
        // result code can be whatever but must match requestCode in onActivityResult
        startActivityForResult(open_file, 1);
    }

    private boolean restore(String open_data_string, boolean isTheme) {
        try {
            Log.d(TAG, String.format("extra open data found: %s", open_data_string));

            // always reset the arrays so we don't get confused with the last index each array
            setupArrays();

            // determine the name to be used for opening saved config files
            File nameSpaceFile = new File(open_data_string);
            File testDirectories = new File(PATH_TO_VALUES);
            final String userSuppliedFilename = nameSpaceFile.getName();
            if (DEBUG) {
                Log.d(TAG, String.format("userSuppliedFilename=%s for nameSpaceFile=%s", userSuppliedFilename, nameSpaceFile));
                Log.d(TAG, "Do our directories exist? " + testDirectories.isDirectory());
            }

            // theme path is final but let user restores can come from anywhere
            final String filename_strings = open_data_string;
            final String theme_filename_strings = String.format("%s/LiquidControl/themes/%s",
                    Environment.getExternalStorageDirectory(), userSuppliedFilename);

            // TODO handle missing files

            // TODO/XXX should we consider filesystem space? our configs are very small (we don't save drawables, yet)
            //    and our sdcard is 16gb so for now we won't worry about it

            // first the strings
            try {
                File configFile;
                if (isTheme) {
                    configFile = new File(theme_filename_strings);
                    if (DEBUG) Log.d(TAG, "Theme detected " + theme_filename_strings);
                } else {
                    configFile = new File(filename_strings);
                }
                if (DEBUG) Log.d(TAG, String.format("Config file {%s}	Exists? %s	CanRead? %s",
                        configFile.getPath(), configFile.exists(), configFile.canRead()));
                FileReader reader = new FileReader(configFile);
                mProperties.load(reader);

                // reset our indexes
                setupArrays();

                // use an army of clones for our dirty work -I think this is how the deathstar was started
                ArrayList<String> array_strings = new ArrayList<String>(settingsArray);
                ArrayList<String> array_ints = new ArrayList<String>(settingsArray);
                ArrayList<String> array_floats = new ArrayList<String>(settingsArray);

                int stringsHandled = 0;
                int intsHandled = 0;
                int floatsHandled = 0;

                for (String intPropCheck : array_ints) {
                    // don't handle floats here
                    if (!intPropCheck.contains("alpha")) {
                        if ((String) mProperties.get(intPropCheck) != null) {
                            try {
                                if (DEBUG) Log.d(TAG, String.format("Int property found: %s	value: %s",
                                        intPropCheck, (String) mProperties.get(intPropCheck)));
                                Settings.System.putInt(mContext.getContentResolver(), intPropCheck,
                                        Integer.parseInt((String) mProperties.get(intPropCheck)));
                                intsHandled = intsHandled + 1;
                                // if we handle the property remove it from the other lists
                                array_strings.remove(intPropCheck);
                                array_floats.remove(intPropCheck);
                            } catch  (NumberFormatException nfe) {
                                if (CLASS_DEBUG) nfe.printStackTrace();
                            } catch (ClassCastException cce) {
                                // ok it's not a int
                            }
                        }
                    }
                }

                for (String floatPropCheck : array_floats) {
                    if (floatPropCheck.contains("alpha")) {
                        if ((String) mProperties.get(floatPropCheck) != null) {
                            if (DEBUG) Log.d(TAG, String.format("Float property found: %s	value: %s",
                                    floatPropCheck, (String) mProperties.get(floatPropCheck)));
                            try {
                                Settings.System.putFloat(mContext.getContentResolver(), floatPropCheck,
                                        Float.parseFloat((String) mProperties.get(floatPropCheck)));
                                array_strings.remove(floatPropCheck);
                            } catch  (NumberFormatException nfe) {
                                if (DEBUG) nfe.printStackTrace();
                            } catch (ClassCastException cce) {
                                // ok it's not a float
                            }
                        }
                    }
                }

                // we now have an array that contains only strings
                for (String stringPropCheck : array_strings) {
                    if ((String) mProperties.get(stringPropCheck) != null) {
                        if (DEBUG) Log.d(TAG, String.format("String Property found: %s	value: %s",
                                stringPropCheck, (String) mProperties.get(stringPropCheck)));
                        try {
                            Settings.System.putString(mContext.getContentResolver(), stringPropCheck,
                                    (String) mProperties.get(stringPropCheck));
                        } catch (NumberFormatException nfe) {
                            if (DEBUG) nfe.printStackTrace();
                        } catch (ClassCastException cce) {
                            // this really shouldn't happen at this point
                        }
                    }
                }
            } catch (Exception e) {
                // TODO covering all my bases not sure what this could throw ...lazy
                if (DEBUG) e.printStackTrace();
            }
        } catch (NullPointerException npe) {
            // let the user know and move on
            Toast.makeText(mContext, "no file was returned", Toast.LENGTH_SHORT).show();
        }
        // TODO return a real value here
        return true;
    }

    private void getUserSuppliedThemeInfo(final String filePath) {
        // get a view to work with
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customLayout = inflater.inflate(R.layout.save_theme_dialog, null);
        final EditText titleText = (EditText) customLayout.findViewById(R.id.title_input_edittext);
        final EditText summaryText = (EditText) customLayout.findViewById(R.id.summary_input_edittext);

        AlertDialog.Builder getInfo = new AlertDialog.Builder(getActivity());
        getInfo.setTitle(getString(R.string.name_theme_title));
        getInfo.setView(customLayout);

        getInfo.setPositiveButton(getString(R.string.positive_button), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // get supplied info
                String value_title = ((Spannable) titleText.getText()).toString();
                String value_summary = ((Spannable) summaryText.getText()).toString();
                if (DEBUG) Log.d(TAG, String.format("found title: %s 	found summary: %s", value_title, value_summary));
                runBackup(filePath, value_title, value_summary);
            }
        });
        getInfo.setNegativeButton(getString(R.string.negative_button), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // just run a normal backup in the theme dir
                runBackup(filePath, null, null);
            }
        });
        getInfo.show();
    }

    private void setupArrays() {
        // be sure we start fresh each time we load
        settingsArray.clear();

        /* XXX These data sets are a pain to maintain so PLEASE KEEP UP TODATE!!! XXX */
        // Strings first
        // UserInterface
        settingsArray.add(Settings.System.CUSTOM_CARRIER_LABEL);
        // StatusBarToggles
        settingsArray.add(Settings.System.STATUSBAR_TOGGLES);
        // Misc
        settingsArray.add(Settings.System.WIDGET_BUTTONS);
        //settingsArray.add(Settings.System.LOCKSCREEN_CUSTOM_APP_ICONS); // TODO String[] can't be handled yet
        //settingsArray.add(Settings.System.LOCKSCREEN_CUSTOM_APP_ACTIVITIES); // TODO String[] can't be handled yet

        // ints next
        // UserInterface
        settingsArray.add(Settings.System.ACCELEROMETER_ROTATION_ANGLES);
        settingsArray.add(Settings.System.HORIZONTAL_RECENTS_TASK_PANEL);
        settingsArray.add(Settings.System.CRT_OFF_ANIMATION);
        settingsArray.add(Settings.System.SCREENSHOT_CAMERA_SOUND);
        settingsArray.add(Settings.System.SHOW_STATUSBAR_IME_SWITCHER);
        settingsArray.add(Settings.Secure.KILL_APP_LONGPRESS_BACK);
        settingsArray.add(Settings.System.ACCELEROMETER_ROTATION_SETTLE_TIME);
        // Navbar
        settingsArray.add(Settings.System.MENU_LOCATION);
        settingsArray.add(Settings.System.MENU_VISIBILITY);
        settingsArray.add(Settings.System.NAVIGATION_BAR_TINT);
        settingsArray.add(Settings.System.NAVIGATION_BAR_BACKGROUND_COLOR);
        settingsArray.add(Settings.System.NAVIGATION_BAR_HOME_LONGPRESS);
        //settingsArray.add(Settings.System.NAVIGATION_BAR_GLOW_DURATION); // TODO String[] can't be handled yet
        settingsArray.add(Settings.System.NAVIGATION_BAR_WIDTH);
        settingsArray.add(Settings.System.NAVIGATION_BAR_HEIGHT);
        // Lockscreen
        settingsArray.add(Settings.System.LOCKSCREEN_CUSTOM_TEXT_COLOR);
        settingsArray.add(Settings.System.LOCKSCREEN_LAYOUT);
        settingsArray.add(Settings.System.LOCKSCREEN_ENABLE_MENU_KEY);
        settingsArray.add(Settings.Secure.LOCK_SCREEN_LOCK_USER_OVERRIDE);
        settingsArray.add(Settings.System.SHOW_LOCK_BEFORE_UNLOCK);
        settingsArray.add(Settings.System.LOCKSCREEN_BATTERY);
        settingsArray.add(Settings.System.VOLUME_WAKE_SCREEN);
        settingsArray.add(Settings.System.VOLUME_MUSIC_CONTROLS);
        settingsArray.add(Settings.System.LOCKSCREEN_HIDE_NAV);
        settingsArray.add(Settings.System.LOCKSCREEN_LANDSCAPE);
        settingsArray.add(Settings.System.LOCKSCREEN_QUICK_UNLOCK_CONTROL);
        settingsArray.add(Settings.System.ENABLE_FAST_TORCH);
        //settingsArray.add(Settings.System.LOCKSCREEN_LOW_BATTERY);
        // Powermenu
        settingsArray.add(Settings.System.POWER_DIALOG_SHOW_AIRPLANE);
        settingsArray.add(Settings.System.POWER_DIALOG_SHOW_EASTEREGG);
        settingsArray.add(Settings.System.POWER_DIALOG_SHOW_FLASHLIGHT);
        settingsArray.add(Settings.System.POWER_DIALOG_SHOW_HIDENAVBAR);
        settingsArray.add(Settings.System.POWER_DIALOG_SHOW_POWERSAVER);
        settingsArray.add(Settings.System.POWER_DIALOG_SHOW_PROFILES);
        settingsArray.add(Settings.System.POWER_DIALOG_SHOW_SCREENSHOT);
        // Powersaver
        settingsArray.add(Settings.Secure.POWER_SAVER_MODE);
        settingsArray.add(Settings.Secure.POWER_SAVER_DATA_MODE);
        settingsArray.add(Settings.Secure.POWER_SAVER_DATA_DELAY);
        settingsArray.add(Settings.Secure.POWER_SAVER_SYNC_MODE);
        settingsArray.add(Settings.Secure.POWER_SAVER_SYNC_INTERVAL);
        settingsArray.add(Settings.Secure.POWER_SAVER_WIFI_MODE);
        settingsArray.add(Settings.Secure.POWER_SAVER_SYNC_DATA_MODE);
        settingsArray.add(Settings.Secure.POWER_SAVER_SYNC_MOBILE_PREFERENCE);
        //Led
        settingsArray.add(Settings.System.NOTIFICATION_LIGHT_OFF);
        settingsArray.add(Settings.System.NOTIFICATION_LIGHT_ON);
        settingsArray.add(Settings.Secure.LED_SCREEN_ON);
        settingsArray.add(Settings.System.NOTIFICATION_LIGHT_COLOR);
        // StatusBarGeneral
        settingsArray.add(Settings.System.STATUSBAR_SHOW_DATE);
        settingsArray.add(Settings.System.STATUSBAR_DATE_FORMAT);
        settingsArray.add(Settings.System.STATUSBAR_REMOVE_AOSP_SETTINGS_LINK);
        settingsArray.add(Settings.System.STATUSBAR_SETTINGS_BEHAVIOR);
        settingsArray.add(Settings.System.STATUSBAR_QUICKTOGGLES_AUTOHIDE);
        settingsArray.add(Settings.System.STATUSBAR_DATE_BEHAVIOR);
        settingsArray.add(Settings.System.STATUS_BAR_BRIGHTNESS_TOGGLE);
        settingsArray.add(Settings.System.STATUSBAR_REMOVE_LIQUIDCONTROL_LINK);
        settingsArray.add(Settings.Secure.ADB_ICON);
        settingsArray.add(Settings.System.STATUSBAR_WINDOWSHADE_USER_BACKGROUND);
        settingsArray.add(Settings.System.STATUSBAR_UNEXPANDED_COLOR);
        settingsArray.add(Settings.System.STATUSBAR_EXPANDED_BACKGROUND_COLOR);
        settingsArray.add(Settings.System.STATUS_BAR_LAYOUT);
        settingsArray.add(Settings.System.STATUSBAR_WINDOWSHADE_HANDLE_IMAGE);
        // StatusBarToggles
        settingsArray.add(Settings.System.STATUSBAR_TOGGLES_USE_BUTTONS);
        settingsArray.add(Settings.System.STATUSBAR_TOGGLES_BRIGHTNESS_LOC);
        settingsArray.add(Settings.System.STATUSBAR_TOGGLES_STYLE);
        // StatusBarClock
        settingsArray.add(Settings.System.STATUSBAR_CLOCK_STYLE);
        settingsArray.add(Settings.System.STATUSBAR_CLOCK_AM_PM_STYLE);
        settingsArray.add(Settings.System.STATUSBAR_SHOW_ALARM);
        settingsArray.add(Settings.System.STATUSBAR_CLOCK_COLOR);
        settingsArray.add(Settings.System.STATUSBAR_CLOCK_WEEKDAY);
        // StatusBarBattery
        settingsArray.add(Settings.System.STATUSBAR_BATTERY_ICON);
        settingsArray.add(Settings.System.STATUSBAR_BATTERY_BAR);
        settingsArray.add(Settings.System.STATUSBAR_BATTERY_BAR_STYLE);
        settingsArray.add(Settings.System.STATUSBAR_BATTERY_BAR_ANIMATE);
        settingsArray.add(Settings.System.STATUSBAR_BATTERY_BAR_THICKNESS);
        settingsArray.add(Settings.System.STATUSBAR_BATTERY_BAR_COLOR);
        // StatusBarSignal
        settingsArray.add(Settings.System.STATUSBAR_SIGNAL_TEXT);
        settingsArray.add(Settings.System.STATUSBAR_SIGNAL_TEXT_COLOR);
        settingsArray.add(Settings.System.STATUSBAR_SIXBAR_SIGNAL);
        settingsArray.add(Settings.System.STATUSBAR_HIDE_SIGNAL_BARS);
        settingsArray.add(Settings.System.STATUSBAR_WIFI_SIGNAL_TEXT);
        settingsArray.add(Settings.System.STATUSBAR_WIFI_SIGNAL_TEXT_COLOR);
        // Misc
        settingsArray.add(Settings.System.EXPANDED_VIEW_WIDGET);
        settingsArray.add(Settings.System.IS_TABLET);

        // floats next
        // Navbar
        settingsArray.add(Settings.System.NAVIGATION_BAR_BUTTON_ALPHA);
        // StatusBarGeneral
        settingsArray.add(Settings.System.STATUSBAR_EXPANDED_BOTTOM_ALPHA);
        settingsArray.add(Settings.System.STATUSBAR_UNEXPANDED_ALPHA);
        settingsArray.add(Settings.System.STATUSBAR_HANDLE_ALPHA);

        // randomize arrays so we don't overly annoy any one area
        Collections.shuffle(settingsArray);
    }
}
