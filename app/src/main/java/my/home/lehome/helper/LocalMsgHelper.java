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

package my.home.lehome.helper;

import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;

import my.home.common.NetworkUtil;
import my.home.common.PrefUtil;
import my.home.lehome.service.LocalMessageService;
import my.home.lehome.util.Constants;

/**
 * Created by legendmohe on 15/3/15.
 */
public class LocalMsgHelper {

    public static boolean startLocalMsgService(Context context) {
        if (!MessageHelper.isLocalMsgServiceEnable())
            return false;
        Intent serviceIntent = new Intent(context, LocalMessageService.class);
        context.startService(serviceIntent);
        return true;
    }

    public static void stopLocalMsgService(Context context) {
        Intent serviceIntent = new Intent(context, LocalMessageService.class);
        context.stopService(serviceIntent);
        return;
    }

    public static boolean canUseLocalMessageService(Context context) {
        NetworkInfo wifiNetworkInfo = NetworkUtil.getWifiNetworkInfo(context);
        if (wifiNetworkInfo.isConnected()) {
            String ssid = NetworkUtil.getFormatSSID(context);
            String prefSSID = PrefUtil.getStringValue(context, Constants.PREF_SSID_KEY);
            if (ssid.equals(prefSSID)) {
                return true;
            }
        }
        return false;
    }

}
