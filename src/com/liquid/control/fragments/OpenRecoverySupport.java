package com.liquid.control.fragments;

import android.content.Intent;
import android.os.Bundle;

import com.liquid.control.SettingsPreferenceFragment;
import com.liquid.control.R;

public class OpenRecovery extends SettingsPreferenceFragment {

    private static final boolean DEBUG = true;
    private static final String TAG = "OpenRecoverySupport";
    private static final String BOARD_NAME = android.os.BUILD

    private static final WEBSITE = "http://www.rootzwiki.com";
    Context mContext;
    LinearLayout mRootView;
    Handler mHandler;
    Runnable mReadWebsite;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity().getApplicationContext();
        mHandler = new Handler();
        addPreferencesFromResource(R.xml.empty_linear_layout);
        mRootView = (LinearLayout) findViewById(R.id.root_linear_layout);
        findAndAddAvailableVersions();     

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
                        if (nextLine != null) {
                            JSONArray jsArray = new JSONArray(nextLine);
                            for (int i = 0; i < jsArray.length; i++) {
                                PreferenceScreen mVersionPresent =
                                        getPreferenceManager().createPreferenceScreen(mContext);
                                // parse strings from JSONObject
                                JSONObject jsObject = (JSONObject) jsArray.get(i);
                                String JSONfilename = jsObject.getString("filename");
                                String JSONid = jsObject.getString("id");
                                String JSONpath = jsObject.getString("path");
                                String JSONmd5 = jsObject.getString("md5");
                                String JSONtype = jsObject.getString("type");

                                // debug
                                String log_formatter = "filename:{%s}	id:{%s}	path:{%s}	md5:{%s}	type:{%s}";
                                if (DEBUG) Log.d(TAG, String.format(log_formatter, JSONfilename, JSONid,
                                        JSONpath, JSONmd5, JSONtype));

                                mVersionPresent.setKey(JSONid);
                                // TODO FINISH!!!
                                        
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
    }


    public void findAndAddAvailableVersions() {
        mHandler.post(mReadWebsite);        
    }
}
