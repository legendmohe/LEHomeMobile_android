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

package my.home.common.util;

/**
 * Created by legendmohe on 15/11/18.
 */
public class AudioUtils {

    private static final float MAX_REPORTABLE_DB = 90.3087f;
    private static final float MAX_REPORTABLE_AMP = 32767f;


    private static int getRawAmplitude(byte[] data, int len) {
        if (len <= 0 || data == null || data.length <= 0) {
            return 0;
        }

        int sum = 0;
        for (int i = 0; i < len; i++) {
            sum += Math.abs(data[i]);
        }
        return sum / len;
    }

    public static float getAmplitude(byte[] data, int len) {
        return (float) (MAX_REPORTABLE_DB + (20 * Math.log10(getRawAmplitude(data, len) / MAX_REPORTABLE_AMP)));
    }
}
