/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package my.home.lehome.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import my.home.domain.usecase.DeleteAutoCompleteHistoryUsecaseImpl;
import my.home.lehome.R;
import my.home.lehome.asynctask.LoadAutoCompleteConfAsyncTask;
import my.home.lehome.helper.NFCHelper;
import my.home.lehome.helper.NetworkHelper;

public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
    private String mDeviceId;
    private String mServerAddress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
        CheckBoxPreference checkBoxPreference = (CheckBoxPreference) findPreference("pref_auto_add_begin_and_end");
        EditTextPreference beginEditTextPreference = (EditTextPreference) findPreference("pref_message_begin");
        EditTextPreference endEditTextPreference = (EditTextPreference) findPreference("pref_message_end");
        boolean is_auto = sharedPreferences.getBoolean("pref_auto_add_begin_and_end", false);
        if (is_auto) {
            checkBoxPreference.setChecked(true);
            beginEditTextPreference.setEnabled(true);
            endEditTextPreference.setEnabled(true);
        } else {
            checkBoxPreference.setChecked(false);
            beginEditTextPreference.setEnabled(false);
            endEditTextPreference.setEnabled(false);
        }
        boolean is_autocomplete = sharedPreferences.getBoolean("pref_cmd_autocomplete", true);
        ((CheckBoxPreference) findPreference("pref_cmd_autocomplete")).setChecked(is_autocomplete);

        boolean is_currect = sharedPreferences.getBoolean("pref_cmd_need_correct", true);
        ((CheckBoxPreference) findPreference("pref_cmd_need_correct")).setChecked(is_currect);

        beginEditTextPreference.setSummary(sharedPreferences.getString("pref_message_begin", ""));
        endEditTextPreference.setSummary(sharedPreferences.getString("pref_message_end", ""));

        EditTextPreference nameEditTextPreference = (EditTextPreference) findPreference("pref_client_id");
        EditTextPreference subEditTextPreference = (EditTextPreference) findPreference("pref_server_address");
        EditTextPreference pubEditTextPreference = (EditTextPreference) findPreference("pref_bind_device");
        nameEditTextPreference.setSummary(sharedPreferences.getString("pref_client_id", ""));
        subEditTextPreference.setSummary(sharedPreferences.getString("pref_server_address", ""));
        pubEditTextPreference.setSummary(sharedPreferences.getString("pref_bind_device", ""));

        CheckBoxPreference confirmPreference = (CheckBoxPreference) findPreference("pref_speech_cmd_need_confirm");
        boolean need_confirm = sharedPreferences.getBoolean("pref_speech_cmd_need_confirm", true);
        confirmPreference.setChecked(need_confirm);
        CheckBoxPreference btSCOPreference = (CheckBoxPreference) findPreference("pref_auto_connect_sco");
        boolean auto_sco = sharedPreferences.getBoolean("pref_auto_connect_sco", true);
        btSCOPreference.setChecked(auto_sco);
        CheckBoxPreference savePowerPreference = (CheckBoxPreference) findPreference("pref_save_power_mode");
        boolean savePowerOn = sharedPreferences.getBoolean("pref_save_power_mode", true);
        savePowerPreference.setChecked(savePowerOn);
        CheckBoxPreference wifiImgPreference = (CheckBoxPreference) findPreference("pref_load_img_wifi");
        boolean wifiLoadOn = sharedPreferences.getBoolean("pref_load_img_wifi", false);
        wifiImgPreference.setChecked(wifiLoadOn);

        CheckBoxPreference locEnablePreference = (CheckBoxPreference) findPreference("pref_loc_me_enable");
        boolean locEnable = sharedPreferences.getBoolean("pref_loc_me_enable", true);
        locEnablePreference.setChecked(locEnable);
        CheckBoxPreference silentLocCheckBoxPreference = (CheckBoxPreference) findPreference("pref_loc_me_silent_enable");
        boolean silent_loc = sharedPreferences.getBoolean("pref_loc_me_silent_enable", false);
        silentLocCheckBoxPreference.setChecked(silent_loc);
        silentLocCheckBoxPreference.setEnabled(locEnable);


        CheckBoxPreference volumeKeyPreference = (CheckBoxPreference) findPreference("pref_volume_key_control_speech");
        boolean volume_key_speech = sharedPreferences.getBoolean("pref_volume_key_control_speech", true);
        volumeKeyPreference.setChecked(volume_key_speech);

        mDeviceId = sharedPreferences.getString("pref_bind_device", "");
        mServerAddress = sharedPreferences.getString("pref_server_address", "");
        Preference button = findPreference("load_auto_item");
        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference arg0) {
                new LoadAutoCompleteConfAsyncTask(getActivity(), mServerAddress, mDeviceId).execute();
                return true;
            }
        });
        button = findPreference("local_ip_item");
        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference arg0) {
                String ipString = NetworkHelper.getIPAddress(true);
                Toast.makeText(getActivity(), getResources().getString(R.string.pref_local_ip_item) + ":" + ipString, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        button = findPreference("pref_delete_autocomplete_history");
        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new DeleteAutoCompleteHistoryUsecaseImpl(getActivity()).execute();
                Toast.makeText(getActivity(), getResources().getString(R.string.pref_delete_autocomplete_history_toast), Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        //init local ssid preference
        EditTextPreference ssidEditTextPreference = (EditTextPreference) findPreference("pref_local_ssid");
        ssidEditTextPreference.setSummary(sharedPreferences.getString("pref_local_ssid", ""));
        //init local server preference
        EditTextPreference addressEditTextPreference = (EditTextPreference) findPreference("pref_local_msg_server_address");
        addressEditTextPreference.setSummary(sharedPreferences.getString("pref_local_msg_server_address", ""));
        //init local subscribe preference
        EditTextPreference subscribeEditTextPreference = (EditTextPreference) findPreference("pref_local_msg_subscribe_address");
        subscribeEditTextPreference.setSummary(sharedPreferences.getString("pref_local_msg_subscribe_address", ""));
        //init state
        CheckBoxPreference lMsgCheckBoxPreference = (CheckBoxPreference) findPreference("pref_enable_local_msg");
        boolean enable_local_msg = sharedPreferences.getBoolean("pref_enable_local_msg", false);
        if (enable_local_msg) {
            lMsgCheckBoxPreference.setChecked(true);
            ssidEditTextPreference.setEnabled(true);
            subscribeEditTextPreference.setEnabled(true);
            addressEditTextPreference.setEnabled(true);
        } else {
            lMsgCheckBoxPreference.setChecked(false);
            ssidEditTextPreference.setEnabled(false);
            subscribeEditTextPreference.setEnabled(false);
            addressEditTextPreference.setEnabled(false);
        }
        
        //nfc state
        CheckBoxPreference nfcCheckBoxPreference = (CheckBoxPreference) findPreference("pref_nfc_cmd_enable");
        boolean enable_nfc_cmd = sharedPreferences.getBoolean("pref_nfc_cmd_enable", true);
        nfcCheckBoxPreference.setChecked(enable_nfc_cmd);


        Intent retIntent = new Intent();
        retIntent.putExtra("old_device_id", pubEditTextPreference.getSummary());
        retIntent.putExtra("old_local_msg_state", enable_local_msg);
        retIntent.putExtra("old_subscribe_address", subscribeEditTextPreference.getSummary());
        this.getActivity().setResult(Activity.RESULT_OK, retIntent);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("pref_message_begin") || key.equals("pref_message_end")) {
            Preference exercisesPref = findPreference(key);
            exercisesPref.setSummary(sharedPreferences.getString(key, ""));
        } else if (key.equals("pref_server_address")) {
            Preference exercisesPref = findPreference(key);
            exercisesPref.setSummary(sharedPreferences.getString(key, ""));
            mServerAddress = sharedPreferences.getString("pref_server_address", "");
        } else if (key.equals("pref_bind_device")) {
            Preference exercisesPref = findPreference(key);
            exercisesPref.setSummary(sharedPreferences.getString(key, ""));
            mDeviceId = sharedPreferences.getString("pref_bind_device", "");
        } else if (key.equals("pref_client_id")) {
            Preference exercisesPref = findPreference(key);
            exercisesPref.setSummary(sharedPreferences.getString(key, ""));
        } else if (key.equals("pref_auto_add_begin_and_end")) {
            if (sharedPreferences.getBoolean("pref_auto_add_begin_and_end", false)) {
                findPreference("pref_message_begin").setEnabled(true);
                findPreference("pref_message_end").setEnabled(true);
            } else {
                findPreference("pref_message_begin").setEnabled(false);
                findPreference("pref_message_end").setEnabled(false);
            }
        } else if (key.equals("pref_enable_local_msg")) {
            if (sharedPreferences.getBoolean("pref_enable_local_msg", false)) {
                findPreference("pref_local_msg_server_address").setEnabled(true);
                findPreference("pref_local_ssid").setEnabled(true);
                findPreference("pref_local_msg_subscribe_address").setEnabled(true);

//                ComponentName receiver = new ComponentName(getActivity(), BootCompleteReceiver.class);
//                PackageManager pm = getActivity().getPackageManager();
//
//                pm.setComponentEnabledSetting(receiver,
//                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
//                        PackageManager.DONT_KILL_APP);
            } else {
                findPreference("pref_local_msg_server_address").setEnabled(false);
                findPreference("pref_local_ssid").setEnabled(false);
                findPreference("pref_local_msg_subscribe_address").setEnabled(false);
//                ComponentName receiver = new ComponentName(getActivity(), BootCompleteReceiver.class);
//                PackageManager pm = getActivity().getPackageManager();
//
//                pm.setComponentEnabledSetting(receiver,
//                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
//                        PackageManager.DONT_KILL_APP);
            }
        } else if (key.equals("pref_local_ssid")) {
            EditTextPreference ssidEditTextPreference = (EditTextPreference) findPreference("pref_local_ssid");
            ssidEditTextPreference.setSummary(sharedPreferences.getString("pref_local_ssid", ""));
        } else if (key.equals("pref_local_msg_server_address")) {
            EditTextPreference addressEditTextPreference = (EditTextPreference) findPreference("pref_local_msg_server_address");
            addressEditTextPreference.setSummary(sharedPreferences.getString("pref_local_msg_server_address", ""));
        } else if (key.equals("pref_local_msg_subscribe_address")) {
            EditTextPreference addressEditTextPreference = (EditTextPreference) findPreference("pref_local_msg_subscribe_address");
            addressEditTextPreference.setSummary(sharedPreferences.getString("pref_local_msg_subscribe_address", ""));
        } else if (key.equals("pref_save_power_mode")) {
            Toast.makeText(getActivity(), R.string.pref_save_power_mode_set, Toast.LENGTH_SHORT).show();
        } else if (key.equals("pref_loc_me_enable")) {
            CheckBoxPreference silentLocCheckBoxPreference = (CheckBoxPreference) findPreference("pref_loc_me_silent_enable");
            silentLocCheckBoxPreference.setEnabled(sharedPreferences.getBoolean("pref_loc_me_silent_enable", false));
        } else if (key.equals("pref_nfc_cmd_enable")) {
            if (sharedPreferences.getBoolean("pref_nfc_cmd_enable", true)) {
                Context context = getActivity().getApplicationContext();
                if (!NFCHelper.isNfcEnable(context)) {
                    Toast.makeText(context, R.string.toast_nfc_feature_disable, Toast.LENGTH_SHORT).show();
                }
            }
        } else if (key.equals("pref_cmd_autocomplete")) {
            Toast.makeText(getActivity(), R.string.pref_cmd_autocomplete_set, Toast.LENGTH_SHORT).show();
        }
    }
}
