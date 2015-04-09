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
import android.util.Log;

import com.squareup.otto.Subscribe;

import java.lang.ref.WeakReference;
import java.util.List;

import my.home.common.BusProvider;
import my.home.domain.events.DLoadAutoCompleteConfEvent;
import my.home.domain.events.DShowAutoCompleteItemEvent;
import my.home.domain.events.DShowCmdSuggestionEvent;
import my.home.domain.util.DomainUtil;
import my.home.model.datasource.AutoCompleteItemDataSource;
import my.home.model.datasource.AutoCompleteItemDataSourceImpl;
import my.home.model.entities.AutoCompleteItem;
import my.home.model.events.MConfAutoCompleteItemEvent;
import my.home.model.events.MGetAutoCompleteItemEvent;

/**
 * Created by legendmohe on 15/2/8.
 */
public class AutoCompleteItemUsecaseImpl implements AutoCompleteItemUsecase {

    private final AutoCompleteItemDataSource dataSource;
    private int mMode = MODE_DO_NOTHING;
    private String mInputText = "";
    private String mConfString = "";
    private WeakReference<Context> mContext;

    public AutoCompleteItemUsecaseImpl(Context context) {
        this.dataSource = AutoCompleteItemDataSourceImpl.getInstance();
        this.mContext = new WeakReference<Context>(context);
    }

    @Override
    public void execute() {
        if (mMode == MODE_DO_NOTHING) {
            Log.w(TAG, "Didn't set mode for this usecase.");
            return;
        }

        try {
            switch (this.mMode) {
                case MODE_GETITEM:
                    if (mContext.get() != null)
                        this.dataSource.getAutoCompleteItems(mContext.get(), mInputText);
                    break;
                case MODE_LOAD_CONF:
                    this.dataSource.loadConf(mContext.get());
                    break;
                case MODE_SAVE_CONF:
                    this.dataSource.saveConf(mContext.get(), mConfString);
                    break;
                default:
                    break;
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        } finally {
            this.mMode = MODE_DO_NOTHING;
            BusProvider.getRestBusInstance().unregister(this);
        }
    }

    @Subscribe
    public void onGetAutoCompleteItems(MGetAutoCompleteItemEvent event) {

        final AutoCompleteItem suggestionItem = filterSuggestionItem(event);
        final MGetAutoCompleteItemEvent finalEvent = event;
        DomainUtil.runOnMainThread(new Runnable() {
            @Override
            public void run() {
                BusProvider.getRestBusInstance().post(new DShowAutoCompleteItemEvent(finalEvent.getResultList()));
                BusProvider.getRestBusInstance().post(new DShowCmdSuggestionEvent(suggestionItem));
            }
        });
    }

    @Subscribe
    public void onConfAutoCompleteItems(MConfAutoCompleteItemEvent event) {
        final MConfAutoCompleteItemEvent finalEvent = event;
        DomainUtil.runOnMainThread(new Runnable() {
            @Override
            public void run() {
                BusProvider.getRestBusInstance().post(new DLoadAutoCompleteConfEvent(finalEvent.getReturnCode()));
            }
        });
    }

    @Override
    public int getMode() {
        return mMode;
    }

    @Override
    public AutoCompleteItemUsecase setMode(int mMode) {
        if (mMode != MODE_DO_NOTHING)
            BusProvider.getRestBusInstance().register(this);
        this.mMode = mMode;
        return this;
    }

    @Override
    public AutoCompleteItemUsecase setInputText(String inputText) {
        this.mInputText = inputText;
        return this;
    }

    @Override
    public String getConfString() {
        return mConfString;
    }

    @Override
    public AutoCompleteItemUsecase setConfString(String mConfString) {
        this.mConfString = mConfString;
        return this;
    }

    private AutoCompleteItem filterSuggestionItem(MGetAutoCompleteItemEvent event) {
        List<AutoCompleteItem> results = event.getResultList();
        for (AutoCompleteItem item : results) {
            if (item.getType() != "tool")
                return item;
        }
        return null;
    }
}
