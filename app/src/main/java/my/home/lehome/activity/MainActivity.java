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
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import my.home.common.PrefUtil;
import my.home.lehome.R;
import my.home.lehome.fragment.ChatFragment;
import my.home.lehome.fragment.FindMyTagFragment;
import my.home.lehome.fragment.HomeStateFragment;
import my.home.lehome.fragment.NavigationDrawerFragment;
import my.home.lehome.fragment.ShortcutFragment;
import my.home.lehome.mvp.presenters.MainActivityPresenter;
import my.home.lehome.mvp.views.ActionBarControlView;
import my.home.lehome.mvp.views.MainActivityView;
import my.home.lehome.util.Constants;


public class MainActivity extends FragmentActivity
        implements
        NavigationDrawerFragment.NavigationDrawerCallbacks,
        ActionBarControlView,
        MainActivityView {

    public static final String TAG = "MainActivity";

    public static boolean STOPPED = false;
    public static boolean VISIBLE = false;
    private final String CHAT_FRAGMENT_TAG = "CHAT_FRAGMENT_TAG";
    private final String SHORTCUT_FRAGMENT_TAG = "SHORTCUT_FRAGMENT_TAG";
    private final String FIND_TAG_FRAGMENT_TAG = "FIND_TAG_FRAGMENT_TAG";
    private final String HOME_STATE_FRAGMENT_TAG = "HOME_STATE_FRAGMENT_TAG";

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private ActionBar mActionBar;
    private int mActionBarHeight;

    private int mCurrentSection;
    private boolean doubleBackToExitPressedOnce;

    private MainActivityPresenter mMainActivityPresenter;
    private boolean mInVolumeDown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() != null && getIntent().getAction() == WakeupActivity.INTENT_VOICE_COMMAND) {
            Window wind = this.getWindow();
            wind.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            wind.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            wind.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
        this.setupViews(null);
        STOPPED = false;

        mMainActivityPresenter = new MainActivityPresenter(this);
        mMainActivityPresenter.start();
        mMainActivityPresenter.onActivityCreate();
    }

    @Override
    public void setupViews(View rootView) {
        final TypedArray styledAttributes = getTheme().obtainStyledAttributes(
                new int[]{android.R.attr.actionBarSize});
        mActionBarHeight = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();
        mActionBar = getActionBar();

        this.setContentView(R.layout.activity_main);
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }


    @Override
    protected void onDestroy() {
        mMainActivityPresenter.stop();
        mMainActivityPresenter.onActivityDestory();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MainActivity.VISIBLE = true;
        if (mMainActivityPresenter != null)
            mMainActivityPresenter.onActivityResume();

        if (getIntent().getAction() == WakeupActivity.INTENT_VOICE_COMMAND) {
            Window wind = this.getWindow();
            wind.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            wind.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            wind.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

            if (mCurrentSection != 0) {
                onNavigationDrawerItemSelected(0);
            }
            if (!getChatFragment().isRecognizing()) {
                Log.d(TAG, "get intent, startRecognize.");
                Message msg = ChatFragment.PublicHandler
                        .obtainMessage(ChatFragment.MSG_TYPE_VOICE_CMD);
                ChatFragment.PublicHandler.sendMessageDelayed(msg, 500);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        MainActivity.VISIBLE = false;
    }

    @Override
    protected void onStart() {
        mMainActivityPresenter.onActivityStart();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mMainActivityPresenter.onActivityStop();
        super.onStop();
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
                break;
            case 1:
                ShortcutFragment shortcutFragment = (ShortcutFragment) fm.findFragmentByTag(SHORTCUT_FRAGMENT_TAG);
                if (shortcutFragment == null) {
                    shortcutFragment = new ShortcutFragment();
                }
                fragment_tag = SHORTCUT_FRAGMENT_TAG;
                fragment = shortcutFragment;
                break;
            case 2:
                FindMyTagFragment findMyTagFragment = (FindMyTagFragment) fm.findFragmentByTag(FIND_TAG_FRAGMENT_TAG);
                if (findMyTagFragment == null) {
                    findMyTagFragment = FindMyTagFragment.newInstance();
                }
                fragment_tag = FIND_TAG_FRAGMENT_TAG;
                fragment = findMyTagFragment;
                break;
            case 3:
                HomeStateFragment homeStateFragment = (HomeStateFragment) fm.findFragmentByTag(HOME_STATE_FRAGMENT_TAG);
                if (homeStateFragment == null) {
                    homeStateFragment = HomeStateFragment.newInstance();
                }
                fragment_tag = HOME_STATE_FRAGMENT_TAG;
                fragment = homeStateFragment;
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
                case 2:
                    getMenuInflater().inflate(R.menu.find_my_tag, menu);
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
                startActivityForResult(intent, Constants.SETTINGS_ACTIVITY_RESULT_CODE);
                // The if returned true the click event will be consumed by the
                // onOptionsItemSelect() call and won't fall through to other item
                // click functions. If your return false it may check the ID of
                // the event in other item selection functions.
                return true;
            case R.id.action_exit:
                if (!mMainActivityPresenter.onAppExit())
                    Toast.makeText(this, getResources().getString(R.string.error_stop_local_msg_service), Toast.LENGTH_SHORT).show();
                STOPPED = true;
                this.finish();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.SETTINGS_ACTIVITY_RESULT_CODE && data != null) {
            if (!mMainActivityPresenter.onSettingsActivityResult(resultCode, data))
                Toast.makeText(this, getResources().getString(R.string.error_local_msg_service), Toast.LENGTH_SHORT).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(getApplicationContext(), getResources().getString(R.string.double_back_to_quit), Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(final int keycode, final KeyEvent e) {
        switch (keycode) {
            case KeyEvent.KEYCODE_MENU:
                if (mCurrentSection == 0 && !mNavigationDrawerFragment.isDrawerOpen()) {
                    getChatFragment().switchInputMode();
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (mCurrentSection != 0 || mNavigationDrawerFragment.isDrawerOpen()) {
                    break;
                }

                if (!PrefUtil.getbooleanValue(this, "pref_volume_key_control_speech", true)) {
                    break;
                }
                if (mInVolumeDown) {
                    mInVolumeDown = false;
                    ChatFragment chatFragment = getChatFragment();
                    if (chatFragment != null && !chatFragment.isRecognizing()) {
                        getChatFragment().startRecognize();
                    }
                    return true;
                } else if (!mInVolumeDown) {
                    mInVolumeDown = true;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mInVolumeDown) {
                                mInVolumeDown = false;
                            }
                        }
                    }, Constants.VOLUME_KEY_DOWN_DELAY);
                    return true;
                }
        }

        return super.onKeyDown(keycode, e);
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

    // --------------------------- Local Message ----------------------------


    @Override
    public void setActionBarTitle(String title) {
        if (TextUtils.isEmpty(title))
            return;
        if (this.getActionBar() != null)
            this.getActionBar().setTitle(title);
    }

    public boolean shouldUseLocalMsg() {
        return mMainActivityPresenter.shouldUseLocalMsg();
    }

//    /**
//     * Handler of incoming messages from service.
//     */
//    class IncomingHandler extends Handler {
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case LocalMessageService.MSG_SERVER_RECEIVE_MSG:
//                    Log.d(TAG, "Received from service: " + msg.obj);
//                    break;
//                default:
//                    super.handleMessage(msg);
//            }
//        }
//    }
}
