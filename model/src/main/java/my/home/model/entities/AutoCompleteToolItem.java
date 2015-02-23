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
 * Created by legendmohe on 15/2/23.
 */
public class AutoCompleteToolItem extends AutoCompleteItem {
    private int specType;

    public static final int SPEC_TYPE_DATE = 0;
    public static final int SPEC_TYPE_TIME = 1;
    public static final int SPEC_TYPE_FAVOR = 2;

    public AutoCompleteToolItem(String type, String content, int specType) {
        super(type, Float.MAX_VALUE, content, "");
        this.specType = specType;
    }

    public int getSpecType() {
        return specType;
    }

    public void setSpecType(int specType) {
        this.specType = specType;
    }
}
