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
import android.util.Log;

import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushManager;

import java.lang.ref.WeakReference;

import my.home.common.PrefUtil;

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

    private static int MAX_RETRY_TIME = 10;
    private static int START_RETRY_TIME = 0;
    private static int STOP_RETRY_TIME = 0;

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
                XGPushManager.registerPush(context, new XGIOperateCallback() {
                    @Override
                    public void onSuccess(Object o, int i) {
                        Log.d(TAG, "start sdk succeed." + context.hashCode());
                        PrefUtil.setBooleanValue(context, PREF_KEY_ENABLE, true);
                        PrefUtil.setBooleanValue(context, PREF_KEY_STARTING, false);
                    }

                    @Override
                    public void onFail(Object o, int i, String s) {
                        Log.d(TAG, "start sdk faild.");
                        START_RETRY_TIME++;
                        if (START_RETRY_TIME <= MAX_RETRY_TIME) {
                            Message msg = Message.obtain();
                            msg.what = MSG_START_SDK;
                            handler.sendMessageDelayed(msg, 1000);
                        } else {
                            PrefUtil.setBooleanValue(context, PREF_KEY_STARTING, false);
                        }
                    }
                });
            } else if (msg.what == PushSDKManager.MSG_STOP_SDK) {
                Log.d(TAG, "try stop sdk.");
                XGPushManager.unregisterPush(context, new XGIOperateCallback() {
                    @Override
                    public void onSuccess(Object o, int i) {
                        Log.d(TAG, "stop sdk succeed:" + context.hashCode());
                        PrefUtil.setBooleanValue(context, PREF_KEY_ENABLE, false);
                        PrefUtil.setBooleanValue(context, PREF_KEY_STOPPING, false);
                    }

                    @Override
                    public void onFail(Object o, int i, String s) {
                        Log.d(TAG, "stop sdk faild.");
                        STOP_RETRY_TIME++;
                        if (STOP_RETRY_TIME <= MAX_RETRY_TIME) {
                            Message msg = Message.obtain();
                            msg.what = MSG_STOP_SDK;
                            handler.sendMessageDelayed(msg, 1000);
                        } else {
                            PrefUtil.setBooleanValue(context, PREF_KEY_STOPPING, false);
                        }
                    }
                });

                // don't stop sdk
//                PrefUtil.setBooleanValue(context, "PushSDKManager.enable", false);
            }
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
        XGPushManager.setTag(context, tagText);
    }

    public static void delPushTag(Context context, String tagText) {
        XGPushManager.deleteTag(context, tagText);
    }

}
