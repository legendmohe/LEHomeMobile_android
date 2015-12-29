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

package my.home.domain.usecase;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;

import my.home.common.BusProvider;
import my.home.common.State;
import my.home.common.StateMachine;
import my.home.common.util.FileUtil;
import my.home.domain.events.DSendingMsgEvent;
import my.home.domain.util.DomainUtil;

/**
 * Created by legendmohe on 15/12/5.
 */
public class SendMsgUsecaseImpl implements SendMsgUsecase {

    private String mHomeId;
    private WeakReference<Context> mContext;
    private RequestQueue mRequestQueue;
    private StateMachine mStateMachine;
    private Event mEvent;
    private File mTargetFile;

    public SendMsgUsecaseImpl(Context context, String homeId) {
        mContext = new WeakReference<>(context);
        mHomeId = homeId;
        init(context);
    }

    private void init(Context context) {
        mRequestQueue = Volley.newRequestQueue(context);

        mStateMachine = new StateMachine();
        IdleState idleState = new IdleState();
        SendingState sendingState = new SendingState();
        mStateMachine.addState(idleState);
        mStateMachine.addState(sendingState);

        idleState.linkTo(sendingState, Event.START);
        sendingState.linkTo(idleState, Event.FINISH);
        sendingState.linkTo(idleState, Event.CANCEL);
        sendingState.linkTo(idleState, Event.ERROR);

        mStateMachine.setInitState(idleState);
        mStateMachine.start();
    }

    public SendMsgUsecase setTargetFile(File targetFile) {
        this.mTargetFile = targetFile;
        return this;
    }

    @Override
    public SendMsgUsecase setEvent(Event event) {
        this.mEvent = event;
        return this;
    }

    public void cleanup() {
        mStateMachine.stop(0);
    }

    @Override
    public void cancel(File file) {
        mStateMachine.postEvent(Event.CANCEL);
    }

    @Override
    public void execute() {
        Log.d(TAG, "execute " + mEvent + " in state " + mStateMachine.getCurrentState());
        if (mEvent != null)
            mStateMachine.postEvent(mEvent);
    }

    private class IdleState extends State {

        public IdleState() {
            super(IdleState.class.getSimpleName());
        }

        @Override
        public void onEnter(State fromState, Enum<?> event, Object data) {
            if (fromState.getClass() == SendingState.class) {
                if (event == Event.FINISH) {
                    DomainUtil.runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            BusProvider.getUIBusInstance().post(new DSendingMsgEvent(
                                    DSendingMsgEvent.TYPE.SUCCESS,
                                    mTargetFile.getAbsolutePath()
                            ));
                        }
                    });
                } else if (event == Event.CANCEL
                        || event == Event.ERROR) {
                    mRequestQueue.cancelAll(mTargetFile.getAbsolutePath());
                    DomainUtil.runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            BusProvider.getUIBusInstance().post(new DSendingMsgEvent(
                                    DSendingMsgEvent.TYPE.FAIL,
                                    mTargetFile.getAbsolutePath()
                            ));
                        }
                    });
                }
            }
        }
    }

    private class SendingState extends State {

        private static final String SERVER_URL = "http://119.29.102.249:8888/mqtt_base64?";

        public SendingState() {
            super(SendingState.class.getSimpleName());
        }

        @Override
        public void onEnter(State fromState, Enum<?> event, Object data) {
            if (fromState.getClass() == IdleState.class) {
                if (event == Event.START) {
                    StringRequest stringRequest = getSendingRequest(mHomeId, mTargetFile);
                    mRequestQueue.add(stringRequest);

                    DomainUtil.runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            BusProvider.getUIBusInstance().post(new DSendingMsgEvent(
                                    DSendingMsgEvent.TYPE.BEGIN,
                                    mTargetFile.getAbsolutePath()
                            ));
                        }
                    });

//                    try {
//                        Thread.sleep(10000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                    mStateMachine.postEvent(Event.FINISH);
                }
            }
        }

        private StringRequest getSendingRequest(String serverId, File file) {
            String url = SERVER_URL + "t=" + serverId + "&type=message";
            final String payload = FileUtil.FileToBase64(file);
            final String fileName = file.getName();

            Log.d(TAG, "sending message: " + url);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d(TAG, "onResponse: " + response);
                            if (response.equals("ok")) {
                                mStateMachine.postEvent(Event.FINISH);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG, "onErrorResponse: " + error);
                    mStateMachine.postEvent(Event.ERROR);
                }
            }
            ) {
//                @Override
//                protected Map<String, String> getParams() throws AuthFailureError {
//                    Map<String, String> params = new HashMap<>();
//                    params.put("filename", fileName);
//                    params.put("payload", payload);
//                    return params;
//                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    JSONObject body = new JSONObject();
                    try {
                        body.put("filename", fileName);
                        body.put("payload", payload);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        return body.toString().getBytes("UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            };
            stringRequest.setTag(mTargetFile.getAbsolutePath());
            return stringRequest;
        }

        @Override
        public void onStop(int cause) {
            if (cause == 0 && mTargetFile != null) {
                mRequestQueue.cancelAll(mTargetFile.getAbsolutePath());
            }
        }
    }
}
