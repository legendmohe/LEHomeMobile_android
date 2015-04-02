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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by legendmohe on 15/3/29.
 */
public class PrefKeyValueStorgeImpl extends CacheKeyValueStorageImpl {

    private final SharedPreferences.Editor mEditor;
    private final SharedPreferences mPref;

    public PrefKeyValueStorgeImpl(Context context) {
        super();
        mPref = PreferenceManager.getDefaultSharedPreferences(context);
        mEditor = mPref.edit();
    }

    @Override
    public boolean storageHasKey(String key) {
        if (mPref.getString(key, null) == null)
            return false;
        return true;
    }

    @Override
    public void storagePutString(String key, String value) {
        mEditor.putString(key, value);
    }

    @Override
    public String storageGetString(String key) {
        return mPref.getString(key, null);
    }
    
    @Override
    public void storageRemoveString(String key) {
    	mEditor.remove(key);
    }

    @Override
    public void sync() {
        mEditor.apply();
    }
}
