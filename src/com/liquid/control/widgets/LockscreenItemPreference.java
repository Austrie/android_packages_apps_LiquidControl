/*
 * Copyright (C) 2012 The LiquidSmoothROMs Project
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

package com.liquid.control.widgets;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.liquid.control.R;
import java.io.File;

public class LockscreenItemPreference extends ListPreference {

    ImageView customIcon;
    View.OnClickListener listener;

    public LockscreenItemPreference(Context c) {
        super(c);
    }

    public LockscreenItemPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        View v = View.inflate(getContext(), R.layout.lockscreen_item_preference, null);
        customIcon = (ImageView) v.findViewById(android.R.id.icon);
        return v;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        if (listener != null)
            view.findViewById(android.R.id.icon).setOnClickListener(listener);
    }

    public void setImageListener(View.OnClickListener l) {
        listener = l;
        if (customIcon != null)
            customIcon.setOnClickListener(l);
    }
}
