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
import android.nfc.tech.NdefFormatable;

import java.io.IOException;
import java.lang.ref.WeakReference;

import my.home.common.State;
import my.home.common.StateMachine;
import my.home.lehome.helper.NFCHelper;
import my.home.lehome.helper.NFCHelper.WriteResponse;
import my.home.lehome.mvp.views.NFCDetectView;

/**
 * Created by legendmohe on 15/12/28.
 */
public class NFCDetectPresenter extends MVPActivityPresenter {
    public static final String TAG = "NFCDetectPresenter";

    private WeakReference<NFCDetectView> mNFCDetectView;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mNfcIntentFiltersArray;
    private String[][] mTechListsArray;

    private StateMachine mStateMachine;
    private final static int EVENT_FOUND = 1;
    private final static int EVENT_WRITED = 2;
    private final static int EVENT_CANCEL = 3;

    public NFCDetectPresenter(NFCDetectView view) {
        this.mNFCDetectView = new WeakReference<>(view);
        mStateMachine = new StateMachine();
        DetectingState detectingState = new DetectingState();
        WritingState writingState = new WritingState();
        FinishedState finishedState = new FinishedState();
        detectingState.linkTo(writingState, EVENT_FOUND);
        detectingState.linkTo(finishedState, EVENT_CANCEL);
        writingState.linkTo(finishedState, EVENT_WRITED);

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
        try {
            techTag.addDataType("text/plain");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
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

    public void cancelIfDetecting() {

    }

    public void startDetecting() {
        if (mNFCDetectView.get() != null) {
            mNFCDetectView.get().onViewStateChange(NFCDetectView.State.DETECTING);
        }
    }

    public void onNewTagDetected(Tag detectedTag) {
        Context context = mNFCDetectView.get().getContext();
        if (NFCHelper.supportedTechs(detectedTag.getTechList())) {
            if (NFCHelper.writableTag(detectedTag)) {
                //writeTag here
                WriteResponse wr = writeTag(NFCHelper.createNdefTextAppMessage(
                                context, mNFCDetectView.get().getTargetContent()), detectedTag
                );
                int status = wr.getStatus();
                String message = wr.getMessage();
            } else {

            }
        } else {

        }
    }

    public WriteResponse writeTag(NdefMessage message, Tag tag) {
        int size = message.toByteArray().length;
        String mess = "";
        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();
                if (!ndef.isWritable()) {
                    return new WriteResponse(0, "Tag is read-only");
                }
                if (ndef.getMaxSize() < size) {
                    mess = "Tag capacity is " + ndef.getMaxSize() + " bytes, message is " + size
                            + " bytes.";
                    return new WriteResponse(0, mess);
                }
                ndef.writeNdefMessage(message);
                mess = "Wrote message to pre-formatted tag.";
                return new WriteResponse(1, mess);
            } else {
                NdefFormatable format = NdefFormatable.get(tag);
                if (format != null) {
                    try {
                        format.connect();
                        format.format(message);
                        mess = "Formatted tag and wrote message";
                        return new WriteResponse(1, mess);
                    } catch (IOException e) {
                        mess = "Failed to format tag.";
                        return new WriteResponse(0, mess);
                    }
                } else {
                    mess = "Tag doesn't support NDEF.";
                    return new WriteResponse(0, mess);
                }
            }
        } catch (Exception e) {
            mess = "Failed to write tag";
            return new WriteResponse(0, mess);
        }
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
        public void onEnter(State fromState, int event, Object data) {
            if (fromState.getClass().equals(DetectingState.class)) {

            }
        }
    }

    class FinishedState extends State {

        public FinishedState() {
            super("FinishedState");
        }

        @Override
        public void onEnter(State fromState, int event, Object data) {
        }
    }

    public enum Result {
        CANCELED, SUCCESS, FAIL
    }

}
