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

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.android.tpush.XGPushBaseReceiver;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushRegisterResult;
import com.tencent.android.tpush.XGPushShowedResult;
import com.tencent.android.tpush.XGPushTextMessage;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import my.home.lehome.R;
import my.home.lehome.helper.LocationHelper;
import my.home.lehome.helper.MessageHelper;


public class PushMessageReceiver extends XGPushBaseReceiver {

    public static final String TAG = PushMessageReceiver.class
            .getSimpleName();

    public void onMessage(Context context, String message) {

        JSONTokener jsonParser = new JSONTokener(message);
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
        } else if (type.equals("bc_loc")) {
        } else if (type.equals("normal") || type.equals("capture")) {
            MessageHelper.inNormalState = true;
        } else if (type.equals("toast")) {
            MessageHelper.sendToast(msg);
            return;
        } else {
            MessageHelper.inNormalState = false;
        }
        MessageHelper.sendServerMsgToList(seq, type, msg, context);
    }

    private void updateContent(Context context, String content) {
        MessageHelper.sendToast(content);
    }

    // --------------- XG calbacks --------------

    @Override
    public void onRegisterResult(Context context, int errorCode, XGPushRegisterResult message) {
        if (context == null || message == null) {
            return;
        }
        Log.d(TAG, message.toString());
        String text = "";
        if (errorCode == XGPushBaseReceiver.SUCCESS) {
            text = context.getString(R.string.msg_push_binded);
            // 在这里拿token
//            String token = message.getToken();
        } else {
            text = message + "注册失败，错误码：" + errorCode;
        }
        Log.d(TAG, text);
        updateContent(context, text);
    }

    @Override
    public void onUnregisterResult(Context context, int errorCode) {
        if (context == null) {
            return;
        }
        String text = "";
        if (errorCode == XGPushBaseReceiver.SUCCESS) {
            text = "反注册成功";
        } else {
            text = "反注册失败" + errorCode;
        }
        Log.d(TAG, text);
        updateContent(context, text);
    }

    @Override
    public void onSetTagResult(Context context, int errorCode, String tagName) {
        if (context == null) {
            return;
        }
        String text = "";
        if (errorCode == XGPushBaseReceiver.SUCCESS) {
            text = context.getString(R.string.msg_device_binded);
        } else {
            text = context.getString(R.string.msg_device_bind_faild);
        }
        Log.d(TAG, text);
        updateContent(context, text);
    }

    @Override
    public void onDeleteTagResult(Context context, int errorCode, String tagName) {
        if (context == null) {
            return;
        }
        String text = "";
        if (errorCode == XGPushBaseReceiver.SUCCESS) {
            text = "\"" + tagName + "\"删除成功";
        } else {
            text = "\"" + tagName + "\"删除失败,错误码：" + errorCode;
        }
        Log.d(TAG, text);
        updateContent(context, text);
    }

    @Override
    public void onTextMessage(Context context, XGPushTextMessage message) {
        String text = "收到消息:" + message.toString();
        // 获取自定义key-value
        String customContent = message.getCustomContent();
        if (customContent != null && customContent.length() != 0) {
            try {
                JSONObject obj = new JSONObject(customContent);
                // key1为前台配置的key
                if (!obj.isNull("key")) {
                    String value = obj.getString("key");
                    Log.d(TAG, "get custom value:" + value);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        String content = message.getContent();
        if (!TextUtils.isEmpty(content)) {
            onMessage(context, content);
        }
        Log.d(TAG, text);
//        updateContent(context, text);
    }

    @Override
    public void onNotifactionClickedResult(Context context, XGPushClickedResult xgPushClickedResult) {

    }

    @Override
    public void onNotifactionShowedResult(Context context, XGPushShowedResult xgPushShowedResult) {

    }
}
