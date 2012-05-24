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

package com.liquid.control.fragments;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.liquid.control.R;
import com.liquid.control.SettingsPreferenceFragment;
import com.liquid.control.util.Crypto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

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

public class DynamicChangeLog extends SettingsPreferenceFragment {
    private static final boolean DEBUG = true;
    private static final boolean JSON_SPEW = true;
    private static final String TAG = "DynamicChangeLog";

    // example of commit list from parser
    // https://api.github.com/repos/LiquidSmoothROMs/android_frameworks_base/commits?page=1
    // example of repo list from parser
    // https://api.github.com/orgs/LiquidSmoothROMs/repos

    // github json api addresses
    private static final String GITHUB_JSON = "https://api.github.com/";
    private static final String ORGANIZATION = "LiquidSmoothROMs/";
    private static final String REPO_URL = GITHUB_JSON + "orgs/" + ORGANIZATION + "repos";
    private static final String REPOS_PARSER = GITHUB_JSON + "repos/show/"
            + ORGANIZATION; // returns a list of our projects
    private static final String COMMITS_PAGE = "commits?page="; //later... + PAGE_NUMBER (30 returns by default)
    private static final String COMMITS_REQUEST_FORMAT = GITHUB_JSON + "repos/" + ORGANIZATION + "%s/" + COMMITS_PAGE + "%s";

    // classwide constants
    private static final String PREF_CAT = "version_preference_screens"; //TODO use a more generic blank screen with category
    private static final int DEFAULT_FLING_SPEED = 60;

    // classwide variables
    private static String BRANCH;
    private static boolean ARE_IN_PROJECT_PATH;
    private static String GRAVATAR_URL;
    private static String COMMIT_AUTHOR;
    private static String COMMIT_MESSAGE;
    private static String COMMIT_DATE;
    private static String COMMIT_SHA;
    private static String COMMIT_URL;
    private static String PROJECT;

    // Dialogs (1001+)
    private static final int COMMIT_INFO_DIALOG = 1001;

    // Menu item ids (101+)
    private static final int MENU_ID_BRANCH = 101;
    private static final int MENU_ID_FAV_PROJECTS = 102;
    private static final int MENU_ID_WEBVIEW = 103;

    // classwide objects
    Context mContext;
    Handler mHandler;
    PreferenceCategory mCategory;
    SharedPreferences mSharedPrefs; //to hold default branches

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        // initialize our classwide objects
        mContext = getActivity().getApplicationContext();
        mHandler = new Handler();
        mSharedPrefs = mContext.getSharedPreferences("dynamic_changelogs", Context.MODE_PRIVATE);

        // disabled to I start the menu items
        //setHasOptionsMenu(true);

        // set defaults
        ARE_IN_PROJECT_PATH = false;

        // load blank screen & set initial title
        addPreferencesFromResource(R.xml.open_recovery); //we can hijack this for our needs
        mCategory = (PreferenceCategory) findPreference(PREF_CAT);
        mCategory.setTitle(getString(R.string.initial_changelog_category_title));

