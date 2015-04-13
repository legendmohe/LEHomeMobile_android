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

package my.home.lehome.mvp.presenters;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;

import java.lang.ref.WeakReference;

import my.home.common.BusProvider;
import my.home.lehome.R;
import my.home.lehome.helper.LocalMsgHelper;
import my.home.lehome.helper.MessageHelper;
import my.home.lehome.mvp.views.MainActivityView;
import my.home.lehome.receiver.NetworkStateReceiver;
import my.home.lehome.service.LocalMessageService;
import my.home.lehome.util.PushUtils;

/**
 * Created by legendmohe on 15/3/15.
 */
public class MainActivityPresenter extends MVPActivityPresenter {

    public static final String TAG = "MainActivityPresenter";

    private WeakReference<MainActivityView> mMainActivityView;
    private Messenger mLocalMsgService = null;
    private boolean mBinded = false;

    public MainActivityPresenter(MainActivityView view) {
        this.mMainActivityView = new WeakReference<MainActivityView>(view);
    }

    @Override
    public void start() {
        BusProvider.getRestBusInstance().register(this);
        setupService();
    }

    @Override
    public void stop() {
        BusProvider.getRestBusInstance().unregister(this);
    }

    @Override
    public void onActivityResume() {
        super.onActivityResume();
        MessageHelper.removeNotification(mMainActivityView.get().getContext());
    }

    private void setupService() {
        if (mMainActivityView.get() == null) {
            return;
        }
        Context context = mMainActivityView.get().getContext();
        MessageHelper.loadPref(context);
        if (!initLocalMessageService()) {
            PushManager.startWork(context,
                    PushConstants.LOGIN_TYPE_API_KEY,
                    PushUtils.getMetaValue(context, "api_key"));
        } else {
            PushManager.stopWork(context);
            LocalMsgHelper.stopLocalMsgService(context);
        }
    }

    private boolean initLocalMessageService() {
        if (mMainActivityView.get() == null) {
            return false;
        }
        Context context = mMainActivityView.get().getContext();
        boolean ret = LocalMsgHelper.canUseLocalMessageService(context);
        if (ret) {
            return LocalMsgHelper.startLocalMsgService(context);
        }
        return ret;
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(TAG, className + " binded.");

            mLocalMsgService = new Messenger(service);
            mBinded = true;
            if (mMainActivityView.get() == null) {
                return;
            }
            Context context = mMainActivityView.get().getContext();
            mMainActivityView.get().setActionBarTitle(
                    context.getString(R.string.app_name)
                            + "("
                            + context.getString(R.string.title_local_msg_mode)
                            + ")"
            );
            PushManager.stopWork(context);
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.d(TAG, className + " unbinded.");
            onServiceUnbind();
        }
    };

    private void onServiceUnbind() {
        mLocalMsgService = null;
        mBinded = false;

        Context context = mMainActivityView.get().getContext();
        mMainActivityView.get().setActionBarTitle(
                context.getString(R.string.app_name)
        );

        PushManager.startWork(context,
                PushConstants.LOGIN_TYPE_API_KEY,
                PushUtils.getMetaValue(context, "api_key"));
    }

    public boolean isLocalMessageServiceBinded() {
        return mBinded;
    }

    public boolean onAppExit() {
        if (mBinded) {
            Message msg = Message.obtain();
            msg.what = LocalMessageService.MSG_STOP_SERVICE;
            try {
                mLocalMsgService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public boolean onSettingsActivityResult(int resultCode, Intent data) {
        Context context = mMainActivityView.get().getContext();
        Bundle extras = data.getExtras();
        String old_device_id = extras.getString("old_device_id");
        boolean old_local_msg_state = extras.getBoolean("old_local_msg_state");
        String old_subscribe_address = extras.getString("old_subscribe_address");

        MessageHelper.loadPref(context);
        if (!old_device_id.equals(MessageHelper.getDeviceID(context))) {
            MessageHelper.delPushTag(context, old_device_id);
            MessageHelper.setPushTag(context, MessageHelper.getDeviceID(context));
        }
        if (mBinded && MessageHelper.isLocalMsgPrefEnable(context)) {
            Message msg = Message.obtain(null, LocalMessageService.MSG_SET_SUBSCRIBE_ADDRESS);
            Bundle bundle = new Bundle();
            bundle.putString("server_address", MessageHelper.getLocalServerSubscribeURL(context));
            msg.setData(bundle);
//            msg.obj = MessageHelper.getLocalServerSubscribeURL();
            try {
                mLocalMsgService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
                return false;
            }
        }

        if (MessageHelper.isLocalMsgPrefEnable(context)) {
            if (!old_local_msg_state
                    || !old_subscribe_address.equals(MessageHelper.getLocalServerSubscribeURL(context))) {
                Intent i = new Intent("my.home.lehome.service.LocalMessageService");
                context.bindService(i, mConnection, Context.BIND_AUTO_CREATE);
            }
        } else {
            if (old_local_msg_state) {
                context.unbindService(mConnection);
                LocalMsgHelper.stopLocalMsgService(context);
                onServiceUnbind();
            }
        }
        return true;
    }

    @Override
    public void onActivityCreate() {
        Context context = mMainActivityView.get().getContext();
        // bind service if needed.
        if (LocalMsgHelper.canUseLocalMessageService(context) && MessageHelper.isLocalMsgPrefEnable(context)) {
            Intent i = new Intent("my.home.lehome.service.LocalMessageService");
            context.bindService(i, mConnection, context.BIND_AUTO_CREATE);
        }
        IntentFilter stateIntentFilter = new IntentFilter();
        stateIntentFilter.addAction(NetworkStateReceiver.VALUE_INTENT_START_LOCAL_SERVER);
        stateIntentFilter.addAction(NetworkStateReceiver.VALUE_INTENT_STOP_LOCAL_SERVER);
        context.registerReceiver(mLocalMsgStateReceiver, stateIntentFilter);
    }

    @Override
    public void onActivityDestory() {
        Context context = mMainActivityView.get().getContext();
        // Unbind from the service
        if (mBinded) {
            context.unbindService(mConnection);
            onServiceUnbind();
        }

        context.unregisterReceiver(mLocalMsgStateReceiver);
    }

    private final BroadcastReceiver mLocalMsgStateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == NetworkStateReceiver.VALUE_INTENT_STOP_LOCAL_SERVER) {
                if (mBinded) {
                    context.unbindService(mConnection);
                    onServiceUnbind();
                }
            } else if (intent.getAction() == NetworkStateReceiver.VALUE_INTENT_START_LOCAL_SERVER) {
                if (!mBinded) {
                    Intent i = new Intent("my.home.lehome.service.LocalMessageService");
                    context.bindService(i, mConnection, context.BIND_AUTO_CREATE);
                }
            }
        }
    };

    public boolean shouldUseLocalMsg() {
        return MessageHelper.isLocalMsgPrefEnable(mMainActivityView.get().getContext()) && isLocalMessageServiceBinded();
    }
}
