package my.home.lehome.mvp.views;

import java.util.List;

import my.home.model.entities.ChatItem;

/**
 * Created by legendmohe on 15/2/21.
 */
public interface ChatItemListView extends MVPView {
    public void onResetDatas(List<ChatItem> chatItems);
}
