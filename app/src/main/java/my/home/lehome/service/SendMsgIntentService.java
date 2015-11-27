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

package my.home.lehome.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import my.home.lehome.R;
import my.home.lehome.service.net.CommandRequest;
import my.home.lehome.util.Constants;
import my.home.model.entities.ChatItem;
import my.home.model.entities.ChatItemConstants;
import my.home.model.manager.DBStaticManager;

/**
 * Created by legendmohe on 15/3/30.
 */
public class SendMsgIntentService extends IntentService {

    public static final int MSG_END_SENDING = 0;
    public static final int MSG_BEGIN_SENDING = 1;

    public final static String TAG = "SendMsgIntentService";

    private PowerManager.WakeLock mWakeLock;
    private RequestQueue mRequestQueue;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public SendMsgIntentService() {
        super("SendMsgIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (mWakeLock == null) {
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
            mWakeLock.acquire();
            Log.d(TAG, "acquire wakelock");
        }

        mRequestQueue = Volley.newRequestQueue(getApplicationContext());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
            Log.d(TAG, "release wakelock");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        preparePengindCommand(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        dispatchCommand(intent);
    }

    private void preparePengindCommand(Intent intent) {
        Messenger messenger;
        if (intent.hasExtra("messenger"))
            messenger = (Messenger) intent.getExtras().get("messenger");
        else
            messenger = null;
        Message repMsg = Message.obtain();
        repMsg.what = MSG_BEGIN_SENDING;

        boolean isBackgroundCmd = intent.getBooleanExtra("bg", false);
        if (isBackgroundCmd)
            return;

        ChatItem item = intent.getParcelableExtra("update");
        if (item == null) {
            item = new ChatItem();
            item.setContent(intent.getStringExtra("cmd"));
            item.setType(ChatItemConstants.TYPE_CLIENT);
            item.setState(Constants.CHATITEM_STATE_ERROR); // set ERROR
            item.setDate(new Date());
            DBStaticManager.addChatItem(getApplicationContext(), item);
        }
        item.setState(Constants.CHATITEM_STATE_PENDING);

        Log.d(TAG, "enqueue item: \n" + item);
        if (messenger != null) {
            Bundle bundle = new Bundle();
            bundle.putBoolean("update", intent.hasExtra("update"));
            bundle.putParcelable("item", item);
            repMsg.setData(bundle);
            try {
                messenger.send(repMsg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        intent.putExtra("pass_item", item);
    }

    private void dispatchCommand(final Intent intent) {
        String cmd = intent.getStringExtra("cmdString");
        String servelURL = intent.getStringExtra("serverUrl");
        String deviceID = intent.getStringExtra("deviceID");
        boolean local = intent.getBooleanExtra("local", false);

        final Context context = getApplicationContext();
        if (local) {
            if (TextUtils.isEmpty(servelURL)) {
                saveAndNotify(intent,
                        CommandRequest.getJsonStringResponse(
                                400,
                                context.getString(R.string.msg_local_saddress_not_set)
                        ));
            }
        } else {
            if (TextUtils.isEmpty(deviceID)) {
                saveAndNotify(intent,
                        CommandRequest.getJsonStringResponse(
                                400,
                                context.getString(R.string.msg_no_deviceid)
                        ));
            }
            if (TextUtils.isEmpty(servelURL)) {
                saveAndNotify(intent,
                        CommandRequest.getJsonStringResponse(
                                400,
                                context.getString(R.string.msg_saddress_not_set)
                        ));
            }
        }
        RequestFuture<String> future = RequestFuture.newFuture();
        CommandRequest request = new CommandRequest(
                local ? Request.Method.POST : Request.Method.GET, // diff
                servelURL,
                cmd,
                future,
                future
        );
        mRequestQueue.add(request);
        try {
            String response = future.get(request.getTimeoutMs() + 5000, TimeUnit.MILLISECONDS);
            Log.d(TAG, "get cmd response:" + response);
            saveAndNotify(intent,
                    CommandRequest.getJsonStringResponse(
                            200,
                            response
                    ));
        } catch (ExecutionException e) {
            Throwable error = e.getCause();
            Log.d(TAG, "get cmd error:" + error.toString());

            String errorString = context.getString(R.string.error_unknown);
            int errorCode = 400;
            if (error instanceof ServerError) {
                errorString = context.getString(R.string.chat_error_conn);
                errorCode = 400;
            } else if (error instanceof TimeoutError) {
                errorString = context.getString(R.string.chat_error_http_error);
                errorCode = 400;
            } else if (error instanceof ParseError) {
                errorString = context.getString(R.string.chat_error_http_error);
                errorCode = 400;
            } else if (error instanceof NoConnectionError) {
                errorString = context.getString(R.string.chat_error_no_connection_error);
                errorCode = 400;
            }
            saveAndNotify(intent,
                    CommandRequest.getJsonStringResponse(
                            errorCode,
                            errorString
                    ));
        } catch (TimeoutException e) {
            saveAndNotify(intent,
                    CommandRequest.getJsonStringResponse(
                            400,
                            context.getString(R.string.chat_error_http_error)
                    ));
        } catch (Exception e) {
            future.cancel(true);
            e.printStackTrace();
//            saveAndNotify(intent,
//                    CommandRequest.getJsonStringResponse(
//                            400,
//                            context.getString(R.string.error_internal)
//                    ));
        }
    }

    private void saveAndNotify(Intent intent, String result) {
        Context context = getApplicationContext();
        int rep_code = -1;
        String desc;
        try {
            JSONObject jsonObject = new JSONObject(result);
            rep_code = jsonObject.getInt("code");
            desc = jsonObject.getString("desc");
        } catch (JSONException e) {
            e.printStackTrace();
            desc = context.getString(R.string.chat_error_json);
        }
        
        Messenger messenger;
        if (intent.hasExtra("messenger"))
            messenger = (Messenger) intent.getExtras().get("messenger");
        else
            messenger = null;

        Message repMsg = Message.obtain();
        repMsg.what = MSG_END_SENDING;

        ChatItem item = intent.getParcelableExtra("pass_item");
        ChatItem newItem = null;
        if (item != null) {
            if (rep_code == 200) {
                item.setState(Constants.CHATITEM_STATE_SUCCESS);
                DBStaticManager.updateChatItem(context, item);
            } else {
                if (rep_code == 415) {
                    item.setState(Constants.CHATITEM_STATE_SUCCESS);
                } else {
                    item.setState(Constants.CHATITEM_STATE_ERROR);
                }
                DBStaticManager.updateChatItem(context, item);

                newItem = new ChatItem();
                newItem.setContent(desc);
                newItem.setType(ChatItemConstants.TYPE_SERVER);
                newItem.setState(Constants.CHATITEM_STATE_ERROR); // always set true
                newItem.setDate(new Date());
                DBStaticManager.addChatItem(context, newItem);
            }
        } else {
            //TODO bug! need refresh list but no messager to use
            if (rep_code != 200) {
                newItem = new ChatItem();
                newItem.setContent(getString(R.string.loc_send_error));
                newItem.setType(ChatItemConstants.TYPE_CLIENT);
                newItem.setState(Constants.CHATITEM_STATE_SUCCESS); // always set true
                newItem.setDate(new Date());
                DBStaticManager.addChatItem(context, newItem);
            }
        }

        Log.d(TAG, "dequeue item: " + item);
        if (messenger != null) {
            Bundle bundle = new Bundle();
            bundle.putParcelable("item", item);
            if (newItem != null)
                bundle.putParcelable("new_item", newItem);
            bundle.putInt("rep_code", rep_code);
            repMsg.setData(bundle);
            try {
                messenger.send(repMsg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
