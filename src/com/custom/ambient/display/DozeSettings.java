/*
 * Copyright (C) 2015 The CyanogenMod Project
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

package com.custom.ambient.display;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.provider.Settings;
import androidx.preference.PreferenceCategory;
import android.view.MenuItem;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;

import com.palladium.support.preferences.SwitchPreference;
import com.palladium.support.preferences.SystemSettingSeekBarPreference;
import com.palladium.support.preferences.SystemSettingSwitchPreference;

public class DozeSettings extends PreferenceActivity implements PreferenceFragment.OnPreferenceStartFragmentCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, getNewFragment())
                    .commit();
        }
    }

    private PreferenceFragment getNewFragment() {
        return new MainSettingsFragment();
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragment preferenceFragment,
            Preference preference) {
        Fragment instantiate = Fragment.instantiate(this, preference.getFragment(),
            preference.getExtras());
        getFragmentManager().beginTransaction().replace(
                android.R.id.content, instantiate).addToBackStack(preference.getKey()).commit();

        return true;
    }

    public static class MainSettingsFragment extends PreferenceFragment
            implements Preference.OnPreferenceChangeListener {

        private static final String KEY_CATEGORY_TILT_SENSOR = "tilt_sensor";
        private static final String KEY_CATEGORY_PROXIMITY_SENSOR = "proximity_sensor";
        private static final String KEY_CATEGORY_DOUBLE_TAP = "double_tap";
        private static final String KEY_CATEGORY_SMART_WAKE = "smart_wake_category";

        private Context mContext;
        private ActionBar actionBar;

        private PreferenceCategory mTiltCategory;
        private PreferenceCategory mProximitySensorCategory;
        private PreferenceCategory mDoubleTapCategory;
        private PreferenceCategory mSmartWakeCategory;
        private SwitchPreference mAoDPreference;
        private SwitchPreference mAmbientDisplayPreference;
        private SwitchPreference mPickUpPreference;
        private SwitchPreference mProximityScreenWakePreference;
        private SwitchPreference mRaiseToWakePreference;
        private SwitchPreference mSmartWakePreference;
        private SwitchPreference mHandwavePreference;
        private SwitchPreference mPocketPreference;
        private SystemSettingSwitchPreference mDoubleTapPreference;
        private SystemSettingSwitchPreference mDozeOnChargePreference;
        private Preference mBrightnessLevels;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

            setPreferencesFromResource(R.xml.doze_settings, rootKey);

            mContext = getActivity();

            actionBar = getActivity().getActionBar();
            assert actionBar != null;
            actionBar.setDisplayHomeAsUpEnabled(true);

            mAoDPreference =
                (SwitchPreference) findPreference(Utils.AOD_KEY);

            mDozeOnChargePreference =
                (SystemSettingSwitchPreference) findPreference(Utils.AOD_CHARGE_KEY);

            mDoubleTapCategory =
                (PreferenceCategory) findPreference(KEY_CATEGORY_DOUBLE_TAP);
            mDoubleTapPreference =
                (SystemSettingSwitchPreference) findPreference(Utils.DOUBLE_TAP_KEY);

            if (Utils.isTapToWakeAvailable(mContext)) {
                mDoubleTapPreference.setOnPreferenceChangeListener(this);
            } else {
                getPreferenceScreen().removePreference(mDoubleTapCategory);
            }

            if (Utils.isAoDAvailable(mContext)) {
                mAoDPreference.setChecked(Utils.isAoDEnabled(mContext));
                mAoDPreference.setOnPreferenceChangeListener(this);
            } else {
                getPreferenceScreen().removePreference(mAoDPreference);
                getPreferenceScreen().removePreference(mDozeOnChargePreference);
            }

            mAmbientDisplayPreference =
                (SwitchPreference) findPreference(Utils.AMBIENT_DISPLAY_KEY);
            mAmbientDisplayPreference.setChecked(Utils.isDozeEnabled(mContext));
            mAmbientDisplayPreference.setOnPreferenceChangeListener(this);

            mPickUpPreference =
                (SwitchPreference) findPreference(Utils.PICK_UP_KEY);
            mPickUpPreference.setChecked(Utils.tiltGestureEnabled(mContext));
            mPickUpPreference.setOnPreferenceChangeListener(this);

            mProximityScreenWakePreference =
                (SwitchPreference) findPreference(Utils.PROXIMITY_SCREEN_WAKE_KEY);
            mProximityScreenWakePreference.setChecked(Utils.isProximityScreenWakeEnabled(mContext));
            mProximityScreenWakePreference.setOnPreferenceChangeListener(this);

            mRaiseToWakePreference =
                (SwitchPreference) findPreference(Utils.GESTURE_RAISE_TO_WAKE_KEY);
            mRaiseToWakePreference.setChecked(Utils.isRaiseToWakeEnabled(mContext));
            mRaiseToWakePreference.setOnPreferenceChangeListener(this);

            mHandwavePreference =
                (SwitchPreference) findPreference(Utils.GESTURE_HAND_WAVE_KEY);
            mHandwavePreference.setChecked(Utils.handwaveGestureEnabled(mContext));
            mHandwavePreference.setOnPreferenceChangeListener(this);

            mPocketPreference =
                (SwitchPreference) findPreference(Utils.GESTURE_POCKET_KEY);
            mPocketPreference.setChecked(Utils.pocketGestureEnabled(mContext));
            mPocketPreference.setOnPreferenceChangeListener(this);

            mSmartWakePreference =
                (SwitchPreference) findPreference(Utils.SMART_WAKE_KEY);
            mSmartWakePreference.setChecked(Utils.isSmartWakeEnabled(mContext));
            mSmartWakePreference.setOnPreferenceChangeListener(this);

            mBrightnessLevels = (Preference) findPreference("doze_brightness");
            if (mBrightnessLevels != null
                    && !mContext.getResources().getBoolean(
                            R.bool.hasDozeBrightnessSensor)) {
                getPreferenceScreen().removePreference(mBrightnessLevels);
            }

            mTiltCategory = (PreferenceCategory) findPreference(KEY_CATEGORY_TILT_SENSOR);
            if (!getResources().getBoolean(R.bool.has_tilt_sensor)) {
                getPreferenceScreen().removePreference(mTiltCategory);
                getPreferenceScreen().removePreference(mPickUpPreference);
            }

            mSmartWakeCategory = (PreferenceCategory) findPreference(KEY_CATEGORY_SMART_WAKE);
            if (!getResources().getBoolean(R.bool.has_amd_tilt_sensor)) {
                getPreferenceScreen().removePreference(mSmartWakeCategory);
            }

            mProximitySensorCategory =
                (PreferenceCategory) findPreference(KEY_CATEGORY_PROXIMITY_SENSOR);
            if (!getResources().getBoolean(R.bool.has_proximity_sensor)) {
                getPreferenceScreen().removePreference(mProximitySensorCategory);
                getPreferenceScreen().removePreference(mHandwavePreference);
                getPreferenceScreen().removePreference(mPocketPreference);
                getPreferenceScreen().removePreference(mProximityScreenWakePreference);
            }

            if (mAoDPreference == null) return;
            setPrefs();
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            final String key = preference.getKey();
            final boolean value = (Boolean) newValue;

            if (Utils.AOD_KEY.equals(key)) {
                mAoDPreference.setChecked(value);
                Utils.enableAoD(value, mContext);
                setPrefs();
                return true;
            } else if (Utils.AMBIENT_DISPLAY_KEY.equals(key)) {
                mAmbientDisplayPreference.setChecked(value);
                Utils.enableDoze(value, mContext);
                return true;
            } else if (Utils.PICK_UP_KEY.equals(key)) {
                mPickUpPreference.setChecked(value);
                Utils.enablePickUp(value, mContext);
                return true;
            } else if (Utils.GESTURE_RAISE_TO_WAKE_KEY.equals(key)) {
                mRaiseToWakePreference.setChecked(value);
                Utils.enableRaiseToWake(value, mContext);
                return true;
            } else if (Utils.PROXIMITY_SCREEN_WAKE_KEY.equals(key)) {
                mProximityScreenWakePreference.setChecked(value);
                Utils.enableProximityScreenWake(value, mContext);
                return true;
            } else if (Utils.GESTURE_HAND_WAVE_KEY.equals(key)) {
                mHandwavePreference.setChecked(value);
                Utils.enableHandWave(value, mContext);
                return true;
            } else if (Utils.GESTURE_POCKET_KEY.equals(key)) {
                mPocketPreference.setChecked(value);
                Utils.enablePocketMode(value, mContext);
                return true;
            } else if (Utils.SMART_WAKE_KEY.equals(key)) {
                mSmartWakePreference.setChecked(value);
                Utils.enableSmartWake(value, mContext);
                return true;
            } else if (Utils.DOUBLE_TAP_KEY.equals(key)) {
                if (!Utils.isTapToWakeEnabled(mContext)); {
                    Settings.Secure.putInt(mContext.getContentResolver(),
                            Settings.Secure.DOUBLE_TAP_TO_WAKE, 1);
                }
                return true;
            }
            return false;
        }

        private void setPrefs() {
            final boolean aodEnabled = Utils.isAoDEnabled(mContext);
            mAmbientDisplayPreference.setEnabled(!aodEnabled);
            mPickUpPreference.setEnabled(!aodEnabled);
            mProximityScreenWakePreference.setEnabled(!aodEnabled);
            mRaiseToWakePreference.setEnabled(!aodEnabled);
            mSmartWakePreference.setEnabled(!aodEnabled);
            mHandwavePreference.setEnabled(!aodEnabled);
            mPocketPreference.setEnabled(!aodEnabled);
            mDoubleTapPreference.setEnabled(!aodEnabled);
            mDozeOnChargePreference.setEnabled(!aodEnabled);
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
        }

        @Override
        public void onResume() {
            super.onResume();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
