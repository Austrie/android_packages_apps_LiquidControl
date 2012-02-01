
package com.liquid.control.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.liquid.control.R;

/**
 * Memory Settings
 */
public class MemorySettings extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "MemorySettings";
    private static final String PROCESSOR = "processor";
    private static final String MEMORY_MANAGEMENT = "memory_management";
    private static final String USE_DITHERING_PREF = "pref_use_dithering";
    private static final String USE_DITHERING_PERSIST_PROP = "persist.sys.use_dithering";
    private static final String USE_DITHERING_DEFAULT = "1";
    private static final String USE_16BPP_ALPHA_PREF = "pref_use_16bpp_alpha";
    private static final String USE_16BPP_ALPHA_PROP = "persist.sys.use_16bpp_alpha";
    private static final String DISABLE_BOOTANIMATION_DEFAULT = "0";

    private CheckBoxPreference mUseDitheringPref;
    private CheckBoxPreference mUse16bppAlphaPref;
    private PreferenceScreen mProcessor;
    private PreferenceScreen mMemoryManagement;
    private AlertDialog alertDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getPreferenceManager() != null) {
            addPreferencesFromResource(R.xml.memory_management);
            PreferenceScreen prefSet = getPreferenceScreen();

            mProcessor = (PreferenceScreen) prefSet.findPreference(PROCESSOR);
            mMemoryManagement = (PreferenceScreen) prefSet.findPreference(MEMORY_MANAGEMENT);
            mUseDitheringPref = (CheckBoxPreference) prefSet.findPreference(USE_DITHERING_PREF);
            mUse16bppAlphaPref = (CheckBoxPreference) prefSet.findPreference(USE_16BPP_ALPHA_PREF);

            String useDithering = SystemProperties.get(USE_DITHERING_PERSIST_PROP, USE_DITHERING_DEFAULT);
            mUseDitheringPref.setChecked("1".equals(useDithering));

            String use16bppAlpha = SystemProperties.get(USE_16BPP_ALPHA_PROP, "0");
            mUse16bppAlphaPref.setChecked("1".equals(use16bppAlpha));

            /* Display the warning dialog */
            alertDialog = new AlertDialog.Builder(getActivity()).create();
            alertDialog.setTitle(R.string.performance_settings_warning_title);
            alertDialog.setMessage(getResources().getString(R.string.performance_settings_warning));
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                    getResources().getString(com.android.internal.R.string.ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    });
            alertDialog.show();
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mUseDitheringPref) {
            SystemProperties.set(USE_DITHERING_PERSIST_PROP, mUseDitheringPref.isChecked() ? "1" : "0");
        } else if (preference == mUse16bppAlphaPref) {
            SystemProperties.set(USE_16BPP_ALPHA_PROP, mUse16bppAlphaPref.isChecked() ? "1" : "0");
        } else {
            // If we didn't handle it, let preferences handle it.
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
        return true;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }
}
