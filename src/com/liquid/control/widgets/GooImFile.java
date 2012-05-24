
package com.liquid.control.widgets;

import android.content.Context;
import android.preference.Preference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.liquid.control.R;

public class GooImFile extends Preference {
    private static final boolean DEBUG = true;
    private static final String TAG = "GooImFilePreference";

    public GooImFile(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setLayoutResource(R.layout.gooim_file_preference);
    }

    public GooImFile(Context context) {
        super(context);
        this.setLayoutResource(R.layout.gooim_file_preference);
    }

    LinearLayout mWidgetFrame;
    TextView mFilename;
    TextView mFileMd5;
    TextView mDownloads;

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        mWidgetFrame = (LinearLayout) view.findViewById(android.R.id.widget_frame);
        mFilename = (TextView) view.findViewById(R.id.filename);
        mDownloads = (TextView) view.findViewById(R.id.downloads);
        mFileMd5 = (TextView) view.findViewById(R.id.md5);

        //must declare the layout visible
        mWidgetFrame.setVisibility(View.VISIBLE);
    }

    public void populateFileInfo(String name, int dls, String md5) {
        try {
            mFilename.setText(name);
            mDownloads.setText("DLs: " + dls);
            mFileMd5.setText("md5: " + md5);
        } catch (NullPointerException ne) {
            if (DEBUG) Log.e(TAG, "recieved null value... do not do that", ne);
        }
    }
}
