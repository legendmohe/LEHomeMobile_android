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

package my.home.lehome.helper;

import android.content.Context;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;

/**
 * Created by legendmohe on 15/12/20.
 */
public class NFCHelper {

    public static NdefMessage createMessage(Context context, String content) {
        if (content.length() == 0)
            return null;
        NdefRecord textRecord = NdefRecord.createMime("text/plain", content.getBytes());
        NdefRecord aar = NdefRecord.createApplicationRecord(context.getPackageName());
        return new NdefMessage(new NdefRecord[]{textRecord, aar});
    }
    
    public static boolean isNfcEnable(Context context) {
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(context);
        return adapter != null && adapter.isEnabled();
    }

    public static boolean isNfcSupported(Context context) {
        return NfcAdapter.getDefaultAdapter(context) != null;
    }
}
