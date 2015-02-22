package my.home.lehome.activity;


import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;

import my.home.lehome.R;
import my.home.lehome.fragment.ChatFragment;
import my.home.lehome.fragment.NavigationDrawerFragment;
import my.home.lehome.fragment.ShortcutFragment;
import my.home.lehome.helper.MessageHelper;
import my.home.lehome.helper.NetworkHelper;
import my.home.lehome.util.PushUtils;


public class MainActivity extends FragmentActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    public static final String TAG = MainActivity.class.getName();

    public static boolean STOPPED = false;
    public static boolean VISIBLE = false;

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTitle;
    private int mCurrentSection;
    private boolean doubleBackToExitPressedOnce;

    private ChatFragment chatFragment;
    private ShortcutFragment shortcurFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.setupService();

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);


        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        Window wind = this.getWindow();
        wind.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        wind.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        wind.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

    }

    private void setupService() {
//    	DBHelper.initHelper(this);

        MessageHelper.loadPref(this);
//    	MessageHelper.setPushTag(getApplicationContext(), MessageHelper.DEVICE_ID);
        PushManager.startWork(getApplicationContext(),
                PushConstants.LOGIN_TYPE_API_KEY,
                PushUtils.getMetaValue(MainActivity.this, "api_key"));

        STOPPED = false;
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
        switch (position) {
            case 0:
                if (chatFragment == null) {
                    chatFragment = new ChatFragment();
                }
                fragment = chatFragment;
//			fragment = new PagerFragment();
                break;
            case 1:
                if (shortcurFragment == null) {
                    shortcurFragment = new ShortcutFragment();
                }
                fragment = shortcurFragment;
                break;

            default:
                break;
        }
        this.onSectionAttached(position);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
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
            case R.id.local_ip_item:
                String ipString = NetworkHelper.getIPAddress(true);
                Toast.makeText(this, getResources().getString(R.string.local_ip_item) + ":" + ipString, Toast.LENGTH_SHORT).show();
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
        return chatFragment;
    }

    public ShortcutFragment getShortcurFragment() {
        return shortcurFragment;
    }

}
