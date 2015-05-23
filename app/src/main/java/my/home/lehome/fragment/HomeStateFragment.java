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


import android.app.AlertDialog;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import my.home.lehome.R;


public class HomeStateFragment extends Fragment {

    public static final String TAG = "HomeStateFragment";

    private TextView mSpeedTextView;

    private int mUid = -1;

    private long mStartRX;
    private long mStartTX;
    private boolean mStopped = false;
    private Handler mHandler = new Handler();

    public static HomeStateFragment newInstance() {
        HomeStateFragment fragment = new HomeStateFragment();
        return fragment;
    }

    public HomeStateFragment() {
        // Required empty public constructor
    }

    private WebView mWebView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
        mStartRX = TrafficStats.getUidRxBytes(mUid);
        mStartTX = TrafficStats.getUidTxBytes(mUid);
        if (mStartRX == TrafficStats.UNSUPPORTED || mStartTX == TrafficStats.UNSUPPORTED) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
            alert.setTitle("Uh Oh!");
            alert.setMessage("Your device does not support traffic stat monitoring.");
            alert.show();
            return;
        }
        mUid = android.os.Process.myUid();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_home_state, container, false);
        mWebView = (WebView) rootView.findViewById(R.id.camera_stream_webview);
        mWebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setBuiltInZoomControls(true);

        mSpeedTextView = (TextView) rootView.findViewById(R.id.speed_textView);

        return rootView;
    }

    @Override
    public void onStart() {
        mStopped = false;
        mHandler.postDelayed(mRunnable, 1000);
        mWebView.loadUrl("http://192.168.1.112:8080/stream_simple.html");
        super.onStart();
    }

    @Override
    public void onStop() {
        mStopped = true;
        mHandler.removeCallbacks(mRunnable);
        mWebView.loadUrl("about:blank");
        super.onStop();
    }

    private final Runnable mRunnable = new Runnable() {

        public void run() {
            if (mStopped)
                return;
            long rxBytes = TrafficStats.getUidRxBytes(mUid) - mStartRX;
            long txBytes = TrafficStats.getUidTxBytes(mUid) - mStartTX;
            Log.d(TAG, "rx:" + (rxBytes >= 0 ? rxBytes >> 10 : 0) + "KB"
                    + " tx:" + (txBytes >= 0 ? txBytes >> 10 : 0) + "KB");
            String showText = "总流量: " + (rxBytes >= 0 ? rxBytes >> 10 : 0) + "KB";
            mSpeedTextView.setText(showText);
            mHandler.postDelayed(mRunnable, 1000);
        }

    };
}
