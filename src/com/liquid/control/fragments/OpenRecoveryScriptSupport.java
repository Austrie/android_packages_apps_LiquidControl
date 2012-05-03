/*
 * Copyright (C) 2012 The LiquidSmoothROMs Project
 * author JBirdVegas 2012
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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.ResponseHandler;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

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
        Log.d(TAG, "Board name: " + BOARD_NAME);

        new GetAvailableVersions().execute();
    }


    public void findAndAddAvailableVersions() {
    }

    public void downloadNewVersion(String http) {
        if (DEBUG) Log.d(TAG, "requesting http page: " + http);
    }

    private class GetAvailableVersions extends AsyncTask<Void, Void, Void> {
        private String result;
        private HttpResponse response;

        // called when we create the AsyncTask object
        public GetAvailableVersions() {
        }

        // can use UI thread here
        protected void onPreExecute() {
            if (DEBUG) Log.d(TAG, "onPreExecute");
        }

        // automatically done on worker thread (separate from UI thread)
        protected Void doInBackground(Void... urls) {
            if (DEBUG) Log.d(TAG, "doInBackGround: " + urls.toString());
            result = "";

            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet request = new HttpGet(WEBSITE);
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                JSONObject jsObject = new JSONObject(httpClient.execute(request, responseHandler));
                JSONArray jsArray = new JSONArray(jsObject.getString("list"));
                if (DEBUG) Log.d(TAG, "JSONArray.length() is: " + jsArray.length());
                for (int i = 0; i < jsArray.length(); i++) {
                    PreferenceScreen mVersionPresent = getPreferenceManager().createPreferenceScreen(mContext);
                    // parse strings from JSONObject
                    JSONObject JSONObject = (JSONObject) jsArray.get(i);
                    final String JSONfilename = JSONObject.getString("filename");
                    final String JSONid = JSONObject.getString("id");
                    final String JSONpath = JSONObject.getString("path");
                    final String JSONmd5 = JSONObject.getString("md5");
                    final String JSONtype = JSONObject.getString("type");

                    // debug
                    String log_formatter = "filename:{%s}	id:{%s}	path:{%s}	md5:{%s}	type:{%s}";
                    if (DEBUG) Log.d(TAG, String.format(log_formatter, JSONfilename, JSONid, JSONpath, JSONmd5, JSONtype));

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
                    mVersionViews.addPreference(mVersionPresent);
                }
            } catch (JSONException e) {
                if (DEBUG) e.printStackTrace();
            } catch (IOException ioe) {
                if (DEBUG) ioe.printStackTrace();
            }
            Log.d(TAG, "response: " + response);
            return null;
        }

        // can use UI thread here
        protected void onPostExecute(Void unused) {
            if (DEBUG) Log.d(TAG, "onPostExecute is envoked");

        }
    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            if (DEBUG) e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                if (DEBUG) e.printStackTrace();
            }
        }
            return sb.toString();
    }
}
