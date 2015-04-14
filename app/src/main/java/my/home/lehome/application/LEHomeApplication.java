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

package my.home.lehome.application;

import android.app.Application;

import com.tencent.android.tpush.XGPushConfig;

public class LEHomeApplication extends Application {
    private final static String TAG = "LEHomeApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        XGPushConfig.enableDebug(getApplicationContext(), false);
//        XGPushConfig.setAccessId(getApplicationContext(), 2100063377);
//        XGPushConfig.setAccessKey(getApplicationContext(), "AE398MRA65DZ");
    }

}
