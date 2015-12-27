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
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;

import my.home.lehome.R;
import my.home.lehome.helper.MessageHelper;

/**
 * Created by legendmohe on 15/12/20.
 */
public class NfcReadNdefAsyncTask extends AsyncTask<Tag, Void, ArrayList<String>> {

    private static final String TAG = "NfcReadNdefAsyncTask";

    private WeakReference<Context> mContext;

    public NfcReadNdefAsyncTask(Context context) {
        mContext = new WeakReference<Context>(context);
    }

    @Override
    protected ArrayList<String> doInBackground(Tag... params) {
        Tag tag = params[0];

        Ndef ndef = Ndef.get(tag);
        if (ndef == null) {
            // NDEF is not supported by this Tag.
            return null;
        }

        ArrayList<String> results = new ArrayList<>();
        NdefMessage ndefMessage = ndef.getCachedNdefMessage();
        NdefRecord[] records = ndefMessage.getRecords();
        for (NdefRecord ndefRecord : records) {
            if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                try {
                    results.add(readText(ndefRecord));
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, "Unsupported Encoding", e);
                }
            }
        }

        return results;
    }

    private String readText(NdefRecord record) throws UnsupportedEncodingException {
        /*
         * See NFC forum specification for "Text Record Type Definition" at 3.2.1
         *
         * http://www.nfc-forum.org/specs/
         *
         * bit_7 defines encoding
         * bit_6 reserved for future use, must be 0
         * bit_5..0 length of IANA language code
         */

        byte[] payload = record.getPayload();

        // Get the Text Encoding
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";

        // Get the Language Code
        int languageCodeLength = payload[0] & 0063;

        // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
        // e.g. "en"

        // Get the Text
        return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
    }

    @Override
    protected void onPostExecute(ArrayList<String> results) {
        if (results != null && results.size() != 0) {
            Log.d(TAG, "resolve text from tag:" + results);
            if (mContext.get() != null) {
                for (String cmd : results) {
                    Toast.makeText(mContext.get(),
                            mContext.get().getString(R.string.com_exec) + ":" + cmd,
                            Toast.LENGTH_SHORT).show();
                    MessageHelper.sendMsgToServer(mContext.get(), cmd);
                }
            }
        }
    }
}
