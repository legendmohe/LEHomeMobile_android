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


import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import my.home.common.util.PrefUtil;
import my.home.lehome.R;
import my.home.lehome.asynctask.LoadProfileHeaderBgAsyncTask;
import my.home.lehome.fragment.ChatFragment;
import my.home.lehome.fragment.FindMyTagFragment;
import my.home.lehome.fragment.GeoFencingFragment;
import my.home.lehome.fragment.HomeStateFragment;
import my.home.lehome.fragment.MessageFragment;
import my.home.lehome.fragment.ShortcutFragment;
import my.home.lehome.mvp.presenters.MainActivityPresenter;
import my.home.lehome.mvp.views.ActionBarControlView;
import my.home.lehome.mvp.views.MainActivityView;
import my.home.lehome.service.LocalMessageService;
import my.home.lehome.util.Constants;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        ActionBarControlView,
        MainActivityView {

    public static final String TAG = "MainActivity";
    public static final String EXTRA_IMAGE_INTENTS = "EXTRA_IMAGE_INTENTS";

    public static boolean STOPPED = false;
    public static boolean VISIBLE = false;
    private final String CHAT_FRAGMENT_TAG = "CHAT_FRAGMENT_TAG";
    private final String SHORTCUT_FRAGMENT_TAG = "SHORTCUT_FRAGMENT_TAG";
    private final String FIND_TAG_FRAGMENT_TAG = "FIND_TAG_FRAGMENT_TAG";
    private final String HOME_STATE_FRAGMENT_TAG = "HOME_STATE_FRAGMENT_TAG";
    private final String GEO_FENCING_FRAGMENT_TAG = "GEO_FENCING_FRAGMENT_TAG";
    private final String MESSAGE_FRAGMENT_TAG = "MESSAGE_FRAGMENT_TAG";

    private static final String PREF_KEY_LAST_OPEN_FRAGMENT_INDEX = "PREF_KEY_LAST_OPEN_FRAGMENT_INDEX";

    private ActionBar mActionBar;
    private int mActionBarHeight;

    private int mCurrentNavindex = -1;
    private boolean doubleBackToExitPressedOnce;

    private MainActivityPresenter mMainActivityPresenter;
    private boolean mInVolumeDown = false;
    private DrawerLayout mDrawer;
    private NavigationView mNavigationView;
    private Uri mSelectedNavHeaderImageUri;
    private int SELECT_PICTURE_REQUEST_CODE = 111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() != null && getIntent().getAction() != null) {
            String action = getIntent().getAction();
            if (action.equals(WakeupActivity.INTENT_VOICE_COMMAND)) {
                Window wind = this.getWindow();
                wind.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
                wind.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
                wind.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
            } else if (action.equals(LocalMessageService.LOCAL_MESSAGE_SERVICE_NOTIFICATION_ACTION)) {
                PrefUtil.setIntValue(this, PREF_KEY_LAST_OPEN_FRAGMENT_INDEX, 0);
            }
        }
        this.setupViews(null);
        STOPPED = false;

        if (savedInstanceState != null) {
            if (savedInstanceState.getParcelable(MainActivity.EXTRA_IMAGE_INTENTS) != null) {
                mSelectedNavHeaderImageUri = savedInstanceState.getParcelable(MainActivity.EXTRA_IMAGE_INTENTS);
            }
        }

        mMainActivityPresenter = new MainActivityPresenter(this);
        mMainActivityPresenter.start();
        mMainActivityPresenter.onActivityCreate(this);
    }

    @Override
    public void setupViews(View rootView) {
        final TypedArray styledAttributes = getTheme().obtainStyledAttributes(
                new int[]{android.R.attr.actionBarSize});
        mActionBarHeight = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();

        this.setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mActionBar = getSupportActionBar();

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                InputMethodManager inputManager =
                        (InputMethodManager) MainActivity.this.
                                getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(
                        MainActivity.this.getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);

                resetNavProfileName();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        mDrawer.setDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        mNavigationView.findViewById(R.id.nav_profile_headerview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImageIntent();
            }
        });

        TextView detailTextView = (TextView) mNavigationView.findViewById(R.id.nav_profile_detail_textview);
        detailTextView.setText(this.getString(R.string.title_remote_msg_mode));


        String profileImagePath = PrefUtil.getStringValue(MainActivity.this, LoadProfileHeaderBgAsyncTask.PREF_KEY_PROFILE_IMAGE, null);
        if (profileImagePath != null) {
            Uri uri = Uri.parse(profileImagePath);
            ImageView iconImageView = (ImageView) mNavigationView.findViewById(R.id.nav_profile_icon);
            ProgressBar progressBar = (ProgressBar) findViewById(R.id.load_profile_icon_progressBar);
            new LoadProfileHeaderBgAsyncTask(MainActivity.this, iconImageView, progressBar).execute(uri);
        }

        selectNavFragment(PrefUtil.getIntValue(this, PREF_KEY_LAST_OPEN_FRAGMENT_INDEX, 0));
    }

    private void resetNavProfileName() {
        TextView nameTextView = (TextView) mNavigationView.findViewById(R.id.nav_profile_name_textview);
        String myName = PrefUtil.getStringValue(this, "pref_client_id", "");
        nameTextView.setText(myName);
    }


    @Override
    protected void onDestroy() {
        mMainActivityPresenter.stop();
        mMainActivityPresenter.onActivityDestory(this);
        // recycle navigator icon
        ImageView iconImageView = (ImageView) mNavigationView.findViewById(R.id.nav_profile_icon);
        Drawable drawable = iconImageView.getDrawable();
        if (drawable != null && drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            if (bitmap != null)
                bitmap.recycle();
            iconImageView.setImageBitmap(null);
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MainActivity.VISIBLE = true;
        if (mMainActivityPresenter != null)
            mMainActivityPresenter.onActivityResume(this);

        if (getIntent() != null && getIntent().getAction().equals(WakeupActivity.INTENT_VOICE_COMMAND)) {
            Window wind = this.getWindow();
            wind.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            wind.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            wind.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

            if (mCurrentNavindex != 0) {
                selectNavFragment(0);
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
        mMainActivityPresenter.onActivityStart(this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        mMainActivityPresenter.onActivityStop(this);
        super.onStop();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (TextUtils.isEmpty(intent.getAction())) {
            Log.d(TAG, "null action intent:" + intent);
            return;
        }
        Log.d(TAG, "onNewIntent: " + intent.getAction());
        setIntent(intent);
        if (intent.getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
            mMainActivityPresenter.handleNfcNdefIntent(intent);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(MainActivity.EXTRA_IMAGE_INTENTS, mSelectedNavHeaderImageUri);
        super.onSaveInstanceState(outState);
    }

    public void onFragmentAttached(int id) {
        mCurrentNavindex = id;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        switch (mCurrentNavindex) {
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
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == Constants.SETTINGS_ACTIVITY_RESULT_CODE && data != null) {
                if (!mMainActivityPresenter.onSettingsActivityResult(resultCode, data))
                    Toast.makeText(this, getResources().getString(R.string.error_local_msg_service), Toast.LENGTH_SHORT).show();
                resetNavProfileName();
            } else if (requestCode == SELECT_PICTURE_REQUEST_CODE) {
                mMainActivityPresenter.onNavHeaderChooserActivityResult(data, mSelectedNavHeaderImageUri);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.double_back_to_quit), Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(final int keycode, final KeyEvent e) {
        switch (keycode) {
            case KeyEvent.KEYCODE_MENU:
                if (mCurrentNavindex == 0 && !mDrawer.isDrawerOpen(GravityCompat.START)) {
                    getChatFragment().switchInputMode();
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (mCurrentNavindex != 0 || mDrawer.isDrawerOpen(GravityCompat.START)) {
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
    public void showServerStateIndicator(boolean isLocal) {
        TextView detailTextView = (TextView) mNavigationView.findViewById(R.id.nav_profile_detail_textview);
        if (isLocal) {
            detailTextView.setText(this.getString(R.string.title_local_msg_mode));
        } else {
            detailTextView.setText(this.getString(R.string.title_remote_msg_mode));
        }
    }

    public boolean shouldUseLocalMsg() {
        return mMainActivityPresenter.shouldUseLocalMsg();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.nav_settings) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, SettingsActivity.class);
            startActivityForResult(intent, Constants.SETTINGS_ACTIVITY_RESULT_CODE);
            return true;
        } else if (id == R.id.nav_exit) {
            if (!mMainActivityPresenter.onAppExit())
                Toast.makeText(this, getResources().getString(R.string.error_stop_local_msg_service), Toast.LENGTH_SHORT).show();
            STOPPED = true;
            this.finish();
            return true;
        } else {
            switch (id) {
                case R.id.nav_control:
                    selectNavFragment(0);
                    break;
                case R.id.nav_favour:
                    selectNavFragment(1);
                    break;
                case R.id.nav_locator:
                    selectNavFragment(2);
                    break;
                case R.id.nav_camera:
                    selectNavFragment(3);
                    break;
                case R.id.nav_message:
                    selectNavFragment(4);
                    break;
                case R.id.nav_geo_fencing:
                    selectNavFragment(5);
                    break;
                default:
                    break;
            }
            mDrawer.closeDrawer(GravityCompat.START);
            return true;
        }
    }

    private void selectNavFragment(int index) {
        if (mCurrentNavindex == index)
            return;
        mCurrentNavindex = index;

        Fragment fragment = null;
        FragmentManager fm = getSupportFragmentManager();
        String fragment_tag = null;
        int titleId = -1;

        if (index == 0) {
            ChatFragment chatFragment = (ChatFragment) fm.findFragmentByTag(CHAT_FRAGMENT_TAG);
            if (chatFragment == null) {
                chatFragment = new ChatFragment();
            }
            fragment_tag = CHAT_FRAGMENT_TAG;
            fragment = chatFragment;
            titleId = R.string.title_section1;

            if (!fragment.isAdded()) {
                Bundle bundle = new Bundle();
                bundle.putBoolean(ChatFragment.BUNDLE_KEY_SCROLL_TO_BOTTOM, true);
                fragment.setArguments(bundle);
            } else {
                fragment.getArguments().putBoolean(ChatFragment.BUNDLE_KEY_SCROLL_TO_BOTTOM, true);
            }
        } else if (index == 1) {
            ShortcutFragment shortcutFragment = (ShortcutFragment) fm.findFragmentByTag(SHORTCUT_FRAGMENT_TAG);
            if (shortcutFragment == null) {
                shortcutFragment = new ShortcutFragment();
            }
            fragment_tag = SHORTCUT_FRAGMENT_TAG;
            fragment = shortcutFragment;
            titleId = R.string.title_section2;
        } else if (index == 2) {
            FindMyTagFragment findMyTagFragment = (FindMyTagFragment) fm.findFragmentByTag(FIND_TAG_FRAGMENT_TAG);
            if (findMyTagFragment == null) {
                findMyTagFragment = FindMyTagFragment.newInstance();
            }
            fragment_tag = FIND_TAG_FRAGMENT_TAG;
            fragment = findMyTagFragment;
            titleId = R.string.title_section3;
        } else if (index == 3) {
            HomeStateFragment homeStateFragment = (HomeStateFragment) fm.findFragmentByTag(HOME_STATE_FRAGMENT_TAG);
            if (homeStateFragment == null) {
                homeStateFragment = HomeStateFragment.newInstance();
            }
            fragment_tag = HOME_STATE_FRAGMENT_TAG;
            fragment = homeStateFragment;
            titleId = R.string.title_section4;
        } else if (index == 4) {
            MessageFragment messageFragment = (MessageFragment) fm.findFragmentByTag(MESSAGE_FRAGMENT_TAG);
            if (messageFragment == null) {
                messageFragment = MessageFragment.newInstance();
            }
            fragment_tag = MESSAGE_FRAGMENT_TAG;
            fragment = messageFragment;
            titleId = R.string.title_section5;
        } else if (index == 5) {
            GeoFencingFragment geoFencingFragment = (GeoFencingFragment) fm.findFragmentByTag(GEO_FENCING_FRAGMENT_TAG);
            if (geoFencingFragment == null) {
                geoFencingFragment = GeoFencingFragment.newInstance();
            }
            fragment_tag = GEO_FENCING_FRAGMENT_TAG;
            fragment = geoFencingFragment;
            titleId = R.string.title_section6;
        }

        this.onFragmentAttached(index);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
//                .setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
                .replace(R.id.container, fragment, fragment_tag)
                .commit();

        PrefUtil.setIntValue(this, PREF_KEY_LAST_OPEN_FRAGMENT_INDEX, index);
        mActionBar.setTitle(titleId);
    }

    private void openImageIntent() {
        Intent chooserIntent = mMainActivityPresenter.prepareNavHeaderImageChooserIntent();
        mSelectedNavHeaderImageUri = chooserIntent.getParcelableExtra(MainActivity.EXTRA_IMAGE_INTENTS);
        startActivityForResult(chooserIntent, SELECT_PICTURE_REQUEST_CODE);
    }

    @Override
    public void changeNavHeaderBgImage(Uri selectedImageUri) {
        if (selectedImageUri != null) {
            ImageView iconImageView = (ImageView) mNavigationView.findViewById(R.id.nav_profile_icon);
            ProgressBar progressBar = (ProgressBar) findViewById(R.id.load_profile_icon_progressBar);
            new LoadProfileHeaderBgAsyncTask(this, iconImageView, progressBar).execute(selectedImageUri);
        }
    }
}
