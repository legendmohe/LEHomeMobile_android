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
import android.util.Log;

import my.home.common.NetworkUtil;
import my.home.common.PrefUtil;
import my.home.lehome.helper.LocalMsgHelper;
import my.home.lehome.util.Constants;

/**
 * Created by legendmohe on 15/3/11.
 */
public class ScreenStateReceiver extends BroadcastReceiver {

    static final String TAG = "ScreenStateReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            NetworkInfo wifiNetworkInfo = NetworkUtil.getWifiNetworkInfo(context);
            if (wifiNetworkInfo.isConnected()) {
                Log.d(TAG, "NetworkInfo: " + wifiNetworkInfo);
                String ssid = NetworkUtil.getFormatSSID(context);
                String prefSSID = getLocalSSID(context);
                if (ssid.equals(prefSSID)) {
                    Log.d(TAG, "start " + TAG);
                    LocalMsgHelper.startLocalMsgService(context);
                }
            }
        }
    }
}
