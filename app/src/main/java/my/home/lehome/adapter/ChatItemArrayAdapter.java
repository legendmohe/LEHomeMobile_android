package my.home.lehome.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.skyfishjy.library.RippleBackground;

import java.text.SimpleDateFormat;
import java.util.List;

import my.home.common.Constants;
import my.home.entities.ChatItem;
import my.home.lehome.R;

public class ChatItemArrayAdapter extends ArrayAdapter<ChatItem> {

    private static final int TYPE_CHATTO = 0;
    private static final int TYPE_CHATFROM = 1;

    private ResendButtonClickListener mResendButtonClickListener;
    private TextView chatTextView;

    @Override
    public void add(ChatItem object) {
        super.add(object);
    }

    public void setData(List<ChatItem> items) {
        clear();
        setNotifyOnChange(false);
        if (items != null) {
            for (ChatItem item : items)
                add(item);
        }
        setNotifyOnChange(true);
        notifyDataSetChanged();
    }

    public ChatItemArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position).getIsMe()) {
            return TYPE_CHATTO;
        } else {
            return TYPE_CHATFROM;
        }
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ChatItem chatItem = getItem(position);
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (chatItem.getIsMe())
                row = inflater.inflate(R.layout.chat_item_onright, parent, false);
            else
                row = inflater.inflate(R.layout.chat_item_onleft, parent, false);

        }

        RelativeLayout wrapper = (RelativeLayout) row.findViewById(R.id.wrapper);
        chatTextView = (TextView) row.findViewById(R.id.chat_content_textview);
        chatTextView.setText(chatItem.getContent());

        if (chatItem.getIsMe()) {
            RippleBackground rippleBackground = (RippleBackground) row.findViewById(R.id.profile_rippleBackground);
            rippleBackground.startRippleAnimation();
            ImageButton errorButton = (ImageButton) row.findViewById(R.id.resend_imagebutton);
//            ProgressBar pendingButton = (ProgressBar) row.findViewById(R.id.pending_progressbar);
            if (chatItem.getState() == Constants.CHATITEM_STATE_SUCCESS
                    || !chatItem.getIsMe()) {
                errorButton.setVisibility(View.GONE);
//                rippleBackground.setVisibility(View.GONE);
                rippleBackground.stopRippleAnimation();
            } else if (chatItem.getState() == Constants.CHATITEM_STATE_PENDING) {
                errorButton.setVisibility(View.GONE);
//                rippleBackground.setVisibility(View.VISIBLE);
                rippleBackground.startRippleAnimation();
            } else {
                errorButton.setVisibility(View.VISIBLE);
//                rippleBackground.setVisibility(View.GONE);
                rippleBackground.stopRippleAnimation();
                if (mResendButtonClickListener != null) {
                    final int pos = position;
                    errorButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mResendButtonClickListener.onResendButtonClicked(pos);
                        }
                    });
                }
            }
        }

        TextView dateTextView = (TextView) row.findViewById(R.id.date_textview);
        String dateString = getTimeWithFormat(position);
        if (dateString != null) {
            dateTextView.setText(dateString);
            dateTextView.setVisibility(View.VISIBLE);
        } else {
            dateTextView.setVisibility(View.INVISIBLE);
        }


        return row;
    }

    private String getTimeWithFormat(int position) {
        ChatItem chatItem = getItem(position);
        String formatString = "MM月dd日 hh时mm分";
        SimpleDateFormat df = new SimpleDateFormat(formatString);
        if (position == 0) {
            return df.format(chatItem.getDate());
        } else {
            ChatItem preItem = getItem(position - 1);
            if (chatItem.getDate().getTime() - preItem.getDate().getTime() > Constants.SHOW_DATE_TEXTVIEW_GAP) {
                return df.format(chatItem.getDate());
            } else {
                return null;
            }
        }
    }

	/*
     * delegate
	 */

    public ResendButtonClickListener getResendButtonClickListener() {
        return mResendButtonClickListener;
    }

    public void setResendButtonClickListener(
            ResendButtonClickListener mResendButtonClickListener) {
        this.mResendButtonClickListener = mResendButtonClickListener;
    }

    public interface ResendButtonClickListener {
        public void onResendButtonClicked(int pos);
    }
}