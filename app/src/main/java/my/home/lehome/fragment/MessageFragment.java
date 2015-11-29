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

package my.home.lehome.fragment;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import my.home.lehome.R;
import my.home.lehome.mvp.presenters.SendMessagePresenter;
import my.home.lehome.mvp.views.SendMessageView;
import my.home.lehome.util.Constants;

public class MessageFragment extends Fragment implements SendMessageView {
    public static final String TAG = "MessageFragment";

    SendMessagePresenter mSendMessagePresenter;

    private int mScreenWidth;
    private int mScreenHeight;
    private ListView mMessagesListView;
    private Button mSendButton;

    public MessageFragment() {
    }

    public static MessageFragment newInstance() {
        MessageFragment fragment = new MessageFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mScreenWidth = size.x;
        mScreenHeight = size.y;

        View contentView = inflater.inflate(R.layout.fragment_send_message, container, false);
        setupViews(contentView);
        return contentView;
    }

    private void setupData() {
        mSendMessagePresenter = new SendMessagePresenter(this);
    }

    @Override
    public void setupViews(View rootView) {
        mSendButton = (Button) rootView.findViewById(R.id.send_message_btn);
        mSendButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (event.getRawY() / mScreenHeight <= Constants.DIALOG_CANCEL_Y_PERSENT) {
                    } else {
                    }
                    return true;
                }
                return false;
            }
        });
        mMessagesListView = (ListView) rootView.findViewById(R.id.message_listview);
    }

    @Override
    public Context getContext() {
        return getActivity();
    }
}
