package com.liquid.control.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileWriter;

import com.liquid.control.R;
import com.liquid.control.util.CMDProcessor;
import com.liquid.control.util.Helpers;

public class PropModder extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "LiquidControl :PropModder";
    private static final String APPEND_CMD = "echo \"%s=%s\" >> /system/build.prop";
    private static final String KILL_PROP_CMD = "busybox sed -i \"/%s/D\" /system/build.prop";
    private static final String REPLACE_CMD = "busybox sed -i \"/%s/ c %<s=%s\" /system/build.prop";
    private static final String LOGCAT_CMD = "busybox sed -i \"/log/ c %s\" /system/etc/init.d/72propmodder_script";
    private static final String SDCARD_BUFFER_CMD = "busybox sed -i \"/179:0/ c echo %s > /sys/devices/virtual/bdi/179:0/read_ahead_kb\" /system/etc/init.d/72propmodder_script";
    private static final String FIND_CMD = "grep -q \"%s\" /system/build.prop";
    private static final String REMOUNT_CMD = "busybox mount -o %s,remount -t yaffs2 /dev/block/mtdblock1 /system";
    private static final String PROP_EXISTS_CMD = "grep -q %s /system/build.prop";
    private static final String SDCARD_BUFFER_ON_THE_FLY_CMD = "echo %s > /sys/devices/virtual/bdi/179:0/read_ahead_kb";
    private static final String DISABLE = "disable";
    private static final String SHOWBUILD_PATH = "/system/tmp/showbuild";
    private static final String INIT_SCRIPT_PATH ="/system/etc/init.d/72propmodder_script";
    private static final String INIT_SCRIPT_TEMP_PATH = "/system/tmp/init_script";
    private static final String WIFI_SCAN_PREF = "pref_wifi_scan_interval";
    private static final String WIFI_SCAN_PROP = "wifi.supplicant_scan_interval";
    private static final String WIFI_SCAN_PERSIST_PROP = "persist.wifi_scan_interval";
    private static final String WIFI_SCAN_DEFAULT = System.getProperty(WIFI_SCAN_PROP);
    private static final String LCD_DENSITY_PREF = "pref_lcd_density";
    private static final String LCD_DENSITY_PROP = "ro.sf.lcd_density";
    private static final String LCD_DENSITY_PERSIST_PROP = "persist.lcd_density";
    private static final String LCD_DENSITY_DEFAULT = System.getProperty(LCD_DENSITY_PROP);
    private static final String MAX_EVENTS_PREF = "pref_max_events";
    private static final String MAX_EVENTS_PROP = "windowsmgr.max_events_per_sec";
    private static final String MAX_EVENTS_PERSIST_PROP = "persist.max_events";
    private static final String MAX_EVENTS_DEFAULT = System.getProperty(MAX_EVENTS_PROP);
    private static final String USB_MODE_PREF = "pref_usb_mode";
    private static final String USB_MODE_PROP = "ro.default_usb_mode";
    private static final String USB_MODE_PERSIST_PROP = "persist.usb_mode";
    private static final String USB_MODE_DEFAULT = System.getProperty(USB_MODE_PROP);
    private static final String RING_DELAY_PREF = "pref_ring_delay";
    private static final String RING_DELAY_PROP = "ro.telephony.call_ring.delay";
    private static final String RING_DELAY_PERSIST_PROP = "persist.call_ring.delay";
    private static final String RING_DELAY_DEFAULT = System.getProperty(RING_DELAY_PROP);
    private static final String VM_HEAPSIZE_PREF = "pref_vm_heapsize";
    private static final String VM_HEAPSIZE_PROP = "dalvik.vm.heapsize";
    private static final String VM_HEAPSIZE_PERSIST_PROP = "persist.vm_heapsize";
    private static final String VM_HEAPSIZE_DEFAULT = System.getProperty(VM_HEAPSIZE_PROP);
    private static final String FAST_UP_PREF = "pref_fast_up";
    private static final String FAST_UP_PROP = "ro.ril.hsxpa";
    private static final String FAST_UP_PERSIST_PROP = "persist.fast_up";
    private static final String FAST_UP_DEFAULT = System.getProperty(FAST_UP_PROP);
    private static final String DISABLE_BOOT_ANIM_PREF = "pref_disable_boot_anim";
    private static final String DISABLE_BOOT_ANIM_PROP_1 = "ro.kernel.android.bootanim";
    private static final String DISABLE_BOOT_ANIM_PROP_2 = "debug.sf.nobootanimation";
    private static final String DISABLE_BOOT_ANIM_PERSIST_PROP = "persist.disable_boot_anim";
    private static final String PROX_DELAY_PREF = "pref_prox_delay";
    private static final String PROX_DELAY_PROP = "mot.proximity.delay";
    private static final String PROX_DELAY_PERSIST_PROP = "persist.prox.delay";
    private static final String PROX_DELAY_DEFAULT = System.getProperty(PROX_DELAY_PROP);
    private static final String LOGCAT_PREF = "pref_logcat";
    private static final String LOGCAT_PERSIST_PROP = "persist.logcat";
    private static final String LOGCAT_DISABLE = "#rm -f /dev/log/main";
    private static final String LOGCAT_ALIVE_PATH = "/system/etc/init.d/72propmodder_script";
    private static final String LOGCAT_ENABLE = "rm -f /dev/log/main";
    private static final String MOD_VERSION_PREF = "pref_mod_version";
    private static final String MOD_VERSION_PROP = "ro.build.display.id";
    private static final String MOD_VERSION_PERSIST_PROP = "persist.build.display.id";
    private static final String MOD_VERSION_DEFAULT = System.getProperty(MOD_VERSION_PROP);
    private static final String MOD_BUTTON_TEXT = "doMod";
    private static final String MOD_VERSION_TEXT = "Mods by PropModder";
    private static final String SLEEP_PREF = "pref_sleep";
    private static final String SLEEP_PROP = "pm.sleep_mode";
    private static final String SLEEP_PERSIST_PROP = "persist.sleep";
    private static final String SLEEP_DEFAULT = System.getProperty(SLEEP_PROP);
    private static final String TCP_STACK_PREF = "pref_tcp_stack";
    private static final String TCP_STACK_PERSIST_PROP = "persist_tcp_stack";
    private static final String TCP_STACK_PROP_0 = "net.tcp.buffersize.default";
    private static final String TCP_STACK_PROP_1 = "net.tcp.buffersize.wifi";
    private static final String TCP_STACK_PROP_2 = "net.tcp.buffersize.umts";
    private static final String TCP_STACK_PROP_3 = "net.tcp.buffersize.gprs";
    private static final String TCP_STACK_PROP_4 = "net.tcp.buffersize.edge";
    private static final String TCP_STACK_BUFFER = "4096,87380,256960,4096,16384,256960";
    private static final String JIT_PREF = "pref_jit";
    private static final String JIT_PERSIST_PROP = "persist_jit";
    private static final String JIT_PROP = "dalvik.vm.execution-mode";
    private static final String CHECK_IN_PREF = "pref_check_in";
    private static final String CHECK_IN_PERSIST_PROP = "persist_check_in";
    private static final String CHECK_IN_PROP = "ro.config.nocheckin";
    private static final String CHECK_IN_PROP_HTC = "ro.config.htc.nocheckin";
    private static final String SDCARD_BUFFER_PREF = "pref_sdcard_buffer";
    private static final String SDCARD_BUFFER_PRESIST_PROP = "persist_sdcard_buffer";
    private static final String THREE_G_PREF = "pref_g_speed";
    private static final String THREE_G_PERSIST_PROP = "persist_3g_speed";
    private static final String THREE_G_PROP_0 = "ro.ril.enable.3g.prefix";
    private static final String THREE_G_PROP_1 = "ro.ril.hep";
    private static final String THREE_G_PROP_2 = FAST_UP_PROP;
    private static final String THREE_G_PROP_3 = "ro.ril.enable.dtm";
    private static final String THREE_G_PROP_4 = "ro.ril.gprsclass";
    private static final String THREE_G_PROP_5 = "ro.ril.hsdpa.category";
    private static final String THREE_G_PROP_6 = "ro.ril.enable.a53";
    private static final String THREE_G_PROP_7 = "ro.ril.hsupa.category";
    private static final String GPU_PREF = "pref_gpu";
    private static final String GPU_PERSIST_PROP = "persist_gpu";
    private static final String GPU_PROP = "debug.sf.hw";
    private static final String VVMAIL_PREF = "pref_vvmail";
    private static final String VVMAIL_PERSIST_PROP = "persist_vvmail";
    private static final String VVMAIL_PROP_0 = "HorizontalVVM";
    private static final String VVMAIL_PROP_1 = "HorizontalBUA";

    private String placeholder;
    private String tcpstack0;
    private String jitVM;

    private String ModPrefHolder = SystemProperties.get(MOD_VERSION_PERSIST_PROP,
                SystemProperties.get(MOD_VERSION_PROP, MOD_VERSION_DEFAULT));

    //handles for our menu hard key press
    private final int MENU_MARKET = 1;
    private final int MENU_REBOOT = 2;
    private int NOTE_ID;

    private ListPreference mWifiScanPref;
    private ListPreference mLcdDensityPref;
    private ListPreference mMaxEventsPref;
    private ListPreference mRingDelayPref;
    private ListPreference mVmHeapsizePref;
    private ListPreference mFastUpPref;
    private CheckBoxPreference mDisableBootAnimPref;
    private ListPreference mProxDelayPref;
    private CheckBoxPreference mLogcatPref;
    private EditTextPreference mModVersionPref;
    private ListPreference mSleepPref;
    private CheckBoxPreference mTcpStackPref;
    private CheckBoxPreference mJitPref;
    private CheckBoxPreference mCheckInPref;
    private ListPreference mSdcardBufferPref;
    private CheckBoxPreference m3gSpeedPref;
    private CheckBoxPreference mGpuPref;
    private CheckBoxPreference mVvmailPref;
    private AlertDialog mAlertDialog;
    private NotificationManager mNotificationManager;

    //handler for command processor
    private final CMDProcessor cmd = new CMDProcessor();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.propmodder);
        PreferenceScreen prefSet = getPreferenceScreen();

        mWifiScanPref = (ListPreference) prefSet.findPreference(WIFI_SCAN_PREF);
        String wifi = Helpers.findBuildPropValueOf(WIFI_SCAN_PROP);
        if (!wifi.equals(DISABLE)) {
            mWifiScanPref.setValue(wifi);
            mWifiScanPref.setSummary(String.format(getString(R.string.pref_wifi_scan_alt_summary), wifi));
        } else {
            mWifiScanPref.setValue(WIFI_SCAN_DEFAULT);
        }
        mWifiScanPref.setOnPreferenceChangeListener(this);

        mLcdDensityPref = (ListPreference) prefSet.findPreference(LCD_DENSITY_PREF);
        String lcd = Helpers.findBuildPropValueOf(LCD_DENSITY_PROP);
        if (!lcd.equals(DISABLE)) {
            mLcdDensityPref.setValue(lcd);
            mLcdDensityPref.setSummary(String.format(getString(R.string.pref_lcd_density_alt_summary), lcd));
        } else {
            mLcdDensityPref.setValue(LCD_DENSITY_DEFAULT);
        }
        mLcdDensityPref.setOnPreferenceChangeListener(this);

        mMaxEventsPref = (ListPreference) prefSet.findPreference(MAX_EVENTS_PREF);
        String maxE = Helpers.findBuildPropValueOf(MAX_EVENTS_PROP);
        if (!maxE.equals(DISABLE)) {
            mMaxEventsPref.setValue(maxE);
            mMaxEventsPref.setSummary(String.format(getString(R.string.pref_max_events_alt_summary), maxE));
        } else {
            mMaxEventsPref.setValue(MAX_EVENTS_DEFAULT);
        }
        mMaxEventsPref.setOnPreferenceChangeListener(this);

        mRingDelayPref = (ListPreference) prefSet.findPreference(RING_DELAY_PREF);
        String ring = Helpers.findBuildPropValueOf(RING_DELAY_PROP);
        if (!ring.equals(DISABLE)) {
            mRingDelayPref.setValue(ring);
            mRingDelayPref.setSummary(String.format(getString(R.string.pref_ring_delay_alt_summary), ring));
        } else {
            mRingDelayPref.setValue(RING_DELAY_DEFAULT);
        }
        mRingDelayPref.setOnPreferenceChangeListener(this);

        mVmHeapsizePref = (ListPreference) prefSet.findPreference(VM_HEAPSIZE_PREF);
        String vm = Helpers.findBuildPropValueOf(VM_HEAPSIZE_PROP);
        if (!vm.equals(DISABLE)) {
            mVmHeapsizePref.setValue(vm);
            mVmHeapsizePref.setSummary(String.format(getString(R.string.pref_vm_heapsize_alt_summary), vm));
        } else {
            mVmHeapsizePref.setValue(VM_HEAPSIZE_DEFAULT);
        }
        mVmHeapsizePref.setOnPreferenceChangeListener(this);

        mFastUpPref = (ListPreference) prefSet.findPreference(FAST_UP_PREF);
        String fast = Helpers.findBuildPropValueOf(FAST_UP_PROP);
        if (!fast.equals(DISABLE)) {
            mFastUpPref.setValue(fast);
            mFastUpPref.setSummary(String.format(getString(R.string.pref_fast_up_alt_summary), fast));
        } else {
            mFastUpPref.setValue(FAST_UP_DEFAULT);
        }
        mFastUpPref.setOnPreferenceChangeListener(this);

        mDisableBootAnimPref = (CheckBoxPreference) prefSet.findPreference(DISABLE_BOOT_ANIM_PREF);
        String ba1 = Helpers.findBuildPropValueOf(DISABLE_BOOT_ANIM_PROP_1);
        String ba2 = Helpers.findBuildPropValueOf(DISABLE_BOOT_ANIM_PROP_2);
        if (ba1.equals("0") && ba2.equals("1")) {
            Log.d(TAG, "bootanimation is enabled");
            mDisableBootAnimPref.setChecked(false);
        } else {
            Log.d(TAG, "bootanimation is disabled");
            mDisableBootAnimPref.setChecked(true);
        }

        mProxDelayPref = (ListPreference) prefSet.findPreference(PROX_DELAY_PREF);
        String prox = Helpers.findBuildPropValueOf(PROX_DELAY_DEFAULT);
        if (!prox.equals(DISABLE)) {
            mProxDelayPref.setValue(prox);
            mProxDelayPref.setSummary(String.format(getString(R.string.pref_prox_delay_alt_summary), prox));
        } else {
            mProxDelayPref.setValue(PROX_DELAY_DEFAULT);
        }
        mProxDelayPref.setOnPreferenceChangeListener(this);

        //we may need a new method of detection here
        mLogcatPref = (CheckBoxPreference) prefSet.findPreference(LOGCAT_PREF);
        boolean rmLogging = cmd.su.runWaitFor(String.format("grep -q \"#rm -f /dev/log/main\" %s", INIT_SCRIPT_PATH)).success();
        mLogcatPref.setChecked(!rmLogging);

        mSleepPref = (ListPreference) prefSet.findPreference(SLEEP_PREF);
        String sleep = Helpers.findBuildPropValueOf(SLEEP_PROP);
        if (!sleep.equals(DISABLE)) {
            mSleepPref.setValue(sleep);
            mSleepPref.setSummary(String.format(getString(R.string.pref_sleep_alt_summary), sleep));
        } else {
            mSleepPref.setValue(SLEEP_DEFAULT);
        }
        mSleepPref.setOnPreferenceChangeListener(this);

        mTcpStackPref = (CheckBoxPreference) prefSet.findPreference(TCP_STACK_PREF);
        String tcp = Helpers.findBuildPropValueOf(TCP_STACK_PROP_0);
        if (tcp.equals(TCP_STACK_BUFFER)) {
            mTcpStackPref.setChecked(true);
        } else {
            mTcpStackPref.setChecked(false);
        }

        mJitPref = (CheckBoxPreference) prefSet.findPreference(JIT_PREF);
        String jit = Helpers.findBuildPropValueOf(JIT_PROP);
        if (jit.equals("int:jit")) {
            mJitPref.setChecked(true);
        } else {
            mJitPref.setChecked(false);
        }

        mModVersionPref = (EditTextPreference) prefSet.findPreference(MOD_VERSION_PREF);
        String mod = Helpers.findBuildPropValueOf(MOD_VERSION_PROP);
        if (mModVersionPref != null) {
            EditText modET = mModVersionPref.getEditText();
            ModPrefHolder = mModVersionPref.getEditText().toString();
            if (modET != null){
                InputFilter lengthFilter = new InputFilter.LengthFilter(32);
                modET.setFilters(new InputFilter[]{lengthFilter});
                modET.setSingleLine(true);
            }
            mModVersionPref.setSummary(String.format(getString(R.string.pref_mod_version_alt_summary), modET));
        }
        Log.d(TAG, String.format("ModPrefHoler = '%s' found build number = '%s'", ModPrefHolder, mod));
        mModVersionPref.setOnPreferenceChangeListener(this);

        mCheckInPref = (CheckBoxPreference) prefSet.findPreference(CHECK_IN_PREF);
        String chk = Helpers.findBuildPropValueOf(CHECK_IN_PROP);
        if (!chk.equals(DISABLE)) {
            mCheckInPref.setChecked(true);
        } else {
            mCheckInPref.setChecked(false);
        }

        //TODO check all init.d scripts for buffer values to display in summary
        //     for now we will just let it go with a generic summary displayed
        mSdcardBufferPref = (ListPreference) prefSet.findPreference(SDCARD_BUFFER_PREF);
        mSdcardBufferPref.setOnPreferenceChangeListener(this);

        m3gSpeedPref = (CheckBoxPreference) prefSet.findPreference(THREE_G_PREF);
        String g0 = Helpers.findBuildPropValueOf(THREE_G_PROP_0);
        String g3 = Helpers.findBuildPropValueOf(THREE_G_PROP_3);
        String g6 = Helpers.findBuildPropValueOf(THREE_G_PROP_6);
        if (g0.equals("1") && g3.equals("1") && g6.equals("1")) {
            m3gSpeedPref.setChecked(true);
        } else {
            m3gSpeedPref.setChecked(false);
        }

        mGpuPref = (CheckBoxPreference) prefSet.findPreference(GPU_PREF);
        String gpu = Helpers.findBuildPropValueOf(GPU_PROP);
        if (!gpu.equals(DISABLE)) {
            mGpuPref.setChecked(true);
        } else {
            mGpuPref.setChecked(false);
        }

        mVvmailPref = (CheckBoxPreference) prefSet.findPreference(VVMAIL_PREF);
        String vvmail0 = Helpers.findBuildPropValueOf(VVMAIL_PROP_0);
        String vvmail1 = Helpers.findBuildPropValueOf(VVMAIL_PROP_1);
        if (!vvmail0.equals(DISABLE) && !vvmail1.equals(DISABLE)) {
            mVvmailPref.setChecked(true);
        } else {
            mVvmailPref.setChecked(false);
        }

        /*
         * we have some requirements so we check
         * and create if needed
         * TODO: .exists() is ok but we should use
         *     : .isDirectory() and .isFile() to be sure
         *     : as .exists() returns positive if a txt file
         *     : exists @ /system/tmp
         */
        File tmpDir = new File("/system/tmp");
        boolean tmpDir_exists = tmpDir.exists();

        File init_d = new File("/system/etc/init.d");
        boolean init_d_exists = init_d.exists();

        File initScript = new File(INIT_SCRIPT_PATH);
        boolean initScript_exists = initScript.exists();

        if (!tmpDir_exists) {
            try {
                Log.d(TAG, "We need to make /system/tmp dir");
                mount("rw");
                cmd.su.runWaitFor("mkdir /system/tmp");
            } finally {
                mount("ro");
            }
        }
        if (!init_d_exists) {
            try {
                Log.d(TAG, "We need to make /system/etc/init.d/ dir");
                mount("rw");
                enableInit();
            } finally {
                mount("ro");
            }
        }
        if (!initScript_exists) {
            try {
                Log.d(TAG, String.format("init.d script not found @ '%s'", INIT_SCRIPT_PATH));
                mount("rw");
                initScript();
            } finally {
                mount("ro");
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "com.liquid.control.fragments.PropModder has been paused");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "com.liquid.control.fragments.PropModder is being resumed");
    }

    /* handle CheckBoxPreference clicks */
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;
        if (preference == mDisableBootAnimPref) {
            value = mDisableBootAnimPref.isChecked();
            return doMod(null, DISABLE_BOOT_ANIM_PROP_1, String.valueOf(value ? 0 : 1))
                    && doMod(DISABLE_BOOT_ANIM_PERSIST_PROP,
                            DISABLE_BOOT_ANIM_PROP_2, String.valueOf(value ? 1 : 0));
        } else if (preference == mLogcatPref) {
            value = mLogcatPref.isChecked();
            placeholder = String.valueOf(value ? LOGCAT_ENABLE : LOGCAT_DISABLE);
            SystemProperties.set(LOGCAT_PERSIST_PROP, placeholder);
            return cmd.su.runWaitFor(String.format(LOGCAT_CMD, placeholder)).success();
        } else if (preference == mTcpStackPref) {
            Log.d(TAG, "mTcpStackPref.onPreferenceTreeClick()");
            value = mTcpStackPref.isChecked();
            return doMod(null, TCP_STACK_PROP_0, String.valueOf(value ? TCP_STACK_BUFFER : DISABLE))
                    && doMod(null, TCP_STACK_PROP_1, String.valueOf(value ? TCP_STACK_BUFFER : DISABLE))
                    && doMod(null, TCP_STACK_PROP_2, String.valueOf(value ? TCP_STACK_BUFFER : DISABLE))
                    && doMod(null, TCP_STACK_PROP_3, String.valueOf(value ? TCP_STACK_BUFFER : DISABLE))
                    && doMod(TCP_STACK_PERSIST_PROP, TCP_STACK_PROP_4, String.valueOf(value ? TCP_STACK_BUFFER : DISABLE));
        } else if (preference == mJitPref) {
            Log.d(TAG, "mJitPref.onPreferenceTreeClick()");
            value = mJitPref.isChecked();
            return doMod(JIT_PERSIST_PROP, JIT_PROP, String.valueOf(value ? "int:fast" : "int:jit"));
        } else if (preference == mCheckInPref) {
            value = mCheckInPref.isChecked();
            return doMod(null, CHECK_IN_PROP_HTC, String.valueOf(value ? 1 : DISABLE))
            && doMod(CHECK_IN_PERSIST_PROP, CHECK_IN_PROP, String.valueOf(value ? 1 : DISABLE));
        } else if (preference == m3gSpeedPref) {
            value = m3gSpeedPref.isChecked();
            return doMod(THREE_G_PERSIST_PROP, THREE_G_PROP_0, String.valueOf(value ? 1 : DISABLE))
                && doMod(null, THREE_G_PROP_1, String.valueOf(value ? 1 : DISABLE))
                && doMod(null, THREE_G_PROP_2, String.valueOf(value ? 2 : DISABLE))
                && doMod(null, THREE_G_PROP_3, String.valueOf(value ? 1 : DISABLE))
                && doMod(null, THREE_G_PROP_4, String.valueOf(value ? 12 : DISABLE))
                && doMod(null, THREE_G_PROP_5, String.valueOf(value ? 8 : DISABLE))
                && doMod(null, THREE_G_PROP_6, String.valueOf(value ? 1 : DISABLE))
                && doMod(null, THREE_G_PROP_7, String.valueOf(value ? 5 : DISABLE));
        } else if (preference == mGpuPref) {
            value = mGpuPref.isChecked();
            return doMod(GPU_PERSIST_PROP, GPU_PROP, String.valueOf(value ? 1 : DISABLE));
        } else if (preference == mVvmailPref) {
            value = mVvmailPref.isChecked();
            return doMod(VVMAIL_PERSIST_PROP, VVMAIL_PROP_0, String.valueOf(value ? true : DISABLE))
                && doMod(null, VVMAIL_PROP_1, String.valueOf(value ? true : DISABLE));
        }

        return false;
    }

    /* handle ListPreferences and EditTextPreferences */
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (newValue != null) {
            Log.e(TAG, "New preference selected: " + newValue);
            if (preference == mWifiScanPref) {
                return doMod(WIFI_SCAN_PERSIST_PROP, WIFI_SCAN_PROP,
                        newValue.toString());
            } else if (preference == mLcdDensityPref) {
                return doMod(LCD_DENSITY_PERSIST_PROP, LCD_DENSITY_PROP,
                        newValue.toString());
            } else if (preference == mMaxEventsPref) {
                return doMod(MAX_EVENTS_PERSIST_PROP, MAX_EVENTS_PROP,
                        newValue.toString());
            } else if (preference == mRingDelayPref) {
                return doMod(RING_DELAY_PERSIST_PROP, RING_DELAY_PROP,
                        newValue.toString());
            } else if (preference == mVmHeapsizePref) {
                return doMod(VM_HEAPSIZE_PERSIST_PROP, VM_HEAPSIZE_PROP,
                        newValue.toString());
            } else if (preference == mFastUpPref) {
                return doMod(FAST_UP_PERSIST_PROP, FAST_UP_PROP,
                        newValue.toString());
            } else if (preference == mProxDelayPref) {
                 return doMod(PROX_DELAY_PERSIST_PROP, PROX_DELAY_PROP,
                        newValue.toString());
            } else if (preference == mModVersionPref) {
                 return doMod(MOD_VERSION_PERSIST_PROP, MOD_VERSION_PROP,
                        newValue.toString());
            } else if (preference == mSleepPref) {
                 return doMod(SLEEP_PERSIST_PROP, SLEEP_PROP,
                        newValue.toString());
            } else if (preference == mSdcardBufferPref) {
                 return mount("rw")
                            && cmd.su.runWaitFor(String.format(SDCARD_BUFFER_ON_THE_FLY_CMD, newValue.toString())).success()
                            && cmd.su.runWaitFor(String.format(SDCARD_BUFFER_CMD, newValue.toString())).success()
                            && mount("ro");
            }
        }

        return false;
    }

    /* method to handle mods */
    private boolean doMod(String persist, String key, String value) {

        if (persist != null) {
            SystemProperties.set(persist, value);
        }
        Log.d(TAG, String.format("Calling script with args '%s' and '%s'", key, value));
        backupBuildProp();
        if (!mount("rw")) {
            throw new RuntimeException("Could not remount /system rw");
        }
        boolean success = false;
        try {
            if (!propExists(key) && value.equals(DISABLE)) {
                Log.d(TAG, String.format("We want {%s} DISABLED however it doesn't exist so we do nothing and move on", key));
            } else if (propExists(key)) {
                if (value.equals(DISABLE)) {
                    Log.d(TAG, String.format("value == %s", DISABLE));
                    success = cmd.su.runWaitFor(String.format(KILL_PROP_CMD, key)).success();
                } else {
                    Log.d(TAG, String.format("value != %s", DISABLE));
                    success = cmd.su.runWaitFor(String.format(REPLACE_CMD, key, value)).success();
                }

            } else {
                Log.d(TAG, "append command starting");
                success = cmd.su.runWaitFor(String.format(APPEND_CMD, key, value)).success();
            }
            if (!success) {
                restoreBuildProp();
            }
        } finally {
            mount("ro");
        }
        return success;
    }

    public boolean mount(String read_value) {
        Log.d(TAG, "Remounting /system " + read_value);
        return cmd.su.runWaitFor(String.format(REMOUNT_CMD, read_value)).success();
    }

    public boolean propExists(String prop) {
        Log.d(TAG, "Checking if prop " + prop + " exists in /system/build.prop");
        return cmd.su.runWaitFor(String.format(PROP_EXISTS_CMD, prop)).success();
    }

    public void updateShowBuild() {
        Log.d(TAG, "Setting up /system/tmp/showbuild");
        try {
            mount("rw");
            cmd.su.runWaitFor("cp -f /system/build.prop " + SHOWBUILD_PATH).success();
            cmd.su.runWaitFor("chmod 777 " + SHOWBUILD_PATH).success();
        } finally {
            mount("ro");
        }
    }

    public boolean initScript() {
        FileWriter wAlive;
        try {
            wAlive = new FileWriter(INIT_SCRIPT_TEMP_PATH);
            //forgive me but without all the \n's the script is one line long O:-)
            wAlive.write("#\n#init.d script by PropModder\n#\n\n");
            wAlive.write("#rm -f /dev/log/main\n");
            wAlive.write("#echo 2048 > /sys/devices/virtual/bdi/179:0/read_ahead_kb");
            wAlive.flush();
            wAlive.close();
            cmd.su.runWaitFor(String.format("cp -f %s %s", INIT_SCRIPT_TEMP_PATH, INIT_SCRIPT_PATH)).success();
            //This should be find because if the chmod fails the install failed
            return cmd.su.runWaitFor(String.format("chmod 755 %s", INIT_SCRIPT_PATH)).success();
        } catch(Exception e) {
            Log.e(TAG, "initScript install failed: " + e);
            e.printStackTrace();
        }

        return false;
    }

    public boolean enableInit() {
        FileWriter wAlive;
        try {
            wAlive = new FileWriter("/system/tmp/initscript");
            //forgive me but without all the \n's the script is one line long O:-)
            wAlive.write("#\n#enable init.d script by PropModder\n#\n\n");
            wAlive.write("log -p I -t boot \"Starting init.d ...\"\n");
            wAlive.write("busybox run-parts /system/etc/init.d");
            wAlive.flush();
            wAlive.close();
            cmd.su.runWaitFor("cp -f /system/tmp/initscript /system/usr/bin/init.sh");
            return cmd.su.runWaitFor("chmod 755 /system/usr/bin/pm_init.sh").success();
        } catch(Exception e) {
            Log.e(TAG, "enableInit install failed: " + e);
            e.printStackTrace();
        }

        return false;
    }

    public boolean backupBuildProp() {
        Log.d(TAG, "Backing up build.prop to /system/tmp/pm_build.prop");
        return cmd.su.runWaitFor("cp /system/build.prop /system/tmp/pm_build.prop").success();
    }
    
    public boolean restoreBuildProp() {
        Log.d(TAG, "Restoring build.prop from /system/tmp/pm_build.prop");
        return cmd.su.runWaitFor("cp /system/tmp/pm_build.prop /system/build.prop").success();
    }
}

