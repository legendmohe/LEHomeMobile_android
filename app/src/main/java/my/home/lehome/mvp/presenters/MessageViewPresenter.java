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

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

import my.home.common.BusProvider;
import my.home.common.util.PrefUtil;
import my.home.domain.events.DRecordingMsgEvent;
import my.home.domain.events.DSendingMsgEvent;
import my.home.domain.usecase.CleanMsgUsecase;
import my.home.domain.usecase.CleanMsgUsecaseImpl;
import my.home.domain.usecase.RecordMsgUsecase;
import my.home.domain.usecase.RecordMsgUsecaseImpl;
import my.home.domain.usecase.SendMsgUsecase;
import my.home.domain.usecase.SendMsgUsecaseImpl;
import my.home.lehome.mvp.views.SendMessageView;
import my.home.lehome.util.Constants;
import my.home.model.entities.MessageItem;
import my.home.model.manager.DBStaticManager;

/**
 * Created by legendmohe on 15/11/28.
 */
public class MessageViewPresenter extends MVPPresenter implements RecordMsgUsecaseImpl.RecorderStateListener {
    private static final String TAG = MessageViewPresenter.class.getSimpleName();

    private static final int START_RECORD = 0;
    private static final int STOP_RECORD = 1;
    private static final int CANCEL_RECORD = 2;

    private WeakReference<SendMessageView> mMessageView;
    private H mH;
    private RecordMsgUsecase mRecordMsgUsecase;
    private SendMsgUsecase mSendMsgUsecase;
    private File mSendingFile;

    public MessageViewPresenter(SendMessageView messageView) {
        mH = new H();
        this.mMessageView = new WeakReference<>(messageView);
        if (this.mMessageView.get() != null) {
            this.mRecordMsgUsecase = new RecordMsgUsecaseImpl(mMessageView.get().getContext(), Constants.MESSAGE_PREFIX);
            ((RecordMsgUsecaseImpl) this.mRecordMsgUsecase).setListener(this);

            String homeId = PrefUtil.getStringValue(this.mMessageView.get().getContext(), "pref_bind_device", "");
            this.mSendMsgUsecase = new SendMsgUsecaseImpl(mMessageView.get().getContext(), homeId);
        }
    }

    @Override
    public void start() {
        BusProvider.getUIBusInstance().register(this);
    }

    @Override
    public void stop() {
        mRecordMsgUsecase.cleanup();
        mSendMsgUsecase.cleanup();
        BusProvider.getUIBusInstance().unregister(this);
    }

    public void startRecording() {
        mH.removeMessages(START_RECORD);
        mH.sendEmptyMessageDelayed(START_RECORD, 300);
    }

    public void cancelRecording() {
        mH.removeMessages(START_RECORD);
        mRecordMsgUsecase
                .setEvent(RecordMsgUsecase.Event.CANCEL)
                .execute();
    }

    public void finishRecording() {
        mH.removeMessages(START_RECORD);
        mH.removeMessages(STOP_RECORD);
        mH.sendEmptyMessageDelayed(STOP_RECORD, 500);
    }

    public void cancelSending() {
        if (mSendingFile != null)
            mSendMsgUsecase.cancel(mSendingFile);
    }

    @Subscribe
    public void onRecordingStart(DRecordingMsgEvent.TYPE startEvent) {
        if (mMessageView.get() != null)
            mMessageView.get().onRecordingBegin();
    }

    @Subscribe
    public void onRecordingMsgEvent(DRecordingMsgEvent event) {
        if (event.getMsgItem() != null) {
            Log.d(TAG, "onRecordingMsgEvent: " + event.getMsgItem().getTitle());

            if (mMessageView.get() != null) {
                mMessageView.get().onAddMsgItem(event.getMsgItem());
                mMessageView.get().onRecordingEnd();
            }

            mSendingFile = event.getResultFile();
            mSendMsgUsecase
                    .setTargetFile(mSendingFile)
                    .setEvent(SendMsgUsecase.Event.START)
                    .execute();
        } else {
            if (mMessageView.get() != null)
                mMessageView.get().onRecordingEnd();
            Log.d(TAG, "record fail or cancel");
        }
    }

    @Subscribe
    public void onSendingMsgEvent(DSendingMsgEvent event) {
        if (mMessageView.get() == null) {
            Log.e(TAG, "onSendingMsgEvent: mMessage is null");
            return;
        }

        Log.d(TAG, "onSendingMsgEvent: " + event.type);
        switch (event.type) {
            case BEGIN:
                mMessageView.get().onSendingMsgBegin(event.tag);
                break;
            case SUCCESS:
                mMessageView.get().onSendingMsgSuccess(event.tag);
                mSendingFile = null;
                break;
            case FAIL:
                mMessageView.get().onSendingMsgFail(event.tag);
                mSendingFile = null;
                break;
        }
    }

    public List<MessageItem> getAllMessages() {
        if (mMessageView.get() != null)
            return DBStaticManager.getAllMessageItems(mMessageView.get().getContext());
        else
            return null;
    }

    public void cleanMessages() {
        if (mMessageView.get() != null) {
            CleanMsgUsecase cleanMsgUsecase = new CleanMsgUsecaseImpl(
                    mMessageView.get().getContext(),
                    Constants.MESSAGE_PREFIX
            );
            cleanMsgUsecase.execute();
            mMessageView.get().onDeleteAllMessages();
        }
    }

    @Override
    public void onGetAmplitude(float value) {
        mMessageView.get().onRecordingAmplitude(value);
    }

    private class H extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case START_RECORD:
                    mH.removeMessages(START_RECORD);
                    mRecordMsgUsecase
                            .setEvent(RecordMsgUsecase.Event.START)
                            .execute();
                    break;
                case CANCEL_RECORD:
                    break;
                case STOP_RECORD:
                    mH.removeMessages(STOP_RECORD);
                    mRecordMsgUsecase
                            .setEvent(RecordMsgUsecase.Event.STOP)
                            .execute();
                    break;
            }
        }
    }
}
