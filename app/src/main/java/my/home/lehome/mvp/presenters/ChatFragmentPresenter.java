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

package my.home.lehome.mvp.presenters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.otto.Subscribe;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

import my.home.common.BusProvider;
import my.home.domain.events.DSaveAutoCompleteLocalHistoryEvent;
import my.home.domain.events.DShowCmdSuggestionEvent;
import my.home.domain.usecase.MarkCurrentInputUsecaseImpl;
import my.home.lehome.asynctask.SaveCaptureAsyncTask;
import my.home.lehome.helper.LocationHelper;
import my.home.lehome.helper.MessageHelper;
import my.home.lehome.mvp.views.ChatItemListView;
import my.home.lehome.mvp.views.ChatSuggestionView;
import my.home.lehome.mvp.views.SaveLocalHistoryView;
import my.home.lehome.service.SendMsgIntentService;
import my.home.lehome.util.CommonUtils;
import my.home.lehome.util.Constants;
import my.home.model.entities.AutoCompleteItem;
import my.home.model.entities.ChatItem;
import my.home.model.entities.ChatItemConstants;
import my.home.model.manager.DBStaticManager;

/**
 * Created by legendmohe on 15/2/19.
 */
public class ChatFragmentPresenter extends MVPPresenter {

    private WeakReference<SaveLocalHistoryView> mSaveLocalHistoryView;
    private WeakReference<ChatItemListView> mChatItemListView;
    private WeakReference<ChatSuggestionView> mChatSuggestionView;

    public static Handler SendMsgHandler;
    private BgMessageBroadcastReceiver mbgMessageBroadcastReceiver = new BgMessageBroadcastReceiver();

    private static class IntentServiceHandler extends Handler {
        private final WeakReference<ChatItemListView> mChatItemListView;

        public IntentServiceHandler(ChatItemListView listView) {
            mChatItemListView = new WeakReference<>(listView);
        }


