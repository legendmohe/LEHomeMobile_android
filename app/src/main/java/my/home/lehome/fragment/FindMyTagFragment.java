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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;
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

    private BeaconChooserAdapter mBeaconsArrayAdapter;
    private ArrayList<Beacon> mBeacons = new ArrayList<>();
    private AlertDialog mBeaconsDialog;

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
        mNameTextView.setText(getString(R.id.tag_finding_tag));
    }

    @Override
    public void onStop() {
        mFindMyTagPresenter.stop();
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

        mBeaconsArrayAdapter = new BeaconChooserAdapter(this.getActivity(), 0, mBeacons);
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

    public void onBeaconEnter(Beacon beacon) {
        final Beacon u = beacon;
        if (mHandler != null) {
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
    }

    public void onBeaconExit(Beacon beacon) {
        final Beacon u = beacon;
        if (mHandler != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mBeaconsArrayAdapter.remove(u);
                    mBeaconsArrayAdapter.notifyDataSetChanged();
                }
            });
        }
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
                        Beacon beacon = mBeaconsArrayAdapter.getItem(which);
                        mFindMyTagPresenter.setFliterUid(beacon.getBluetoothAddress());
                        cleanScreen();
                    }
                });
        mBeaconsArrayAdapter.clear();
        mBeaconsDialog = builderSingle.show();
    }

    @Override
    public void onBtEnable() {
        cleanScreen();
    }

    @Override
    public void onBtDisable() {
        cleanScreen();
        mUUIDTextView.setText(getString(R.string.find_my_tag_bt_off));
    }

    @Override
    public void onBtTurningOn() {
        mUUIDTextView.setText(getString(R.string.find_my_tag_bt_turning_on));
    }

    public void cleanScreen() {
        if (mDistanceTextView != null) {
            mDistanceTextView.setText("0.0");
            mNameTextView.setText("...");
            mPowerTextView.setText("");
            mUUIDTextView.setText("");
        }
    }

    class BeaconChooserAdapter extends ArrayAdapter<Beacon> {

        private class ViewHolder {
            private TextView beaconTextview;
        }

        public BeaconChooserAdapter(Context context, int textViewResourceId, ArrayList<Beacon> items) {
            super(context, textViewResourceId, items);
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(this.getContext())
                        .inflate(R.layout.beacon_item, parent, false);

                viewHolder = new ViewHolder();
                viewHolder.beaconTextview = (TextView) convertView.findViewById(R.id.beacon_textview);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            Beacon beacon = getItem(position);
            if (beacon != null) {
                viewHolder.beaconTextview.setText(String.format("%s(%s)", beacon.getBluetoothName(), beacon.getBluetoothAddress()));
            }

            return convertView;
        }
    }
}
