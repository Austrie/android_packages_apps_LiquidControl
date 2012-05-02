package com.liquid.control.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.liquid.control.SettingsPreferenceFragment;
import com.liquid.control.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class OpenRecoveryScriptSupport extends SettingsPreferenceFragment {

    private static final boolean DEBUG = true;
    private static final String TAG = "LC : OpenRecoverySupport";
    private static final String BOARD_NAME = android.os.Build.BOARD;

    // TEST WEBSITE TILL OUR BUILDS ARE AVAILABLE
    private static final String WEBSITE = "http://goo.im/json2/&path=/devs/teameos/roms/nightlies/toro&ro_board=toro";
    private static final String PREF_VERSIONS = "version_preference_screens";
    Context mContext;
    PreferenceCategory mVersionViews;
    Handler mHandler;
    Runnable mReadWebsite;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity().getApplicationContext();
        mHandler = new Handler();
        addPreferencesFromResource(R.xml.open_recovery);
        mVersionViews = (PreferenceCategory) findPreference(PREF_VERSIONS);
        findAndAddAvailableVersions();
        Log.d(TAG, "Board name: " + BOARD_NAME);

    }


    public void findAndAddAvailableVersions() {
        //runnables
        mReadWebsite = new Runnable() {
            public void run() {
                String nextLine;
                URL url = null;
                URLConnection urlConn = null;
                InputStreamReader  inStream = null;
                BufferedReader buff = null;
                try {
                    // Create the URL obect that points
                    // at the default file index.html
                    url  = new URL(WEBSITE);
                    urlConn = url.openConnection();
                    inStream = new InputStreamReader(urlConn.getInputStream());
                    buff= new BufferedReader(inStream);
                    while (true) {
                        nextLine = buff.readLine();
                        if (DEBUG) Log.d(TAG, "buff.readLine() returned: " + nextLine);
                        if (nextLine != null) {
                            try {
                                JSONArray jsArray = new JSONArray(nextLine);
                                for (int i = 0; i < jsArray.length(); i++) {
                                    PreferenceScreen mVersionPresent =
                                            getPreferenceManager().createPreferenceScreen(mContext);
                                    // parse strings from JSONObject
                                    JSONObject jsObject = (JSONObject) jsArray.get(i);
                                    final String JSONfilename = jsObject.getString("filename");
                                    final String JSONid = jsObject.getString("id");
                                    final String JSONpath = jsObject.getString("path");
                                    final String JSONmd5 = jsObject.getString("md5");
                                    final String JSONtype = jsObject.getString("type");

                                    // debug
                                    String log_formatter = "filename:{%s}	id:{%s}	path:{%s}	md5:{%s}	type:{%s}";
                                    if (DEBUG) Log.d(TAG, String.format(log_formatter, JSONfilename, JSONid,
                                            JSONpath, JSONmd5, JSONtype));

                                    mVersionPresent.setKey(JSONid);
                                    // TODO we should prob pull a version from this for the title
                                    mVersionPresent.setTitle(JSONfilename);
                                    mVersionPresent.setSummary(JSONtype);
                                    mVersionPresent.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                                        @Override
                                        public boolean onPreferenceClick(Preference p) {
                                            downloadNewVersion(JSONpath);
                                            return true;
                                        }
                                    });
                                    mVersionViews.setTitle("we made it to add preference");
                                    mVersionViews.addPreference(mVersionPresent);
                                }
                            } catch (JSONException e) {
                                if (DEBUG) e.printStackTrace();
                            }
                        } else {
                            break;
                        }
                    }
                } catch (MalformedURLException bad_http) {
                    if (DEBUG) bad_http.printStackTrace();
                } catch (IOException  ioe) {
                    if (DEBUG) ioe.printStackTrace();
                }
            }
        };
        mHandler.post(mReadWebsite);        
    }

    public void downloadNewVersion(String http) {
        if (DEBUG) Log.d(TAG, "requesting http page: " + http);
    }
}
