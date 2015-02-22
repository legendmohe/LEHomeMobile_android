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

import android.content.Context;

import java.lang.ref.WeakReference;

import my.home.model.datasource.AutoCompleteItemDataSourceImpl;

/**
 * Created by legendmohe on 15/2/20.
 */
public class MarkCurrentInputUsecaseImpl implements MarkCurrentInputUsecase {
    private WeakReference<Context> mContext;
    private String mInput;

    public MarkCurrentInputUsecaseImpl(Context context, String input) {
        this.mContext = new WeakReference<Context>(context);
        this.mInput = input;
    }

    @Override
    public void execute() {
//        BusProvider.getRestBusInstance().register(this);
        AutoCompleteItemDataSourceImpl.getInstance().markCurrentInput(mInput);
//        BusProvider.getRestBusInstance().unregister(this);
    }
}
