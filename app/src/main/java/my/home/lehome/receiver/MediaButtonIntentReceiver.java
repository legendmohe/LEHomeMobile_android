package my.home.lehome.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.widget.Toast;

public class MediaButtonIntentReceiver extends BroadcastReceiver {

    private long sLastClickTime = 0;
    private long DOUBLE_CLICK_DELAY = 350;

    public void onReceive(Context context, Intent intent) {
        String intentAction = intent.getAction();
        if (!Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
            return;
        }
        KeyEvent event = (KeyEvent) intent
                .getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        if (event == null) {
            return;
        }
        int action = event.getAction();

        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_HEADSETHOOK:
                if (action == KeyEvent.ACTION_DOWN) {
                    long time = SystemClock.uptimeMillis();
                    // double click
                    if (time - sLastClickTime < DOUBLE_CLICK_DELAY)
                        // do something
                        Toast.makeText(context, "BUTTON PRESSED DOUBLE!",
                                Toast.LENGTH_SHORT).show();
                        // single click
                    else {
                        // do something
                        Toast.makeText(context, "BUTTON PRESSED!",
                                Toast.LENGTH_SHORT).show();
                    }
                    sLastClickTime = time;
                }
                break;
        }
        abortBroadcast();

    }
}
