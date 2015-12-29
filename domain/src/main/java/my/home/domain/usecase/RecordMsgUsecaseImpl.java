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

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.LinkedBlockingQueue;

import my.home.common.BusProvider;
import my.home.common.State;
import my.home.common.StateMachine;
import my.home.common.speex.AudioRawData;
import my.home.common.speex.AudioRecorderRunnable;
import my.home.common.speex.ProcessSpeexRunnable;
import my.home.common.speex.SpeexWriteClient;
import my.home.common.util.AudioUtils;
import my.home.common.util.FileUtil;
import my.home.domain.events.DRecordingMsgEvent;
import my.home.domain.util.DomainUtil;
import my.home.model.entities.MessageItem;

/**
 * Created by legendmohe on 15/12/5.
 */
public class RecordMsgUsecaseImpl implements
        RecordMsgUsecase,
        ProcessSpeexRunnable.ProcessSpeexListener,
        AudioRecorderRunnable.AudioRecorderListener {

    private AudioRecorderRunnable mAudioRecorderRunnable;
    private ProcessSpeexRunnable mProcessSpeexRunnable;
    private SpeexWriteClient mSpeexWriteClient = new SpeexWriteClient();
    private LinkedBlockingQueue<AudioRawData> mDataQueue = new LinkedBlockingQueue<>();

    private WeakReference<Context> mContext;
    private WeakReference<RecorderStateListener> mStateListener;

    private AudioUtils.FFT mFFT;

    private StateMachine mStateMachine = new StateMachine();
    private Event mEvent;
    private Enum<?> mLastEvent;
    private String mSaveFilePrefix;
    private static SimpleDateFormat gDateFormat = new SimpleDateFormat("yy年M月d日H时m分s秒", Locale.CHINA);

    public RecordMsgUsecaseImpl(Context context, String prefix) {
        this.mSaveFilePrefix = prefix;
        this.mContext = new WeakReference<>(context);
        BusProvider.getRestBusInstance().register(this);

        IdleState idleState = new IdleState();
        RecordingState recordingState = new RecordingState();

        idleState.linkTo(recordingState, Event.START);
        recordingState.linkTo(idleState, Event.CANCEL);
        recordingState.linkTo(idleState, Event.STOP);

        mStateMachine.addState(idleState);
        mStateMachine.addState(recordingState);
        mStateMachine.setInitState(idleState);
        mStateMachine.start();
    }

    @Override
    public void cleanup() {
        stopRecording();
        mStateMachine.stop(0);
        BusProvider.getRestBusInstance().unregister(this);
    }

    public void setListener(RecorderStateListener listener) {
        this.mStateListener = new WeakReference<>(listener);
    }

    private void startRecording() {
        mProcessSpeexRunnable = new ProcessSpeexRunnable(
                mDataQueue,
                this
        );
        mAudioRecorderRunnable = new AudioRecorderRunnable(
                mDataQueue,
                1.3f,
                this
        );

        new Thread(mProcessSpeexRunnable).start();
        new Thread(mAudioRecorderRunnable).start();
    }

    private void stopRecording() {
        if (mAudioRecorderRunnable != null) {
            mAudioRecorderRunnable.stop();
            mAudioRecorderRunnable = null;
        }
        if (mProcessSpeexRunnable != null) {
            mProcessSpeexRunnable.stop();
            mProcessSpeexRunnable = null;
        }
    }

    @Override
    public RecordMsgUsecase setEvent(Event event) {
        mEvent = event;
        return this;
    }

    @Override
    public void execute() {
        if (mEvent != null) {
            mStateMachine.postEvent(mEvent);
        }
    }

    public void postResult(final File file) {
        Log.d(TAG, "write SpeexOgg file finished.");
        MessageItem msgItem = null;
        if (file != null) {
            msgItem = new MessageItem();
            msgItem.setContent(file.getAbsolutePath());
            msgItem.setTitle(file.getName());
            msgItem.setDate(new Date());
            msgItem.setState(-1);
            msgItem.setType(-1);
//            if (this.mContext.get() != null) {
//                DBStaticManager.addMessageItem(this.mContext.get(), msgItem);
//            }
        }
        final MessageItem finalMsgItem = msgItem;
        DomainUtil.runOnMainThread(new Runnable() {
            @Override
            public void run() {
                BusProvider.getUIBusInstance().post(new DRecordingMsgEvent(file, finalMsgItem));
            }
        });
    }

    @Override
    public void onProcess(short[] data, int len) {

        if (mStateListener.get() != null) {
            final double value = AudioUtils.getAmplitude(data, len);
            DomainUtil.runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    mStateListener.get().onGetAmplitude(value);
                }
            });
//            mStateListener.get().processWaveform(data, len);
        }
    }

    @Override
    public void onProcessSpeexData(final byte[] data, final int len) {

    }

    @Override
    public void onProcessSpeexFinish(List<byte[]> data) {
        File resultFile = null;
        if (mLastEvent == Event.STOP) {
            resultFile = writeRawDataToFile(data);
        }
        postResult(resultFile);
    }

    private File writeRawDataToFile(List<byte[]> data) {
//        String fileName = this.mSaveFilePrefix + gDateFormat.format(new Date()) + ".spx";
        String fileName = this.mSaveFilePrefix + "_cache.spx";
        File saveFile = new File(FileUtil.getDiskCacheDir(mContext.get(), fileName));
        Log.d(TAG, "save record: " + saveFile.getAbsolutePath());

        // follow AudioRecorderRunnable.SAMPLE_RATE
        mSpeexWriteClient.start(saveFile, SpeexWriteClient.MODE_NB, SpeexWriteClient.SAMPLERATE_16000, true);
        try {
            for (byte[] packet : data) {
                mSpeexWriteClient.writePacket(packet, packet.length);
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            mSpeexWriteClient.stop();
        }
        return saveFile;
    }

    @Override
    public void onRecordStart() {
        DomainUtil.runOnMainThread(new Runnable() {
            @Override
            public void run() {
                BusProvider.getUIBusInstance().post(DRecordingMsgEvent.TYPE.START);
            }
        });
    }

    public class IdleState extends State {

        public IdleState() {
            super(IdleState.class.getSimpleName());
        }

        @Override
        public void onEnter(State fromState, Enum<?> event, Object data) {
            if (fromState.getClass() == RecordingState.class) {
                if (event == Event.CANCEL || event == Event.STOP) {
                    mLastEvent = event;
                    stopRecording();
                }
            }
        }
    }

    public class RecordingState extends State {

        public RecordingState() {
            super(RecordingState.class.getSimpleName());
        }

        @Override
        public void onEnter(State fromState, Enum<?> event, Object data) {
            if (fromState.getClass() == IdleState.class) {
                if (event == Event.START) {
                    startRecording();
                }
            }
        }
    }

    public interface RecorderStateListener {
        void onGetAmplitude(double value);

        void processWaveform(short[] notProcessData, int len);
    }
}
