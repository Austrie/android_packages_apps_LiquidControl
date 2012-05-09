
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

public class Md5Preference extends Preference {
    private static final boolean DEBUG = true;
    private static final String TAG = "Md5Preference";
    private static final String NULL = "Null";

    public Md5Preference(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (DEBUG) Log.d(TAG, "FileInfoPrefenece Object created");
    }

    TextView mLocalChecksum;
    TextView mGooImChecksum;
    ImageView mMatch;

    @Override
    protected View onCreateView(ViewGroup parent) {
        View layout = View.inflate(getContext(), R.layout.md5_preference, null);
        mLocalChecksum = (TextView) layout.findViewById(R.id.ors_local_checksum);
        mGooImChecksum = (TextView) layout.findViewById(R.id.ors_gooim_checksum);
        mMatch = (ImageView) layout.findViewById(R.id.ors_verify_image);
        return layout;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
    }

    public void setLocalChecksum(String l_c_) {
        try {
            mLocalChecksum.setText(l_c_);
        } catch (NullPointerException ne) {
            mLocalChecksum.setText(NULL);
        }
    }

    public String getLocalChecksum() {
        return mLocalChecksum.getText().toString();
    }

    public void setGooImChecksum(String gooim_c_) {
        try {
            mGooImChecksum.setText(gooim_c_);
        } catch (NullPointerException ne) {
            mGooImChecksum.setText(NULL);
        }
    }

    public void isMatch(boolean match_) {
        if (match_) {
            mMatch.setImageResource(R.drawable.ors_match);
        } else {
            mMatch.setImageResource(R.drawable.ic_null);
        }
    }

    public boolean checkForMatch(String local_, String gooim_) {
        boolean truth = false;
        if (local_ != null && gooim_ != null) {
            if (local_.equals(gooim_)) {
                isMatch(true);
                truth = true;
            } else {
                isMatch(false);
                truth = false;
            }
        } else {
           truth = false;
        }
        return truth;
    }
}
