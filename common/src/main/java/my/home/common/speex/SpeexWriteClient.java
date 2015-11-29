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

import java.io.File;
import java.io.IOException;


/**
 * A client write tags to local file.
 */
public class SpeexWriteClient {
    public static final String TAG = "SpeexWriteClient";

    public final static int MODE_NB = 0;
    public final static int MODE_WB = 1;
    public final static int MODE_UWB = 2;

    public final static int SAMPLERATE_8000 = 8000;
    public final static int SAMPLERATE_16000 = 16000;
    public final static int SAMPLERATE_32000 = 32000;

    // (0=NB, 1=WB and 2=UWB)
    private int mMode = 0;

    // 8000; 16000; 32000; 8000;
    protected int mSampleRate = SAMPLERATE_8000;

    /**
     * Defines the number of mChannels of the audio input (1=mono, 2=stereo).
     */
    protected int mChannels = 1;

    /**
     * Defines the number of frames per speex packet.
     */
    protected int mframesPerPacket = 1;

    /**
     * Defines whether or not to use VBR (Variable Bit Rate).
     */
    protected boolean mEnableVBR = false;

    OggSpeexWriter speexWriter = null;// new OggSpeexWriter(mMode, mSampleRate,
    // mChannels, mframesPerPacket, mEnableVBR);

    public SpeexWriteClient() {

    }

    public void start(File file, int mode, int sampleRate, boolean enableVBR) {
        mMode = mode;
        mSampleRate = sampleRate;
        mEnableVBR = enableVBR;
        init(file);
    }

    private void init(File file) {
        if (speexWriter != null) {
            this.stop();
        }
        speexWriter = new OggSpeexWriter(mMode, mSampleRate, mChannels, mframesPerPacket, mEnableVBR);
        try {
            speexWriter.open(file);
            speexWriter.writeHeader("SpeexWriteClient");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if (speexWriter != null) {
            try {
                speexWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            speexWriter = null;
        }
        Log.d(TAG, "writer closed!");
    }

    public void writePacket(byte[] buf, int size) {
//		Log.d(TAG, "here should be:===========================640,actual=" + size);
        try {
            speexWriter.writePacket(buf, 0, size);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
