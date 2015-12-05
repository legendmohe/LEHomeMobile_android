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

package my.home.lehome.mvp.presenters;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.squareup.otto.Subscribe;

import java.lang.ref.WeakReference;
import java.util.List;

import my.home.common.BusProvider;
import my.home.domain.events.DRecordingMsgEvent;
import my.home.domain.usecase.CleanMsgUsecase;
import my.home.domain.usecase.CleanMsgUsecaseImpl;
import my.home.domain.usecase.RecordMsgUsecase;
import my.home.domain.usecase.RecordMsgUsecaseImpl;
import my.home.lehome.mvp.views.SendMessageView;
import my.home.model.entities.MessageItem;
import my.home.model.manager.DBStaticManager;

/**
 * Created by legendmohe on 15/11/28.
 */
public class SendMessagePresenter extends MVPPresenter {
    private static final String TAG = SendMessagePresenter.class.getSimpleName();

    private static final int START_RECORD = 0;
    private static final int STOP_RECORD = 1;
    private static final int CANCEL_RECORD = 2;

    private WeakReference<SendMessageView> mMessageView;
    private H mH;
    private RecordMsgUsecase mMessageUsecase;

    public SendMessagePresenter(SendMessageView messageView) {
        mH = new H();
        this.mMessageView = new WeakReference<>(messageView);
        if (this.mMessageView.get() != null) {
            this.mMessageUsecase = new RecordMsgUsecaseImpl(mMessageView.get().getContext(), "msg_");
        }
    }

    @Override
    public void start() {
        BusProvider.getUIBusInstance().register(this);
    }

    @Override
    public void stop() {
        BusProvider.getUIBusInstance().unregister(this);
    }

    public void startRecording() {
        mH.removeMessages(START_RECORD);
        mH.sendEmptyMessageDelayed(START_RECORD, 300);
    }

    public void cancelRecording() {
        mH.removeMessages(START_RECORD);
        postEventToUsecase(RecordMsgUsecase.Event.CANCEL);
    }

    public void finishRecording() {
        mH.removeMessages(START_RECORD);
        postEventToUsecase(RecordMsgUsecase.Event.STOP);
    }

    @Subscribe
    public void onRecordingMsgEvent(DRecordingMsgEvent event) {
        if (event.getMsgItem() != null) {
            Log.d(TAG, "onRecordingMsgEvent: " + event.getMsgItem().getTitle());

            mMessageView.get().onAddMsgItem(event.getMsgItem());
        } else {
            Log.d(TAG, "record fail or cancel");
        }
    }

    private void postEventToUsecase(RecordMsgUsecase.Event event) {
        mMessageUsecase.setEvent(event);
        mMessageUsecase.execute();
    }

    public List<MessageItem> getAllMessages() {
        if (mMessageView.get() != null)
            return DBStaticManager.getAllMessageItems(mMessageView.get().getContext());
        else
            return null;
    }

    public void cleanMessages() {
        if (mMessageView.get() != null) {
            CleanMsgUsecase cleanMsgUsecase = new CleanMsgUsecaseImpl(mMessageView.get().getContext());
            cleanMsgUsecase.execute();
            mMessageView.get().onDeleteAllMessages();
        }
    }

    private class H extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case START_RECORD:
                    mH.removeMessages(START_RECORD);
                    postEventToUsecase(RecordMsgUsecase.Event.START);
                    break;
                case CANCEL_RECORD:
                    break;
                case STOP_RECORD:
                    break;
            }
        }
    }
}
