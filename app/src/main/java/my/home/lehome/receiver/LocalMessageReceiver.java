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

/**
 * Created by legendmohe on 15/3/11.
 */
public class LocalMessageReceiver extends BroadcastReceiver {

    public final static String LOCAL_MSG_RECEIVER_ACTION = "my.home.lehome.receiver.LocalMessageReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(LOCAL_MSG_RECEIVER_ACTION)) {

        }
    }
}
