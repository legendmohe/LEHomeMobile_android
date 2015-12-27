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

package my.home.lehome.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CommonUtils {

    public static String removeLastChar(String s) {
        if (s == null || s.length() == 0) {
            return s;
        }
        return s.substring(0, s.length() - 1);
    }

    public static boolean copyStringToClipboard(Context context, String label, String src) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, src);
        clipboard.setPrimaryClip(clip);
        return true;
    }

    public static String getDateFormatString(String format) {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat(format, Locale.getDefault()); //called without pattern
        return df.format(c.getTime());
    }

    public static String concatenateStrings(String[] targets) {
        StringBuilder sb = new StringBuilder();
        for (String target :
                targets) {
            sb.append(target);
        }
        return sb.toString();
    }
}
