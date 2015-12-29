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

package my.home.lehome.asynctask;


import android.content.Context;
import android.nfc.Tag;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

import my.home.common.StateMachine;

/**
 * Created by legendmohe on 15/12/29.
 */
public class NfcWriteNdefAsyncTask extends AsyncTask<String, Void, NfcWriteNdefAsyncTask.Result> {

    private static final String TAG = "NfcWriteNdefAsyncTask";

    private WeakReference<Context> mContext;
    private StateMachine mStateMachine;
    private Tag mTag;

    public NfcWriteNdefAsyncTask(Context context, StateMachine statemachine, Tag tag) {
        mContext = new WeakReference<>(context);
        mStateMachine = statemachine;
        mTag = tag;
    }

    @Override
    protected Result doInBackground(String... params) {
        return null;
    }

    @Override
    protected void onPostExecute(Result result) {
        
    }

    public enum Result {
        CANCELED, SUCCESS, UNWRITABLE, UNSUPPORTED, READONLY, OVERSIZE, EXCEPTION
    }
}
