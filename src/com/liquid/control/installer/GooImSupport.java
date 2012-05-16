/*
 * Copyright (C) 2012 The LiquidSmoothROMs Project
 * author JBirdVegas@gmail.com 2012
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

package com.liquid.control.installer;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;

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

public class GooImSupport extends SettingsPreferenceFragment {

    private static final boolean DEBUG = true;
    private static final String TAG = "LC : GooImSupport";
    private static final String DEVICE_NAME = android.os.Build.DEVICE;

    public static final String LIQUID_JSON_PARSER = "http://goo.im/json2&path=/devs/teamliquid/";
    public static final String JSON_PARSER = "http://goo.im/json2&path=/devs&ro_board=toro";
    private static final String FORMATED_JSON_PATH = "http://goo.im/json2&path=%s&ro_board=toro";
    private static final String PREF_VERSIONS = "version_preference_screens";
    private static String PARSED_WEBSITE;
    private static String STATIC_LOCATION;

    //Dialogs
    private static final int WEB_VIEW = 101;

    Context mContext;
    PreferenceCategory mVersionViews;
    Handler mHandler;
    Runnable mReadWebsite;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mContext = getActivity().getApplicationContext();
        mHandler = new Handler();
        addPreferencesFromResource(R.xml.open_recovery);
        mVersionViews = (PreferenceCategory) findPreference(PREF_VERSIONS);
        Log.d(TAG, "Device name: " + android.os.Build.DEVICE);
        setHasOptionsMenu(true);

        // else if rotated while on another dev's
        // product list we reload our products page
        if (state == null) {
            GetAvailableVersions listPop = new GetAvailableVersions();
            listPop.PARSER = LIQUID_JSON_PARSER + (DEVICE_NAME.contains("toro") ? "vzw" : "gsm");
            listPop.execute();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.other_gooim_devs, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.other_gooim_devs:
                getFolder(JSON_PARSER);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void getFolder(String s_) {
        GetDevList getDev = new GetDevList();
        // fail safe some times our string doesnt
        // make it into the async for some reason
        STATIC_LOCATION = s_;
        getDev.http = s_;
        getDev.execute();
    }

    private class GetAvailableVersions extends AsyncTask<Void, Void, Void> {
        String PARSER;

        // called when we create the AsyncTask object
        public GetAvailableVersions() {
        }

        // can use UI thread here
        protected void onPreExecute() {
            // start with a clean view, always
            mVersionViews.removeAll();
        }

        // automatically done on worker thread (separate from UI thread)
        protected Void doInBackground(Void... urls) {
            if (PARSER == null) {
                Log.e(TAG, "website path was null");
                return null;
            }

            if (DEBUG) Log.d(TAG, "addressing website " + PARSER);

            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet request = new HttpGet(PARSER);
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
                    final String JSONshort_url = JSONObject.getString("short_url");

                    // debug
                    String log_formatter = "filename:{%s}	id:{%s}	path:{%s}	md5:{%s}	type:{%s}	short_url:{%s}";
                    if (DEBUG) Log.d(TAG, String.format(log_formatter, JSONfilename, JSONid, JSONpath, JSONmd5, JSONtype, JSONshort_url));

                    mVersionPresent.setKey(JSONid);
                    // TODO we should prob pull a version from this for the title
                    mVersionPresent.setTitle(JSONfilename);
                    mVersionPresent.setSummary(JSONtype);
                    mVersionPresent.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference p) {
                            PARSED_WEBSITE = JSONshort_url;
                            showDialog(WEB_VIEW);
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
            return null;
        }

        // can use UI thread here
        protected void onPostExecute(Void unused) {
        }
    }

    private class GetDevList extends AsyncTask<Void, Void, Void> {
        public String http;
        String format_web_address;

        // can use UI thread here
        protected void onPreExecute() {
            mVersionViews.removeAll();

            if (http == null && STATIC_LOCATION != null) {
                http = STATIC_LOCATION;
                STATIC_LOCATION = null;
            }

            format_web_address = String.format("http://goo.im/json2&path=%s&ro_board=%s",
                http, android.os.Build.DEVICE);
        }

        // automatically done on worker thread (separate from UI thread)
        protected Void doInBackground(Void... urls) {
            // we user seperate try blocks for folders and files
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet request = new HttpGet(http.contains("http") ? http : format_web_address);
                if (DEBUG) Log.d(TAG, "using website: " + (http.contains("http") ? http : format_web_address));
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                JSONObject jsObject = new JSONObject(httpClient.execute(request, responseHandler));
                JSONArray jsArray = new JSONArray(jsObject.getString("list"));
                if (DEBUG) Log.d(TAG, "JSONArray.length() is: " + jsArray.length());
                for (int i = 0; i < jsArray.length(); i++) {

                    PreferenceScreen mDevsFolder = getPreferenceManager().createPreferenceScreen(mContext);
                    final JSONObject obj_ = (JSONObject) jsArray.get(i);
                    // parse strings from JSONObject
                    try {
                        final String folder = obj_.getString("folder");
                        mDevsFolder.setTitle(folder);
                        mDevsFolder.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                            @Override
                            public boolean onPreferenceClick(Preference p) {
                                // move to the next folder
                                if (DEBUG) Log.d(TAG, "Sending http=" + folder);
                                getFolder(folder);
                                return true;
                            }
                        });

                        if (!http.contains(folder)) {
                            // we don't want to add the same folder we are currently viewing
                            if (DEBUG) Log.d(TAG, "not adding the folder we are viewing");
                            mVersionViews.addPreference(mDevsFolder);
                        }
                    } catch (JSONException je) {
                        // we didn't find a folder maybe we have files?
                        if (DEBUG) je.printStackTrace();
                    }

                    // seperate try block so we don't fail if we have folders and files
                    try {
                        PreferenceScreen mDevsFiles = getPreferenceManager().createPreferenceScreen(mContext);
                        final String JSONfilename = obj_.getString("filename");
                        final String JSONid = obj_.getString("id");
                        final String JSONpath = obj_.getString("path"); // unused right now
                        final String JSONmd5 = obj_.getString("md5");
                        final String JSONtype = obj_.getString("type"); // unused right now
                        final String JSONshort_url = obj_.getString("short_url");

                        mDevsFiles.setKey(JSONid);
                        // TODO we should prob pull a version from this for the title
                        mDevsFiles.setTitle(JSONfilename);
                        mDevsFiles.setSummary(JSONtype);
                        mDevsFiles.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                            @Override
                            public boolean onPreferenceClick(Preference p) {
                                PARSED_WEBSITE = JSONshort_url;
                                showDialog(WEB_VIEW);
                                return true;
                            }
                        });
                        mVersionViews.addPreference(mDevsFiles);
                    } catch (JSONException je) {
                        // if we don't find file info just skip this part
                    }
                }
            } catch (JSONException e) {
                if (DEBUG) e.printStackTrace();
            } catch (IOException ioe) {
                if (DEBUG) ioe.printStackTrace();
            }
            return null;
        }

        // can use UI thread here
        protected void onPostExecute(Void unused) {
            // we handled all our tasks in doInBackground()
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

    public Dialog onCreateDialog(final int id) {
        switch (id) {
            default:
            case WEB_VIEW:
                String mAddress = PARSED_WEBSITE;

                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View customLayout = inflater.inflate(R.layout.webview_dialog, null);

                AlertDialog.Builder mDownloadFile = new AlertDialog.Builder(getActivity());
                mDownloadFile.setView(customLayout);
                final WebView mWebView = (WebView) customLayout.findViewById(R.id.webview1);
                mWebView.getSettings().setJavaScriptEnabled(true);
                if (mAddress != null) mWebView.loadUrl(mAddress);
                PARSED_WEBSITE = null;
                mDownloadFile.setPositiveButton(getString(R.string.positive_button), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // do nothing
                    }
                });
                final AlertDialog ad_0 = mDownloadFile.create();
                ad_0.show();
                // we remove the dialog that called the webview
                // there is no public method to kill webviews
                // so user must be exit on their own
                Handler mKillDialog = new Handler();
                Runnable mReleaseDialog = new Runnable() {
                    public void run() {
                        ad_0.dismiss();
                    }
                };
                mKillDialog.postDelayed(mReleaseDialog, 4 * 1000);
                return ad_0;
        }
    }
}
