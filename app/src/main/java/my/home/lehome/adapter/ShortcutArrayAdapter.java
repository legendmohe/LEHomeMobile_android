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
import android.widget.TextView;

import java.util.List;

import my.home.lehome.R;
import my.home.model.entities.Shortcut;

public class ShortcutArrayAdapter extends ArrayAdapter<Shortcut> {

    private TextView chatTextView;

    public void setData(List<Shortcut> items) {
        clear();
        setNotifyOnChange(false);
        if (items != null) {
            for (Shortcut item : items)
                add(item);
        }
        setNotifyOnChange(true);
        notifyDataSetChanged();
    }

    public ShortcutArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.shortcut_item, parent, false);
        }

        Shortcut Shortcut = getItem(position);
        chatTextView = (TextView) row.findViewById(R.id.shortcut_item);
        chatTextView.setText(Shortcut.getContent());

        return row;
    }
}
