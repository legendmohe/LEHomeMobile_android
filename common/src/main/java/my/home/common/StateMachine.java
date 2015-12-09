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

import android.os.Handler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by legendmohe on 15/12/5.
 */
public class StateMachine {

    Set<State> mStates = new HashSet<>();
    private State mInitState;
    private State mCurrentState;
    private Object mHandleLock = new Object();

    Handler mHandler;

    public StateMachine() {
        mHandler = new Handler();
    }

    public StateMachine(Handler handler) {
        mHandler = handler;
    }

    public void setInitState(State initState) {
        mInitState = initState;
    }

    public void addState(State state) {
        synchronized (this) {
            mStates.add(state);
            state.setStateMachine(this);
        }
    }

    public void start() {
        synchronized (this) {
            for (State state : mStates) {
                state.onStart();
            }
            mCurrentState = mInitState;
        }
    }

    public void stop(int cause) {
        synchronized (this) {
            for (State state : mStates) {
                state.onStop(cause);
            }
        }
    }

    public void reset(int cause) {
        synchronized (this) {
            for (State state : mStates) {
                state.onReset(cause);
            }
            mCurrentState = mInitState;
        }
    }

    public void postEvent(int event) {
        postEvent(event, null);
    }
    
    public void postEvent(final int event, final Object data) {
        if (mHandler == null) {
            return;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                synchronized (mHandleLock) {
                    State nextState = mCurrentState.mToStates.get(event);
                    if (nextState == null) {
                        mCurrentState.onUnhandleEvent(event, data);
                        return;
                    }
                    mCurrentState.onLeave(nextState, event, data);
                    nextState.onEnter(mCurrentState, event, data);
                    mCurrentState = nextState;
                }
            }
        });
    }
    
    public boolean canMoveTo(State toState) {
        if (toState == null) {
            return false;
        }
        synchronized (this) {
            HashMap<Integer, State> states = mCurrentState.mToStates;
            for (Integer event : states.keySet()) {
                if (states.get(event).equals(toState)) {
                    return true;
                }
            }
            return false;
        }
    }

    public boolean canAccept(int event) {
        synchronized (this) {
            return mCurrentState.mToStates.containsKey(event);
        }
    }

    public State getCurrentState() {
        return mCurrentState;
    }
}
