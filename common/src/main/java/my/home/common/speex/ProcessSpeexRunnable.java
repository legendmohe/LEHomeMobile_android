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

import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by legendmohe on 15/11/13.
 */
public class ProcessSpeexRunnable implements Runnable {
    private static final String TAG = "ProcessSpeexRunnable";
    private final WeakReference<ProcessSpeexListener> mListener;

    private Speex mSpeexLib;
    private LinkedBlockingQueue<AudioRawData> mBufferQueue;
    private LinkedList<byte[]> mEncodedData;
    private boolean mStopped = false;
    private static final AudioRawData sStopAudioData = new AudioRawData(null, -1);

    private int totByte;

    public ProcessSpeexRunnable(LinkedBlockingQueue<AudioRawData> queue, ProcessSpeexListener listener) {
        this.mBufferQueue = queue;
        this.mListener = new WeakReference<ProcessSpeexListener>(listener);
        init();
    }

    @Override
    public void run() {
        if (this.mStopped || mSpeexLib == null) {
            Log.w(TAG, "ProcessSpeexRunnable not runnable.");
            return;
        }
        try {
            while (this.mBufferQueue.size() != 0 || !this.mStopped) {
                AudioRawData data = null;
                try {
                    data = this.mBufferQueue.take();
                } catch (InterruptedException e) {
                    Log.e(TAG, "poll error: " + e.toString());
                    continue;
                }
                if (data == null) {
                    continue;
                } else if (data == sStopAudioData) {
                    this.mStopped = true;
                    continue;
                } else if (data.len > 0)
                    process(data.data, data.len);
            }
        } finally {
            if (mListener.get() != null) {
                mListener.get().onProcessSpeexFinish(mEncodedData);
            }

            this.clean();
            Log.d(TAG, "thread exit.");
        }
    }

    public void stop() {
        if (this.mBufferQueue == null) {
            this.mStopped = true;
        } else {
            try {
                this.mBufferQueue.put(sStopAudioData);
            } catch (InterruptedException e) {
                Log.e(TAG, "stop flag error: " + e.toString());
            }
        }
    }

    private void init() {
        mSpeexLib = new Speex();
        mSpeexLib.init();

        mEncodedData = new LinkedList<>();
    }

    private void clean() {
        if (mSpeexLib != null) {
            mSpeexLib.close();
            mSpeexLib = null;
        }
    }

    private void process(short[] buffer, int n) {
        byte[] encoded = new byte[n];
        int encLen = this.mSpeexLib.encode(buffer, 0, encoded, buffer.length);

        totByte += encLen;
        Log.d(TAG, "process speex from " + buffer.length / 2 + " byte to " + totByte + " byte");
        if (mEncodedData != null) {
            byte[] wrapData = new byte[encLen];
            System.arraycopy(encoded, 0, wrapData, 0, wrapData.length);
            mEncodedData.add(wrapData);
        }

        if (mListener.get() != null) {
            mListener.get().onProcessSpeexData(encoded, encLen);
        }
    }

    public interface ProcessSpeexListener {
        void onProcessSpeexData(byte[] data, int len);

        void onProcessSpeexFinish(List<byte[]> data);
    }
}
