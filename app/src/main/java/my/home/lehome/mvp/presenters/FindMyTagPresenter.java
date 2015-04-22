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

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.RemoteException;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.lang.ref.WeakReference;
import java.security.InvalidParameterException;
import java.util.Collection;

import my.home.lehome.mvp.views.FindMyTagDistanceView;

/**
 * Created by legendmohe on 15/4/21.
 */
public class FindMyTagPresenter extends MVPPresenter implements BeaconConsumer {
    protected static final String TAG = "FindMyTagPresenter";

    private static final String RANGE_UNI_ID = "FindMyTagPresenter_ID";
    // layout for iBeacon
    public static final String MY_BEACON_LAYOUT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25";

    private WeakReference<FindMyTagDistanceView> mFindMyTagView;
    private BluetoothAdapter mBluetoothAdapter;
    private BeaconManager mBeaconManager;

    private boolean mIsBtEnableAlready = false;
    private boolean mBinded = false;

    public FindMyTagPresenter(FindMyTagDistanceView view) {
        if (view == null)
            throw new InvalidParameterException("view cannot be null");

        mFindMyTagView = new WeakReference<FindMyTagDistanceView>(view);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public void start() {
        Log.d(TAG, "mBeaconManager start.");
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        } else {
            mIsBtEnableAlready = true;
            setupBeaconManager();
            mBinded = true;
        }
    }

    @Override
    public void stop() {
        Log.d(TAG, "mBeaconManager stop.");
        if (mBinded) {
            mBeaconManager.unbind(this);
            mBinded = true;
        }
        if (!mIsBtEnableAlready && mBluetoothAdapter != null) {
            mBluetoothAdapter.disable();
        }
    }

    @Override
    public void onBeaconServiceConnect() {
        try {
//            mBeaconManager.startMonitoringBeaconsInRegion(new Region(RANGE_UNI_ID, null, null, null));
            mBeaconManager.startRangingBeaconsInRegion(new Region(RANGE_UNI_ID, null, null, null));
        } catch (RemoteException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        mFindMyTagView.get().showBeaconsDialog();
    }

    private void setupBeaconManager() {
        mBeaconManager = BeaconManager.getInstanceForApplication(mFindMyTagView.get().getContext());
        mBeaconManager.getBeaconParsers().clear();
        mBeaconManager.getBeaconParsers().add(
                new BeaconParser().setBeaconLayout(MY_BEACON_LAYOUT)
        );
//        mBeaconManager.setMonitorNotifier(new MonitorNotifier() {
//            @Override
//            public void didEnterRegion(Region region) {
//                Log.d(TAG, "didEnterRegion: " + region.toString());
//                mFindMyTagView.get().onBeaconEnter(region.toString());
//            }
//
//            @Override
//            public void didExitRegion(Region region) {
//                Log.d(TAG, "didExitRegion: " + region.toString());
//                mFindMyTagView.get().onBeaconExit(region.toString());
//            }
//
//            @Override
//            public void didDetermineStateForRegion(int state, Region region) {
//                Log.d(TAG, "didDetermineStateForRegion: " + region.toString() + " state: " + state);
//                mFindMyTagView.get().onBeaconState(state, region.toString());
//            }
//        });

        mBeaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    Beacon beacon = beacons.iterator().next();
                    Log.i(TAG, "UUID: " + beacon.getBluetoothAddress() + " beacon: " + beacon.getBluetoothName() + " distance: " + beacon.getDistance() + " data: " + beacon.getDataFields());
                    mFindMyTagView.get().onBeaconDistance(beacon.getBluetoothAddress(), beacon.getBluetoothName(), beacon.getDistance(), beacon.getDataFields());
                }
            }
        });
        mBeaconManager.bind(this);
    }

    @Override
    public Context getApplicationContext() {
        return mFindMyTagView.get().getApplicationContext();
    }

    @Override
    public void unbindService(ServiceConnection serviceConnection) {
        mFindMyTagView.get().getContext().unbindService(serviceConnection);
    }

    @Override
    public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
        return mFindMyTagView.get().getContext().bindService(intent, serviceConnection, i);
    }
}
