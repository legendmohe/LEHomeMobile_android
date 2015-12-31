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


import android.nfc.NdefMessage;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.AsyncTask;

import java.io.IOException;
import java.lang.ref.WeakReference;


/**
 * Created by legendmohe on 15/12/29.
 */
public class NfcWriteNdefAsyncTask extends AsyncTask<NdefMessage, Void, NfcWriteNdefAsyncTask.Result> {

    private static final String TAG = "NfcWriteNdefAsyncTask";

    private WeakReference<WriteNdefListener> mListener;
    private Tag mTag;

    public NfcWriteNdefAsyncTask(WriteNdefListener listener, Tag tag) {
        mListener = new WeakReference<>(listener);
        mTag = tag;
    }

    @Override
    protected Result doInBackground(NdefMessage... params) {
        NdefMessage message = params[0];
        int size = message.toByteArray().length;
        try {
            Ndef ndef = Ndef.get(mTag);
            if (ndef != null) {
                ndef.connect();
                if (!ndef.isWritable()) {
                    return Result.READONLY;
                }
                if (ndef.getMaxSize() < size) {
                    return Result.OVERSIZE;
                }
                ndef.writeNdefMessage(message);
                return Result.SUCCESS;
            } else {
                NdefFormatable format = NdefFormatable.get(mTag);
                if (format != null) {
                    try {
                        format.connect();
                        format.format(message);
                        return Result.SUCCESS;
                    } catch (IOException e) {
                        return Result.EXCEPTION;
                    }
                } else {
                    return Result.UNSUPPORTED;
                }
            }
        } catch (Exception e) {
            return Result.EXCEPTION;
        }
    }

    @Override
    protected void onPostExecute(Result result) {
        if (mListener.get() != null)
            mListener.get().onWriteFinished(result);
    }

    public enum Result {
        SUCCESS, UNWRITABLE, UNSUPPORTED, READONLY, OVERSIZE, EXCEPTION
    }

    public interface WriteNdefListener {
        void onWriteFinished(Result result);
    }
}
