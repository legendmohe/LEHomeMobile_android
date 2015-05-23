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

import android.text.TextUtils;

/**
 * Created by legendmohe on 15/5/23.
 */
public class ChatItemUtils {

    public static String getThumbnailPath(String src) {
        if (TextUtils.isEmpty(src))
            return null;
        int lastIdx = src.lastIndexOf(".");
        String suffix = src.substring(lastIdx + 1);
        String rest = src.substring(0, lastIdx);
        return rest + ".thumbnail." + suffix;
    }
}
