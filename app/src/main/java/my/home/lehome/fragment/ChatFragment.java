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

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.doomonafireball.betterpickers.calendardatepicker.CalendarDatePickerDialog;
import com.doomonafireball.betterpickers.radialtimepicker.RadialTimePickerDialog;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.List;

import my.home.common.ComUtil;
import my.home.common.UIUtil;
import my.home.lehome.R;
import my.home.lehome.activity.MainActivity;
import my.home.lehome.adapter.AutoCompleteAdapter;
import my.home.lehome.adapter.ChatItemArrayAdapter;
import my.home.lehome.adapter.ChatItemArrayAdapter.ResendButtonClickListener;
import my.home.lehome.adapter.ShortcutArrayAdapter;
import my.home.lehome.asynctask.LoadMoreChatItemAsyncTask;
import my.home.lehome.helper.MessageHelper;
import my.home.lehome.mvp.presenters.ChatFragmentPresenter;
import my.home.lehome.mvp.views.ChatItemListView;
import my.home.lehome.mvp.views.ChatSuggestionView;
import my.home.lehome.mvp.views.SaveLocalHistoryView;
import my.home.lehome.util.CommonUtils;
import my.home.lehome.util.Constants;
import my.home.lehome.view.DelayAutoCompleteTextView;
import my.home.lehome.view.OnSwipeTouchListener;
import my.home.lehome.view.PhotoViewerDialog;
import my.home.lehome.view.SimpleAnimationListener;
import my.home.lehome.view.SpeechDialog;
import my.home.model.entities.AutoCompleteItem;
import my.home.model.entities.AutoCompleteToolItem;
import my.home.model.entities.ChatItem;
import my.home.model.entities.Shortcut;
import my.home.model.manager.DBStaticManager;

