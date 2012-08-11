/*
 * Copyright (C) 2007-2008 Esmertec AG.
 * Copyright (C) 2007-2008 The Android Open Source Project
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

package com.android.mms.ui;

import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.R;

import android.content.Context;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.CheckBoxPreference;
import android.provider.SearchRecentSuggestions;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;

import com.android.mms.templates.TemplatesListActivity;
import com.android.mms.util.Recycler;

import android.content.res.Resources;

/**
 * With this activity, users can set preferences for MMS and SMS and
 * can access and manipulate SMS messages stored on the SIM.
 */
public class MessagingPreferenceActivity extends PreferenceActivity {
    // Symbolic names for the keys used for preference lookup
    public static final String MMS_DELIVERY_REPORT_MODE = "pref_key_mms_delivery_reports";
    public static final String EXPIRY_TIME              = "pref_key_mms_expiry";
    public static final String PRIORITY                 = "pref_key_mms_priority";
    public static final String READ_REPORT_MODE         = "pref_key_mms_read_reports";
    public static final String SMS_DELIVERY_REPORT_MODE = "pref_key_sms_delivery_reports";
    public static final String NOTIFICATION_ENABLED     = "pref_key_enable_notifications";
    public static final String SMS_SPLIT_MESSAGE        = "pref_key_sms_split_160";
    public static final String SMS_SPLIT_COUNTER        = "pref_key_sms_split_counter";
    public static final String NOTIFICATION_VIBRATE     = "pref_key_vibrate";
    public static final String NOTIFICATION_VIBRATE_WHEN= "pref_key_vibrateWhen";
    public static final String NOTIFICATION_RINGTONE    = "pref_key_ringtone";
    public static final String AUTO_RETRIEVAL           = "pref_key_mms_auto_retrieval";
    public static final String RETRIEVAL_DURING_ROAMING = "pref_key_mms_retrieval_during_roaming";
    public static final String MMS_SAVE_LOCATION        = "pref_save_location";
    public static final String AUTO_DELETE              = "pref_key_auto_delete";
    public static final String BLACK_BACKGROUND         = "pref_key_mms_black_background";
    public static final String TRANSPARENT_BACKGROUND   = "pref_key_mms_transparent_background";
    public static final String BUBBLE_SPEECH            = "pref_key_mms_bubble_speech";
    public static final String BACK_TO_ALL_THREADS      = "pref_key_mms_back_to_all_threads";
    public static final String SEND_ON_ENTER            = "pref_key_mms_send_on_enter";
    public static final String USER_AGENT               = "pref_key_mms_user_agent";
    public static final String USER_AGENT_CUSTOM        = "pref_key_mms_user_agent_custom";
    public static final String ENABLE_EMOJIS            = "pref_key_enable_emojis";
    public static final String HIDE_AVATAR_PP            = "pref_key_mms_hide_avatar";
    public static final String STRIP_UNICODE            = "pref_key_strip_unicode";
    public static final String FULL_TIMESTAMP           = "pref_key_mms_full_timestamp";
    public static final String ONLY_MOBILE_NUMBERS      = "pref_key_mms_only_mobile_numbers";
    public static final String SENT_TIMESTAMP           = "pref_key_mms_use_sent_timestamp";
    public static final String SENT_TIMESTAMP_GMT_CORRECTION = "pref_key_mms_use_sent_timestamp_gmt_correction";
    public static final String MESSAGE_FONT_SIZE     = "pref_key_mms_message_font_size";
    public static final String CONVO_FROM_FONT_SIZE     = "pref_key_mms_convo_from_font_size";
    public static final String CONVO_SUBJECT_FONT_SIZE     = "pref_key_mms_convo_subject_font_size";
    public static final String EMAIL_ADDR_COMPLETION        = "pref_key_mms_email_addr_completion";
    public static final String NOTIFICATION_VIBRATE_PATTERN = "pref_key_mms_notification_vibrate_pattern";
    public static final String NOTIFICATION_VIBRATE_PATTERN_CUSTOM = "pref_key_mms_notification_vibrate_pattern_custom";
    public static final String NOTIFICATION_VIBRATE_CALL = "pref_key_mms_notification_vibrate_call";
    public static final String MANAGE_TEMPLATES = "pref_key_templates_manage";
    public static final String SHOW_GESTURE = "pref_key_templates_show_gesture";
    public static final String GESTURE_SENSITIVITY = "pref_key_templates_gestures_sensitivity";
    public static final String GESTURE_SENSITIVITY_VALUE = "pref_key_templates_gestures_sensitivity_value";
    public static final String ENABLE_SMS_POPUP            = "pref_key_mms_enable_sms_popup";

