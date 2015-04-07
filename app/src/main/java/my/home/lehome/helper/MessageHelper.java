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

package my.home.lehome.helper;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.baidu.android.pushservice.PushManager;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import my.home.common.KeyValueStorage;
import my.home.common.PrefKeyValueStorgeImpl;
import my.home.lehome.R;
import my.home.lehome.activity.MainActivity;
import my.home.lehome.fragment.ChatFragment;
import my.home.lehome.util.Constants;
import my.home.lehome.util.PushUtils;
import my.home.model.entities.ChatItem;
import my.home.model.manager.DBStaticManager;

public class MessageHelper {
    public final static String TAG = "MessageHelper";

    private static final int maxNotiLen = 140;
    private static int unreadMsgCount = 0;
    public static String SERVER_ADDRESS = "";
    public static String LOCAL_SERVER_ADDRESS = "";
    public static String LOCAL_SERVER_SUBSCRIBE_ADDRESS;
    public static String MESSAGE_BEGIN = "";
    public static String MESSAGE_END = "";
    public static String DEVICE_ID = "";
    public static boolean inNormalState = true;
    public static boolean needCorrect = true;
    public static boolean localMsgServiceEnable = false;

    public final static int NOTIFICATION_ID = 1;
    public final static String NOTIFICATION_INTENT_ACTION = "my.home.lehome.helper.MessagerHelper:noti_intent";

    public static void setPushTag(Context context, String tagText) {
        List<String> tags = PushUtils.getTagsList(tagText);
        PushManager.setTags(context, tags);
    }

    public static void delPushTag(Context context, String tagText) {
        List<String> tags = PushUtils.getTagsList(tagText);
        PushManager.delTags(context, tags);
    }

    public static void loadPref(Context context) {
        SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        DEVICE_ID = mySharedPreferences.getString("pref_bind_device", "");
        SERVER_ADDRESS = mySharedPreferences.getString("pref_server_address", "http://lehome.sinaapp.com");
        LOCAL_SERVER_ADDRESS = mySharedPreferences.getString("pref_local_msg_server_address", "http://192.168.1.111:8000");
        LOCAL_SERVER_SUBSCRIBE_ADDRESS = mySharedPreferences.getString("pref_local_msg_subscribe_address", "tcp://192.168.1.111:9000");
        boolean auto_complete_cmd = mySharedPreferences.getBoolean("pref_auto_add_begin_and_end", false);
        if (auto_complete_cmd) {
            MESSAGE_BEGIN = mySharedPreferences.getString("pref_message_begin", "");
            MESSAGE_END = mySharedPreferences.getString("pref_message_end", "");
            if (MESSAGE_BEGIN.endsWith("/")) {
                MESSAGE_BEGIN = CommonHelper.removeLastChar(MESSAGE_BEGIN);
            }
            if (MESSAGE_END.endsWith("/")) {
                MESSAGE_END = CommonHelper.removeLastChar(MESSAGE_END);
            }
        } else {
            MESSAGE_BEGIN = "";
            MESSAGE_END = "";
        }
        needCorrect = mySharedPreferences.getBoolean("pref_cmd_need_correct", true);
        localMsgServiceEnable = mySharedPreferences.getBoolean("pref_enable_local_msg", false);
    }

    public static String getFormatMessage(String content) {
        if (!inNormalState) {
            return "*" + content;
        }
        content = MESSAGE_BEGIN + content + MESSAGE_END;
        if (!needCorrect) {
            content = "*" + content;
        }
        return content;
    }

    public static String getFormatLocalMessage(String content) {
        if (!inNormalState) {
            return content;
        }
        content = MESSAGE_BEGIN + content + MESSAGE_END;
        return content;
    }

    public static String getServerURL(String content) {
        if (TextUtils.isEmpty(SERVER_ADDRESS))
            return null;
        try {
            content = URLEncoder.encode(content, "utf-8");
        } catch (UnsupportedEncodingException e) {
            content = "";
            e.printStackTrace();
        }
        return SERVER_ADDRESS + "/cmd/put/" + content + "?id=" + DEVICE_ID;
    }

