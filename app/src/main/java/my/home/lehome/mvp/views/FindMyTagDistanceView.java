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

package my.home.lehome.mvp.views;

import android.content.Context;

import java.util.List;

/**
 * Created by legendmohe on 15/4/21.
 */
public interface FindMyTagDistanceView extends MVPView {
    public Context getApplicationContext();

    void onBeaconEnter(String uid);
//
//    void onBeaconExit(String uid);
//
//    void onBeaconState(int var1, String uid);

    void onBeaconDistance(String uid, String bdName, double distance, List<Long> data);

    void showBeaconsDialog();

    void onBtEnable();

    void onBtDisable();

    void onBtTurningOn();
}
