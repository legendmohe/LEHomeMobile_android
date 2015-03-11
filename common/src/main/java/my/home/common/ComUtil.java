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

public class ComUtil {
    public static String readFileContent(String fileName) {
        return null;
    }

    public static String DateToCmdString(int year, int month, int day) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(year);
        stringBuilder.append("年");
        stringBuilder.append(month);
        stringBuilder.append("月");
        stringBuilder.append(day);
        stringBuilder.append("日");
        return stringBuilder.toString();
    }

    public static String TimeToCmdString(int hourOfDay, int minute) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(hourOfDay);
        stringBuilder.append("点");
        stringBuilder.append(minute);
        stringBuilder.append("分");
        return stringBuilder.toString();
    }
}
