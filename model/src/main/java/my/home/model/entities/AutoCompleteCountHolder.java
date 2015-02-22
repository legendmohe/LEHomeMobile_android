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

package my.home.model.entities;

/**
 * Created by legendmohe on 15/2/19.
 */
public class AutoCompleteCountHolder {
    public String from = "";
    public String to = "";
    public int count = 0;

    public AutoCompleteCountHolder(String from, String to, int count) {
        this.from = from;
        this.to = to;
        this.count = count;
    }
}