    // Menu entries
    private static final int MENU_RESTORE_DEFAULTS    = 1;

    private Preference mSmsLimitPref;
    private Preference mSmsDeliveryReportPref;
    private Preference mMmsLimitPref;
    private Preference mMmsDeliveryReportPref;
    private Preference mMmsReadReportPref;
    private Preference mManageSimPref;
    private Preference mClearHistoryPref;
    private ListPreference mVibrateWhenPref;
    private Preference mManageTemplate;
    private Recycler mSmsRecycler;
    private Recycler mMmsRecycler;
    private CheckBoxPreference mHideAvatarPref;
    private CheckBoxPreference mEnableSmsPopupPref;
    private CheckBoxPreference mBlackBackgroundPref;
    private CheckBoxPreference mTransparentBackgroundPref;
    private CheckBoxPreference mBubbleSpeechPref;
    private ListPreference mGestureSensitivity;

    private static final int CONFIRM_CLEAR_SEARCH_HISTORY_DIALOG = 3;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.preferences);

        setMessagePreferences();
    }

    private void setMessagePreferences() {
        mManageSimPref = findPreference("pref_key_manage_sim_messages");
        mSmsLimitPref = findPreference("pref_key_sms_delete_limit");
        mSmsDeliveryReportPref = findPreference("pref_key_sms_delivery_reports");
        mMmsDeliveryReportPref = findPreference("pref_key_mms_delivery_reports");
        mMmsReadReportPref = findPreference("pref_key_mms_read_reports");
        mMmsLimitPref = findPreference("pref_key_mms_delete_limit");
        mClearHistoryPref = findPreference("pref_key_mms_clear_history");
        mHideAvatarPref = (CheckBoxPreference)findPreference("pref_key_mms_hide_avatar");
        mEnableSmsPopupPref = (CheckBoxPreference)findPreference("pref_key_mms_enable_sms_popup");
        mBlackBackgroundPref = (CheckBoxPreference)findPreference("pref_key_mms_black_background");
        mTransparentBackgroundPref = (CheckBoxPreference)findPreference("pref_key_mms_transparent_background");
        mBubbleSpeechPref = (CheckBoxPreference)findPreference("pref_key_mms_bubble_speech");
        mVibrateWhenPref = (ListPreference) findPreference(NOTIFICATION_VIBRATE_WHEN);
        mManageTemplate = findPreference(MANAGE_TEMPLATES);
        mGestureSensitivity = (ListPreference) findPreference(GESTURE_SENSITIVITY);
        mEnableSmsPopupPref.setChecked(Settings.System.getInt(getContentResolver(), Settings.System.USE_POPUP_SMS, 1) == 1);

        if (!MmsApp.getApplication().getTelephonyManager().hasIccCard()) {
            // No SIM card, remove the SIM-related prefs
            PreferenceCategory smsCategory =
                (PreferenceCategory)findPreference("pref_key_sms_settings");
            smsCategory.removePreference(mManageSimPref);
        }

        boolean SMSDeliveryReport = Resources.getSystem()
                .getBoolean(com.android.internal.R.bool.config_sms_delivery_reports_support);
        if (!SMSDeliveryReport) {
            PreferenceCategory smsCategory =
                (PreferenceCategory)findPreference("pref_key_sms_settings");
            smsCategory.removePreference(mSmsDeliveryReportPref);
            if (!MmsApp.getApplication().getTelephonyManager().hasIccCard()) {
                getPreferenceScreen().removePreference(smsCategory);
            }
        }

        if (!MmsConfig.getMmsEnabled()) {
            // No Mms, remove all the mms-related preferences
            PreferenceCategory mmsOptions =
                (PreferenceCategory)findPreference("pref_key_mms_settings");
            getPreferenceScreen().removePreference(mmsOptions);

            PreferenceCategory storageOptions =
                (PreferenceCategory)findPreference("pref_key_storage_settings");
            storageOptions.removePreference(findPreference("pref_key_mms_delete_limit"));
        } else {
            boolean MMSDeliveryReport = Resources.getSystem()
                    .getBoolean(com.android.internal.R.bool.config_mms_delivery_reports_support);
            boolean MMSReadReport = Resources.getSystem()
                    .getBoolean(com.android.internal.R.bool.config_mms_read_reports_support);
            if (!MMSDeliveryReport) {
                PreferenceCategory mmsOptions =
                    (PreferenceCategory)findPreference("pref_key_mms_settings");
                mmsOptions.removePreference(mMmsDeliveryReportPref);
            }
            if (!MMSReadReport) {
                PreferenceCategory mmsOptions =
                    (PreferenceCategory)findPreference("pref_key_mms_settings");
                mmsOptions.removePreference(mMmsReadReportPref);
            }
        }

        // If needed, migrate vibration setting from a previous version
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (!sharedPreferences.contains(NOTIFICATION_VIBRATE_WHEN) &&
                sharedPreferences.contains(NOTIFICATION_VIBRATE)) {
            int stringId = sharedPreferences.getBoolean(NOTIFICATION_VIBRATE, false) ?
                    R.string.prefDefault_vibrate_true :
                    R.string.prefDefault_vibrate_false;
            mVibrateWhenPref.setValue(getString(stringId));
        }

        mManageTemplate.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(MessagingPreferenceActivity.this,
                        TemplatesListActivity.class);
                startActivity(intent);
                return false;
            }
        });

        String gestureSensitivity = String.valueOf(sharedPreferences.getInt(GESTURE_SENSITIVITY_VALUE, 3));

        mGestureSensitivity.setSummary(gestureSensitivity);
        mGestureSensitivity.setValue(gestureSensitivity);
        mGestureSensitivity.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int value = Integer.parseInt((String) newValue);
                sharedPreferences.edit().putInt(GESTURE_SENSITIVITY_VALUE, value).commit();
                mGestureSensitivity.setSummary(String.valueOf(value));
                return true;
            }
        });

        mSmsRecycler = Recycler.getSmsRecycler();
        mMmsRecycler = Recycler.getMmsRecycler();

        // Fix up the recycler's summary with the correct values
        setSmsDisplayLimit();
        setMmsDisplayLimit();
    }

    private void setSmsDisplayLimit() {
        mSmsLimitPref.setSummary(
                getString(R.string.pref_summary_delete_limit,
                        mSmsRecycler.getMessageLimit(this)));
    }

    private void setMmsDisplayLimit() {
        mMmsLimitPref.setSummary(
                getString(R.string.pref_summary_delete_limit,
                        mMmsRecycler.getMessageLimit(this)));
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.clear();
        menu.add(0, MENU_RESTORE_DEFAULTS, 0, R.string.restore_default);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESTORE_DEFAULTS:
                restoreDefaultPreferences();
                return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        boolean value;
        if (preference == mSmsLimitPref) {
            new NumberPickerDialog(this,
                    mSmsLimitListener,
                    mSmsRecycler.getMessageLimit(this),
                    mSmsRecycler.getMessageMinLimit(),
                    mSmsRecycler.getMessageMaxLimit(),
                    R.string.pref_title_sms_delete).show();
        } else if (preference == mMmsLimitPref) {
            new NumberPickerDialog(this,
                    mMmsLimitListener,
                    mMmsRecycler.getMessageLimit(this),
                    mMmsRecycler.getMessageMinLimit(),
                    mMmsRecycler.getMessageMaxLimit(),
                    R.string.pref_title_mms_delete).show();
        } else if (preference == mManageSimPref) {
            startActivity(new Intent(this, ManageSimMessages.class));
        } else if (preference == mHideAvatarPref) {
            value = mHideAvatarPref.isChecked();
            if (value) {
               mHideAvatarPref.setChecked(true);
            } else {
               mHideAvatarPref.setChecked(false);
            }
        } else if (preference == mEnableSmsPopupPref) {
            value = mEnableSmsPopupPref.isChecked();
            Settings.System.putInt(getContentResolver(), Settings.System.USE_POPUP_SMS, value ? 1: 0);
            mEnableSmsPopupPref.setChecked(value ? true : false);
        } else if (preference == mBlackBackgroundPref) {
            value = mBlackBackgroundPref.isChecked();
            if (value) {
               mBlackBackgroundPref.setChecked(true);
               mTransparentBackgroundPref.setChecked(false);
               mBubbleSpeechPref.setChecked(false);
            } else {
               mBlackBackgroundPref.setChecked(false);
            }
        } else if (preference == mTransparentBackgroundPref) {
            value = mTransparentBackgroundPref.isChecked();
            if (value) {
               mBlackBackgroundPref.setChecked(false);
               mTransparentBackgroundPref.setChecked(true);
               mBubbleSpeechPref.setChecked(false);
            } else {
               mTransparentBackgroundPref.setChecked(false);
            }
        } else if (preference == mBubbleSpeechPref) {
            value = mBubbleSpeechPref.isChecked();
            if (value) {
               mBlackBackgroundPref.setChecked(false);
               mTransparentBackgroundPref.setChecked(false);
               mBubbleSpeechPref.setChecked(true);
            } else {
               mBubbleSpeechPref.setChecked(false);
            }
        } else if (preference == mClearHistoryPref) {
            showDialog(CONFIRM_CLEAR_SEARCH_HISTORY_DIALOG);
            return true;
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }


    private void restoreDefaultPreferences() {
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit().clear().apply();
        setPreferenceScreen(null);
        addPreferencesFromResource(R.xml.preferences);
        setMessagePreferences();
    }

    NumberPickerDialog.OnNumberSetListener mSmsLimitListener =
        new NumberPickerDialog.OnNumberSetListener() {
            public void onNumberSet(int limit) {
                mSmsRecycler.setMessageLimit(MessagingPreferenceActivity.this, limit);
                setSmsDisplayLimit();
            }
    };

    NumberPickerDialog.OnNumberSetListener mMmsLimitListener =
        new NumberPickerDialog.OnNumberSetListener() {
            public void onNumberSet(int limit) {
                mMmsRecycler.setMessageLimit(MessagingPreferenceActivity.this, limit);
                setMmsDisplayLimit();
            }
    };

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case CONFIRM_CLEAR_SEARCH_HISTORY_DIALOG:
                return new AlertDialog.Builder(MessagingPreferenceActivity.this)
                    .setTitle(R.string.confirm_clear_search_title)
                    .setMessage(R.string.confirm_clear_search_text)
                    .setPositiveButton(android.R.string.ok, new AlertDialog.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            SearchRecentSuggestions recent =
                                ((MmsApp)getApplication()).getRecentSuggestions();
                            if (recent != null) {
                                recent.clearHistory();
                            }
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .create();
        }
        return super.onCreateDialog(id);
    }

}
