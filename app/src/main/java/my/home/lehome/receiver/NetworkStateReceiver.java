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

package my.home.lehome.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import my.home.common.NetworkUtil;
import my.home.common.PrefUtil;

/**
 * Created by legendmohe on 15/3/8.
 */
public class NetworkStateReceiver extends BroadcastReceiver {

    static final String TAG = "NetworkStateReceiver";
    // same value in SettingsFragment
    static final String PREF_SSID_KEY = "pref_local_ssid";

    @Override
    public void onReceive(Context context, Intent intent) {
        NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        if (info != null) {
            Log.d(TAG, "NetworkInfo: " + info);
            if (info.isConnected()) {
                String ssid = NetworkUtil.getSSID(context);
                String prefSSID = PrefUtil.getStringValue(context, PREF_SSID_KEY);
                if (ssid.equals(prefSSID)) {
                }
            }
        }
    }


}
