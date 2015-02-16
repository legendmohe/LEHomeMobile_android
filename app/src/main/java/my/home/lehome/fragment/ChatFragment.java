package my.home.lehome.fragment;

import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
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
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
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
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import my.home.common.Constants;
import my.home.lehome.R;
import my.home.lehome.activity.MainActivity;
import my.home.lehome.adapter.AutoCompleteAdapter;
import my.home.lehome.adapter.ChatItemArrayAdapter;
import my.home.lehome.adapter.ChatItemArrayAdapter.ResendButtonClickListener;
import my.home.lehome.asynctask.LoadMoreChatItemAsyncTask;
import my.home.lehome.asynctask.SendCommandAsyncTask;
import my.home.lehome.helper.DBHelper;
import my.home.lehome.helper.MessageHelper;
import my.home.lehome.util.UIUtils;
import my.home.lehome.view.DelayAutoCompleteTextView;
import my.home.lehome.view.SpeechDialog;
import my.home.lehome.view.SpeechDialog.SpeechDialogResultListener;
import my.home.model.entities.AutoCompleteItem;
import my.home.model.entities.ChatItem;
import my.home.model.entities.Shortcut;

public class ChatFragment extends Fragment implements SpeechDialogResultListener, ResendButtonClickListener, AutoCompleteAdapter.onLoadConfListener {
    public static final String TAG = ChatFragment.class.getName();

    /*
     * common UI
     */
    private ChatItemArrayAdapter mAdapter;
    //	private ProgressBar mProgressBar;
    private Button switchButton;
    private Toast mToast;
    private OnGlobalLayoutListener mKeyboardListener;
    private ListView mCmdListview;
    private DelayAutoCompleteTextView mSendCmdEdittext;
//    private ArrayAdapter<String> mAutoCompleteAdapter;

    /*
     * common variables
     */
    public static Handler mHandler;
    private int mNewMsgNum = 0;
    private int mTopVisibleIndex;
    private boolean mNeedShowUnread = false;
    private boolean mScrollViewInButtom = false;
    private boolean mKeyboard_open = false;
    private boolean mInSpeechMode = false;

    /*
     * history
     */
    private HashSet<String> mAutoCompleteHashSet = new HashSet<String>();
    private AutoCompleteAdapter mAutoCompleteAdapter;

    /*
     * speech
     */
    SpeechDialog mSpeechDialog;
    private boolean scriptInputMode;
    public boolean inRecogintion = false;
    private int mScreenWidth = 0;
    private int mScreenHeight = 0;

    /*
     * constant
     */
    public static final int CHATITEM = 1;
    public static final int TOAST = 2;
    public static final int VOICE_CMD = 3;

    /*
     * settings
     */
    public static final int CHATITEM_LOAD_LIMIT = 20;
    public static final int CHATITEM_LOWEST_INDEX = 1;
    public static final float DIALOG_CANCEL_Y_PERSENT = 0.57f;

    @SuppressLint("HandlerLeak")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (mAdapter == null) {
            mAdapter = new ChatItemArrayAdapter(this.getActivity(), R.layout.chat_item_onright);
            mAdapter.setResendButtonClickListener(this);
        }
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == CHATITEM) {
                    ChatItem newItem = (ChatItem) msg.obj;
                    if (newItem != null) {
                        Log.d(TAG, "onSubscribalbeReceiveMsg : " + newItem.getContent());
                        mAdapter.add(newItem);
                        mAdapter.notifyDataSetChanged();
                        if (!mScrollViewInButtom) {
                            mNeedShowUnread = true;
                            mNewMsgNum++;
                            ChatFragment.this.showTip(mNewMsgNum + " new message");
                        } else {
                            mNewMsgNum = 0;
                            ChatFragment.this.scrollMyListViewToBottom();
                        }
                    }
                } else if (msg.what == TOAST) {
                    if (getActivity() != null) {
                        Context context = getActivity().getApplicationContext();
                        if (context != null) {
                            Toast.makeText(
                                    context
                                    , (String) msg.obj
                                    , Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                } else if (msg.what == VOICE_CMD) {
                    startRecognize(getActivity());
                }
            }

        };

    }

    ;

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAutoCompleteAdapter.destory();
    }

    public static boolean sendMessage(Message msg) {
        if (ChatFragment.mHandler != null) {
            ChatFragment.mHandler.sendMessage(msg);
            return true;
        }
        return false;
    }

    @SuppressLint("ShowToast")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.chat_fragment, container, false);

        mCmdListview = (ListView) rootView.findViewById(R.id.chat_list);
