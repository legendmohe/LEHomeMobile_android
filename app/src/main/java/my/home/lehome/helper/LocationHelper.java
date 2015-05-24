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
import android.net.Uri;
import android.util.Log;

import my.home.common.PrefUtil;
import my.home.lehome.service.LocationIntentService;

/**
 * Created by legendmohe on 15/5/23.
 */
public class LocationHelper {

    public static final String TAG = "LocationHelper";


    public static void enqueueLocationRequest(Context context, int seq, String type, String clientId) {
        Log.d(TAG, "enqueu location request for: " + clientId);
        String clientName = PrefUtil.getStringValue(context, "pref_client_id", "");
        if (clientName.equals(clientId)) {
            Intent serviceIntent = new Intent(context, LocationIntentService.class);
            serviceIntent.putExtra("seq", seq);
            serviceIntent.putExtra("type", type);
            serviceIntent.putExtra("id", clientId);
            context.startService(serviceIntent);
        }
    }

    public static String getBaiduStaticMapImgUrl(String lng, String lat, int width, int height, int zoom) {
        String format = "http://api.map.baidu.com/staticimage?scale=1&width=%d&height=%d&center=%s,%s&zoom=%d&markers=%s,%s&markerStyles=m,";
        return String.format(format, width, height, lng, lat, zoom, lng, lat);
    }

    public static Intent getBaiduMapUrlIntent(String lng, String lat, String title, String content, String comName, String appName) {
//        String format = "intent://map/marker?location=%s,%s&title=%s&content=%s&src=%s|%s#Intent;scheme=bdapp;package=com.baidu.BaiduMap;end";
//        return new Intent(String.format(format, lng, lat, title, content, comName, appName));
        String format = "http://api.map.baidu.com/marker?location=%s,%s&title=%s&content=%s&output=html&src=%s|%s";
        String url = String.format(format, lat, lng, title, content, comName, appName);
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        return i;
//        return new Intent("intent://map/line?coordtype=&zoom=&region=abd&name=28&src=yourCompanyName|yourAppName#Intent;scheme=bdapp;package=com.baidu.BaiduMap;end");
    }

    public static String[] parseLocationFromSrc(String src) {
        return src.split("\\|");
    }
}
