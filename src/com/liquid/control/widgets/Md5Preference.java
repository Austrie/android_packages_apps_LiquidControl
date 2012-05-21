
package com.liquid.control.widgets;

import android.content.Context;
import android.preference.Preference;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.liquid.control.R;

public class Md5Preference extends Preference {
    private static final boolean DEBUG = true;
    private static final String TAG = "Md5Preference";
    private static final String NULL = "Null";
    private float mDensity = 0;

    public Md5Preference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Md5Preference(Context context) {
        super(context);
    }

    LinearLayout widgetFrameView;
    ImageView iView;
    View mView;

    @Override
    protected void onBindView(View view) {
        mView = view;
        super.onBindView(view);
        mDensity = getContext().getResources().getDisplayMetrics().density;
    }

    public void isMatch(boolean match_) {
        if (mView == null) {
            Log.d(TAG, "mView is null returning...");
            return;
        }

        Log.d(TAG, "generating image");
        iView = new ImageView(getContext());
        widgetFrameView = ((LinearLayout) mView
                .findViewById(android.R.id.widget_frame));

        if (widgetFrameView == null) {
            Log.d(TAG, "widgetFrameView is null returning...");
            return;
        }

        widgetFrameView.setVisibility(View.VISIBLE);
        widgetFrameView.setPadding(
                widgetFrameView.getPaddingLeft(),
                widgetFrameView.getPaddingTop(),
                ((int) mDensity * 8),
                widgetFrameView.getPaddingBottom()
                );
        // remove old result
        int count = widgetFrameView.getChildCount();
        if (count > 0) {
            widgetFrameView.removeViews(0, count);
        }
        widgetFrameView.setMinimumWidth(0);
        iView.setImageResource(match_ ? R.drawable.ors_match : R.drawable.ors_fail);
        widgetFrameView.addView(iView);

        Log.d(TAG, "image is child #" + count + "	total children:" + widgetFrameView.getChildCount());
    }
}
