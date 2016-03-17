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

package my.home.common;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by legendmohe on 15/3/31.
 */
public abstract class CacheKeyValueStorageImpl implements KeyValueStorage.IKeyStringStorge {
    public static final String TAG = "CacheKeyValueStorage";

    private final int MSG_STORAGE_WHAT = 0;

    private final Runnable mSyncRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (mSyncLock) {
                Log.d(TAG, "sync cache: " + mSyncCache.size());
                for (String keyString : mSyncCache.keySet()) {
                    storagePutString(keyString, mSyncCache.get(keyString));
                }
                storageSync();
                mSyncCache.clear();
            }
        }
    };

    final ConcurrentHashMap<String, String> mCache = new ConcurrentHashMap<>();
    final ConcurrentHashMap<String, String> mSyncCache = new ConcurrentHashMap<>();
    private final Object mSyncLock = new Object();
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            new Thread(mSyncRunnable).start();
        }
    };

    public CacheKeyValueStorageImpl() {
    }

    @Override
    public synchronized boolean hasKey(String key) {
        if (mCache.containsKey(key))
            return true;
        return storageHasKey(key);
    }

    @Override
    public void putString(String key, String value) {
        mCache.put(key, value);

        Message msg = Message.obtain();
        msg.what = MSG_STORAGE_WHAT;

        synchronized (mSyncLock) {
            mSyncCache.put(key, value);
            mHandler.removeMessages(MSG_STORAGE_WHAT);
            mHandler.sendMessageDelayed(msg, 300);
        }
    }

    @Override
    public synchronized String getString(String key) {
        if (mCache.containsKey(key)) {
            return mCache.get(key);
        }
        String value = storageGetString(key);
        if (value != null)
            mCache.put(key, value);
        return value;
    }

    @Override
    public synchronized void removeString(String key) {
        mCache.remove(key);
        storageRemoveString(key);
    }

    @Override
    public void sync() {
        storageSync();
    }

    abstract boolean storageHasKey(String key);

    abstract void storagePutString(String key, String value);

    abstract String storageGetString(String key);

    abstract void storageRemoveString(String key);

    abstract void storageSync();
}

