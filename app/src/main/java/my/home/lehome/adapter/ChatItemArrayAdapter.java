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

package my.home.lehome.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.skyfishjy.library.RippleBackground;

import java.text.SimpleDateFormat;
import java.util.List;

import my.home.lehome.R;
import my.home.lehome.util.Constants;
import my.home.model.entities.ChatItem;

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
        ChatItem chatItem = getItem(position);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (chatItem.getIsMe())
                convertView = inflater.inflate(R.layout.chat_item_onright, parent, false);
            else
                convertView = inflater.inflate(R.layout.chat_item_onleft, parent, false);

            ViewHolder viewHolder = new ViewHolder();
            viewHolder.chatTextView = (TextView) convertView.findViewById(R.id.chat_content_textview);
            viewHolder.rippleBackground = (RippleBackground) convertView.findViewById(R.id.profile_rippleBackground);
            viewHolder.errorButton = (ImageButton) convertView.findViewById(R.id.resend_imagebutton);
            viewHolder.dateTextView = (TextView) convertView.findViewById(R.id.date_textview);
            convertView.setTag(viewHolder);
        }

        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.chatTextView.setText(chatItem.getContent());

        if (chatItem.getIsMe()) {
            viewHolder.rippleBackground.startRippleAnimation();
            if (chatItem.getState() == Constants.CHATITEM_STATE_SUCCESS
                    || !chatItem.getIsMe()) {
                viewHolder.errorButton.setVisibility(View.GONE);
                viewHolder.rippleBackground.stopRippleAnimation();
            } else if (chatItem.getState() == Constants.CHATITEM_STATE_PENDING) {
                viewHolder.errorButton.setVisibility(View.GONE);
                viewHolder.rippleBackground.startRippleAnimation();
            } else {
                viewHolder.errorButton.setVisibility(View.VISIBLE);
                viewHolder.rippleBackground.stopRippleAnimation();
                if (mResendButtonClickListener != null) {
                    final int pos = position;
                    viewHolder.errorButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mResendButtonClickListener.onResendButtonClicked(pos);
                        }
                    });
                }
            }
        }

        String dateString = getTimeWithFormat(position);
        if (dateString != null) {
            viewHolder.dateTextView.setText(dateString);
            viewHolder.dateTextView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.dateTextView.setVisibility(View.INVISIBLE);
        }


        return convertView;
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

    static class ViewHolder {
        TextView chatTextView;
        RippleBackground rippleBackground;
        ImageButton errorButton;
        TextView dateTextView;
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