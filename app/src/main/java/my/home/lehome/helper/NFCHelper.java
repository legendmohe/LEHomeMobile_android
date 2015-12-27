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

import java.nio.charset.Charset;
import java.util.Locale;

/**
 * Created by legendmohe on 15/12/20.
 */
public class NFCHelper {

    public static NdefMessage createNdefTextAppMessage(Context context, String content) {
        if (content.length() == 0)
            return null;
        NdefRecord textRecord = NFCHelper.createTextRecord(content, Locale.getDefault(), true);
        NdefRecord aar = NdefRecord.createApplicationRecord(context.getPackageName());
        return new NdefMessage(new NdefRecord[]{aar, textRecord});
    }
    
    public static boolean isNfcEnable(Context context) {
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(context);
        return adapter != null && adapter.isEnabled();
    }

    public static boolean isNfcSupported(Context context) {
        return NfcAdapter.getDefaultAdapter(context) != null;
    }

    public static NdefRecord createTextRecord(String payload, Locale locale, boolean encodeInUtf8) {
        byte[] langBytes = locale.getLanguage().getBytes(Charset.forName("US-ASCII"));
        Charset utfEncoding = encodeInUtf8 ? Charset.forName("UTF-8") : Charset.forName("UTF-16");
        byte[] textBytes = payload.getBytes(utfEncoding);
        int utfBit = encodeInUtf8 ? 0 : (1 << 7);
        char status = (char) (utfBit + langBytes.length);
        byte[] data = new byte[1 + langBytes.length + textBytes.length];
        data[0] = (byte) status;
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);
        NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                NdefRecord.RTD_TEXT, new byte[0], data);
        return record;
    }
}
