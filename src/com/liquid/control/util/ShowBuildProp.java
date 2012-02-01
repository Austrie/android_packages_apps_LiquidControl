package com.liquid.control.util;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;

import com.liquid.control.util.CMDProcessor;
import com.liquid.control.R;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public class ShowBuildProp extends AlertActivity {

    private static final String TAG = "PropModder :ShowBuildProp";
    private static final String MOUNT_RW = "busybox mount -o rw,remount -t yaffs2 /dev/block/mtdblock1 /system";
    private static final String MOUNT_RO = "busybox mount -o ro,remount -t yaffs2 /dev/block/mtdblock1 /system";
    private static final String SHOWBUILD_PATH = "/system/tmp/showbuild";

    private final CMDProcessor cmd = new CMDProcessor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            ShowMe();
        } else {
            Log.d(TAG, "orientation change detected not reloading webview");
        }
    }

    public void ShowMe() {
        Reader reader = null;
        StringBuilder data = null;

        Log.d(TAG, "Setting up /system/tmp/showbuild");
        try {
            //update the build.prop
            cmd.su.runWaitFor(MOUNT_RW);
            cmd.su.runWaitFor("cp -f /system/build.prop " + SHOWBUILD_PATH);
            cmd.su.runWaitFor("chmod 777 " + SHOWBUILD_PATH);

            data = new StringBuilder(2048);
            char tmp[] = new char[2048];
            int numRead;
            reader = new BufferedReader(new FileReader(SHOWBUILD_PATH));
            while ((numRead = reader.read(tmp)) >= 0) {
                data.append(tmp, 0, numRead);
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException while reading file:", e);
            showErrorAndFinish();
            return;
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "IOException while closing reader:", e);
            }
            cmd.su.runWaitFor(MOUNT_RO);
        }

        if (TextUtils.isEmpty(data)) {
            showErrorAndFinish();
            return;
        }

        WebView webView = new WebView(this);

        // Begin the loading. This will be done in a separate thread in WebView.
        webView.loadDataWithBaseURL(null, data.toString(), "text/plain", "utf-8", null);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                // Change from 'Loading...' to the real title
                mAlert.setTitle(getString(R.string.showbuild_dialog));
            }
        });

        final AlertController.AlertParams p = mAlertParams;
        p.mTitle = getString(R.string.showbuild_loading);
        p.mView = webView;
        p.mForceInverseBackground = true;
        setupAlert();
    }

    private void showErrorAndFinish() {
        Toast.makeText(this, R.string.showbuild_error, Toast.LENGTH_LONG).show();
        finish();
    }
}

