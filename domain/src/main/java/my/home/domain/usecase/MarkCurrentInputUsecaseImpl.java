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
