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
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import my.home.lehome.R;
import my.home.lehome.mvp.presenters.FindMyTagPresenter;
import my.home.lehome.mvp.views.FindMyTagView;


public class FindMyTagFragment extends Fragment implements FindMyTagView {
    public static final String TAG = "FindMyTagFragment";

    private FindMyTagPresenter mFindMyTagPresenter;
    private TextView mDistanceTextView;
    private TextView mUidTextView;
    private Handler mHandler;

    public static FindMyTagFragment newInstance() {
        FindMyTagFragment fragment = new FindMyTagFragment();
        return fragment;
    }

    public FindMyTagFragment() {
        mFindMyTagPresenter = new FindMyTagPresenter(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
    }

    @Override
    public void onDestroy() {
        mHandler = null;
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_find_my_tag, container, false);
        setupViews(rootView);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        mFindMyTagPresenter.start();
    }

    @Override
    public void onStop() {
        mFindMyTagPresenter.stop();
        super.onStop();
    }

    @Override
    public void setupViews(View rootView) {
        mDistanceTextView = (TextView) rootView.findViewById(R.id.distance_textview);
        mUidTextView = (TextView) rootView.findViewById(R.id.uid_textview);
    }

    @Override
    public Context getContext() {
        return getActivity();
    }

    @Override
    public Context getApplicationContext() {
        return getActivity().getApplicationContext();
    }

    @Override
    public void onBeaconEnter(String uid) {

    }

    @Override
    public void onBeaconExit(String uid) {

    }

    @Override
    public void onBeaconState(int var1, String uid) {

    }

    @Override
    public void onBeaconDistance(String uid, double distance) {
        final double d = distance;
        final String u = uid;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mDistanceTextView != null) {
                    mDistanceTextView.setText(String.format("%1.2f", d));
                    mUidTextView.setText(u);
                }
            }
        });
    }
}
