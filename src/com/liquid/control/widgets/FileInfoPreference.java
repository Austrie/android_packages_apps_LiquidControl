
package com.liquid.control.widgets;

import android.content.Context;
import android.preference.Preference;
import android.view.View;
import android.view.ViewGroup;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import com.liquid.control.R;

public class FileInfoPreference extends Preference {
    private static final boolean DEBUG = true;
    private static final String TAG = "FileInfoPreference";
    String NO_FILE = "File not found";

    public FileInfoPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (DEBUG) Log.d(TAG, "FileInfoPrefenece Object created");
    }

    TextView mFilename;
    TextView mFilepath;
    TextView mFilesize;

    @Override
    protected View onCreateView(ViewGroup parent) {
        View layout = View.inflate(getContext(), R.layout.file_info_preference, null);
        mFilename = (TextView) layout.findViewById(R.id.zip_filename);
        mFilepath = (TextView) layout.findViewById(R.id.zip_filepath);
        mFilesize = (TextView) layout.findViewById(R.id.zip_filesize);
        return layout;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
    }

/////////////////////////
    public void setFilename(String fn_) {
        try {
            mFilename.setText(fn_);
        } catch (NullPointerException ne) {
            mFilename.setText(NO_FILE);
            if (DEBUG) ne.printStackTrace();
        }
    }

    public void setFilepath(String fp_) {
        try {
            mFilepath.setText(fp_);
        } catch (NullPointerException ne) {
            mFilepath.setText(NO_FILE);
            if (DEBUG) ne.printStackTrace();
        }
    }

    public void setFilesize(String fs_) {
        try { 
            Log.d(TAG, "Filesize: " + fs_);
            mFilesize.setText(fs_);
        } catch (NullPointerException ne) {
            mFilesize.setText(NO_FILE);
            if (DEBUG) ne.printStackTrace();
        }
    }
///////////////////////
}
