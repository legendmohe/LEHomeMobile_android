package my.home.lehome.fragment;

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

import java.util.HashSet;

import my.home.common.Constants;
import my.home.lehome.R;
import my.home.lehome.activity.MainActivity;

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

        Preference button = (Preference) findPreference("homescreen_shortcut");
        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference arg0) {
                Intent shortcutIntent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
                shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                Intent addIntent = new Intent();
                addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
                addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, R.string.app_name);
                addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(getActivity().getApplicationContext(), R.drawable.ic_launcher));

                addIntent.setAction("com.android.launcher.action.UNINSTALL_SHORTCUT");
                getActivity().sendBroadcast(addIntent);
                addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
                getActivity().getApplicationContext().sendBroadcast(addIntent);

//                        	ShortcutIconResource icon =
//                        		    Intent.ShortcutIconResource.fromContext(getActivity(), R.drawable.ic_launcher);
//
//                    		Intent intent = new Intent();
//
//                    		Intent launchIntent = new Intent(getActivity(), MainActivity.class);
//
//                    		intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launchIntent);
//                    		intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, R.string.app_name);
//                    		intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
//
//							getActivity().setResult(Activity.RESULT_OK, intent);

                Toast.makeText(
                        getActivity()
                        , R.string.pref_homescreen_shortcut_smy
                        , Toast.LENGTH_SHORT)
                        .show();
                return true;
            }
        });
        button = (Preference) findPreference("clean_cmd_history");
        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference arg0) {
                Context context = getActivity().getApplicationContext();
                SharedPreferences pref = context.getSharedPreferences(Constants.PREF_NAME, 0);
                SharedPreferences.Editor editor = pref.edit();
                editor.putStringSet(Constants.CMD_HISTORY_PREF_NAME, new HashSet<String>());
                editor.commit();

                Toast.makeText(
                        getActivity()
                        , R.string.pref_clean_cmd_history
                        , Toast.LENGTH_SHORT)
                        .show();
                return true;
            }
        });
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
        }
    }
}
