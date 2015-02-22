package my.home.domain.usecase;

import android.content.Context;

import com.squareup.otto.Subscribe;

import java.lang.ref.WeakReference;

import my.home.common.BusProvider;
import my.home.domain.events.DSaveAutoCompleteLocalHistoryEvent;
import my.home.model.datasource.AutoCompleteItemDataSourceImpl;
import my.home.model.events.MSaveAutoCompleteLocalHistoryEvent;

/**
 * Created by legendmohe on 15/2/19.
 */
public class SaveAutoCompleteLocalHistoryUsecaseImpl implements SaveAutoCompleteLocalHistoryUsecase {
    private WeakReference<Context> mContext;

    public SaveAutoCompleteLocalHistoryUsecaseImpl(Context context) {
        this.mContext = new WeakReference<Context>(context);
    }

    @Override
    public void execute() {
        BusProvider.getRestBusInstance().register(this);
        AutoCompleteItemDataSourceImpl.getInstance().saveLocalHistory(mContext.get());
        BusProvider.getRestBusInstance().unregister(this);
    }

    @Subscribe
    public void onSaveAutoCompleteLocalHistoryItems(MSaveAutoCompleteLocalHistoryEvent event) {
        if (event.getReturnCode() == MSaveAutoCompleteLocalHistoryEvent.SUCCESS)
            BusProvider.getRestBusInstance().post(new DSaveAutoCompleteLocalHistoryEvent(event.getReturnCode()));
        else
            BusProvider.getRestBusInstance().post(new DSaveAutoCompleteLocalHistoryEvent(event.getReturnCode()));
    }
}
