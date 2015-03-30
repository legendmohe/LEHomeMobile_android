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
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import my.home.lehome.R;
import my.home.lehome.helper.DBHelper;
import my.home.lehome.util.Constants;
import my.home.model.entities.ChatItem;

/**
 * Created by legendmohe on 15/3/30.
 */
public class SendMsgIntentService extends IntentService {

    public static final int MSG_END_SENDING = 0;
    public static final int MSG_BEGIN_SENDING = 1;

    public final static String TAG = "SendMsgIntentService";
    public final static String SEND_MSG_INTENT_SERVICE_ACTION = "my.home.lehome.receiver.SendMsgServiceReceiver";

    private static final Gson gson = new Gson();

    private boolean mLocalMsg = false;
    private ChatItem mCurrentItem;
    private String mCmdString;
    private String mServerURL;
    private String mDeviceID;
    private Messenger mCurMessager;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public SendMsgIntentService() {
        super("SendMsgIntentService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        BeforeSending(intent);
        String resultString = dispatchSendingTask(intent);
        AfterSending(intent, resultString);
    }

    private void BeforeSending(Intent intent) {
        if (intent.hasExtra("messenger"))
            mCurMessager = (Messenger) intent.getExtras().get("messenger");
        else
            mCurMessager = null;
        Message repMsg = Message.obtain();
        repMsg.what = MSG_BEGIN_SENDING;

        boolean useLocal = intent.getBooleanExtra("local", false);
        ChatItem updateItem = intent.getParcelableExtra("update");
        mCmdString = intent.getStringExtra("cmdString");
        mServerURL = intent.getStringExtra("serverUrl");
        mDeviceID = intent.getStringExtra("deviceID");

        if (updateItem != null) {
            mCurrentItem = updateItem;
        } else {
            mCurrentItem = new ChatItem();
            mCurrentItem.setContent(mCmdString);
            mCurrentItem.setIsMe(true);
            mCurrentItem.setState(Constants.CHATITEM_STATE_ERROR); // set ERROR
            mCurrentItem.setDate(new Date());
            DBHelper.addChatItem(getApplicationContext(), mCurrentItem);
        }
        mCurrentItem.setState(Constants.CHATITEM_STATE_PENDING); // set PENDING temporary

        mLocalMsg = useLocal;
        if (mCurMessager != null) {
            Bundle bundle = new Bundle();
            bundle.putBoolean("update", intent.hasExtra("update"));
            bundle.putString("item", gson.toJson(mCurrentItem));
            bundle.putLong("update_id", mCurrentItem.getId());
            bundle.putInt("update_state", mCurrentItem.getState());
            repMsg.setData(bundle);
            try {
                mCurMessager.send(repMsg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private String dispatchSendingTask(Intent intent) {
        Log.d(TAG, "sending: " + mCurrentItem.getContent() + " use local: " + mLocalMsg);
        if (mLocalMsg) {
            if (TextUtils.isEmpty(mServerURL))
                return getErrorJsonString(
                        400,
                        getApplicationContext().getResources().getString(R.string.msg_local_saddress_not_set)
                );
            return sendToLocalServer(mServerURL, mCmdString);
        } else {
            if (TextUtils.isEmpty(mDeviceID)) {
                return getErrorJsonString(
                        400,
                        getApplicationContext().getResources().getString(R.string.msg_no_deviceid)
                );
            }
            if (TextUtils.isEmpty(mServerURL))
                return getErrorJsonString(
                        400,
                        getApplicationContext().getResources().getString(R.string.msg_saddress_not_set)
                );
            return sendToServer(mServerURL);
        }
    }

    private String sendToServer(String cmdURL) {
        Context context = getApplicationContext();

        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response;
        String responseString = null;
        JSONObject repObject = new JSONObject();
        try {
            response = httpclient.execute(new HttpGet(cmdURL));
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                responseString = out.toString();
            } else {
                //Closes the connection.
                response.getEntity().getContent().close();
                try {
                    repObject.put("code", 400);
                    repObject.put("desc", context.getString(R.string.chat_error_conn));
                } catch (JSONException je) {
                }
                responseString = repObject.toString();
            }
        } catch (ClientProtocolException e) {
            try {
                repObject.put("code", 400);
                repObject.put("desc", context.getString(R.string.chat_error_protocol_error));
            } catch (JSONException je) {
            }
            responseString = repObject.toString();
        } catch (IOException e) {
            try {
                repObject.put("code", 400);
                repObject.put("desc", context.getString(R.string.chat_error_http_error));
            } catch (JSONException je) {
            }
            responseString = repObject.toString();
        }
        return responseString;
    }

    private String sendToLocalServer(String serverAddress, String cmd) {
        Context context = getApplicationContext();

        HttpParams httpParameters = new BasicHttpParams();
        int timeoutConnection = 3000;
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
        int timeoutSocket = 5000;
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

        HttpClient httpclient = new DefaultHttpClient(httpParameters);
        HttpResponse response;
        String responseString = null;
        JSONObject repObject = new JSONObject();
        try {
            List<NameValuePair> pairList = new ArrayList<>();
            pairList.add(new BasicNameValuePair("cmd", cmd));
            HttpPost httpPost = new HttpPost(serverAddress);
            httpPost.setEntity(new UrlEncodedFormEntity(pairList, "utf-8"));

            response = httpclient.execute(httpPost);
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                try {
                    repObject.put("code", 200);
                    repObject.put("desc", out.toString());
                } catch (JSONException je) {
                }
                responseString = repObject.toString();
            } else {
                //Closes the connection.
                response.getEntity().getContent().close();
                try {
                    repObject.put("code", 400);
                    repObject.put("desc", context.getString(R.string.chat_error_conn));
                } catch (JSONException je) {
                }
                responseString = repObject.toString();
            }
        } catch (ClientProtocolException e) {
            try {
                repObject.put("code", 400);
                repObject.put("desc", context.getString(R.string.chat_error_protocol_error));
            } catch (JSONException je) {
            }
            responseString = repObject.toString();
        } catch (IOException e) {
            try {
                repObject.put("code", 400);
                repObject.put("desc", context.getString(R.string.chat_error_http_error));
            } catch (JSONException je) {
            }
            responseString = repObject.toString();
        }
        return responseString;
    }

    private void AfterSending(Intent intent, String result) {
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

        Log.d(TAG, "send cmd finish: " + rep_code + " " + desc);
        if (mCurMessager != null) {
            mCurMessager = (Messenger) intent.getExtras().get("messenger");
        }
        Message repMsg = Message.obtain();
        repMsg.what = MSG_END_SENDING;
        long update_id = -1;
        int update_state = -1;

        if (rep_code == 200) {
            mCurrentItem.setState(Constants.CHATITEM_STATE_SUCCESS);
            update_id = mCurrentItem.getId();
            update_state = mCurrentItem.getState();
            DBHelper.updateChatItem(context, mCurrentItem);
        } else {
            if (rep_code == 415) {
                mCurrentItem.setState(Constants.CHATITEM_STATE_SUCCESS);
            } else {
                mCurrentItem.setState(Constants.CHATITEM_STATE_ERROR);
            }
            update_id = mCurrentItem.getId();
            update_state = mCurrentItem.getState();
            DBHelper.updateChatItem(context, mCurrentItem);

            ChatItem newItem = new ChatItem();
            newItem.setContent(desc);
            newItem.setIsMe(false);
            newItem.setState(Constants.CHATITEM_STATE_ERROR); // always set true
            newItem.setDate(new Date());
            DBHelper.addChatItem(context, newItem);
            mCurrentItem = newItem;
        }

        if (mCurMessager != null) {
            Bundle bundle = new Bundle();
            bundle.putString("item", gson.toJson(mCurrentItem));
            bundle.putInt("rep_code", rep_code);
            bundle.putLong("update_id", update_id);
            bundle.putInt("update_state", update_state);
            repMsg.setData(bundle);
            try {
                mCurMessager.send(repMsg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        mCurMessager = null;
    }

    private String getErrorJsonString(int code, String error) {
        return "{\"code\":" + code + ",\"desc\":\"" + error + "\"}";
    }
}
