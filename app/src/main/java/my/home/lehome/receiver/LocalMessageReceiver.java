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
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import my.home.lehome.R;
import my.home.lehome.helper.LocationHelper;
import my.home.lehome.helper.MessageHelper;

/**
 * Created by legendmohe on 15/3/11.
 */
public class LocalMessageReceiver extends BroadcastReceiver {

    public final static String LOCAL_MSG_RECEIVER_ACTION = "my.home.lehome.receiver.LocalMessageReceiver";
    public final static String LOCAL_MSG_REP_KEY = "Local:Rep";
    private static final String TAG = "LocalMessageReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(LOCAL_MSG_RECEIVER_ACTION)) {
            String lm = intent.getStringExtra(LOCAL_MSG_REP_KEY);
            Log.d(TAG, "receive local msg: " + lm);
            if (lm != null) {
                JSONTokener jsonParser = new JSONTokener(lm);
                String type = "";
                String msg = "";
                String err_msg = "";
                int seq = -1;
                try {
                    JSONObject cmdObject = (JSONObject) jsonParser.nextValue();
                    type = cmdObject.getString("type");
                    msg = cmdObject.getString("msg");
                    seq = cmdObject.getInt("seq");
                    if (MessageHelper.enqueueMsgSeq(context, seq))
                        return;
                } catch (JSONException e) {
                    e.printStackTrace();
                    err_msg = context.getString(R.string.msg_push_msg_format_error);
                } catch (Exception e) {
                    e.printStackTrace();
                    err_msg = context.getString(R.string.msg_push_msg_format_error);
                }

                if (!TextUtils.isEmpty(err_msg)) {
                    MessageHelper.sendToast(err_msg);
                    return;
                }

                if (type.equals("req_loc")) {
                    LocationHelper.enqueueLocationRequest(context, seq, type, msg);
                    return;
                }

                if (type.equals("normal") || type.equals("capture")) {
                    MessageHelper.inNormalState = true;
                } else if (type.equals("toast")) {
                    MessageHelper.sendToast(msg);
                    return;
                } else {
                    MessageHelper.inNormalState = false;
                }
                MessageHelper.sendServerMsgToList(seq, type, msg, context);
            }
        }
    }
}
