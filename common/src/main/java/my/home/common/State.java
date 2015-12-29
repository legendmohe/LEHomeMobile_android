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

import java.util.HashMap;


/**
 * Created by legendmohe on 15/12/5.
 */
public abstract class State {

    HashMap<Enum<?>, State> mToStates = new HashMap<>();
    private StateMachine mStateMachine;

    @SuppressWarnings("unused")
    private String mName = "UNKNOWN";

    public State(String name) {
        mName = name;
    }

    public void linkTo(State toState, Enum<?> event) {
        if (toState == null) {
            throw new IllegalArgumentException("toState cannot be null");
        }
        mToStates.put(event, toState);
    }

    public void onStart() {
    }

    public void onStop(int cause) {
    }

    public void onReset(int cause) {
    }

    public void onUnhandleEvent(Enum<?> event, Object data) {
    }

    public void onEnter(State fromState, Enum<?> event, Object data) {
    }

    public void onLeave(State toState, Enum<?> event, Object data) {
    }

    protected StateMachine getStateMachine() {
        return mStateMachine;
    }

    protected void setStateMachine(StateMachine stateMachine) {
        mStateMachine = stateMachine;
    }
}
