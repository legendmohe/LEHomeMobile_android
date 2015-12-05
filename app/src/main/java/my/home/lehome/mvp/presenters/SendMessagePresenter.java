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

import android.util.Log;

import com.squareup.otto.Subscribe;

import java.lang.ref.WeakReference;

import my.home.common.BusProvider;
import my.home.domain.events.DRecordingMsgEvent;
import my.home.domain.usecase.RecordMsgUsecase;
import my.home.domain.usecase.RecordMsgUsecaseImpl;
import my.home.lehome.mvp.views.SendMessageView;

/**
 * Created by legendmohe on 15/11/28.
 */
public class SendMessagePresenter extends MVPPresenter {
    private static final String TAG = SendMessagePresenter.class.getSimpleName();

    private WeakReference<SendMessageView> mMessageView;
    private RecordMsgUsecase mMessageUsecase;

    public SendMessagePresenter(SendMessageView messageView) {
        this.mMessageView = new WeakReference<>(messageView);
        if (this.mMessageView.get() != null) {
            this.mMessageUsecase = new RecordMsgUsecaseImpl(mMessageView.get().getContext(), "msg_");
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

    public void startRecording() {
        mMessageUsecase.setEvent(RecordMsgUsecase.Event.START);
    }

    public void cancelRecording() {
        mMessageUsecase.setEvent(RecordMsgUsecase.Event.CANCEL);
    }

    public void finishRecording() {
        mMessageUsecase.setEvent(RecordMsgUsecase.Event.STOP);
    }

    @Subscribe
    public void onRecordingMsgEvent(DRecordingMsgEvent event) {
        if (event.getMsgItem() != null) {
            Log.d(TAG, "onRecordingMsgEvent: " + event.getMsgItem().getTitle());
        } else {
            Log.d(TAG, "record fail or cancel");
        }
    }
}
