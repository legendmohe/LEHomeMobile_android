package my.home.lehome.fragment;

import android.widget.DatePicker;
import android.widget.TimePicker;

/**
 * Created by legendmohe on 15/2/22.
 */
public interface DateTimePickerFragmentListener {
    public void onTimeSelected(TimePicker view, int hourOfDay, int minute);

    public void onDateSelected(DatePicker view, int year, int month, int day);
}