public class ChatFragment extends Fragment implements SpeechDialog.SpeechDialogListener
        , ResendButtonClickListener
        , ChatItemArrayAdapter.ImageClickListener
        , AutoCompleteAdapter.onLoadConfListener
        , SaveLocalHistoryView
        , ChatItemListView
        , ChatSuggestionView
        , CalendarDatePickerDialog.OnDateSetListener
        , RadialTimePickerDialog.OnTimeSetListener {

    public static final String TAG = ChatFragment.class.getName();
    public static final String BUNDLE_KEY_SCROLL_TO_BOTTOM = "BUNDLE_KEY_SCROLL_TO_BOTTOM";
    private static final String FRAG_TAG_TIME_PICKER = "timePickerDialogFragment";
    private static final String FRAG_TAG_DATE_PICKER = "calendarDatePickerDialog";

    /*
     * common UI
     */
    private ChatItemArrayAdapter mAdapter;
    private Button mSwitchButton;
    private Button mSuggestionButton;
    private Toast mToast;
    private OnGlobalLayoutListener mKeyboardListener;
    private ListView mCmdListview;
    private RelativeLayout mSendCmdLayout;
    //    private WeakReference<ActionBarControlView> mActionBarControlViewRf;
    private DelayAutoCompleteTextView mSendCmdEdittext;
    private ChatFragmentPresenter mChatFragmentPresenter;

    /*
     * common variables
     */
    public static Handler PublicHandler;
    private int mNewMsgNum = 0;
    private boolean mNeedShowUnread = false;
    private boolean mScrollViewInButtom = false;
    private boolean mKeyboard_open = false;
    private boolean mInSpeechMode = false;

    /*
     * history
     */
    private AutoCompleteAdapter mAutoCompleteAdapter;

    /*
     * speech
     */
    SpeechDialog mSpeechDialog;
    private boolean scriptInputMode;
    private boolean inRecogintion = false;
    private int mScreenWidth = 0;
    private int mScreenHeight = 0;

    /*
     * photo
     */
    private PhotoViewerDialog mPhotoDialog = null;

    /*
     * constant
     */
    public static final int MSG_TYPE_CHATITEM = 1;
    public static final int MSG_TYPE_TOAST = 2;
    public static final int MSG_TYPE_VOICE_CMD = 3;

    @SuppressLint("HandlerLeak")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (mAdapter == null) {
            mAdapter = new ChatItemArrayAdapter(this.getActivity(), R.layout.chat_item_client);
            mAdapter.setResendButtonClickListener(this);
            mAdapter.setImageClickListener(this);
        }
        PublicHandler = new MyHandler(this);
        mChatFragmentPresenter = new ChatFragmentPresenter(this, this, this);
        mChatFragmentPresenter.start();

        mPhotoDialog = new PhotoViewerDialog(getActivity());
    }

    public boolean isScrollViewInButtom() {
        return mScrollViewInButtom;
    }

    public void setScrollViewInButtom(boolean mScrollViewInButtom) {
        this.mScrollViewInButtom = mScrollViewInButtom;
    }

    public int getNewMsgNum() {
        return mNewMsgNum;
    }

    public void setNewMsgNum(int mNewMsgNum) {
        this.mNewMsgNum = mNewMsgNum;
    }

    public boolean isNeedShowUnread() {
        return mNeedShowUnread;
    }

    public void setNeedShowUnread(boolean mNeedShowUnread) {
        this.mNeedShowUnread = mNeedShowUnread;
    }

    @Override
    public void onImageViewClicked(Bundle bundle) {
        if (mPhotoDialog != null) {
            String imageURL = bundle.getString("imageURL");
            String extraTitle = bundle.getString("extraTitle");
            Intent intent = bundle.getParcelable("extraIntent");
            mPhotoDialog.setTarget(imageURL, intent, extraTitle);
            mPhotoDialog.show();
        }
    }

    @Override
    public void onImageViewLongClicked(String imageURL) {
    }

    private static class MyHandler extends Handler {
        private final WeakReference<ChatFragment> mFragment;
        private Runnable mToastRunnable = new Runnable() {
            @Override
            public void run() {
                ChatFragment fragment = mFragment.get();
                if (!fragment.isScrollViewInButtom()) {
                    int newNum = fragment.getNewMsgNum();
                    fragment.setNewMsgNum(++newNum);
                    fragment.showTip(newNum + " new message");
                    fragment.setNeedShowUnread(true);
                }
            }
        };

        public MyHandler(ChatFragment fragment) {
            mFragment = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            final ChatFragment fragment = mFragment.get();
            if (fragment != null) {
                switch (msg.what) {
                    case MSG_TYPE_CHATITEM:
                        ChatItem newItem = (ChatItem) msg.obj;
                        if (newItem != null) {
                            Log.d(TAG, "onSubscribalbeReceiveMsg : " + newItem.getContent());
                            fragment.getAdapter().add(newItem);
                            fragment.getAdapter().notifyDataSetChanged();
                            if (!fragment.isScrollViewInButtom()) {
                                fragment.getCmdListView().removeCallbacks(mToastRunnable);
                                fragment.getCmdListView().postDelayed(mToastRunnable, 500);
                            } else {
                                fragment.setNewMsgNum(0);
                                fragment.scrollMyListViewToBottom();
                            }
                        }
                        break;
                    case MSG_TYPE_TOAST:
                        if (fragment.getActivity() != null) {
                            Context context = fragment.getActivity().getApplicationContext();
                            if (context != null) {
                                Toast.makeText(
                                        context
                                        , (String) msg.obj
                                        , Toast.LENGTH_SHORT)
                                        .show();
                            }
                        }
                        break;
                    case MSG_TYPE_VOICE_CMD:
                        fragment.startRecognize();
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAutoCompleteAdapter.destory();
        mChatFragmentPresenter.stop();
    }

    public static boolean sendMessage(Message msg) {
        if (ChatFragment.PublicHandler != null) {
            ChatFragment.PublicHandler.sendMessage(msg);
            return true;
        }
        return false;
    }

    @SuppressLint("ShowToast")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.chat_fragment, container, false);
        setupViews(rootView);

        mSuggestionButton.setVisibility(View.GONE);
        mSendCmdEdittext.setCanShowDropdown(false);
        mToast = Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT);
//        mActionBarControlViewRf = new WeakReference<ActionBarControlView>((MainActivity)getActivity());
        // for retainning fragment
        setRetainInstance(true);
        // for user experience
        if (getArguments() != null && getArguments().getBoolean(BUNDLE_KEY_SCROLL_TO_BOTTOM)) {
            scrollMyListViewToBottom();
            getArguments().clear();
        }
        return rootView;
    }

    @Override
    public void setupViews(View rootView) {
        mCmdListview = (ListView) rootView.findViewById(R.id.chat_list);
        mCmdListview.setAdapter(mAdapter);
        mCmdListview.setOnScrollListener(new OnScrollListener() {
            int topVisibleIndex;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (mKeyboard_open && scrollState == SCROLL_STATE_TOUCH_SCROLL) {
                    InputMethodManager inputManager =
                            (InputMethodManager) getActivity().
                                    getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(
                            getActivity().getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                } else if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                    if (topVisibleIndex == 0
                            && mAdapter.getItem(0).getId() > Constants.CHATITEM_LOWEST_INDEX) {
                        new LoadMoreChatItemAsyncTask(ChatFragment.this).execute(Constants.CHATITEM_LOAD_LIMIT);
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                if (visibleItemCount == 0)
                    return;
                topVisibleIndex = firstVisibleItem;
                int vH = view.getHeight();
                View childView = view.getChildAt(visibleItemCount - 1);
                if (firstVisibleItem + visibleItemCount == totalItemCount && vH >= childView.getBottom()) {
                    Log.d(TAG, "reach buttom");
                    mScrollViewInButtom = true;
                    if (mNeedShowUnread) {
                        mNeedShowUnread = false;
                    }
                } else {
                    if (mScrollViewInButtom == true) {
                        mScrollViewInButtom = false;
                    }
                }
            }
        });
        mCmdListview.setOnTouchListener(new OnTouchListener() {

            private final GestureDetector gestureDetector = new GestureDetector(
                    getContext(), new GestureDetector.SimpleOnGestureListener() {

                private final float _SHOW_KEYBOARD_Y = 140.0f;

                @Override
                public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                    if (distanceY > _SHOW_KEYBOARD_Y) {
                        mSendCmdEdittext.requestFocus();
                        mSendCmdEdittext.postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                InputMethodManager keyboard = (InputMethodManager)
                                        getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                keyboard.showSoftInput(mSendCmdEdittext, 0);
                            }
                        }, 200);
                    }
                    return false;
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mScrollViewInButtom) {
                    return gestureDetector.onTouchEvent(event);
                }
                return false;
            }
        });

        mSuggestionButton = (Button) rootView.findViewById(R.id.cmd_suggestion_button);
        mSuggestionButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                AutoCompleteItem item = (AutoCompleteItem) v.getTag();
                if (item instanceof AutoCompleteToolItem) {
                    Log.d(TAG, "selected AutoCompleteToolItem: " + item.getContent());
                    AutoCompleteToolItem toolItem = (AutoCompleteToolItem) item;
                    performToolItem(toolItem);
                } else {
                    setSendCmdEditText(item.getCmd());
                }
            }
        });
