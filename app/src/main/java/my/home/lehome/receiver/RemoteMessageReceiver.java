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

import com.xiaomi.mipush.sdk.ErrorCode;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.xiaomi.mipush.sdk.MiPushCommandMessage;
import com.xiaomi.mipush.sdk.MiPushMessage;
import com.xiaomi.mipush.sdk.PushMessageReceiver;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.Arrays;
import java.util.List;

import my.home.common.util.PrefUtil;
import my.home.lehome.R;
import my.home.lehome.helper.LocationHelper;
import my.home.lehome.helper.MessageHelper;
import my.home.lehome.helper.PushSDKManager;
import my.home.lehome.mvp.presenters.MainActivityPresenter;


public class RemoteMessageReceiver extends PushMessageReceiver {

    private static final String TAG = "RemoteMessageReceiver";

    private RemoteMessageSDKStateHandler mRemoteMessageSDKStateHandler;

    public RemoteMessageReceiver() {
        super();
        mRemoteMessageSDKStateHandler = PushSDKManager.mRemoteStateHandler;
    }

    public void onMessage(Context context, String message) {
        if (PrefUtil.getbooleanValue(context, MainActivityPresenter.APP_EXIT_KEY, false)) {
            Log.d(TAG, "app set exit. ignore network state change.");
            return;
        }

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

        if (type.equals("req_loc") || type.equals("req_geo")) {
            LocationHelper.enqueueLocationRequest(context, seq, type, msg);
            return;
        } else if (Arrays.asList(MessageHelper.NORMAIL_FILTER_TAG_LIST).contains(type)) {
            MessageHelper.inNormalState = true;
        } else if (type.equals("toast")) {
            MessageHelper.sendToast(msg);
            return;
        } else {
            MessageHelper.inNormalState = false;
        }
        MessageHelper.sendServerMsgToList(seq, type, msg, context);
    }

    @Override
    public void onReceivePassThroughMessage(Context context, MiPushMessage message) {
        String text = "收到消息:" + message.toString();
        Log.d(TAG, text);
        String content = message.getContent();
        if (!TextUtils.isEmpty(content)) {
            onMessage(context, content);
        }
    }
    @Override
    public void onNotificationMessageClicked(Context context, MiPushMessage message) {

    }
    @Override
    public void onNotificationMessageArrived(Context context, MiPushMessage message) {

    }
    @Override
    public void onCommandResult(Context context, MiPushCommandMessage message) {
        String command = message.getCommand();
        List<String> arguments = message.getCommandArguments();
        String cmdArg1 = ((arguments != null && arguments.size() > 0) ? arguments.get(0) : null);
        String cmdArg2 = ((arguments != null && arguments.size() > 1) ? arguments.get(1) : null);
        if (MiPushClient.COMMAND_REGISTER.equals(command)) {
            Log.d(TAG, "COMMAND_REGISTER");
        } else if (MiPushClient.COMMAND_SET_ALIAS.equals(command)) {
            Log.d(TAG, "COMMAND_SET_ALIAS");
        } else if (MiPushClient.COMMAND_UNSET_ALIAS.equals(command)) {
            Log.d(TAG, "COMMAND_UNSET_ALIAS");
        } else if (MiPushClient.COMMAND_SUBSCRIBE_TOPIC.equals(command)) {
            if (mRemoteMessageSDKStateHandler != null) {
                mRemoteMessageSDKStateHandler.onSubscribeTopic(message.getResultCode() == ErrorCode.SUCCESS);
            }
            String text;
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                text = context.getString(R.string.msg_device_binded);
            } else {
                text = context.getString(R.string.msg_device_bind_faild);
            }
            Log.d(TAG, text);
            updateContent(context, text);
        } else if (MiPushClient.COMMAND_UNSUBSCRIBE_TOPIC.equals(command)) {
            if (mRemoteMessageSDKStateHandler != null) {
                mRemoteMessageSDKStateHandler.onUnsubscribeTopic(message.getResultCode() == ErrorCode.SUCCESS);
            }
            String text;
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                text = context.getString(R.string.msg_device_unbinded);
            } else {
                text = context.getString(R.string.msg_device_unbind_faild);
            }
            Log.d(TAG, text);
            updateContent(context, text);
        } else if (MiPushClient.COMMAND_SET_ACCEPT_TIME.equals(command)) {
            Log.d(TAG, "COMMAND_SET_ACCEPT_TIME");
        }
    }
    @Override
    public void onReceiveRegisterResult(Context context, MiPushCommandMessage message) {
        String command = message.getCommand();
        if (MiPushClient.COMMAND_REGISTER.equals(command)) {
            if (mRemoteMessageSDKStateHandler != null) {
                mRemoteMessageSDKStateHandler.onReceiveRegisterResult(message.getResultCode() == ErrorCode.SUCCESS);
            }
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                updateContent(context, context.getString(R.string.msg_push_binded));
            } else {
                updateContent(context, "注册失败，错误码：" + message.getResultCode());
            }
        }
    }


    private void updateContent(Context context, String content) {
        MessageHelper.sendToast(content);
    }

    public interface RemoteMessageSDKStateHandler {
        void onReceiveRegisterResult(boolean success);

        void onSubscribeTopic(boolean success);

        void onUnsubscribeTopic(boolean success);
    }
}
