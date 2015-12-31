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

package my.home.lehome.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import my.home.lehome.R;
import my.home.lehome.mvp.presenters.NFCDetectPresenter;
import my.home.lehome.mvp.views.NFCDetectView;

public class NFCDetectDialog extends Activity implements NFCDetectView {

    public static final String EXTRA_TEXT_CONTENT = "EXTRA_TEXT_CONTENT";
    public static final int RESULT_CODE_SUCCESS = 1;
    public static final int RESULT_CODE_FAIL = 2;
    public static final int RESULT_CODE_CANCEL = 3;
    private static final String TAG = "NFCDetectDialog";

    private NFCDetectPresenter mNFCDetectPresenter;
    private State mState;
    private Button mCmdButton;
    private TextView mTitleTextview;
    private String mTargetContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfcdetect);
        setFinishOnTouchOutside(false);
        setupData();
        setupViews(null);

        if (TextUtils.isEmpty(mTargetContent)) {
            mTitleTextview.setText(R.string.nfc_empty_content);
            mCmdButton.setEnabled(true);
            mCmdButton.setText(R.string.nfc_close_dialog);
            mState = State.FAIL;
        } else {
            mNFCDetectPresenter.onActivityCreate(this);
            mNFCDetectPresenter.startDetecting();
        }
    }

    private void setupData() {
        mNFCDetectPresenter = new NFCDetectPresenter(this);
        mTargetContent = getIntent().getStringExtra(NFCDetectDialog.EXTRA_TEXT_CONTENT);
    }

    @Override
    public void setupViews(View rootView) {
        mTitleTextview = (TextView) findViewById(R.id.nfc_detect_title);
        mCmdButton = (Button) findViewById(R.id.nfc_detect_button);
        mCmdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "click cancel on state " + mState);
                switch (mState) {
                    case DETECTING:
                        mNFCDetectPresenter.cancelDetecting();
                        setResult(RESULT_CODE_CANCEL);
                        finish();
                        break;
                    case SUCCESS:
                        setResult(RESULT_CODE_SUCCESS);
                        finish();
                        break;
                    case FAIL:
                        setResult(RESULT_CODE_FAIL);
                        finish();
                        break;
                    default:
                        break;
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mNFCDetectPresenter.onActivityDestory(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mNFCDetectPresenter.onActivityResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // TODO set result here?
//        switch (mState) {
//            case DETECTING:
//                setResult(RESULT_CODE_CANCEL);
//                break;
//            case SUCCESS:
//                setResult(RESULT_CODE_SUCCESS);
//                break;
//            case FAIL:
//                setResult(RESULT_CODE_FAIL);
//                break;
//            default:
//                break;
//        }
        mNFCDetectPresenter.onActivityPause(this);
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.mNFCDetectPresenter.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.mNFCDetectPresenter.stop();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            mNFCDetectPresenter.onNewTagDetected(detectedTag);
        }
    }

    @Override
    public void onViewStateChange(State state) {
        mState = state;
        switch (state) {
            case DETECTING:
                mTitleTextview.setText(R.string.nfc_detecting_tag);
                break;
            case WRITING:
                mTitleTextview.setText(R.string.nfc_writing);
                mCmdButton.setEnabled(false);
                break;
            case SUCCESS:
                mTitleTextview.setText(R.string.nfc_write_success);
                mCmdButton.setEnabled(true);
                mCmdButton.setText(R.string.nfc_close_dialog);
                break;
            case FAIL:
                mTitleTextview.setText(R.string.nfc_write_fail);
                mCmdButton.setEnabled(true);
                mCmdButton.setText(R.string.nfc_close_dialog);
                break;
            case CANCEL:
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void showStateToast(String content) {
        Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
    }

    @Override
    public String getTargetContent() {
        return mTargetContent;
    }
}
