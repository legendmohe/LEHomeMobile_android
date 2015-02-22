package my.home.lehome.mvp.presenters;

import android.content.Context;

import com.squareup.otto.Subscribe;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

import my.home.common.BusProvider;
import my.home.common.Constants;
import my.home.domain.events.DSaveAutoCompleteLocalHistoryEvent;
import my.home.domain.usecase.MarkCurrentInputUsecaseImpl;
import my.home.domain.usecase.SaveAutoCompleteLocalHistoryUsecaseImpl;
import my.home.lehome.asynctask.SendCommandAsyncTask;
import my.home.lehome.helper.DBHelper;
import my.home.lehome.mvp.views.ChatItemListView;
import my.home.lehome.mvp.views.SaveLocalHistoryView;
import my.home.model.entities.ChatItem;

/**
 * Created by legendmohe on 15/2/19.
 */
public class ChatFragmentPresenter extends MVPPresenter {

    private WeakReference<SaveLocalHistoryView> mSaveLocalHistoryView;
    private WeakReference<ChatItemListView> mChatItemListView;

    public ChatFragmentPresenter(SaveLocalHistoryView saveLocalHistoryView, ChatItemListView chatItemListView) {
        this.mSaveLocalHistoryView = new WeakReference<SaveLocalHistoryView>(saveLocalHistoryView);
        this.mChatItemListView = new WeakReference<ChatItemListView>(chatItemListView);
    }

    public void markAndSendCurrentInput(String input) {
        new SendCommandAsyncTask(mSaveLocalHistoryView.get().getContext(), input).execute();
        new MarkCurrentInputUsecaseImpl(mSaveLocalHistoryView.get().getContext(), input).execute();
    }

    public void markAndSendCurrentChatItem(ChatItem chatItem) {
        new SendCommandAsyncTask(mSaveLocalHistoryView.get().getContext(), chatItem).execute();
        new MarkCurrentInputUsecaseImpl(mSaveLocalHistoryView.get().getContext(), chatItem.getContent()).execute();
    }

    public void saveSaveLocalHistory() {
        new SaveAutoCompleteLocalHistoryUsecaseImpl(mSaveLocalHistoryView.get().getContext()).execute();
    }

    public void resetDatas(Context context) {
        List<ChatItem> chatItems = DBHelper.loadLatest(context, Constants.CHATITEM_LOAD_LIMIT);
        if (chatItems != null) {
            Collections.reverse(chatItems); // reverse descend items
            mChatItemListView.get().onResetDatas(chatItems);
        }
    }

    @Override
    public void start() {
        BusProvider.getRestBusInstance().register(this);
    }

    @Override
    public void stop() {
        BusProvider.getRestBusInstance().unregister(this);
    }

    @Subscribe
    public void onSaveAutoCompleteLocalHistoryItems(DSaveAutoCompleteLocalHistoryEvent event) {
        if (event.getReturnCode() == DSaveAutoCompleteLocalHistoryEvent.SUCCESS)
            this.mSaveLocalHistoryView.get().onSaveLocalHistoryFinish(true);
        else
            this.mSaveLocalHistoryView.get().onSaveLocalHistoryFinish(false);
    }
}
