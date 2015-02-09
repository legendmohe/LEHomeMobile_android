package my.home.lehome.asynctask;

import android.os.AsyncTask;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.List;

import my.home.lehome.R;
import my.home.lehome.adapter.ChatItemArrayAdapter;
import my.home.lehome.fragment.ChatFragment;
import my.home.lehome.helper.DBHelper;
import my.home.model.ChatItem;

public class LoadMoreChatItemAsyncTask extends
        AsyncTask<Integer, String, List<ChatItem>> {

    private ChatFragment fragment;

    public LoadMoreChatItemAsyncTask(ChatFragment fragment) {
        this.fragment = fragment;
    }

    @Override
    protected List<ChatItem> doInBackground(Integer... params) {
        if (params[0] <= 0) {
            return null;
        }
        long currentId = fragment.getAdapter().getItem(0).getId();
        return DBHelper.loadBefore(this.fragment.getActivity(), currentId, params[0]);
    }

    @Override
    protected void onPreExecute() {
        ProgressBar progressBar = (ProgressBar) fragment.getActivity().findViewById(R.id.load_more_progressbar);
        progressBar.setVisibility(View.VISIBLE);
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(List<ChatItem> result) {
        ChatItemArrayAdapter adapter = fragment.getAdapter();
        adapter.setNotifyOnChange(false);
        for (ChatItem chatItem : result) {
            adapter.insert(chatItem, 0);
        }
        adapter.setNotifyOnChange(true);
        adapter.notifyDataSetChanged();

        ListView listView = (ListView) fragment.getView().findViewById(R.id.chat_list);
        listView.setSelection(result.size());

        ProgressBar progressBar = (ProgressBar) fragment.getActivity().findViewById(R.id.load_more_progressbar);
        progressBar.setVisibility(View.INVISIBLE);
        super.onPostExecute(result);
    }

}