//        ScaleInAnimationAdapter animationAdapter = new ScaleInAnimationAdapter(mAdapter);
//        animationAdapter.setAbsListView(mCmdListview);
        mCmdListview.setAdapter(mAdapter);


        mCmdListview.setOnScrollListener(new OnScrollListener() {
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
                    if (mTopVisibleIndex == 0
                            && mAdapter.getItem(0).getId() > CHATITEM_LOWEST_INDEX) {
                        new LoadMoreChatItemAsyncTask(ChatFragment.this).execute(CHATITEM_LOAD_LIMIT);
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                mTopVisibleIndex = firstVisibleItem;
                if (firstVisibleItem + visibleItemCount == totalItemCount) {
                    Log.d(TAG, "reach buttom");
                    mScrollViewInButtom = true;
                    if (mNeedShowUnread) {
                        mNeedShowUnread = false;
                    }
                } else {
                    mScrollViewInButtom = false;
                }
            }
        });


        final Button clearButton = (Button) rootView.findViewById(R.id.cmd_clear_button);
        clearButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mSendCmdEdittext.setText("");
            }
        });
        clearButton.setVisibility(View.GONE);

        switchButton = (Button) rootView.findViewById(R.id.switch_input_button);
        switchButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!mInSpeechMode) {
                    Button switch_btn = (Button) getView().findViewById(R.id.switch_input_button);
                    switch_btn.setBackgroundResource(R.drawable.chatting_setmode_voice_btn);
                    getView().findViewById(R.id.speech_button).setVisibility(View.VISIBLE);
                    getView().findViewById(R.id.send_cmd_edittext).setVisibility(View.INVISIBLE);
                    mInSpeechMode = true;
                    AnimatorSet animatorSet = UIUtils.getDismissViewScaleAnimatorSet(clearButton);
                    clearButton.setVisibility(View.GONE);
                    animatorSet.start();

                    if (mKeyboard_open) {
                        InputMethodManager inputManager =
                                (InputMethodManager) getActivity().
                                        getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputManager.hideSoftInputFromWindow(
                                getActivity().getCurrentFocus().getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                } else {
                    Button switch_btn = (Button) getView().findViewById(R.id.switch_input_button);
                    switch_btn.setBackgroundResource(R.drawable.chatting_setmode_msg_btn);
                    getView().findViewById(R.id.speech_button).setVisibility(View.INVISIBLE);
                    getView().findViewById(R.id.send_cmd_edittext).setVisibility(View.VISIBLE);
                    mInSpeechMode = false;
//					clearButton.setVisibility(View.VISIBLE);
                }
            }
        });
        mSpeechDialog = SpeechDialog.getInstance(getActivity());
        Button speechButton = (Button) rootView.findViewById(R.id.speech_button);

        speechButton.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (!mSpeechDialog.isShowing()) {
                        startRecognize(getActivity());
                    }
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawY() / mScreenHeight <= DIALOG_CANCEL_Y_PERSENT) {
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
                        if (event.getRawY() / mScreenHeight <= DIALOG_CANCEL_Y_PERSENT) {
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
                        MainActivity mainActivity = (MainActivity) getActivity();
                        new SendCommandAsyncTask(mainActivity, messageString).execute();
                        ChatFragment.this.addCmdHistory(messageString);
                        mSendCmdEdittext.setText("");
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
                mSendCmdEdittext.setText(item.getCmd());
                Editable editable = mSendCmdEdittext.getText();
                Selection.setSelection(editable, editable.length());
                mSendCmdEdittext.performFiltering(editable);
//                mSendCmdEdittext.showDropDown();
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
                if (s.length() > 0) {
                    if (clearButton.getVisibility() == View.INVISIBLE
                            || clearButton.getVisibility() == View.GONE) {
                        AnimatorSet animatorSet = UIUtils.getShowViewScaleAnimatorSet(clearButton);
                        clearButton.setVisibility(View.VISIBLE);
                        animatorSet.start();
                    }
                } else {
                    clearButton.setVisibility(View.GONE);
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

//        mProgressBar = (ProgressBar) rootView.findViewById(R.id.send_msg_progressbar);
//        mProgressBar.setVisibility(View.INVISIBLE);

        mToast = Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT);

        scrollMyListViewToBottom();
        return rootView;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == mCmdListview.getId()) {
            AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
            MenuInflater inflater = getActivity().getMenuInflater();
            ChatItem chatItem = mAdapter.getItem(info.position);
            if (chatItem.getIsMe()) {
                inflater.inflate(R.menu.chat_item_is_me, menu);
            } else {
                inflater.inflate(R.menu.chat_item_not_me, menu);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.voice_input:
                scriptInputMode = true;
                startRecognize(getActivity());
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
                    DBHelper.addShortcut(this.getActivity(), shortcut);
                } else {
                    activity.getShortcurFragment().addShortcut(selectedString);
                }
                return true;
            case R.id.resend_item:
                MainActivity mainActivity = (MainActivity) getActivity();
                new SendCommandAsyncTask(mainActivity, selectedString).execute();
                this.addCmdHistory(selectedString);
                return true;
            case R.id.copy_item:
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(getString(R.string.app_name), selectedString);
                clipboard.setPrimaryClip(clip);
                return true;
            case R.id.copy_to_input:
                if (!TextUtils.isEmpty(selectedString)) {
                    mSendCmdEdittext.append(selectedString);
                    if (mInSpeechMode) {
                        switchButton.performClick();
                    }
                }
                return true;
            default:
                return super.onContextItemSelected(item);
        }
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
//		scrollMyListViewToBottom();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mKeyboard_open) {
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

        View rootView = getView();
        rootView.getViewTreeObserver().removeOnGlobalLayoutListener(mKeyboardListener);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        MessageHelper.resetUnreadCount();
        this.resetDatas();

//		this.registerBTSCO(getActivity().getApplicationContext());
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
//    	this.unregisterBTSCO(getActivity().getApplicationContext());
//    	this.closeSCO(getActivity().getApplicationContext());
    }

    @Override
    public void onStart() {
//        mSendCmdEdittext.setAdapter(this.setupAutoComplete(getActivity()));
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        this.saveCmdHistory(getActivity());
    }


    // =========================================================================================