        // network communication must be done async
        // and be sure we don't waste bandwidth on silly rotation
        // if first run then the Bundle state will be null
        if (state == null) {
            // populate the initial screen
            new DisplayProjectsList().execute();
        }
    }

    // this is the only method called right before every display of the menu
    // here we choose what dynamic content to display for the menu
    public void onPrepareOptionsMenu(Menu menu) {
        // remove old menu items
        menu.clear();

        // cant change branch if we are not viewing a project folder's commits
        if (ARE_IN_PROJECT_PATH)
            menu.add(0, MENU_ID_BRANCH, 0, getString(R.string.changelog_menu_branch_title))
                    .setIcon(R.drawable.new_file);
        // we always want users to be able to access thier favs
        menu.add(0, MENU_ID_FAV_PROJECTS, 0, getString(R.string.changelog_menu_fav_projects_title))
                .setIcon(R.drawable.save);

        // they should also be able to view the current project commit log in browser
        menu.add(0, MENU_ID_WEBVIEW, 0, getString(R.string.changelog_menu_webview_title))
                .setIcon(R.drawable.save_as);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // pass the method on we don't need it our work was done in onPrepareOptionsMenu(Menu)
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case MENU_ID_BRANCH:
                // TODO DO WORK
                return true;
            case MENU_ID_FAV_PROJECTS:
                // just show favs from shared prefs
                return true;
            case MENU_ID_WEBVIEW:
                // display current github project commit list for current branch
                return true;
            // This should never happen but just in case let the system handle the return
            default:
                return super.onContextItemSelected(item);
        }
    }

    private class DisplayProjectsList extends AsyncTask<Void, Void, Void> {
        // called when we create the AsyncTask object
        public DisplayProjectsList() {
        }

        // can use UI thread here
        protected void onPreExecute() {
            // start with a clean view, always
            mCategory.removeAll();
            mCategory.setTitle(getString(R.string.loading_projects));
        }

        // automatically done on worker thread (separate from UI thread)
        protected Void doInBackground(Void... unused) {
            try {
                // network comms are not simple and require a few components
                // the client is the main construct
                HttpClient httpClient = new DefaultHttpClient();

                Log.i(TAG, "attempting to connect to: " + REPO_URL);

                // get requests the actual website
                HttpGet requestWebsite = new HttpGet(REPO_URL);
                // construct that handles recieving web streams to strings
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                // hold the response in a JSONArray
                JSONArray repoProjectsArray = new JSONArray(httpClient.execute(requestWebsite, responseHandler));
        
                // debugging
                if (DEBUG) Log.d(TAG, "repoProjectsArray.length() is: " + repoProjectsArray.length());

                // scroll through each item in array (projects in repo organization)
                for (int i = 0; i < repoProjectsArray.length(); i++) {
                    // make a new PreferenceScreen to 
                    PreferenceScreen mProject = getPreferenceManager().createPreferenceScreen(mContext);
                    // make an object of each repo
                    JSONObject projectsObject = (JSONObject) repoProjectsArray.get(i);

                    // extract info about each project
                    final String projectName = projectsObject.getString("name");
                    final String projectHtmlUrl = projectsObject.getString("html_url");
                    final String projectDescription = projectsObject.getString("description");
                    final int githubProjectId = projectsObject.getInt("id");

                    // apply info to our preference screen
                    mProject.setKey(githubProjectId + "");
                    if (projectDescription.contains("") || projectDescription == null) {
                        mProject.setTitle(projectName);
                        mProject.setSummary(projectDescription);
                    } else {
                        mProject.setTitle(projectDescription);
                        mProject.setSummary(projectName);
                    }

                    mProject.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference p) {
                            GetCommitList listFirstCommits = new GetCommitList();
                            listFirstCommits.PAGE_ = 1; // we start at the most recent commits
                            listFirstCommits.PROJECT_ = projectName;
                            listFirstCommits.execute();
                            return true;
                        }
                    });

                    mCategory.addPreference(mProject);
                }
            } catch (JSONException je) {
                if (DEBUG) Log.e(TAG, "Bad json interaction...", je);
            } catch (IOException ioe) {
                if (DEBUG) Log.e(TAG, "IOException...", ioe);
            } catch (NullPointerException ne) {
                if (DEBUG) Log.e(TAG, "NullPointer...", ne); //we may need to catch in the for(){} block
            }
            return null;
        }

        // can use UI thread here
        protected void onPostExecute(Void unused) {
            mCategory.setTitle(getString(R.string.org_projects));
        }
    }

    private class GetCommitList extends AsyncTask<Void, Void, Void> {
        // inner class constants
        final String DEFAULT_BRANCH = "ics";

        // inner class variables; populated before calling .execute(); if no BRANCH_ we assume ics
        int PAGE_ = -1;
        String BRANCH_;
        String PROJECT_;

        public GetCommitList() {
        }

        protected void onPreExecute() {
            // show commit after we load next set
            mCategory.setTitle(getString(R.string.loading_commits));
            if (PAGE_ <= 1)
                mCategory.removeAll();
        }

        protected Void doInBackground(Void... unused) {
            // so we don't acidentally crash the ui
            if (PROJECT_ == null || PAGE_ == -1) //int is not a valid null type
                return null;

            // TODO: deal with branches later
            String requestCommits = String.format(COMMITS_REQUEST_FORMAT, PROJECT_, PAGE_);

            if (BRANCH_ == null) BRANCH_ = DEFAULT_BRANCH;
            try {
                HttpClient httpClient = new DefaultHttpClient();

                Log.i(TAG, "attempting to connect to: " + requestCommits);
                HttpGet requestWebsite = new HttpGet(requestCommits);
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                JSONArray projectCommitsArray = new JSONArray(httpClient.execute(requestWebsite, responseHandler));
        
                // debugging
                if (DEBUG) Log.d(TAG, "projectCommitsArray.length() is: " + projectCommitsArray.length());
                if (JSON_SPEW) Log.d(TAG, "projectCommitsArray.toString() is: " + projectCommitsArray.toString());

                for (int i = 0; i < projectCommitsArray.length(); i++) {
                    PreferenceScreen mCommit = getPreferenceManager().createPreferenceScreen(mContext);
                    // make an object of each commit
                    JSONObject projectsObject = (JSONObject) projectCommitsArray.get(i);

                    // some fields are just plain strings we can parse
                    final String commitSsh = projectsObject.getString("sha"); // for setKey

                    final String commitWebPath = projectsObject.getString("url"); // JSON commit path

                    // author could possible be null so use a try block to prevent failures
                    // (merges have committers not authors, authors exist for the parent commits)
                    try {
                        // this is slightly different as we have many values for fields
                        // therefor each of these fields will be an object to itself (for each commit)
                        // author; committer; parents and commit
                        JSONObject authorObject = (JSONObject) projectsObject.getJSONObject("author");
                        JSONObject commitObject = (JSONObject) projectsObject.getJSONObject("commit");
                        if (JSON_SPEW) Log.d(TAG, "authorObject: " + authorObject.toString());

                        // pull needed info from our new objects (for each commit)
                        final String authorName = authorObject.getString("login"); // github screen name
                        final String authorAvatar = authorObject.getString("avatar_url"); // author's avatar url
                        final String commitMessage = commitObject.getString("message"); // commit message

                        // to grab the date we need to make a new object from
                        // the commit object and collect info from there
                        JSONObject authorObject_ = (JSONObject) commitObject.getJSONObject("author");
                        final String commitDate = authorObject_.getString("date"); // commit date

                        // apply info to our preference screen
                        mCommit.setKey(commitSsh + "");
                        mCommit.setTitle(commitMessage);
                        mCommit.setSummary(authorName);

                        mCommit.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                            @Override
                            public boolean onPreferenceClick(Preference p) {
                                GRAVATAR_URL = authorAvatar;
                                PROJECT = PROJECT_;
                                COMMIT_URL = commitWebPath;
                                COMMIT_AUTHOR = authorName;
                                COMMIT_MESSAGE = commitMessage;
                                COMMIT_DATE = commitDate;
                                COMMIT_SHA = commitSsh + "";
                                showDialog(COMMIT_INFO_DIALOG);
                                return true;
                            }
                        });

                        mCategory.addPreference(mCommit);                        
                    } catch (JSONException je) {
                        // no author found for commit
                        if (DEBUG) Log.d(TAG, "encountered a null value", je);
                    }
                }
                // append next 30 commits onClick()
                final PreferenceScreen mNext = getPreferenceManager().createPreferenceScreen(mContext);
                mNext.setTitle(getString(R.string.next_commits_page_title));
                mNext.setSummary(getString(R.string.next_commits_page_summary));
                mNext.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference p) {
                        GetCommitList nextList = new GetCommitList();
                        nextList.PAGE_ = PAGE_ + 1; // next page of commits (30)
                        nextList.PROJECT_ = PROJECT_; // stay in same project folder
                        nextList.execute();
                        mCategory.removePreference(mNext); // don't keep in list after we click
                        return true;
                    }
                });
                // avoid adding if we don't have commits, prob network fail :-/
                if (mCategory.getPreferenceCount() > 1)
                    mCategory.addPreference(mNext);
            } catch (JSONException je) {
                if (DEBUG) Log.e(TAG, "Bad json interaction...", je);
            } catch (IOException ioe) {
                if (DEBUG) Log.e(TAG, "IOException...", ioe);
            } catch (NullPointerException ne) {
                if (DEBUG) Log.e(TAG, "NullPointer...", ne);
            }
            return null;
        }

        protected void onPostExecute(Void unused) {
            mCategory.setTitle(getString(R.string.commits_title));
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        // send String[] (url[0]) return Bitmap
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String avatarUrl = urls[0];
            if (DEBUG) Log.d(TAG, "downloading: " + avatarUrl);
            Bitmap mAvatar = null;
            try {
                InputStream in = new URL(avatarUrl).openStream();
                mAvatar = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e(TAG, "failed to download avatar", e);
            }
            return mAvatar;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    public Dialog onCreateDialog(final int id) {
        switch (id) {
            default:
            case COMMIT_INFO_DIALOG:
                // get service and inflate our dialog
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View commitExtendedInfoLayout = inflater.inflate(R.layout.extended_commit_info_layout, null);

                // references for our objects
                ScrollView scroller = (ScrollView) commitExtendedInfoLayout.findViewById
                        (R.id.extended_commit_info_layout_scrollview);
                // so we scroll smoothly if commit message is large
                scroller.setSmoothScrollingEnabled(true);
                scroller.fling(DEFAULT_FLING_SPEED);


                ImageView avatar = (ImageView) commitExtendedInfoLayout.findViewById(R.id.author_avatar);

                // try to populate the image from gravatar
                // @link http://stackoverflow.com/a/9288544
                avatar.setVisibility(View.GONE);
                new DownloadImageTask(avatar).execute(GRAVATAR_URL);
                avatar.setVisibility(View.VISIBLE);

                TextView author_tv = (TextView) commitExtendedInfoLayout.findViewById
                        (R.id.commit_author);
                TextView message_tv = (TextView) commitExtendedInfoLayout.findViewById
                        (R.id.commit_message);
                TextView date_tv = (TextView) commitExtendedInfoLayout.findViewById
                        (R.id.commit_date);
                TextView sha_tv = (TextView) commitExtendedInfoLayout.findViewById
                        (R.id.commit_sha);

                // setText for TextViews
                author_tv.setText(COMMIT_AUTHOR);
                message_tv.setText(COMMIT_MESSAGE);
                date_tv.setText(COMMIT_DATE);

                // we split the sha-1 hash into two strings because
                // it looks horrible by default display and smaller
                // size text is hard to read
                int halfHashLength = COMMIT_SHA.length() / 2;
                StringBuilder splitHash = new StringBuilder();
                splitHash.append(COMMIT_SHA.substring(0, halfHashLength));
                splitHash.append("-\n"); // to seperate the strings
                splitHash.append(COMMIT_SHA.substring(halfHashLength));
                splitHash.trimToSize();
                // set the text from our StringBuilder
                sha_tv.setText(splitHash.toString());

                // make a builder to helps construct our dialog
                final AlertDialog.Builder commitInfo = new AlertDialog.Builder(getActivity());
                commitInfo.setTitle(getString(R.string.commit_extended_info_title));
                commitInfo.setView(commitExtendedInfoLayout);

                // the order we place the buttons in is important
                // standard is: | CANCEL | OK |
                // per our needs we use: | CLOSE | WEBVIEW |
                commitInfo.setNegativeButton(getString(R.string.button_close), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int button) {
                        // just let the dialog go
                    }
                });

                commitInfo.setPositiveButton(getString(R.string.button_webview), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int button) {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        String webviewUrl = "https://github.com/" + ORGANIZATION + PROJECT + "/commit/" + COMMIT_SHA;
                        i.setData(Uri.parse(webviewUrl));
                        startActivity(i);
                    }
                });

                AlertDialog ad_commit = commitInfo.create();
                ad_commit.show();
                return ad_commit;
        }
    }
}
