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

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;

import my.home.common.FourStateHandler;
import my.home.lehome.R;

/**
 * Created by legendmohe on 15/5/29.
 */
public class LoadLongMsgAsyncTask extends AsyncTask<String, Long, Integer> {
    private static final String TAG = "LoadLongMsgAsyncTask";

    FourStateHandler mHandler;
    WeakReference<Context> mContext;

    public LoadLongMsgAsyncTask(FourStateHandler handler, Context context) {
        super();
        mHandler = handler;
        mContext = new WeakReference<>(context);
    }

    @Override
    protected void onPreExecute() {
        if (mHandler != null)
            mHandler.start();
    }

    @Override
    protected Integer doInBackground(String... params) {
        String url = params[0];
        Log.d(TAG, "background task url: " + url);
        if (TextUtils.isEmpty(url))
            return 400;
        HttpResponse response = null;
        String content = null;
        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet();
            request.setURI(new URI(url));
            response = client.execute(request);
            content = EntityUtils.toString(response.getEntity());
        } catch (URISyntaxException | IOException e) {
            mHandler.getWhat().putString("err_msg", mContext.get().getString(R.string.long_msg_http_exception));
            return 400;
        }

        JSONTokener jsonParser = new JSONTokener(content);
        try {
            JSONObject cmdObject = (JSONObject) jsonParser.nextValue();
            content = cmdObject.getString("data");
        } catch (Exception e) {
            mHandler.getWhat().putString("err_msg", mContext.get().getString(R.string.long_msg_parse_exception));
            return 400;
        }

        content = content.substring(4);
        try {
            content = URLDecoder.decode(content, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            mHandler.getWhat().putString("err_msg", mContext.get().getString(R.string.long_msg_parse_exception));
            return 400;
        }

        mHandler.getWhat().putString("response", content);
        return 200;
    }

    @Override
    protected void onPostExecute(Integer repCode) {
        if (mHandler == null) {
            return;
        }

        if (repCode == 200) {
            mHandler.complete(true);
        } else {
            mHandler.complete(false);
        }
        mHandler = null;
    }

    @Override
    protected void onCancelled() {
        if (mHandler == null) {
            return;
        }

        mHandler.cancel();
        mHandler = null;
    }
}
