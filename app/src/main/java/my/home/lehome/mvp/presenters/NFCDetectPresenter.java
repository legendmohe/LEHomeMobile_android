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

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.util.Log;

import java.lang.ref.WeakReference;

import my.home.common.State;
import my.home.common.StateMachine;
import my.home.lehome.asynctask.NfcWriteNdefAsyncTask;
import my.home.lehome.helper.NFCHelper;
import my.home.lehome.mvp.views.NFCDetectView;

/**
 * Created by legendmohe on 15/12/28.
 */
public class NFCDetectPresenter extends MVPActivityPresenter implements NfcWriteNdefAsyncTask.WriteNdefListener {
    public static final String TAG = "NFCDetectPresenter";

    private WeakReference<NFCDetectView> mNFCDetectView;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mNfcIntentFiltersArray;
    private String[][] mTechListsArray;

    private StateMachine mStateMachine;

    enum EVENT {
        FOUND, CANCEL, FINISH
    }

    public NFCDetectPresenter(NFCDetectView view) {
        this.mNFCDetectView = new WeakReference<>(view);
        mStateMachine = new StateMachine();
        DetectingState detectingState = new DetectingState();
        WritingState writingState = new WritingState();
        FinishedState finishedState = new FinishedState();
        detectingState.linkTo(writingState, EVENT.FOUND);
        detectingState.linkTo(finishedState, EVENT.CANCEL);
        writingState.linkTo(finishedState, EVENT.FINISH);

        mStateMachine.addState(writingState);
        mStateMachine.addState(finishedState);
        mStateMachine.addState(detectingState);
        mStateMachine.setInitState(detectingState);
    }

    @Override
    public void start() {
        mStateMachine.start();
    }

    @Override
    public void stop() {
    }

    @Override
    public void onActivityCreate(Activity activity) {
        if (NFCHelper.isNfcSupported(activity))
            this.setupNFCForegroundDispatch(activity);
    }

    @Override
    public void onActivityDestory(Activity activity) {
        cancelDetecting();
    }

    @Override
    public void onActivityResume(Activity activity) {
        if (NFCHelper.isNfcSupported(activity))
            this.enableNFCForegroundDispatch(activity);
    }

    @Override
    public void onActivityPause(Activity activity) {
        if (NFCHelper.isNfcSupported(activity))
            this.disableNFCForegroundDispatch(activity);
    }

    private void setupNFCForegroundDispatch(Activity activity) {
        Context context = mNFCDetectView.get().getContext();
        mPendingIntent = PendingIntent.getActivity(
                context, 0, new Intent(context, activity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter techTag = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        mNfcIntentFiltersArray = new IntentFilter[]{techTag,};
        mTechListsArray = new String[][]{new String[]{Ndef.class.getName()}};
    }

    private void enableNFCForegroundDispatch(Activity activity) {
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(activity);
        adapter.enableForegroundDispatch(activity, mPendingIntent, mNfcIntentFiltersArray, mTechListsArray);
    }

    private void disableNFCForegroundDispatch(Activity activity) {
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(activity);
        adapter.disableForegroundDispatch(activity);
    }

    public void cancelDetecting() {
        mStateMachine.postEvent(EVENT.CANCEL);
    }

    public void startDetecting() {
        if (mNFCDetectView.get() != null) {
            mNFCDetectView.get().onViewStateChange(NFCDetectView.State.DETECTING);
        }
    }

    public void onNewTagDetected(Tag detectedTag) {
        Log.d(TAG, "found new tag:" + detectedTag);
        mStateMachine.postEvent(EVENT.FOUND, detectedTag);

    }

    @Override
    public void onWriteFinished(NfcWriteNdefAsyncTask.Result result) {
        mStateMachine.postEvent(EVENT.FINISH, result);
    }

    class DetectingState extends State {

        public DetectingState() {
            super("DetectingState");
        }

        @Override
        public void onStart() {
            mNFCDetectView.get().onViewStateChange(NFCDetectView.State.DETECTING);
        }
    }

    class WritingState extends State {

        public WritingState() {
            super("WritingState");
        }

        @Override
        public void onEnter(State fromState, Enum<?> event, Object data) {
            if (fromState.getClass().equals(DetectingState.class)) {
                mNFCDetectView.get().onViewStateChange(NFCDetectView.State.WRITING);

                Tag detectedTag = (Tag) data;
                Context context = mNFCDetectView.get().getContext();
                if (NFCHelper.supportedTechs(detectedTag.getTechList())) {
                    if (NFCHelper.writableTag(detectedTag)) {
                        //writeTag here
                        NdefMessage message = NFCHelper.createNdefTextAppMessage(context, mNFCDetectView.get().getTargetContent());
                        new NfcWriteNdefAsyncTask(NFCDetectPresenter.this, detectedTag).execute(message);
                    } else {
                        mStateMachine.postEvent(EVENT.FINISH, NfcWriteNdefAsyncTask.Result.READONLY);
                    }
                } else {
                    mStateMachine.postEvent(EVENT.FINISH, NfcWriteNdefAsyncTask.Result.UNSUPPORTED);
                }
            }
        }
    }

    class FinishedState extends State {

        public FinishedState() {
            super("FinishedState");
        }

        @Override
        public void onEnter(State fromState, Enum<?> event, Object data) {
            if (event == EVENT.CANCEL) {
                mNFCDetectView.get().onViewStateChange(NFCDetectView.State.CANCEL);
            } else if (event == EVENT.FINISH) {
                NfcWriteNdefAsyncTask.Result result = (NfcWriteNdefAsyncTask.Result) data;
                switch (result) {
                    case SUCCESS:
                        mNFCDetectView.get().onViewStateChange(NFCDetectView.State.SUCCESS);
                        break;
                    case UNWRITABLE:
                    case UNSUPPORTED:
                    case READONLY:
                    case OVERSIZE:
                    case EXCEPTION:
                        Log.d(TAG, "tag write fail:" + result);
                        mNFCDetectView.get().showStateToast(result.toString());
                        mNFCDetectView.get().onViewStateChange(NFCDetectView.State.FAIL);
                        break;
                    default:
                        break;
                }
            }
        }
    }

}
