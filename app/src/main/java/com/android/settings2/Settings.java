/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings2;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.Switch;
import android.widget.TextView;

import com.android.settings2.wifi.WifiEnabler;
import com.android.settings2.wifi.WifiSettings;
import com.socks.library.KLog;

import java.util.List;


/**
 * Top-level settings activity to handle single pane and double pane UI layout.
 */
public class Settings extends PreferenceActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        KLog.e("Settings onCreate");
    }


    /**
     * Populate the activity with the top-level headers.
     */
    @Override
    public void onBuildHeaders(List<Header> headers) {

        KLog.e("Settings onBuildHeaders");

        if (!onIsHidingHeaders()) {
            loadHeadersFromResource(R.xml.settings_headers, headers);
        }
    }


    @Override
    public void setListAdapter(ListAdapter adapter) {
        KLog.e("Settings setListAdapter --> adapter == null: " + (adapter == null));

        if (adapter == null) {
            super.setListAdapter(null);
        } else {
            super.setListAdapter(new HeaderAdapter(this, getHeaders()));
        }
    }

    @Override
    protected void onResume() {
        KLog.e("Settings onResume");

        super.onResume();

        ListAdapter listAdapter = getListAdapter();
        if (listAdapter instanceof HeaderAdapter) {
            ((HeaderAdapter) listAdapter).resume();
        }
        invalidateHeaders();
    }

    @Override
    protected void onPause() {
        KLog.e("Settings onPause");


        super.onPause();
        ListAdapter listAdapter = getListAdapter();
        if (listAdapter instanceof HeaderAdapter) {
            ((HeaderAdapter) listAdapter).pause();
        }
    }

    private static final String EXTRA_UI_OPTIONS = "settings:ui_options";
    @Override
    public Intent onBuildStartFragmentIntent(String fragmentName, Bundle args, int titleRes, int shortTitleRes) {
        KLog.e("Settings --> onBuildStartFragmentIntent");

        Intent intent = super.onBuildStartFragmentIntent(fragmentName, args, titleRes, shortTitleRes);

        // Some fragments want split ActionBar; these should stay in sync with
        // uiOptions for fragments also defined as activities in manifest.
        if (WifiSettings.class.getName().equals(fragmentName) //||
//                WifiP2pSettings.class.getName().equals(fragmentName) ||
//                BluetoothSettings.class.getName().equals(fragmentName) ||
//                DreamSettings.class.getName().equals(fragmentName) ||
//                LocationSettings.class.getName().equals(fragmentName) ||
//                ToggleAccessibilityServicePreferenceFragment.class.getName().equals(fragmentName) ||
//                PrintSettingsFragment.class.getName().equals(fragmentName) ||
//                PrintServiceSettingsFragment.class.getName().equals(fragmentName)
            ) {
            intent.putExtra(EXTRA_UI_OPTIONS, ActivityInfo.UIOPTION_SPLIT_ACTION_BAR_WHEN_NARROW);
        }

        intent.setClass(this, SubSettings.class);
        return intent;
    }

    private static class HeaderAdapter extends ArrayAdapter<Header> {
        static final int HEADER_TYPE_CATEGORY = 0;
        static final int HEADER_TYPE_NORMAL = 1;
        static final int HEADER_TYPE_SWITCH = 2;
        static final int HEADER_TYPE_BUTTON = 3;
        private static final int HEADER_TYPE_COUNT = HEADER_TYPE_BUTTON + 1;

        private final WifiEnabler mWifiEnabler;
        //        private final BluetoothEnabler mBluetoothEnabler;

        private static class HeaderViewHolder {
            ImageView icon;
            TextView title;
            TextView summary;
            Switch switch_;
            ImageButton button_;
            View divider_;
        }

        private LayoutInflater mInflater;

        static int getHeaderType(Header header) {
            if (header.fragment == null && header.intent == null) {
                return HEADER_TYPE_CATEGORY;
            } else if (header.id == R.id.wifi_settings || header.id == R.id.bluetooth_settings) {
                return HEADER_TYPE_SWITCH;
//            } else if (header.id == R.id.security_settings) {
//                return HEADER_TYPE_BUTTON;
            } else {
                return HEADER_TYPE_NORMAL;
            }
        }

        @Override
        public int getItemViewType(int position) {
            Header header = getItem(position);
            return getHeaderType(header);
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false; // because of categories
        }

        @Override
        public boolean isEnabled(int position) {
            return getItemViewType(position) != HEADER_TYPE_CATEGORY;
        }

        @Override
        public int getViewTypeCount() {
            return HEADER_TYPE_COUNT;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        public HeaderAdapter(Context context, List<Header> objects) {
            super(context, 0, objects);

            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            // Temp Switches provided as placeholder until the adapter replaces these with actual
            // Switches inflated from their layouts. Must be done before adapter is set in super
            mWifiEnabler = new WifiEnabler(context, new Switch(context));
//            mBluetoothEnabler = new BluetoothEnabler(context, new Switch(context));
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            HeaderViewHolder holder;
            Header header = getItem(position);
            int headerType = getHeaderType(header);
            Log.d("Liu", "headerType: " + headerType);
            View view = null;

            if (convertView == null) {
                holder = new HeaderViewHolder();
                switch (headerType) {
                    case HEADER_TYPE_CATEGORY:
                        view = new TextView(getContext(), null,
                                android.R.attr.listSeparatorTextViewStyle);
                        holder.title = (TextView) view;
                        break;

                    case HEADER_TYPE_SWITCH:
                        view = mInflater.inflate(R.layout.preference_header_switch_item, parent, false);
                        holder.icon = (ImageView) view.findViewById(R.id.icon);
                        holder.title = (TextView) view.findViewById(android.R.id.title);
                        holder.summary = (TextView) view.findViewById(android.R.id.summary);
                        holder.switch_ = (Switch) view.findViewById(R.id.switchWidget);
                        break;

                    case HEADER_TYPE_BUTTON:
                        view = mInflater.inflate(R.layout.preference_header_button_item, parent, false);
                        holder.icon = (ImageView) view.findViewById(R.id.icon);
                        holder.title = (TextView) view.findViewById(android.R.id.title);
                        holder.summary = (TextView) view.findViewById(android.R.id.summary);
                        holder.button_ = (ImageButton) view.findViewById(R.id.buttonWidget);
                        holder.divider_ = view.findViewById(R.id.divider);
                        break;

                    case HEADER_TYPE_NORMAL:
                        view = mInflater.inflate(R.layout.preference_header_item, parent, false);
                        holder.icon = (ImageView) view.findViewById(R.id.icon);
                        holder.title = (TextView) view.findViewById(android.R.id.title);
                        holder.summary = (TextView) view.findViewById(android.R.id.summary);
                        break;
                }
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (HeaderViewHolder) view.getTag();
            }

            // All view fields must be updated every time, because the view may be recycled
            switch (headerType) {
                case HEADER_TYPE_CATEGORY:
                    holder.title.setText(header.getTitle(getContext().getResources()));
                    break;

                case HEADER_TYPE_SWITCH:
                    // Would need a different treatment if the main menu had more switches
                    if (header.id == R.id.wifi_settings) {
                        mWifiEnabler.setSwitch(holder.switch_);
                    } else {
//                        mBluetoothEnabler.setSwitch(holder.switch_);
                    }
                    updateCommonHeaderView(header, holder);
                    break;

                case HEADER_TYPE_BUTTON:

                    //...
                    updateCommonHeaderView(header, holder);
                    break;

                case HEADER_TYPE_NORMAL:
                    updateCommonHeaderView(header, holder);
                    break;
            }

            return view;
        }

        private void updateCommonHeaderView(Header header, HeaderViewHolder holder) {

            holder.icon.setImageResource(header.iconRes);
            holder.title.setText(header.getTitle(getContext().getResources()));
            CharSequence summary = header.getSummary(getContext().getResources());
            if (!TextUtils.isEmpty(summary)) {
                holder.summary.setVisibility(View.VISIBLE);
                holder.summary.setText(summary);
            } else {
                holder.summary.setVisibility(View.GONE);
            }
        }

        public void resume() {
            mWifiEnabler.resume();
//            mBluetoothEnabler.resume();
        }

        public void pause() {
            mWifiEnabler.pause();
//            mBluetoothEnabler.pause();
        }
    }


    /*
     * Settings subclasses for launching independently.
     *
     * startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)); 可以直接启动WifiSettingsActivity
     */
    public static class WifiSettingsActivity extends Settings { /* empty */ }

}