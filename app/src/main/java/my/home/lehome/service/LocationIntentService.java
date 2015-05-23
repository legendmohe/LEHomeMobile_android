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
import android.text.TextUtils;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import my.home.lehome.R;
import my.home.lehome.helper.LocalMsgHelper;
import my.home.lehome.helper.MessageHelper;

/**
 * Created by legendmohe on 15/5/23.
 */
public class LocationIntentService extends IntentService {

    public final static String TAG = "LocationIntentService";

    private Object mSyncObject = new Object();

    private LocationClient mLocationClient = null;
    private BDLocation mCurLocation = null;
    private BDLocationListener mMyListener = new MyLocationListener();

    public LocationIntentService() {
        super("LocationIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initLocationClient();
    }

    private void initLocationClient() {
        Log.d(TAG, "initLocationClient");

        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);//设置定位模式
        option.setCoorType("bd09ll");//返回的定位结果是百度经纬度,默认值gcj02
        option.setIsNeedAddress(true);
        option.setOpenGps(true);
        option.setNeedDeviceDirect(false);//返回的定位结果包含手机机头的方向

        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.setLocOption(option);
        mLocationClient.registerLocationListener(mMyListener);
    }

    private void cleanup() {
        Log.d(TAG, "cleanup");
        mLocationClient.unRegisterLocationListener(mMyListener);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "handle new location request.");

        int seq = intent.getIntExtra("seq", -1);
        String type = intent.getStringExtra("type");
        String id = intent.getStringExtra("id");
        if (TextUtils.isEmpty(type) || TextUtils.isEmpty(id)) {
            Log.d(TAG, "LocationIntentService got invaild intent.");
            return;
        }

        synchronized (mSyncObject) {
            if (mLocationClient != null) {
                int retCode = mLocationClient.requestLocation();
                Log.d(TAG, "retCode:" + retCode);
                try {
                    mSyncObject.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        MessageHelper.sendServerMsgToList(seq, "client", getString(R.string.loc_sending_loc), getApplicationContext());
        sendResultToServer(formatResponse(id));
        Log.d(TAG, "LocationIntentService exit intent.");
    }

    private void sendResultToServer(String broadcast) {
        String message;
        String serverURL;
        Context context = getApplicationContext();
        boolean local = MessageHelper.isLocalMsgPrefEnable(context)
                && LocalMsgHelper.isLMSSID(context);
        if (local) {
            message = broadcast;
            serverURL = MessageHelper.getLocalServerURL(context);
        } else {
            message = "*" + broadcast;
            serverURL = MessageHelper.getServerURL(context, message);
        }
        Intent serviceIntent = new Intent(context, SendMsgIntentService.class);
        serviceIntent.putExtra("bg", true);
        serviceIntent.putExtra("local", local);
        serviceIntent.putExtra("cmdString", message);
        serviceIntent.putExtra("cmd", broadcast);
        serviceIntent.putExtra("serverUrl", serverURL);
        serviceIntent.putExtra("deviceID", MessageHelper.getDeviceID(context));
        context.startService(serviceIntent);
    }

    private String formatResponse(String id) {
        StringBuilder builder = new StringBuilder("@");  // broadcast indicator
        builder.append(id).append("|");
        if (TextUtils.isEmpty(mCurLocation.getAddrStr()) || mCurLocation.getAddrStr().equals("null")) {
            mCurLocation.setAddrStr(getString(R.string.loc_no_addr, id));
        }
        builder.append(mCurLocation.getAddrStr()).append("|");
        if (TextUtils.isEmpty(mCurLocation.getAddrStr())) {
            mCurLocation.setLatitude(-1.0);
        }
        builder.append(mCurLocation.getLatitude()).append("|");
        if (TextUtils.isEmpty(mCurLocation.getAddrStr())) {
            mCurLocation.setLongitude(-1.0);
        }
        builder.append(mCurLocation.getLongitude());
        return builder.toString();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        mLocationClient.start();
    }

    @Override
    public void onDestroy() {
        cleanup();
        mLocationClient.stop();
        super.onDestroy();
    }

    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null)
                return;
            StringBuffer sb = new StringBuffer(256);
            sb.append("time : ");
            sb.append(location.getTime());
            sb.append("\nerror code : ");
            sb.append(location.getLocType());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());
            sb.append("\nradius : ");
            sb.append(location.getRadius());
            sb.append("\ntype : ");
            sb.append(location.getLocType());
            if (location.getLocType() == BDLocation.TypeGpsLocation) {
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
            }

            Log.d(TAG, sb.toString());

            LocationIntentService.this.mCurLocation = location;
            synchronized (LocationIntentService.this.mSyncObject) {
                LocationIntentService.this.mSyncObject.notifyAll();
            }
        }
    }
}
