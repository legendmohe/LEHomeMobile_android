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

package my.home.domain.usecase;

/**
 * Created by legendmohe on 15/2/8.
 */
public interface AutoCompleteItemUsecase extends Usecase {

    public static final String TAG = "AutoCompleteItemUsecaseImpl";

    public static final int MODE_DO_NOTHING = -1;
    public static final int MODE_GETITEM = 0;
    public static final int MODE_SAVE_CONF = 1;
    public static final int MODE_LOAD_CONF = 2;

    int getMode();

    AutoCompleteItemUsecase setMode(int mMode);

    AutoCompleteItemUsecase setInputText(String inputText);

    String getConfString();

    AutoCompleteItemUsecase setConfString(String mConfString);
}