    public static String getLocalServerURL() {
        if (TextUtils.isEmpty(LOCAL_SERVER_ADDRESS))
            return null;
        return LOCAL_SERVER_ADDRESS + "/home/cmd";
    }

    public static String getLocalServerSubscribeURL() {
        return LOCAL_SERVER_SUBSCRIBE_ADDRESS;
    }

    public static boolean isLocalMsgServiceEnable() {
        return localMsgServiceEnable;
    }

    public static void resetUnreadCount() {
        unreadMsgCount = 0;
    }

    public static boolean hasUnread() {
        return unreadMsgCount > 0 ? true : false;
    }

    public static void sendToast(String content) {
        Message msg = new Message();
        msg.what = ChatFragment.MSG_TYPE_TOAST;
        msg.obj = content;
        ChatFragment.sendMessage(msg);
    }

    public static void sendServerMsgToList(int seq, String content, Context context) {
        ChatItem newItem = new ChatItem();
        newItem.setContent(content);
        newItem.setIsMe(false);
        newItem.setDate(new Date());
        newItem.setSeq(seq);
        DBStaticManager.addChatItem(context, newItem);

        if (!MainActivity.VISIBLE) {
            unreadMsgCount++;
            content = content.replaceAll("\\s?", "");
            int len = content.length();
            if (len >= MessageHelper.maxNotiLen) {
                content = context.getString(R.string.noti_bref_msg, content.substring(0, maxNotiLen));
            }
            if (unreadMsgCount <= 1) {
                addNotification(
                        context.getString(R.string.noti_new_msg)
                        , content
                        , content
                        , context);
            } else {
                String f_content = context.getString(R.string.noti_num_new_msg, unreadMsgCount) + content;
                addNotification(
                        context.getString(R.string.noti_new_msg)
                        , f_content
                        , content
                        , context
                );
            }
        } else {
            unreadMsgCount = 0;
            Message msg = new Message();
            msg.what = ChatFragment.MSG_TYPE_CHATITEM;
            msg.obj = newItem;
            ChatFragment.sendMessage(msg);
        }
    }

    private static void addNotification(String title, String content, String ticker, Context context) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentText(content)
                .setTicker(ticker)
                .setDefaults(Notification.DEFAULT_ALL);
        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setAction(NOTIFICATION_INTENT_ACTION);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);
        builder.setContentIntent(contentIntent);
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, builder.build());
    }

    // Remove notification
    public static void removeNotification(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(NOTIFICATION_ID);
    }

    private final static String MSG_SEQ_STORAGE_KEY = "MSG_SEQ_STORAGE_KEY";
    private final static Object MSG_SEQ_STORAGE_LOCK_OBJECT = new Object();

    public static boolean enqueueMsgSeq(Context context, int seq) {
        synchronized (MSG_SEQ_STORAGE_LOCK_OBJECT) {
            if (KeyValueStorage.getInstance().getStorageImpl() == null) {
                KeyValueStorage.getInstance().setStorgeImpl(new PrefKeyValueStorgeImpl(context));
            }
            LinkedList<Integer> limitedQueue = null;
            if (KeyValueStorage.getInstance().hasKey(MSG_SEQ_STORAGE_KEY)) {
                limitedQueue = (LinkedList<Integer>) KeyValueStorage.getInstance().getObject(MSG_SEQ_STORAGE_KEY, LinkedList.class);
//                Log.d(TAG, "limitedQueue" + limitedQueue);
                if (limitedQueue != null && limitedQueue.contains(seq))
                    return true;
            }
            if (limitedQueue == null) {
                limitedQueue = new LinkedList<>();
            }
            boolean added = limitedQueue.add(seq);
            while (added && limitedQueue.size() > Constants.MESSAGE_SEQ_QUEUE_LIMIT) {
                limitedQueue.remove();
            }
            KeyValueStorage.getInstance().putObject(MSG_SEQ_STORAGE_KEY, limitedQueue);
        }
        return false;
    }
}
