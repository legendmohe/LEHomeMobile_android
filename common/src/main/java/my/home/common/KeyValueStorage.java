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

import android.text.TextUtils;

import com.google.gson.Gson;

/**
 * Created by legendmohe on 15/3/29.
 */
public class KeyValueStorage {

    public static interface IKeyStringStorge {
        public boolean hasKey(String key);

        public void putString(String key, String value);

        public String getString(String key);

        public void sync();
    }

    private static KeyValueStorage mInstance;
    private static Object mLock = new Object();
    private IKeyStringStorge mStorageImpl;

    private KeyValueStorage() {
    }

    public static KeyValueStorage getInstance() {
        if (mInstance == null) {
            synchronized (mLock) {
                if (mInstance == null) {
                    mInstance = new KeyValueStorage();
                }
            }
        }
        return mInstance;
    }

    public void setStorgeImpl(IKeyStringStorge impl) {
        mStorageImpl = impl;
    }

    public IKeyStringStorge getStorageImpl() {
        return mStorageImpl;
    }

    public boolean hasKey(String key) {
        if (mStorageImpl != null) {
            return mStorageImpl.hasKey(key);
        }
        return false;
    }

    public void putObject(String key, Object obj) {
        String objJson = new Gson().toJson(obj);
        putString(key, objJson);
    }

    public Object getObject(String key, Class type) {
        String objJson = getString(key);
        if (TextUtils.isEmpty(objJson)) {
            return null;
        }
        Object result = new Gson().fromJson(objJson, type);
        return result;
    }

    public void putString(String key, String value) {
        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
            return;
        }
        if (mStorageImpl != null) {
            mStorageImpl.putString(key, value);
        }
    }

    public String getString(String key) {
        if (mStorageImpl != null) {
            return mStorageImpl.getString(key);
        }
        return null;
    }

    public void sync() {
        if (mStorageImpl != null) {
            mStorageImpl.sync();
        }
    }
}