        @Override
        public void handleMessage(Message msg) {
            ChatItemListView listView = mChatItemListView.get();
            if (listView != null) {
                Bundle bundle = msg.getData();
                bundle.setClassLoader(ChatItem.class.getClassLoader());
                ChatItem item;
                switch (msg.what) {
                    case SendMsgIntentService.MSG_BEGIN_SENDING:
                        item = bundle.getParcelable("item");
                        if (bundle.getBoolean("update", false)) {
                            listView.onChatItemRequest(item, true);
                        } else {
                            listView.onChatItemRequest(item, false);
                        }
                        break;
                    case SendMsgIntentService.MSG_END_SENDING:
                        int rep_code = bundle.getInt("rep_code", -1);
                        item = bundle.getParcelable("item");
                        if (rep_code == 200) {
                            listView.onChatItemResponse(rep_code, item.getId(), item.getState(), null);
                        } else {
                            ChatItem newItem = bundle.getParcelable("new_item");
                            listView.onChatItemResponse(rep_code, item.getId(), item.getState(), newItem);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public ChatFragmentPresenter(SaveLocalHistoryView saveLocalHistoryView
            , ChatItemListView chatItemListView
            , ChatSuggestionView chatSuggestionView) {
        this.mSaveLocalHistoryView = new WeakReference<>(saveLocalHistoryView);
        this.mChatItemListView = new WeakReference<>(chatItemListView);
        this.mChatSuggestionView = new WeakReference<>(chatSuggestionView);

        SendMsgHandler = new IntentServiceHandler(chatItemListView);
    }

    public void markAndSendCurrentInput(String input, boolean local) {
        if (mChatItemListView.get() == null)
            return;
//        new SendCommandAsyncTask(mChatItemListView.get().getContext(), input, local).execute();
        Context context = mChatItemListView.get().getContext();
        String message;
        String serverURL;
        if (local) {
            message = MessageHelper.getFormatLocalMessage(context, input);
            serverURL = MessageHelper.getLocalServerURL(mChatItemListView.get().getContext());
        } else {
            message = MessageHelper.getFormatMessage(context, input);
            serverURL = MessageHelper.getServerURL(context, message);
        }
        Intent serviceIntent = new Intent(context, SendMsgIntentService.class);
        serviceIntent.putExtra("local", local);
        serviceIntent.putExtra("cmdString", message);
        serviceIntent.putExtra("cmd", input.trim());
        serviceIntent.putExtra("serverUrl", serverURL);
        serviceIntent.putExtra("deviceID", MessageHelper.getDeviceID(context));
        serviceIntent.putExtra("messenger", new Messenger(ChatFragmentPresenter.SendMsgHandler));
        mChatItemListView.get().getContext().startService(serviceIntent);
        new MarkCurrentInputUsecaseImpl(mSaveLocalHistoryView.get().getContext(), input.trim()).execute();
    }

    public void markAndSendCurrentChatItem(ChatItem chatItem, boolean local) {
        if (mChatItemListView.get() == null)
            return;
//        new SendCommandAsyncTask(mChatItemListView.get().getContext(), chatItem, local).execute();
        Context context = mChatItemListView.get().getContext();
        String message;
        String serverURL;
        if (local) {
            message = MessageHelper.getFormatLocalMessage(context, chatItem.getContent());
            serverURL = MessageHelper.getLocalServerURL(mChatItemListView.get().getContext());
        } else {
            message = MessageHelper.getFormatMessage(context, chatItem.getContent());
            serverURL = MessageHelper.getServerURL(context, message);
        }
        Intent serviceIntent = new Intent(context, SendMsgIntentService.class);
        serviceIntent.putExtra("local", local);
        serviceIntent.putExtra("update", chatItem);
        serviceIntent.putExtra("cmdString", message);
        serviceIntent.putExtra("cmd", chatItem.getContent().trim());
        serviceIntent.putExtra("serverUrl", serverURL);
        serviceIntent.putExtra("deviceID", MessageHelper.getDeviceID(context));
        serviceIntent.putExtra("messenger", new Messenger(ChatFragmentPresenter.SendMsgHandler));
        mChatItemListView.get().getContext().startService(serviceIntent);
        new MarkCurrentInputUsecaseImpl(mSaveLocalHistoryView.get().getContext(), chatItem.getContent().trim()).execute();

//        if (mChatItemListView.get() != null)
//            mChatItemListView.get().onChatItemRequest(item, true);
    }

//    public void saveSaveLocalHistory() {
//        new SaveAutoCompleteLocalHistoryUsecaseImpl(mSaveLocalHistoryView.get().getContext()).execute();
//    }

    public void resetDatas(Context context) {
        List<ChatItem> chatItems = DBStaticManager.loadLatest(context, Constants.CHATITEM_LOAD_LIMIT);
        if (chatItems != null) {
            Collections.reverse(chatItems); // reverse descend items
            mChatItemListView.get().onResetDatas(chatItems);
        }
    }

    public void showNextCmdSuggestion(List<AutoCompleteItem> results, AutoCompleteItem item) {
        for (int i = 0; i < results.size() - 1; i++) {
            if (results.get(i).equals(item)) {
                item = results.get(i + 1);
                break;
            }
        }
        if (mChatSuggestionView.get() != null) {
            mChatSuggestionView.get().onShowSuggestion(item);
        }
    }

    public void showPreCmdSuggestion(List<AutoCompleteItem> results, AutoCompleteItem item) {
        if (item == null)
            return;
        for (int i = 1; i < results.size(); i++) {
            if (results.get(i).equals(item)) {
                item = results.get(i - 1);
                break;
            }
        }
        if (mChatSuggestionView.get() != null) {
            mChatSuggestionView.get().onShowSuggestion(item);
        }
    }

    @Override
    public void start() {
        BusProvider.getRestBusInstance().register(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SendMsgIntentService.ACTION_SEND_MSG_BEGIN);
        intentFilter.addAction(SendMsgIntentService.ACTION_SEND_MSG_END);
        Context context = mChatItemListView.get().getContext();
        context.registerReceiver(mbgMessageBroadcastReceiver, intentFilter);
    }

    @Override
    public void stop() {
        Context context = mChatItemListView.get().getContext();
        context.unregisterReceiver(mbgMessageBroadcastReceiver);
        BusProvider.getRestBusInstance().unregister(this);
    }

    @Subscribe
    public void onSaveAutoCompleteLocalHistoryItems(DSaveAutoCompleteLocalHistoryEvent event) {
        if (event.getReturnCode() == DSaveAutoCompleteLocalHistoryEvent.SUCCESS)
            this.mSaveLocalHistoryView.get().onSaveLocalHistoryFinish(true);
        else
            this.mSaveLocalHistoryView.get().onSaveLocalHistoryFinish(false);
    }

    @Subscribe
    public void onShowCmdSuggestionEvent(DShowCmdSuggestionEvent event) {
        if (mChatSuggestionView.get() != null) {
            mChatSuggestionView.get().onShowSuggestion(event.getItem());
        }
    }

//    @Subscribe
//    public void onGetAutoCompleteItems(DShowAutoCompleteItemEvent event) {
//        if (mChatSuggestionView.get() != null) {
//            mChatSuggestionView.get().onGetAutoCompleteItems(event.getResultList());
//        }
//    }

    public void saveImageItem(ChatItem item) {
        if (mChatItemListView.get() == null
                || item == null
                || item.getType() != ChatItemConstants.TYPE_SERVER_IMAGE)
            return;
        String image_url = item.getContent();
        String path = ImageLoader.getInstance().getDiskCache().get(image_url).getAbsolutePath();
//        String fileName = new File(image_url).getName();
        String fileName = CommonUtils.getDateFormatString("yyyy-MM-dd_hh-mm-ss") + ".jpg";
        new SaveCaptureAsyncTask(mChatItemListView.get().getContext()).execute(path, fileName);
    }

    public void openLocationInBrowser(ChatItem item) {
        if (mChatItemListView.get() == null)
            return;
        String src = item.getContent();
        final String[] location = LocationHelper.parseLocationFromSrc(src);
        final String latitude = location[2];
        final String longitude = location[3];
        Intent openIntent = LocationHelper.getBaiduMapUrlIntent(
                longitude, latitude,
                location[0], location[1],
                "lehome", "lehome"
        );
        mChatItemListView.get().getContext().startActivity(openIntent);
    }

    public void copyLocationInfo(ChatItem item, String label) {
        if (mChatItemListView.get() == null)
            return;

        Context context = mChatItemListView.get().getContext();
        String src = item.getContent();
        final String[] location = LocationHelper.parseLocationFromSrc(src);
        CommonUtils.copyStringToClipboard(context, label, location[1]);
    }
    
    private class BgMessageBroadcastReceiver extends BroadcastReceiver {
 
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mChatItemListView.get() == null)
                return;
                
            Bundle bundle = intent.getExtras();
            bundle.setClassLoader(ChatItem.class.getClassLoader());
            ChatItemListView listView = mChatItemListView.get();
            ChatItem item;
            
            if (intent.getAction().equals(SendMsgIntentService.ACTION_SEND_MSG_BEGIN)) {
                item = bundle.getParcelable("item");
                if (bundle.getBoolean("update", false)) {
                    listView.onChatItemRequest(item, true);
                } else {
                    listView.onChatItemRequest(item, false);
                }
            } else if (intent.getAction().equals(SendMsgIntentService.ACTION_SEND_MSG_END)) {
                int rep_code = bundle.getInt("rep_code", -1);
                item = bundle.getParcelable("item");
                if (rep_code == 200) {
                    listView.onChatItemResponse(rep_code, item.getId(), item.getState(), null);
                } else {
                    ChatItem newItem = bundle.getParcelable("new_item");
                    listView.onChatItemResponse(rep_code, item.getId(), item.getState(), newItem);
                }
            }
        }
    }
}
