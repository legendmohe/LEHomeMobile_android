package my.home.lehome.asynctask;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.net.URI;

import my.home.common.BusProvider;
import my.home.domain.AutoCompleteItemUsecase;
import my.home.domain.AutoCompleteItemUsecaseImpl;
import my.home.lehome.R;
import my.home.model.events.ConfAutoCompleteItemEvent;

/**
 * Created by legendmohe on 15/2/15.
 */
public class LoadAutoCompleteConfAsyncTask extends AsyncTask<String, String, Boolean> {

    public static final String TAG = "LoadAutoCompleteConfAsyncTask";
    public static final String CONF_URL = "http://lehome.sinaapp.com/auto/init";

    private WeakReference<Context> mContext;
    private String mDeviceID;
    private boolean mResult;
    private AutoCompleteItemUsecase mAutoCompleteItemUsecase;

    public LoadAutoCompleteConfAsyncTask(Context context, String deviceID) {
        mContext = new WeakReference<Context>(context);
        mDeviceID = deviceID;
        mAutoCompleteItemUsecase = new AutoCompleteItemUsecaseImpl(context);
    }

    @Override
    protected void onPreExecute() {
        Toast.makeText(
                mContext.get()
                , R.string.pref_loading_auto_item
                , Toast.LENGTH_SHORT)
                .show();
        BusProvider.getRestBusInstance().register(this);
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(String... params) {
        try {
            HttpClient httpclient = new DefaultHttpClient();

            HttpGet request = new HttpGet();
            request.setURI(new URI(CONF_URL + "?id=" + mDeviceID));
            HttpResponse response = httpclient.execute(request);
            String repString = EntityUtils.toString(response.getEntity());

            String repJSON;
            int rep_code;
            try {
                JSONObject jsonObject = new JSONObject(repString);
                rep_code = jsonObject.getInt("code");
                repJSON = jsonObject.getString("data");
            } catch (JSONException e) {
                e.printStackTrace();
                return Boolean.FALSE;
            }

            //run usecase
            new AutoCompleteItemUsecaseImpl(mContext.get())
                    .setMode(AutoCompleteItemUsecase.MODE_SAVE_CONF)
                    .setConfString(repJSON)
                    .execute();
            if (mResult) {
                new AutoCompleteItemUsecaseImpl(mContext.get())
                        .setMode(AutoCompleteItemUsecase.MODE_LOAD_CONF)
                        .execute();
            }
            return mResult;
        } catch (Exception e) {
            Log.e(TAG, "Error in http connection " + e.toString());
        }
        return Boolean.FALSE;
    }

    @Subscribe
    public void onConfAutoCompleteItems(ConfAutoCompleteItemEvent event) {
        if (event.getReturnCode() == ConfAutoCompleteItemEvent.ERROR) {
            mResult = false;
        } else {
            mResult = true;
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        Toast.makeText(
                mContext.get()
                , result == true ? R.string.pref_load_auto_item_success : R.string.pref_load_auto_item_faild
                , Toast.LENGTH_SHORT)
                .show();
        BusProvider.getRestBusInstance().unregister(this);
        super.onPostExecute(result);
    }
}
