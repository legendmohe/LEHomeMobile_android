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

package my.home.lehome.asynctask;

import android.os.AsyncTask;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.List;

import my.home.lehome.R;
import my.home.lehome.adapter.ChatItemArrayAdapter;
import my.home.lehome.fragment.ChatFragment;
import my.home.model.entities.ChatItem;
import my.home.model.manager.DBStaticManager;

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
        return DBStaticManager.loadBefore(this.fragment.getActivity(), currentId, params[0]);
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