//        mSuggestionButton.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                mSendCmdEdittext.setCanShowDropdown(true);
//                mSendCmdEdittext.showDropDown();
//                onShowSuggestion(null);
//                return true;
//            }
//        });
        mSuggestionButton.setOnTouchListener(new OnSwipeTouchListener(getActivity()) {

//            private final Animation leftAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.shake_left);
//            private final Animation rightAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.shake_right);

            @Override
            public void onSwipeTop() {
                Animation upAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.shake_up);
                upAnim.setAnimationListener(new SimpleAnimationListener() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mChatFragmentPresenter.showPreCmdSuggestion(
                                mAutoCompleteAdapter.getResultList(),
                                (AutoCompleteItem) mSuggestionButton.getTag()
                        );
                    }
                });
                mSuggestionButton.startAnimation(upAnim);
            }

            @Override
            public void onSwipeLeft() {
                mSendCmdEdittext.setCanShowDropdown(true);
                mSendCmdEdittext.showDropDown();
                onShowSuggestion(null);
            }

            @Override
            public void onSwipeBottom() {
                Animation downAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.shake_down);
                downAnim.setAnimationListener(new SimpleAnimationListener() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mChatFragmentPresenter.showNextCmdSuggestion(
                                mAutoCompleteAdapter.getResultList(),
                                (AutoCompleteItem) mSuggestionButton.getTag()
                        );
                    }
                });
                mSuggestionButton.startAnimation(downAnim);
            }

            @Override
            public void onClick() {
                Animation clickAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.shake_click);
                clickAnim.setAnimationListener(new SimpleAnimationListener() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mSuggestionButton.performClick();
                    }
                });
                mSuggestionButton.startAnimation(clickAnim);
            }
        });

        mSpeechDialog = SpeechDialog.getInstance(getActivity());
        final Button speechButton = (Button) rootView.findViewById(R.id.speech_button);
        speechButton.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    speechButton.setSelected(true);
                    if (!mSpeechDialog.isShowing()) {
                        startRecognize();
                    }
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    speechButton.setSelected(false);
                    if (event.getRawY() / mScreenHeight <= Constants.DIALOG_CANCEL_Y_PERSENT) {
                        Log.d(TAG, "cancelListening.");
                        mSpeechDialog.cancelListening();
                    } else {
                        Log.d(TAG, "finishListening.");
                        mSpeechDialog.finishListening();
                    }
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
//                	Log.d(TAG, String.valueOf(event.getRawY()/mScreenHeight));
                    if (mSpeechDialog != null) {
                        if (event.getRawY() / mScreenHeight <= Constants.DIALOG_CANCEL_Y_PERSENT) {
                            mSpeechDialog.setReleaseCancelVisible(true);
                        } else {
                            mSpeechDialog.setReleaseCancelVisible(false);
                        }
                    }
                    return true;
                }
                return false;
            }
        });

        mSendCmdEdittext = (DelayAutoCompleteTextView) rootView.findViewById(R.id.send_cmd_edittext);
        mSendCmdEdittext.setOnEditorActionListener(new OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // Perform action on key press
                    String messageString = mSendCmdEdittext.getText().toString();
                    if (!messageString.trim().equals("")) {
                        mChatFragmentPresenter.markAndSendCurrentInput(messageString, shouldUseLocalMsg());
                        mSendCmdEdittext.setText("");
                        mSendCmdEdittext.setCanShowDropdown(false);
                    }
                    return true;
                } else {
                    return false;
                }
            }

        });
        mAutoCompleteAdapter = new AutoCompleteAdapter(getActivity());
        mAutoCompleteAdapter.setOnLoadConfListener(this);
        mAutoCompleteAdapter.initAutoCompleteItem();
        mSendCmdEdittext.setAdapter(mAutoCompleteAdapter);
        mSendCmdEdittext.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AutoCompleteItem item = (AutoCompleteItem) parent.getItemAtPosition(position);
                if (item instanceof AutoCompleteToolItem) {
                    Log.d(TAG, "selected AutoCompleteToolItem: " + item.getContent());
                    AutoCompleteToolItem toolItem = (AutoCompleteToolItem) item;
                    performToolItem(toolItem);
                } else {
                    setSendCmdEditText(item.getCmd());
                }
            }
        });
        mSendCmdEdittext.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    onShowSuggestion(null);
                    mSendCmdEdittext.setCanShowDropdown(false);
                }
            }
        });

        mKeyboardListener = (new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = getView().getRootView().getHeight() - getView().getHeight();
                Log.v(TAG, "height" + String.valueOf(heightDiff));
                if (heightDiff > 200) { // if more than 100 pixels, its probably a keyboard...
                    Log.v(TAG, "keyboard show.");
                    if (!mKeyboard_open) {
                        ChatFragment.this.scrollMyListViewToBottom();
                    }
                    mKeyboard_open = true;
                } else if (mKeyboard_open) {
                    mKeyboard_open = false;
                    mSendCmdEdittext.clearFocus();
                    mCmdListview.requestFocus();
                    Log.d(TAG, "keyboard hide.");
                }
            }
        });
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(mKeyboardListener);

        mSendCmdLayout = (RelativeLayout) rootView.findViewById(R.id.send_cmd_layout);

        mSwitchButton = (Button) rootView.findViewById(R.id.switch_input_button);
        mSwitchButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!mInSpeechMode) {
                    Button switch_btn = (Button) getView().findViewById(R.id.switch_input_button);
                    switch_btn.setBackgroundResource(R.drawable.chatting_setmode_voice_btn);
//                    switch_btn.setBackgroundResource(android.R.drawable.ic_menu_edit);
                    getView().findViewById(R.id.speech_button).setVisibility(View.VISIBLE);
                    getView().findViewById(R.id.send_cmd_edittext).setVisibility(View.INVISIBLE);
                    mInSpeechMode = true;
//                    AnimatorSet animatorSet = UIUtils.getDismissViewScaleAnimatorSet(toolButton);
//                    toolButton.setVisibility(View.GONE);
//                    animatorSet.start();
//                    Animation animation = new ResizeHeightAnim(mSendCmdLayout, mSendCmdLayout.getHeight()*2);
//                    animation.setDuration(300);
//                    animation.setRepeatCount(0);
//                    animation.setInterpolator(new AccelerateDecelerateInterpolator());
//                    mSendCmdLayout.startAnimation(animation);

                    if (mKeyboard_open) {
                        InputMethodManager inputManager = (InputMethodManager) getActivity().
                                getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputManager.hideSoftInputFromWindow(
                                getActivity().getCurrentFocus().getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                } else {
                    Button switch_btn = (Button) getView().findViewById(R.id.switch_input_button);
                    switch_btn.setBackgroundResource(R.drawable.chatting_setmode_msg_btn);
//                    switch_btn.setBackgroundResource(android.R.drawable.ic_btn_speak_now);
                    getView().findViewById(R.id.speech_button).setVisibility(View.INVISIBLE);
                    getView().findViewById(R.id.send_cmd_edittext).setVisibility(View.VISIBLE);

//                    Animation animation = new ResizeHeightAnim(mSendCmdLayout, mSendCmdLayout.getHeight()/2);
//                    animation.setDuration(300);
//                    animation.setRepeatCount(0);
//                    animation.setInterpolator(new AccelerateDecelerateInterpolator());
//                    mSendCmdLayout.startAnimation(animation);

                    mSendCmdEdittext.requestFocus();
                    mSendCmdEdittext.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            InputMethodManager keyboard = (InputMethodManager)
                                    getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            keyboard.showSoftInput(mSendCmdEdittext, 0);
                        }
                    }, 200);
                    mInSpeechMode = false;
                }
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == mCmdListview.getId()) {
            AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
            MenuInflater inflater = getActivity().getMenuInflater();
            ChatItem chatItem = mAdapter.getItem(info.position);
            if (chatItem.isMe()) {
                inflater.inflate(R.menu.chat_item_is_me, menu);
            } else if (chatItem.isServer()) {
                inflater.inflate(R.menu.chat_item_not_me, menu);
            } else if (chatItem.isServerImageItem()) {
                inflater.inflate(R.menu.chat_item_server_image, menu);
            } else if (chatItem.isServerLocItem()) {
                inflater.inflate(R.menu.chat_item_server_loc, menu);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.voice_input:
                scriptInputMode = true;
                startRecognize();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        String selectedString = mAdapter.getItem(info.position).getContent();
        switch (item.getItemId()) {
            case R.id.add_chat_item_to_shortcut:
                MainActivity activity = (MainActivity) getActivity();
                if (activity.getShortcurFragment() == null) {
                    Shortcut shortcut = new Shortcut();
                    shortcut.setContent(selectedString);
                    shortcut.setInvoke_count(0);
                    shortcut.setWeight(1.0);
                    DBStaticManager.addShortcut(this.getActivity(), shortcut);
                } else {
                    activity.getShortcurFragment().addShortcut(selectedString);
                }
                return true;
            case R.id.resend_item:
                mChatFragmentPresenter.markAndSendCurrentInput(selectedString, shouldUseLocalMsg());
                return true;
            case R.id.copy_item:
                CommonUtils.copyStringToClipboard(getActivity(), getString(R.string.app_name), selectedString);
                this.showTip(getString(R.string.msg_copyed_to_clipboard));
                return true;
            case R.id.copy_to_input:
                if (!TextUtils.isEmpty(selectedString)) {
                    if (mInSpeechMode) {
                        mSwitchButton.performClick();
                    }
                    mSendCmdEdittext.append(selectedString);
                    mSendCmdEdittext.requestFocus();
                    mSendCmdEdittext.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            InputMethodManager keyboard = (InputMethodManager)
                                    getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            keyboard.showSoftInput(mSendCmdEdittext, 0);
                        }
                    }, 200);
                }
                return true;
            case R.id.save_item:
                mChatFragmentPresenter.saveImageItem(mAdapter.getItem(info.position));
                return true;
            case R.id.copy_loc_item_info:
                mChatFragmentPresenter.copyLocationInfo(
                        mAdapter.getItem(info.position),
                        getString(R.string.app_name)
                );
                this.showTip(getString(R.string.msg_copyed_to_clipboard));
                return true;
            case R.id.action_photo_extra_intent:
                mChatFragmentPresenter.openLocationInBrowser(mAdapter.getItem(info.position));
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public void switchInputMode() {
        mSwitchButton.performClick();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        registerForContextMenu(mCmdListview);
        setHasOptionsMenu(true);

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mScreenWidth = size.x;
        mScreenHeight = size.y;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mKeyboard_open && getActivity() != null) {
            InputMethodManager inputManager =
                    (InputMethodManager) getActivity().
                            getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(
                    getActivity().getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }

        mToast.cancel();
        if (null != mSpeechDialog && mSpeechDialog.isShowing()) {
            mSpeechDialog.dismiss();
        }

        if (null != mPhotoDialog && mPhotoDialog.isShowing()) {
            mPhotoDialog.dismiss();
        }

        View rootView = getView();
        rootView.getViewTreeObserver().removeOnGlobalLayoutListener(mKeyboardListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        MessageHelper.resetUnreadCount();
        mChatFragmentPresenter.resetDatas(getActivity());
        RadialTimePickerDialog rtpd = (RadialTimePickerDialog) getFragmentManager().findFragmentByTag(
                FRAG_TAG_TIME_PICKER);
        if (rtpd != null) {
            rtpd.setOnTimeSetListener(this);
        }
        CalendarDatePickerDialog cdpd = (CalendarDatePickerDialog) getFragmentManager()
                .findFragmentByTag(FRAG_TAG_DATE_PICKER);
        if (cdpd != null) {
            cdpd.setOnDateSetListener(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
//        mChatFragmentPresenter.saveSaveLocalHistory();
    }


    // =========================================================================================

    @Override
    public void onLoadComplete(boolean loadSuccess) {
        Log.i(TAG, "load autocomplete conf: " + loadSuccess);
    }


    private void showTip(String str) {
        if (!TextUtils.isEmpty(str)) {
            mToast.setText(str);
            mToast.show();
        }
    }

    public void scrollMyListViewToBottom() {
//        mCmdListview.postDelayed(new Runnable() {
//            @Override
//            public void run() {
                // Select the last row so it will scroll into view...
                mCmdListview.setSelection(mAdapter.getCount() - 1);
//                mCmdListview.smoothScrollToPosition(mAdapter.getCount() - 1);
//            }
//        }, 300);
    }

    public ChatItemArrayAdapter getAdapter() {
        return mAdapter;
    }

    public ListView getCmdListView() {
        return mCmdListview;
    }

    /**
     * ========================s2t===========================
     */

	/*
     * Speech Dialog
	 */
    public boolean isRecognizing() {
        return inRecogintion;
    }

    public void startRecognize() {
        Log.d(TAG, "show mSpeechDialog");

        SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean auto_sco = mySharedPreferences.getBoolean("pref_auto_connect_sco", true);
        Log.d(TAG, "auto_sco: " + auto_sco);

        inRecogintion = true;
        mSpeechDialog.setmUseBluetooth(auto_sco);
        mSpeechDialog.setup(getContext(), ChatFragment.this);
        mSpeechDialog.show();
    }

    public void finishVoiceRecognize() {
        if (mSpeechDialog.isShowing() && inRecogintion) {
            mSpeechDialog.finishListening();
        }
    }

    public void cancelVoiceRecognize() {
        if (mSpeechDialog.isShowing() && inRecogintion) {
            mSpeechDialog.cancelListening();
        }
    }

    @Override
    public void onResult(List<String> results) {
        Log.d(TAG, "onResult: " + results.toString());
        if (results.size() == 0) {
            showTip(getString(R.string.speech_no_result));
            return;
        }

        String resultString = results.get(0);
        if (scriptInputMode == true) {
            resultString = "运行脚本#" + resultString + "#";
            scriptInputMode = false;
        }
        final String msgString = resultString;
        final Context context = getActivity();

        Log.d(TAG, "result: " + msgString);

        if (!msgString.trim().equals("")) {
            SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            boolean need_confirm = mySharedPreferences.getBoolean("pref_speech_cmd_need_confirm", true);
            if (!need_confirm) {
                mChatFragmentPresenter.markAndSendCurrentInput(msgString, shouldUseLocalMsg());
                inRecogintion = false;
            } else {
                AlertDialog.Builder alert = new AlertDialog.Builder(context);

                alert.setMessage(msgString);
                alert.setTitle(getResources().getString(R.string.speech_cmd_need_confirm));

                alert.setNeutralButton(getResources().getString(R.string.com_send_to_edittext)
                        , new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mSendCmdEdittext.append(msgString);
                        if (mInSpeechMode) {
                            mSwitchButton.performClick();
                        }
                        inRecogintion = false;
                    }
                });

                alert.setPositiveButton(getResources().getString(R.string.com_comfirm)
                        , new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mChatFragmentPresenter.markAndSendCurrentInput(msgString, shouldUseLocalMsg());
                        inRecogintion = false;
                    }
                });

                alert.setNegativeButton(getResources().getString(R.string.com_cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                inRecogintion = false;
                            }
                        });

                alert.show();
            }
        } else {
            inRecogintion = false;
        }
    }

    @Override
    public void onDissmiss(int state) {
        inRecogintion = false;
    }

    @Override
    public void onResendButtonClicked(int pos) {
        ChatItem item = this.getAdapter().getItem(pos);
        mChatFragmentPresenter.markAndSendCurrentChatItem(item, shouldUseLocalMsg());
    }

    @Override
    public void onSaveLocalHistoryFinish(boolean success) {
        if (!success) {
            Toast.makeText(
                    getActivity()
                    , getString(R.string.save_local_history_error)
                    , Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void updateRequestChatItemState(ArrayAdapter<ChatItem> adapter, Long id, int state) {
        for (int i = adapter.getCount() - 1; i >= 0; i--) {
            if (adapter.getItem(i).getId().equals(id)) {
                adapter.getItem(i).setState(state);
                break;
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onResetDatas(List<ChatItem> chatItems) {
        mAdapter.setData(chatItems);
    }

    @Override
    public void onChatItemRequest(ChatItem reqItem, boolean isUpdate) {
        if (isUpdate) {
            updateRequestChatItemState(getAdapter(), reqItem.getId(), reqItem.getState());
        } else {
            getAdapter().add(reqItem);
            getAdapter().notifyDataSetChanged();
            scrollMyListViewToBottom();
        }
    }

    @Override
    public void onChatItemResponse(int repCode, long reqID, int reqState, ChatItem repItem) {
        if (repCode == 200) {
            updateRequestChatItemState(getAdapter(), reqID, reqState);
        } else {
            getAdapter().add(repItem);
            updateRequestChatItemState(getAdapter(), reqID, reqState);
            scrollMyListViewToBottom();
        }
    }


    @Override
    public Context getContext() {
        return getActivity();
    }

    private void appendSendCmdEditText(String content) {
        setSendCmdEditText(mSendCmdEdittext.getText() + content);
    }

    private void setSendCmdEditText(String content) {
        mSendCmdEdittext.setText(content);
        mSendCmdEdittext.requestFocus();
        Editable editable = mSendCmdEdittext.getText();
        Selection.setSelection(editable, editable.length());
    }

    /*
     * Date Time picker callback
     */

//    @Override
//    public void onTimeSelected(TimePicker view, int hourOfDay, int minute) {
//        appendSendCmdEditText(Utils.TimeToCmdString(hourOfDay, minute));
//    }
//
//    @Override
//    public void onDateSelected(DatePicker view, int year, int month, int day) {
//        appendSendCmdEditText(Utils.DateToCmdString(year, month, day));
//    }

    @Override
    public void onDateSet(CalendarDatePickerDialog calendarDatePickerDialog, int year, int month, int day) {
        appendSendCmdEditText(ComUtil.DateToCmdString(year, month, day));
    }

    @Override
    public void onTimeSet(RadialTimePickerDialog radialTimePickerDialog, int hourOfDay, int minute) {
        appendSendCmdEditText(ComUtil.TimeToCmdString(hourOfDay, minute));
    }

    private void showTimeDialog() {
//        TimePickerFragment timeFragment = new TimePickerFragment();
//        timeFragment.setDateTimePickerFragmentListener(this);
//        timeFragment.show(getFragmentManager(), "timePicker");
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        RadialTimePickerDialog timePickerDialog = RadialTimePickerDialog
                .newInstance(this, hour, minute, DateFormat.is24HourFormat(getActivity()));
        timePickerDialog.show(getFragmentManager(), FRAG_TAG_TIME_PICKER);
    }

    private void showDateDialog() {
//        DatePickerFragment dateFragment = new DatePickerFragment();
//        dateFragment.setDateTimePickerFragmentListener(this);
//        dateFragment.show(getFragmentManager(), "datePicker");

        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
//        return new DatePickerDialog(getActivity(), this, year, month, day);

        CalendarDatePickerDialog calendarDatePickerDialog = CalendarDatePickerDialog
                .newInstance(this, year, month, day);
        calendarDatePickerDialog.show(getFragmentManager(), FRAG_TAG_DATE_PICKER);
    }

    private void showShortcutDialog(List<Shortcut> items) {
        if (items == null || items.size() == 0) {
            showTip(getString(R.string.menu_tool_favor_empty));
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.menu_tool_favor_title);
        builder.setNegativeButton("cancel",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        final ShortcutArrayAdapter adapter = new ShortcutArrayAdapter(getActivity(), R.layout.shortcut_item);
        adapter.setData(items);
        builder.setAdapter(adapter,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String selectedContent = adapter.getItem(which).getContent();
                        Log.d(TAG, "selected shortcut: " + selectedContent);
                        appendSendCmdEditText(selectedContent);
                    }
                });
        builder.show();
    }


    private void performToolItem(AutoCompleteToolItem item) {
        switch (item.getSpecType()) {
            case AutoCompleteToolItem.SPEC_TYPE_DATE:
                showDateDialog();
                break;
            case AutoCompleteToolItem.SPEC_TYPE_TIME:
                showTimeDialog();
                break;
            case AutoCompleteToolItem.SPEC_TYPE_FAVOR:
                List<Shortcut> items = DBStaticManager.getAllShortcuts(this.getActivity());
                showShortcutDialog(items);
                break;
        }
    }

    /*
     *  onShowSuggestion(AutoCompleteItem item)
     */

    @Override
    public void onShowSuggestion(AutoCompleteItem item) {
        if (item == null || mSendCmdEdittext.getText() == null || mSendCmdEdittext.getText().length() == 0) {
            if (mSuggestionButton.getVisibility() != View.GONE) {
                AnimatorSet animatorSet = UIUtil.getDismissViewScaleAnimatorSet(mSuggestionButton, 200);
                mSuggestionButton.setVisibility(View.VISIBLE);
                animatorSet.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mSuggestionButton.setVisibility(View.GONE);
                        mSuggestionButton.setText("");
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                animatorSet.start();
            } else {
                mSuggestionButton.setText("");
            }
            // disable autocomplete dropdown
            mSuggestionButton.setTag(null);
        } else if (!mSendCmdEdittext.isCanShowDropdown()) {
            if (mSuggestionButton.getVisibility() != View.VISIBLE) {
                AnimatorSet animatorSet = UIUtil.getShowViewScaleAnimatorSet(mSuggestionButton, 200);
                mSuggestionButton.setVisibility(View.VISIBLE);
                animatorSet.start();
            }
            mSuggestionButton.setTag(item);
            mSuggestionButton.setText(item.getContent());
        }
    }

//    @Override
//    public void onGetAutoCompleteItems(List<AutoCompleteItem> item) {
//        if (item == null || item.size() == 0) {
//            mSendCmdEdittext.setCanShowDropdown(false);
//        }
//    }

    private boolean shouldUseLocalMsg() {
        return ((MainActivity) getActivity()).shouldUseLocalMsg();
    }
}