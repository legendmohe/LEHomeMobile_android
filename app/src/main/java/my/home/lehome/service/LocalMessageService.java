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

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import my.home.lehome.service.aidl.LocalMessageServiceAidlInterface;

/**
 * Created by legendmohe on 15/3/9.
 */
public class LocalMessageService extends Service {
    private static final String TAG = "LocalMassageService";

    private String mServiceAddress = "";

    private LocalMessageServiceAidlInterface.Stub mBinder = new LocalMessageServiceAidlInterface.Stub() {
        @Override
        public boolean sendLocalMessage(String cmd) {
            if (LocalMessageService.this != null)
                return LocalMessageService.this.sendLocalMessage(cmd);
            return false;
        }

        @Override
        public void connectServer(String address) {
            if (LocalMessageService.this != null)
                LocalMessageService.this.connectServer(address);
        }

        @Override
        public void disconnectServer() {
            if (LocalMessageService.this != null)
                LocalMessageService.this.disconnectServer();
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "in onCreate");
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
        super.onDestroy();
        Log.d(TAG, "in onDestroy");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void initServer() {
        if (TextUtils.isEmpty(mServiceAddress))
            return;

        Log.d(TAG, "init server: " + mServiceAddress);
    }

    private void connectServer(String address) {
        mServiceAddress = address;
        Log.d(TAG, "connect server: " + address);

    }

    private void disconnectServer() {
        Log.d(TAG, "disconnect server: " + mServiceAddress);

    }

    private boolean sendLocalMessage(String cmd) {
        Log.d(TAG, "init server: " + mServiceAddress);
        return false;
    }
}
