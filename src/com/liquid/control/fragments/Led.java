
package com.liquid.control.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.liquid.control.R;
import com.liquid.control.SettingsPreferenceFragment;
import com.liquid.control.util.ShortcutPickerHelper;

public class Led extends SettingsPreferenceFragment {

    public static final String TAG = "LEDPreferences";
    private ShortcutPickerHelper mPicker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //mPicker = new ShortcutPickerHelper(this, this);

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ShortcutPickerHelper.REQUEST_PICK_SHORTCUT
                    || requestCode == ShortcutPickerHelper.REQUEST_PICK_APPLICATION
                    || requestCode == ShortcutPickerHelper.REQUEST_CREATE_SHORTCUT) {
                mPicker.onActivityResult(requestCode, resultCode, data);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
