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


import android.app.ActionBar;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;

import my.home.lehome.R;
import my.home.lehome.fragment.ChatFragment;
import my.home.lehome.fragment.NavigationDrawerFragment;
import my.home.lehome.fragment.ShortcutFragment;
import my.home.lehome.helper.MessageHelper;
import my.home.lehome.mvp.views.ActionBarControlView;
import my.home.lehome.service.aidl.LocalMessageServiceAidlInterface;
import my.home.lehome.util.PushUtils;


public class MainActivity extends FragmentActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, ActionBarControlView {

    public static final String TAG = MainActivity.class.getName();

    public static boolean STOPPED = false;
    public static boolean VISIBLE = false;
    private final String CHAT_FRAGMENT_TAG = "CHAT_FRAGMENT_TAG";
    private final String SHORTCUT_FRAGMENT_TAG = "SHORTCUT_FRAGMENT_TAG";

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTitle;
    private int mCurrentSection;
    private boolean doubleBackToExitPressedOnce;

    //    private ChatFragment mChatFragment;
//    private ShortcutFragment mShortcurFragment;
    private ActionBar mActionBar;
    private int mActionBarHeight;

    private LocalMessageServiceAidlInterface mLocalMsgService;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mLocalMsgService = LocalMessageServiceAidlInterface.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mLocalMsgService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() != null && getIntent().getAction() == WakeupActivity.INTENT_VOICE_COMMAND) {
            Window wind = this.getWindow();
            wind.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            wind.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            wind.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
//        wind.requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        final TypedArray styledAttributes = getTheme().obtainStyledAttributes(
                new int[]{android.R.attr.actionBarSize});
        mActionBarHeight = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();
        mActionBar = getActionBar();

        this.setContentView(R.layout.activity_main);
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();
        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        this.setupService();
        STOPPED = false;
    }

    @Override
    public void setupViews(View rootView) {

    }

    private void setupService() {
        MessageHelper.loadPref(this);
        PushManager.startWork(getApplicationContext(),
                PushConstants.LOGIN_TYPE_API_KEY,
                PushUtils.getMetaValue(MainActivity.this, "api_key"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    ;

    @Override
    protected void onResume() {
        super.onResume();
        MainActivity.VISIBLE = true;

        if (getIntent().getAction() == WakeupActivity.INTENT_VOICE_COMMAND) {
            Window wind = this.getWindow();
            wind.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            wind.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            wind.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

            if (mCurrentSection != 0) {
                onNavigationDrawerItemSelected(0);
            }
            if (!getChatFragment().inRecogintion) {
                Log.d(TAG, "get intent, startRecognize.");
                Message msg = ChatFragment.mHandler
                        .obtainMessage(ChatFragment.MSG_TYPE_VOICE_CMD);
                ChatFragment.mHandler.sendMessageDelayed(msg, 500);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        MainActivity.VISIBLE = false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // TODO Auto-generated method stub
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent: " + intent.getAction());
        setIntent(intent);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        Fragment fragment = null;
        FragmentManager fm = getSupportFragmentManager();
        String fragment_tag = null;
        switch (position) {
            case 0:
                ChatFragment chatFragment = (ChatFragment) fm.findFragmentByTag(CHAT_FRAGMENT_TAG);
                if (chatFragment == null) {
                    chatFragment = new ChatFragment();
                }
                fragment_tag = CHAT_FRAGMENT_TAG;
                fragment = chatFragment;
//			fragment = new PagerFragment();
                break;
            case 1:
                ShortcutFragment shortcutFragment = (ShortcutFragment) fm.findFragmentByTag(SHORTCUT_FRAGMENT_TAG);
                if (shortcutFragment == null) {
                    shortcutFragment = new ShortcutFragment();
                }
                fragment_tag = SHORTCUT_FRAGMENT_TAG;
                fragment = shortcutFragment;
                break;

            default:
                break;
        }
        this.onSectionAttached(position);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment, fragment_tag)
                .commit();
    }

    @Override
    public void onDrawerOpened(View drawerView) {
        InputMethodManager inputManager =
                (InputMethodManager) this.
                        getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(
                this.getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
//        showActionBar();
    }

    @Override
    public void onDrawerClosed(View drawerView) {

    }

    public void onSectionAttached(int number) {
        mCurrentSection = number;
        switch (number) {
            case 0:
                mTitle = getString(R.string.title_section1);
                break;
            case 1:
                mTitle = getString(R.string.title_section2);
                break;
            case 2:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
//        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            switch (mCurrentSection) {
                case 0:
                    getMenuInflater().inflate(R.menu.main, menu);
                    break;
                case 1:
                    getMenuInflater().inflate(R.menu.shortcut, menu);
                    break;
                default:
                    break;
            }

            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, SettingsActivity.class);
                startActivityForResult(intent, 0);
                // The if returned true the click event will be consumed by the
                // onOptionsItemSelect() call and won't fall through to other item
                // click functions. If your return false it may check the ID of
                // the event in other item selection functions.
                return true;
            case R.id.action_exit:
                this.finish();
                PushManager.stopWork(getApplicationContext());
                STOPPED = true;
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String old_device_id = MessageHelper.DEVICE_ID;
        MessageHelper.loadPref(this);
        if (!old_device_id.equals(MessageHelper.DEVICE_ID)) {
            MessageHelper.delPushTag(getApplicationContext(), old_device_id);
            MessageHelper.setPushTag(getApplicationContext(), MessageHelper.DEVICE_ID);
        }
//    	MessageHelper.sendServerMsgToList(getResources().getString(R.string.pref_sub_address) + ":" + ConnectionService.SUBSCRIBE_ADDRESS);
//    	MessageHelper.sendServerMsgToList(getResources().getString(R.string.pref_pub_address) + ":" + ConnectionService.PUBLISH_ADDRESS);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getResources().getString(R.string.double_back_to_quit), Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    public ChatFragment getChatFragment() {
        FragmentManager fm = getSupportFragmentManager();
        return (ChatFragment) fm.findFragmentByTag(CHAT_FRAGMENT_TAG);
    }

    public ShortcutFragment getShortcurFragment() {
        FragmentManager fm = getSupportFragmentManager();
        return (ShortcutFragment) fm.findFragmentByTag(SHORTCUT_FRAGMENT_TAG);
    }

    @Override
    public void showActionBar() {
        if (!mActionBar.isShowing()) {
            mActionBar.show();
        }
    }

    @Override
    public void hideActionBar() {
        if (mActionBar.isShowing()) {
            mActionBar.hide();
        }
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public int getActionBarHeight() {
        return mActionBarHeight;
    }
}
