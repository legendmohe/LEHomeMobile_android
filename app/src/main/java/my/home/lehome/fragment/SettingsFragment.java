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

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import my.home.lehome.R;
import my.home.lehome.asynctask.LoadAutoCompleteConfAsyncTask;
import my.home.lehome.helper.NetworkHelper;

public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
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
        boolean is_currect = sharedPreferences.getBoolean("pref_cmd_need_correct", true);
        ((CheckBoxPreference) findPreference("pref_cmd_need_correct")).setChecked(is_currect);

        beginEditTextPreference.setSummary(sharedPreferences.getString("pref_message_begin", ""));
        endEditTextPreference.setSummary(sharedPreferences.getString("pref_message_end", ""));

        EditTextPreference subEditTextPreference = (EditTextPreference) findPreference("pref_server_address");
        EditTextPreference pubEditTextPreference = (EditTextPreference) findPreference("pref_bind_device");
        subEditTextPreference.setSummary(sharedPreferences.getString("pref_server_address", ""));
        pubEditTextPreference.setSummary(sharedPreferences.getString("pref_bind_device", ""));

        CheckBoxPreference confirmPreference = (CheckBoxPreference) findPreference("pref_speech_cmd_need_confirm");
        boolean need_confirm = sharedPreferences.getBoolean("pref_speech_cmd_need_confirm", true);
        confirmPreference.setChecked(need_confirm);
        CheckBoxPreference btSCOPreference = (CheckBoxPreference) findPreference("pref_auto_connect_sco");
        boolean auto_sco = sharedPreferences.getBoolean("pref_auto_connect_sco", true);
        btSCOPreference.setChecked(auto_sco);

//        Preference button = (Preference) findPreference("homescreen_shortcut");
//        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//            @Override
//            public boolean onPreferenceClick(Preference arg0) {
//                Intent shortcutIntent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
//                shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//
//                Intent addIntent = new Intent();
//                addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
//                addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, R.string.app_name);
//                addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(getActivity().getApplicationContext(), R.drawable.ic_launcher));
//
//                addIntent.setAction("com.android.launcher.action.UNINSTALL_SHORTCUT");
//                getActivity().sendBroadcast(addIntent);
//                addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
//                getActivity().sendBroadcast(addIntent);
//
////                        	ShortcutIconResource icon =
////                        		    Intent.ShortcutIconResource.fromContext(getActivity(), R.drawable.ic_launcher);
////
////                    		Intent intent = new Intent();
////
////                    		Intent launchIntent = new Intent(getActivity(), MainActivity.class);
////
////                    		intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launchIntent);
////                    		intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, R.string.app_name);
////                    		intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
////
////							getActivity().setResult(Activity.RESULT_OK, intent);
//
//                Toast.makeText(
//                        getActivity()
//                        , R.string.pref_homescreen_shortcut_smy
//                        , Toast.LENGTH_SHORT)
//                        .show();
//                return true;
//            }
//        });
//        Preference button = (Preference) findPreference("clean_cmd_history");
//        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//            @Override
//            public boolean onPreferenceClick(Preference arg0) {
//                Context context = getActivity().getApplicationContext();
//                SharedPreferences pref = context.getSharedPreferences(Constants.PREF_NAME, 0);
//                SharedPreferences.Editor editor = pref.edit();
//                editor.putStringSet(Constants.CMD_HISTORY_PREF_NAME, new HashSet<String>());
//                editor.commit();
//
//                Toast.makeText(
//                        getActivity()
//                        , R.string.pref_clean_cmd_history
//                        , Toast.LENGTH_SHORT)
//                        .show();
//                return true;
//            }
//        });
        final String device_id = sharedPreferences.getString("pref_bind_device", "");
        Preference button = (Preference) findPreference("load_auto_item");
        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference arg0) {
                new LoadAutoCompleteConfAsyncTask(getActivity(), device_id).execute();
                return true;
            }
        });
        button = (Preference) findPreference("local_ip_item");
        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference arg0) {
                String ipString = NetworkHelper.getIPAddress(true);
                Toast.makeText(getActivity(), getResources().getString(R.string.pref_local_ip_item) + ":" + ipString, Toast.LENGTH_SHORT).show();
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
            subscribeEditTextPreference.setEnabled(true);
            addressEditTextPreference.setEnabled(false);
        }
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
        } else if (key.equals("pref_server_address") || key.equals("pref_bind_device")) {
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
        }
    }
}
