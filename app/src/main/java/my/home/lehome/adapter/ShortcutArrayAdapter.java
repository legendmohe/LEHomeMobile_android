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

    @Override
    public void add(Shortcut object) {
        super.add(object);
    }

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
