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

package my.home.lehome.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import my.home.common.BusProvider;
import my.home.domain.events.DLoadAutoCompleteConfEvent;
import my.home.domain.events.DShowAutoCompleteItemEvent;
import my.home.domain.usecase.AutoCompleteItemUsecase;
import my.home.domain.usecase.AutoCompleteItemUsecaseImpl;
import my.home.lehome.R;
import my.home.model.entities.AutoCompleteItem;

/**
 * Created by legendmohe on 15/2/14.
 */
public class AutoCompleteAdapter extends BaseAdapter implements Filterable {
    public static final String TAG = AutoCompleteAdapter.class.getName();

    //    private static final int MAX_RESULTS = 10;
    private AutoCompleteItemUsecase mAutoCompleteItemUsecase;
    private WeakReference<Context> mContext;
    private WeakReference<onLoadConfListener> mLoadConfListener;
    private List<AutoCompleteItem> mResultList = new ArrayList<AutoCompleteItem>();

    public AutoCompleteAdapter(Context context) {
        mContext = new WeakReference<Context>(context);
        mAutoCompleteItemUsecase = new AutoCompleteItemUsecaseImpl(context);
        registerBus();
    }

    public void initAutoCompleteItem() {
        mAutoCompleteItemUsecase.setMode(AutoCompleteItemUsecase.MODE_LOAD_CONF)
                .execute();
    }

    private void registerBus() {
        BusProvider.getRestBusInstance().register(this);
    }

    public void destory() {
        BusProvider.getRestBusInstance().unregister(this);
    }

    @Override
    public int getCount() {
        return mResultList.size();
    }

    @Override
    public AutoCompleteItem getItem(int index) {
        return mResultList.get(index);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            Context context = mContext.get();
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.auto_complete_item_layout, parent, false);
        }
        ((TextView) convertView.findViewById(R.id.content_textview)).setText(getItem(position).getContent());
        ((TextView) convertView.findViewById(R.id.type_textview)).setText(getItem(position).getType());
        return convertView;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new Filter.FilterResults();
                if (constraint != null) {
                    getAutoCompleteItem(constraint.toString().trim());
                    // Assign the data to the FilterResults
                    filterResults.values = mResultList;
                    filterResults.count = mResultList.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
//                Log.i(TAG, String.valueOf(results.count));
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
        return filter;
    }

    private void getAutoCompleteItem(String inputSrc) {
        if (inputSrc.trim().length() != 0) {
            mAutoCompleteItemUsecase.setMode(AutoCompleteItemUsecase.MODE_GETITEM)
                    .setInputText(inputSrc)
                    .execute();
        }
    }

    @Subscribe
    public void onGetAutoCompleteItems(DShowAutoCompleteItemEvent event) {
        mResultList = event.getResultList();
    }

    @Subscribe
    public void onConfAutoCompleteItems(DLoadAutoCompleteConfEvent event) {
        if (mLoadConfListener == null || mLoadConfListener.get() == null)
            return;
        if (event.getReturnCode() == DLoadAutoCompleteConfEvent.ERROR) {
            mLoadConfListener.get().onLoadComplete(false);
        } else {
            mLoadConfListener.get().onLoadComplete(true);
        }
    }

    public void setOnLoadConfListener(onLoadConfListener listener) {
        mLoadConfListener = new WeakReference<onLoadConfListener>(listener);
    }

    public List<AutoCompleteItem> getResultList() {
        return mResultList;
    }

    public void setResultList(List<AutoCompleteItem> mResultList) {
        this.mResultList = mResultList;
    }

    public interface onLoadConfListener {
        public void onLoadComplete(boolean loadSuccess);
    }
}
