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

package my.home.common;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * Created by legendmohe on 15/5/28.
 */
public class FourStateHandler extends Handler {
    public static final String TAG = "FourStateHandler";

    public static final int MSG_START = 0;
    public static final int MSG_CANCEL = 1;
    public static final int MSG_SUCCESS = 2;
    public static final int MSG_FAIL = 3;

    public static final int STATE_IDLE = 0;
    public static final int STATE_PENDING = 1;
    public static final int STATE_SUCCESS = 2;
    public static final int STATE_FAIL = 3;
    private final WeakReference<StateCallback> mStateCallback;

    private int mOldState = STATE_IDLE;
    private int mNewState = -1;
    private Bundle mWhat = new Bundle();

    public FourStateHandler(Bundle what, StateCallback callback) {
        super();
        setWhat(what);
        mStateCallback = new WeakReference<>(callback);
    }

    public FourStateHandler(Bundle what, StateCallback callback, Looper looper) {
        super(looper);
        setWhat(what);
        mStateCallback = new WeakReference<>(callback);
    }

    @Override
    public void handleMessage(Message msg) {
        if (mStateCallback.get() == null) {
            return;
        }
        switch (msg.what) {
            case MSG_START:
                if (mOldState == STATE_IDLE || mOldState == STATE_SUCCESS || mOldState == STATE_FAIL) {
                    mNewState = STATE_PENDING;
                    mStateCallback.get().onStatePending(mOldState, mNewState, mWhat);
                    mOldState = mNewState;
                }
                return;
            case MSG_CANCEL:
                if (mOldState == STATE_PENDING) {
                    mNewState = STATE_IDLE;
                    mStateCallback.get().onStateIdle(mOldState, mNewState, mWhat);
                    mOldState = mNewState;
                }
                return;
            case MSG_SUCCESS:
                if (mOldState == STATE_PENDING) {
                    mNewState = STATE_SUCCESS;
                    mStateCallback.get().onStateSuccess(mOldState, mNewState, mWhat);
                    mOldState = mNewState;
                }
                return;
            case MSG_FAIL:
                if (mOldState == STATE_PENDING) {
                    mNewState = STATE_FAIL;
                    mStateCallback.get().onStatefail(mOldState, mNewState, mWhat);
                    mOldState = mNewState;
                }
                return;
        }
        mStateCallback.get().onUnhandleState(mOldState, mWhat);
    }

    public void start() {
        sendEmptyMessage(MSG_START);
    }

    public void cancel() {
        sendEmptyMessage(MSG_CANCEL);
    }

    public void complete(boolean success) {
        sendEmptyMessage(success ? MSG_SUCCESS : MSG_FAIL);
    }

    public int getCurrentState() {
        return mOldState;
    }

    public Bundle getWhat() {
        return mWhat;
    }

    public void setWhat(Bundle what) {
        this.mWhat = what;
    }

    public interface StateCallback {
        void onStateIdle(int oldState, int newState, Bundle what);

        void onStatePending(int oldState, int newState, Bundle what);

        void onStateSuccess(int oldState, int newState, Bundle what);

        void onStatefail(int oldState, int newState, Bundle what);

        void onUnhandleState(int oldState, Bundle what);
    }
}
