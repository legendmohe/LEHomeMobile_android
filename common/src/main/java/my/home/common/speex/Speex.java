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

/**
 * Created by legendmohe on 15/11/9.
 */
public class Speex {
    /* quality
    * 1 : 4kbps (very noticeable artifacts, usually intelligible)
    * 2 : 6kbps (very noticeable artifacts, good intelligibility)
    * 4 : 8kbps (noticeable artifacts sometimes)
    * 6 : 11kpbs (artifacts usually only noticeable with headphones)
    * 8 : 15kbps (artifacts not usually noticeable)
    */
    private static final int DEFAULT_COMPRESSION = 8;

    public Speex() {
    }

    public void init() {
        load();
        open(DEFAULT_COMPRESSION);
    }

    private void load() {
        try {
            System.loadLibrary("speex");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public native int open(int compression);

    public native int getFrameSize();

    public native int decode(byte encoded[], short lin[], int size);

    public native int encode(short lin[], int offset, byte encoded[], int size);

    public native void close();
}
