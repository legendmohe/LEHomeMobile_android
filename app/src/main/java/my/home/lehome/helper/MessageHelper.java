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

import com.tencent.android.tpush.XGPushManager;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;

import my.home.common.KeyValueStorage;
import my.home.common.PrefKeyValueStorgeImpl;
import my.home.common.PrefUtil;
import my.home.lehome.R;
import my.home.lehome.activity.MainActivity;
import my.home.lehome.fragment.ChatFragment;
import my.home.lehome.util.CommonUtils;
import my.home.lehome.util.Constants;
import my.home.model.entities.ChatItem;
import my.home.model.entities.ChatItemConstants;
import my.home.model.manager.DBStaticManager;

public class MessageHelper {
    public final static String TAG = "MessageHelper";

    private static final int maxNotiLen = 140;
    private static int unreadMsgCount = 0;
    public static String MESSAGE_BEGIN = "";
    public static String MESSAGE_END = "";
    public static boolean inNormalState = true;

    public final static int NOTIFICATION_ID = 1;
    public final static String NOTIFICATION_INTENT_ACTION = "my.home.lehome.helper.MessagerHelper:noti_intent";

    public static void setPushTag(Context context, String tagText) {
//        List<String> tags = PushUtils.getTagsList(tagText);
//        PushManager.setTags(context, tags);
        XGPushManager.setTag(context, tagText);
    }

    public static void delPushTag(Context context, String tagText) {
//        List<String> tags = PushUtils.getTagsList(tagText);
//        PushManager.delTags(context, tags);
        XGPushManager.deleteTag(context, tagText);
    }

    public static void loadPref(Context context) {
        SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean auto_complete_cmd = mySharedPreferences.getBoolean("pref_auto_add_begin_and_end", false);
        if (auto_complete_cmd) {
            MESSAGE_BEGIN = mySharedPreferences.getString("pref_message_begin", "");
            MESSAGE_END = mySharedPreferences.getString("pref_message_end", "");
            if (MESSAGE_BEGIN.endsWith("/")) {
                MESSAGE_BEGIN = CommonUtils.removeLastChar(MESSAGE_BEGIN);
            }
            if (MESSAGE_END.endsWith("/")) {
                MESSAGE_END = CommonUtils.removeLastChar(MESSAGE_END);
            }
        } else {
            MESSAGE_BEGIN = "";
            MESSAGE_END = "";
        }
    }

    public static String getFormatMessage(Context context, String content) {
        if (!inNormalState) {
            return "*" + content;
        }
        content = MESSAGE_BEGIN + content + MESSAGE_END;
        if (!needCorrect(context)) {
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

    public static String getServerURL(Context context, String content) {
        String serverAddress = getServerAddress(context);
        if (TextUtils.isEmpty(serverAddress))
            return null;
        try {
            content = URLEncoder.encode(content, "utf-8");
        } catch (UnsupportedEncodingException e) {
            content = "";
            e.printStackTrace();
        }
        return serverAddress + "/cmd/put/" + content + "?id=" + getDeviceID(context);
    }

    public static String getLocalServerURL(Context context) {
        String localServerAddress = getLocalServerAddress(context);
        if (TextUtils.isEmpty(localServerAddress))
            return null;
        return localServerAddress + "/home/cmd";
    }

    public static String getServerAddress(Context context) {
        return PrefUtil.getStringValue(context, "pref_server_address", "http://lehome.sinaapp.com");
    }

    public static String getLocalServerAddress(Context context) {
        return PrefUtil.getStringValue(context, "pref_local_msg_server_address", "http://192.168.1.111:8000");
    }

    public static String getLocalServerSubscribeURL(Context context) {
        return PrefUtil.getStringValue(context, "pref_local_msg_subscribe_address", "tcp://192.168.1.111:9000");
    }

    public static boolean isLocalMsgPrefEnable(Context context) {
        return PrefUtil.getbooleanValue(context, "pref_enable_local_msg", false);
    }

    public static boolean needCorrect(Context context) {
        return PrefUtil.getbooleanValue(context, "pref_cmd_need_correct", true);
    }

    public static String getDeviceID(Context context) {
        return PrefUtil.getStringValue(context, "pref_bind_device", "");
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

    public static void sendServerMsgToList(int seq, String type, String content, Context context) {
        ChatItem newItem = new ChatItem();
        newItem.setContent(content);
        newItem.setDate(new Date());
        newItem.setSeq(seq);

        if (type.equals("capture")) {
            newItem.setType(ChatItemConstants.TYPE_SERVER_IMAGE);
        } else if (type.equals("bc_loc")) {
            newItem.setType(ChatItemConstants.TYPE_SERVER_LOC);
        } else if (type.equals("client")) {
            newItem.setType(ChatItemConstants.TYPE_ME);
        } else {
            newItem.setType(ChatItemConstants.TYPE_SERVER);
        }

        DBStaticManager.addChatItem(context, newItem);

        if (!MainActivity.VISIBLE) {
            unreadMsgCount++;
            content = content.replaceAll("\\s?", "");
            int len = content.length();
            if (len >= MessageHelper.maxNotiLen) {
                content = context.getString(R.string.noti_bref_msg, content.substring(0, maxNotiLen));
            }
            if (newItem.getType() == ChatItemConstants.TYPE_SERVER_IMAGE) {
                content = context.getString(R.string.noti_bref_new_capture);
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
            Integer[] resultArray = null;
            if (KeyValueStorage.getInstance().hasKey(MSG_SEQ_STORAGE_KEY)) {
                resultArray = (Integer[]) KeyValueStorage.getInstance().getObject(MSG_SEQ_STORAGE_KEY, Integer[].class);
                Arrays.sort(resultArray);
                int idx = Arrays.binarySearch(resultArray, seq);
                if (idx >= 0) {
                    return true;
                }
            }
            LinkedList<Integer> limitedQueue = new LinkedList<>();
            if (resultArray != null && resultArray.length != 0) {
                limitedQueue.addAll(Arrays.asList(resultArray));
            }
            limitedQueue.add(seq);
            while (limitedQueue.size() > Constants.MESSAGE_SEQ_QUEUE_LIMIT) {
                limitedQueue.removeFirst();
            }
            KeyValueStorage.getInstance().putObject(MSG_SEQ_STORAGE_KEY, limitedQueue.toArray(), Integer[].class);
        }
        return false;
    }
}
