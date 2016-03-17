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
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.xiaomi.mipush.sdk.MiPushClient;

import java.lang.ref.WeakReference;

import my.home.common.util.PrefUtil;
import my.home.lehome.receiver.RemoteMessageReceiver;

//import com.tencent.android.tpush.XGIOperateCallback;
//import com.tencent.android.tpush.XGPushManager;

/**
 * Created by legendmohe on 15/4/16.
 */
public class PushSDKManager {
    public final static String TAG = "PushSDKManager";

    public final static int MSG_START_SDK = 0;
    public final static int MSG_STOP_SDK = 1;

    private static final String PREF_KEY_ENABLE = "PushSDKManager.enable";
    private static final String PREF_KEY_STARTING = "PushSDKManager.starting";
    private static final String PREF_KEY_STOPPING = "PushSDKManager.stopping";

    private static WeakReference<Context> CURRENT_CONTEXT;

    public static final String APP_ID = "2882303761517427372";
    public static final String APP_KEY = "5291742795372";

    private static final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            final Context context = CURRENT_CONTEXT.get();
            if (context == null) {
                Log.d(TAG, "null context.");
                return;
            }
            if (msg.what == PushSDKManager.MSG_START_SDK) {
                Log.d(TAG, "try start sdk.");
                MiPushClient.registerPush(context, APP_ID, APP_KEY);
            } else if (msg.what == PushSDKManager.MSG_STOP_SDK) {
                Log.d(TAG, "try stop sdk.");
                MiPushClient.unregisterPush(context);
                PrefUtil.setBooleanValue(context, PREF_KEY_ENABLE, false);
                PrefUtil.setBooleanValue(context, PREF_KEY_STOPPING, false);
            }
        }
    };

    public static final RemoteMessageReceiver.RemoteMessageSDKStateHandler mRemoteStateHandler = new RemoteMessageReceiver.RemoteMessageSDKStateHandler() {
        @Override
        public void onReceiveRegisterResult(boolean success) {
            final Context context = CURRENT_CONTEXT.get();
            if (context == null) {
                Log.d(TAG, "null context.");
                return;
            }
            PrefUtil.setBooleanValue(context, PREF_KEY_ENABLE, true);
            PrefUtil.setBooleanValue(context, PREF_KEY_STARTING, false);
            if (!TextUtils.isEmpty(MessageHelper.getDeviceID(context)))
                PushSDKManager.setPushTag(context, MessageHelper.getDeviceID(context));
        }

        @Override
        public void onSubscribeTopic(boolean success) {

        }

        @Override
        public void onUnsubscribeTopic(boolean success) {

        }
    };

    public static void startPushSDKService(final Context context) {
        startPushSDKService(context, false);
    }

    synchronized public static void startPushSDKService(Context context, boolean force) {
        boolean enable = PrefUtil.getbooleanValue(context, PREF_KEY_ENABLE, false);
        if (!enable || force) {
            boolean starting = PrefUtil.getbooleanValue(context, PREF_KEY_STARTING, false);
            Log.d(TAG, "start context: " + context.hashCode() + " enable:" + enable + " force:" + force + " starting:" + starting);
            if (!starting) {
                PrefUtil.setBooleanValue(context, PREF_KEY_STARTING, true);
                CURRENT_CONTEXT = new WeakReference<>(context);
                Message msg = Message.obtain();
                msg.what = MSG_START_SDK;
                handler.sendMessage(msg);
            }
        } else {
            Log.d(TAG, "skip startPushSDKService");
        }
    }

    synchronized public static void stopPushSDKService(Context context) {
        boolean enable = PrefUtil.getbooleanValue(context, PREF_KEY_ENABLE, false);
        if (enable) {
            Log.d(TAG, "stop context: " + context.hashCode());
            boolean stopping = PrefUtil.getbooleanValue(context, PREF_KEY_STOPPING, false);
            if (!stopping) {
                PrefUtil.setBooleanValue(context, PREF_KEY_STOPPING, true);
                CURRENT_CONTEXT = new WeakReference<>(context);
                Message msg = Message.obtain();
                msg.what = MSG_STOP_SDK;
                handler.sendMessage(msg);
            }
        } else {
            Log.d(TAG, "skip stopPushSDKService");
        }
    }

    public static void setPushTag(Context context, String tagText) {
        MiPushClient.subscribe(context, tagText, "LEHome");
    }

    public static void delPushTag(Context context, String tagText) {
        MiPushClient.unsubscribe(context, tagText, "LEHome");
    }
}
