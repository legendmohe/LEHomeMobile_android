package my.home.lehome.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

/**
 * Created by legendmohe on 15/2/14.
 */
public class DelayAutoCompleteTextView extends AutoCompleteTextView {

    private static final int MESSAGE_TEXT_CHANGED = 100;
    private static final int DEFAULT_AUTOCOMPLETE_DELAY = 350;

    private int mAutoCompleteDelay = DEFAULT_AUTOCOMPLETE_DELAY;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            DelayAutoCompleteTextView.super.performFiltering((CharSequence) msg.obj, msg.arg1);
        }
    };

    public DelayAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setAutoCompleteDelay(int autoCompleteDelay) {
        mAutoCompleteDelay = autoCompleteDelay;
    }

    public void performFiltering(CharSequence text) {
        DelayAutoCompleteTextView.super.performFiltering(text, 0);
    }

    @Override
    protected void performFiltering(CharSequence text, int keyCode) {
        mHandler.removeMessages(MESSAGE_TEXT_CHANGED);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MESSAGE_TEXT_CHANGED, text), mAutoCompleteDelay);
    }

//    @Override
//    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
//        super.onFocusChanged(focused, direction, previouslyFocusedRect);
//        if (focused && getAdapter() != null) {
//            performFiltering(getText(), 0);
//        }
//    }

//    @Override
//    public boolean enoughToFilter() {
//        return true;
//    }

    @Override
    public void onFilterComplete(int count) {
        super.onFilterComplete(count);
        if (!isPopupShowing())
            showDropDown();
    }
}