//    private ArrayAdapter<String> setupAutoComplete(Context context) {
//        SharedPreferences pref = context.getSharedPreferences(Constants.PREF_NAME, 0);
//        Set<String> cmdSet = pref.getStringSet(Constants.CMD_HISTORY_PREF_NAME, new HashSet<String>());
//
//        mAutoCompleteHashSet.clear();
//        mAutoCompleteHashSet.addAll(cmdSet);
//        mAutoCompleteAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1);
//        mAutoCompleteAdapter.addAll(cmdSet);
//
//        Log.d(TAG, "setupAutoCompleteArrayAdapter: " + cmdSet.size());
//        return mAutoCompleteAdapter;
//    }

    private void addCmdHistory(String cmd) {
        if (!mAutoCompleteHashSet.contains(cmd)) {
            mAutoCompleteHashSet.add(cmd);
//            mAutoCompleteAdapter.add(cmd);

            Log.d(TAG, "addCmdHistory: " + cmd);
        }
    }

    private void saveCmdHistory(Context context) {
        SharedPreferences pref = context.getSharedPreferences(Constants.PREF_NAME, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putStringSet(Constants.CMD_HISTORY_PREF_NAME, mAutoCompleteHashSet);
        editor.commit();

        Log.d(TAG, "saveCmdHistory: " + mAutoCompleteHashSet.size());
    }

    @Override
    public void onLoadComplete(boolean loadSuccess) {
        Log.i(TAG, "load autocomplete conf: " + loadSuccess);
    }

    public void resetDatas() {
        List<ChatItem> chatItems = DBHelper.loadLatest(this.getActivity(), CHATITEM_LOAD_LIMIT);
        if (chatItems != null) {
            Collections.reverse(chatItems); // reverse descend items
            mAdapter.setData(chatItems);
        }
    }

    private void showTip(String str) {
        if (!TextUtils.isEmpty(str)) {
            mToast.setText(str);
            mToast.show();
        }
    }

    public void scrollMyListViewToBottom() {
        mCmdListview.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                mCmdListview.setSelection(mAdapter.getCount() - 1);
//                mCmdListview.smoothScrollToPosition(mAdapter.getCount() - 1);
            }
        });
    }

    public void addChatItem(ChatItem item) {
        Animation anim = AnimationUtils.loadAnimation(
                this.getActivity(), android.R.anim.slide_out_right
        );
        anim.setDuration(500);
        mAdapter.add(item);
        mCmdListview.getChildAt(mAdapter.getPosition(item)).startAnimation(anim);

        new Handler().postDelayed(new Runnable() {

            public void run() {
                mAdapter.notifyDataSetChanged();

            }

        }, anim.getDuration());
    }

    public ChatItemArrayAdapter getAdapter() {
        return mAdapter;
    }

    /***
     * ========================s2t===========================
     */

	/*
     * Speech Dialog
	 */
    public void startRecognize(Context context) {
        Log.d(TAG, "show mSpeechDialog");

        SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean auto_sco = mySharedPreferences.getBoolean("pref_auto_connect_sco", true);
        Log.d(TAG, "auto_sco: " + auto_sco);

        inRecogintion = true;
        mSpeechDialog.setmUseBluetooth(auto_sco);
        mSpeechDialog.setup(context, ChatFragment.this);
        mSpeechDialog.show();
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
                new SendCommandAsyncTask(context, msgString).execute();
                ChatFragment.this.addCmdHistory(msgString);
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
                            switchButton.performClick();
                        }
                        inRecogintion = false;
                    }
                });

                alert.setPositiveButton(getResources().getString(R.string.com_comfirm)
                        , new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        new SendCommandAsyncTask(context, msgString).execute();
                        ChatFragment.this.addCmdHistory(msgString);
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
        MainActivity mainActivity = (MainActivity) getActivity();
        new SendCommandAsyncTask(mainActivity, item).execute();
        //this.addCmdHistory(item.getContent());
    }

}