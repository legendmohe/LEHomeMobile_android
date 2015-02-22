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

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import java.lang.ref.WeakReference;
import java.util.Calendar;

/**
 * Created by legendmohe on 15/2/22.
 */
public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    WeakReference<DateTimePickerFragmentListener> listenerWeakReference;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void setDateTimePickerFragmentListener(DateTimePickerFragmentListener listener) {
        listenerWeakReference = new WeakReference<DateTimePickerFragmentListener>(listener);
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if (listenerWeakReference.get() != null && view.isShown()) {
            listenerWeakReference.get().onTimeSelected(view, hourOfDay, minute);
        }
    }
}
