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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.List;

import my.home.lehome.R;

/**
 * Created by legendmohe on 15/11/30.
 */
public class MessageAdapter extends ArrayAdapter<Message> {

    public void setData(List<Message> items) {
        clear();
        setNotifyOnChange(false);
        if (items != null) {
            for (Message item : items)
                add(item);
        }
        setNotifyOnChange(true);
        notifyDataSetChanged();
    }

    public MessageAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.message_item, parent, false);

            ViewHolder viewHolder = new ViewHolder();
            viewHolder.titleTextView = convertView.findViewById(R.id.message_item);
            convertView.setTag(viewHolder)
        }

        Message message = getItem(position);
        final ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.titleTextView.setText(message.getContent());

        return row;
    }

    static class ViewHolder {
        TextView titleTextView;
    }
}