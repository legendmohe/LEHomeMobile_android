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

package my.home.domain.events;

import my.home.model.entities.AutoCompleteItem;

/**
 * Created by legendmohe on 15/2/26.
 */
public class DShowCmdSuggestionEvent {
    private AutoCompleteItem item;

    public DShowCmdSuggestionEvent(AutoCompleteItem item) {
        this.item = item;
    }

    public AutoCompleteItem getItem() {
        return item;
    }

    public void setItem(AutoCompleteItem item) {
        this.item = item;
    }
}
