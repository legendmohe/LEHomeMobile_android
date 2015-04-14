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

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import my.home.lehome.R;
import my.home.lehome.receiver.LocalMessageReceiver;
import my.home.lehome.receiver.ScreenStateReceiver;

/**
 * Created by legendmohe on 15/3/9.
 */
public class LocalMessageService extends Service {
    private static final String TAG = "LocalMessageService";

    public static final int MSG_SET_SUBSCRIBE_ADDRESS = 0;
    //    public static final int MSG_REGISTER_CLIENT = 1;
//    public static final int MSG_UNREGISTER_CLIENT = 2;
    public static final int MSG_STOP_SERVICE = 3;
//    public static final int MSG_SEND_CMD = 4;

    public static final int MSG_SERVER_RECEIVE_MSG = 0;

    private String mServiceAddress = "";
    private ScreenStateReceiver mScreenStateReceiver;
    NotificationManager mNM;
    //    ArrayList<Messenger> mClients = new ArrayList<Messenger>();
    private Thread mSubThread;
    private SubRunnable mSubRunnable;
    private PowerManager.WakeLock mWakeLock;

    /**
     * Handler of incoming messages from clients.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = null;
            Log.d(TAG, "handle msg: " + msg.what);
            switch (msg.what) {
//                case MSG_REGISTER_CLIENT:
//                    mClients.add(msg.replyTo);
//                    break;
//                case MSG_UNREGISTER_CLIENT:
//                    mClients.remove(msg.replyTo);
//                    break;
                case MSG_SET_SUBSCRIBE_ADDRESS:
                    bundle = msg.getData();
                    if (bundle != null) {
                        setServerAddress(bundle.getString("server_address"));
                    }
                    break;
                case MSG_STOP_SERVICE:
                    stopLocalMsgService();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    @Override
    public IBinder onBind(Intent intent) {
        Toast.makeText(getApplicationContext(), getString(R.string.msg_local_binding), Toast.LENGTH_SHORT).show();
        return mMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        Log.d(TAG, "in onCreate");

        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (mScreenStateReceiver == null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            mScreenStateReceiver = new ScreenStateReceiver();
            registerReceiver(mScreenStateReceiver, filter);
        }

        SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mServiceAddress = mySharedPreferences.getString("pref_local_msg_subscribe_address", null);
        initSubscriber(mServiceAddress);

        if (mWakeLock == null) {
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
            mWakeLock.acquire();
        }
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(TAG, "in onRebind");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "in onUnbind");
        return true;
    }

    @Override
    public void onDestroy() {
        if (mSubThread != null) {
            mSubRunnable.setGoingStop(true);
            if (!mSubThread.isInterrupted()) {
                mSubThread.interrupt();
                try {
                    mSubThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if (mScreenStateReceiver != null) {
            unregisterReceiver(mScreenStateReceiver);
            mScreenStateReceiver = null;
        }
        super.onDestroy();
        if (mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }
        Log.d(TAG, "onDestroy");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void setServerAddress(String address) {
        Log.d(TAG, "connect server: " + address);
        if (mServiceAddress != null && !mServiceAddress.equals(address)) {
            mServiceAddress = address;
            initSubscriber(mServiceAddress);
        }
    }

    private void stopLocalMsgService() {
        Log.d(TAG, "disconnect server: " + mServiceAddress);
        mSubRunnable.setGoingStop(true);
        stopSelf();
    }

    private void initSubscriber(String serverAddress) {
        Log.d(TAG, "init server: " + serverAddress);

        if (TextUtils.isEmpty(serverAddress) && !serverAddress.startsWith("tcp://")) {
            return;
        }
        if (mSubThread != null) {
            mSubRunnable.setGoingStop(true);
            if (!mSubThread.isInterrupted()) {
                mSubThread.interrupt();
                try {
                    mSubThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        mSubRunnable = new SubRunnable(serverAddress);
        mSubThread = new Thread(mSubRunnable);
        mSubThread.start();
    }

    private void sendSubResponse(String repString) {
//        for (int i = mClients.size() - 1; i >= 0; i--) {
//            try {
//                Message msg = Message.obtain();
//                msg.what = MSG_SERVER_RECEIVE_MSG;
//                msg.obj = repString;
//                mClients.get(i).send(msg);
//            } catch (RemoteException e) {
//                mClients.remove(i);
//            }
//        }
        if (TextUtils.isEmpty(repString))
            return;
        Intent repIntent = new Intent();
        repIntent.setAction(LocalMessageReceiver.LOCAL_MSG_RECEIVER_ACTION);
        repIntent.putExtra(LocalMessageReceiver.LOCAL_MSG_REP_KEY, repString);
        sendBroadcast(repIntent);
    }

    private boolean shouldSendReponse(String repString) {
        return !repString.contains("\"heartbeat\"");
//        JSONTokener jsonParser = new JSONTokener(repString);
//        try {
//            JSONObject repJO = (JSONObject) jsonParser.nextValue();
//            String type = repJO.getString("type");
//            if (!type.equals("heartbeat"))
//                return true;
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return false;
    }

    private class SubRunnable implements Runnable {

        private ZMQ.Socket subscriber;
        private ZMQ.Context zmqContext;
        private boolean isGoingStop = false;
        private String serverAddress = null;

        public SubRunnable(String serverAddress) {
            this.serverAddress = serverAddress;
        }

        public void setGoingStop(boolean goingStop) {
            this.isGoingStop = goingStop;
        }

        @Override
        public void run() {
            zmqContext = ZMQ.context(1);
            subscriber = zmqContext.socket(ZMQ.SUB);
            ZMQ.Poller poller = new ZMQ.Poller(1);
            poller.register(subscriber, ZMQ.Poller.POLLIN);

            try {
                subscriber.connect(serverAddress);
                subscriber.subscribe("".getBytes());
                while (!Thread.currentThread().isInterrupted() && !isGoingStop) {
                    poller.poll(1000 * 5);
                    if (poller.pollin(0) && !isGoingStop) {
                        String repString = subscriber.recvStr(ZMQ.DONTWAIT);
                        Log.d(TAG, "received response: " + repString);
                        if (shouldSendReponse(repString))
                            sendSubResponse(repString);
                    }
                }
            } catch (ZMQException e) {
                Log.e(TAG, Log.getStackTraceString(e));
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),
                        getString(R.string.error_connect_local_server),
                        Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            } finally {
                if (!Thread.currentThread().isInterrupted()) {
                    subscriber.close();
                    zmqContext.term();
                }
            }
            Log.d(TAG, "SubRunnable exit. " + isGoingStop + " thread: " + Thread.currentThread().isInterrupted());
        }
    }

    ;
}
