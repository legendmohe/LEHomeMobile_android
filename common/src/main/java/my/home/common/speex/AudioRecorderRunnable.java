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

package my.home.common.speex;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by legendmohe on 15/11/13.
 */
public class AudioRecorderRunnable implements Runnable {
    private static final String TAG = "AudioRecorderRunnable";

    private static final int SAMPLE_RATE = 16000;

    private boolean mStopped = false;
    private LinkedBlockingQueue<AudioRawData> mBufferQueue;
    private WeakReference<AudioRecorderListener> mListener;
    private float mGain;

    public AudioRecorderRunnable(LinkedBlockingQueue<AudioRawData> queue, float gain, AudioRecorderListener listener) {
        this.mBufferQueue = queue;
        this.mGain = gain;
        this.mListener = new WeakReference<AudioRecorderListener>(listener);
    }

    @Override
    public void run() {
        if (this.mStopped) {
            Log.w(TAG, "ProcessRunnable is running.");
            return;
        }
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        AudioRecord recorder = null;
        short[][] buffers = new short[256][160];
        int ix = 0;
        float gain = this.mGain;
        try {
            int n = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            recorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    n * 10);
            recorder.setPositionNotificationPeriod(160);
            recorder.startRecording();
            if (mListener.get() != null)
                mListener.get().onRecordStart();
            while (!this.mStopped) {
                short[] buffer = buffers[ix++ % buffers.length];
                int numRead = recorder.read(buffer, 0, buffer.length);
                if (numRead > 0) {
                    for (int i = 0; i < numRead; ++i)
                        buffer[i] = (short) Math.min((int) (buffer[i] * gain), (int) Short.MAX_VALUE);
                }
                this.mBufferQueue.offer(new AudioRawData(buffer, numRead));
            }
        } catch (Throwable x) {
            Log.w(TAG, "Error reading voice audio", x);
        } finally {
            if (recorder != null) {
                recorder.stop();
                recorder.release();
            }
            Log.d(TAG, "thread exit.");
        }
    }

    public void stop() {
        this.mStopped = true;
    }

    public interface AudioRecorderListener {
        void onRecordStart();
    }
}
