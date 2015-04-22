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
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import my.home.lehome.R;
import my.home.lehome.mvp.presenters.FindMyTagPresenter;
import my.home.lehome.mvp.views.FindMyTagDistanceView;


public class FindMyTagFragment extends Fragment implements FindMyTagDistanceView {
    public static final String TAG = "FindMyTagFragment";

    private FindMyTagPresenter mFindMyTagPresenter;
    private TextView mDistanceTextView;
    private TextView mNameTextView;
    private TextView mPowerTextView;
    private TextView mUUIDTextView;
    private Handler mHandler;

    private ArrayAdapter<String> mBeaconsArrayAdapter;
    private AlertDialog mBeaconsDialog;
    private String mFliterUid = "";

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
        setRetainInstance(true);
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        mBeaconsArrayAdapter.clear();
        mFindMyTagPresenter.start();
    }

    @Override
    public void onStop() {
        mFindMyTagPresenter.stop();
        mFindMyTagPresenter = null;
        mBeaconsArrayAdapter.clear();
        if (mBeaconsDialog != null && mBeaconsDialog.isShowing()) {
            mBeaconsDialog.dismiss();
        }
        super.onStop();
    }

    @Override
    public void setupViews(View rootView) {
        mDistanceTextView = (TextView) rootView.findViewById(R.id.distance_textview);
        mNameTextView = (TextView) rootView.findViewById(R.id.bdname_textview);
        mPowerTextView = (TextView) rootView.findViewById(R.id.power_textview);
        mUUIDTextView = (TextView) rootView.findViewById(R.id.uidd_textview);

        mBeaconsArrayAdapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.select_dialog_item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.refresh_tag_list:
                showBeaconsDialog();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Context getContext() {
        return getActivity();
    }

    @Override
    public Context getApplicationContext() {
        return getActivity().getApplicationContext();
    }

    public void onBeaconEnter(String uid) {
        final String u = uid;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mBeaconsArrayAdapter.getPosition(u) != -1) {
                    return;
                }
                mBeaconsArrayAdapter.add(u);
                mBeaconsArrayAdapter.notifyDataSetChanged();
            }
        });
    }

    public void onBeaconExit(String uid) {
        final String u = uid;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBeaconsArrayAdapter.remove(u);
                mBeaconsArrayAdapter.notifyDataSetChanged();
            }
        });
    }

//    public void onBeaconState(int var1, String uid) {
//
//    }

    @Override
    public void onBeaconDistance(String uid, String bdName, double distance, List<Long> data) {
        final double d = distance;
        final String n = bdName;
        final String u = uid;
        if (!isAdded())
            return;
        final String p = getString(R.string.find_my_tag_power_title) + ": " + data.get(0).toString();

        this.onBeaconEnter(uid);

        if (TextUtils.isEmpty(mFliterUid) || !uid.equals(mFliterUid))
            return;

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mDistanceTextView != null) {
                    mDistanceTextView.setText(String.format("%1.2f", d));
                    mNameTextView.setText(n);
                    mPowerTextView.setText(p);
                    mUUIDTextView.setText(u);
                }
            }
        });
    }

    public void showBeaconsDialog() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(
                this.getActivity());
        builderSingle.setTitle(getString(R.string.find_my_tag_select_beacon));
        builderSingle.setNegativeButton(getString(R.string.dialog_cancel),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builderSingle.setAdapter(mBeaconsArrayAdapter,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mFliterUid = mBeaconsArrayAdapter.getItem(which);
                        cleanScreen();
                    }
                });
        mBeaconsArrayAdapter.clear();
        mBeaconsDialog = builderSingle.show();
    }

    public void cleanScreen() {
        if (mDistanceTextView != null) {
            mDistanceTextView.setText("0.0");
            mNameTextView.setText("...");
            mPowerTextView.setText("");
            mUUIDTextView.setText("");
        }
    }
}
