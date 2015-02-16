package my.home.model.datasource;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import my.home.common.BusProvider;
import my.home.model.entities.AutoCompleteItem;
import my.home.model.events.ConfAutoCompleteItemEvent;
import my.home.model.events.GetAutoCompleteItemEvent;

/**
 * Created by legendmohe on 15/2/13.
 */
public class AutoCompleteItemDataSourceImpl implements AutoCompleteItemDataSource {

    public static final String TAG = "AutoCompleteItemDataSourceImpl";

    public static AutoCompleteItemDataSourceImpl INSTANCE;

    private Map mNodes;
    private Map mLinks;
    private String mMessageSeq;
    private String mInitState;
    private boolean mLoadSuccess = false;

    public static AutoCompleteItemDataSourceImpl getInstance() {

        if (INSTANCE == null)
            INSTANCE = new AutoCompleteItemDataSourceImpl();

        return INSTANCE;
    }

    private AutoCompleteItemDataSourceImpl() {
        super();
    }

    @Override
    public void saveConf(Context context, String confJSONString) {
        if (context == null) {
            Log.e(TAG, "invaild mcontext.");
            BusProvider.getRestBusInstance().post(new ConfAutoCompleteItemEvent(ConfAutoCompleteItemEvent.ERROR));
            return;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(CONF_KEY, confJSONString);
        editor.apply();
        BusProvider.getRestBusInstance().post(new ConfAutoCompleteItemEvent(ConfAutoCompleteItemEvent.SUCCESS));
    }

    @Override
    public void loadConf(Context context) {
        if (context == null) {
            Log.e(TAG, "invaild mcontext.");
            BusProvider.getRestBusInstance().post(new ConfAutoCompleteItemEvent(ConfAutoCompleteItemEvent.ERROR));
            return;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE);
        if (sharedPreferences == null) {
            Log.e(TAG, "invaild share preference.");
            BusProvider.getRestBusInstance().post(new ConfAutoCompleteItemEvent(ConfAutoCompleteItemEvent.ERROR));
            return;
        }
        String confSrc = sharedPreferences.getString(CONF_KEY, "{}");
        if (initConf(confSrc)) {
            BusProvider.getRestBusInstance().post(new ConfAutoCompleteItemEvent(ConfAutoCompleteItemEvent.SUCCESS));
        } else {
            BusProvider.getRestBusInstance().post(new ConfAutoCompleteItemEvent(ConfAutoCompleteItemEvent.ERROR));
        }
    }

    private boolean initConf(String confSrc) {
        try {
            JSONObject confJSONObject = new JSONObject(confSrc);

            mNodes = new HashMap();
            mLinks = new HashMap();

            JSONObject jLinks = confJSONObject.getJSONObject("links");
            for (Iterator iter = jLinks.keys(); iter.hasNext(); ) {
                String key = (String) iter.next();
                JSONArray vals = jLinks.getJSONArray(key);
                List linkValue = new ArrayList();
                for (int i = 0; i < vals.length(); i++) {
                    linkValue.add(vals.get(i));
                }
                mLinks.put(key, linkValue);
            }

            JSONObject jNodes = confJSONObject.getJSONObject("nodes");
            for (Iterator iter = jNodes.keys(); iter.hasNext(); ) {
                String key = (String) iter.next();
                JSONArray vals = jNodes.getJSONArray(key);
                List nodeValue = new ArrayList();
                for (int i = 0; i < vals.length(); i++) {
                    nodeValue.add(vals.get(i));
                }
                mNodes.put(key, nodeValue);
            }

            mMessageSeq = confJSONObject.getString("message_seq");
            mInitState = confJSONObject.getString("init_state");
            mNodes.put("message", new ArrayList<String>(Arrays.asList(mMessageSeq)));

            mLoadSuccess = true;
            return true;
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing data " + e.toString());
            mLoadSuccess = false;
            return false;
        }
    }

    @Override
    public void getAutoCompleteItems(String currentInput) {
        List resultList = new ArrayList();
        if (mLoadSuccess == false)
            BusProvider.getRestBusInstance().post(new GetAutoCompleteItemEvent(resultList));

        boolean in_msg_state = false;
        StringBuffer inputBuffer = new StringBuffer(currentInput);
        StringBuffer cmdBuffer = new StringBuffer();
        String curState = mInitState;
        while (inputBuffer.length() > 0) {
            if (in_msg_state) {
                if (inputBuffer.toString().startsWith(mMessageSeq)) {
                    in_msg_state = false;
                }
                cmdBuffer.append(inputBuffer.charAt(0));
                inputBuffer.deleteCharAt(0);
                continue;
            }
            boolean found = false;
            String tempInput = inputBuffer.toString();
            if (!mLinks.containsKey(curState)) {
                BusProvider.getRestBusInstance().post(new GetAutoCompleteItemEvent(resultList));
                return;
            }
            for (String nextState : (List<String>) mLinks.get(curState)) {
                for (String val : (List<String>) mNodes.get(nextState)) {
                    if (tempInput.startsWith(val)) {
                        curState = nextState;
                        inputBuffer.delete(0, val.length());
                        cmdBuffer.append(val);
                        found = true;
                        if (curState.equals("message")) {
                            in_msg_state = true;
                        }
                        break;
                    }
                }
                if (found == true) break;
            }
            if (found == false) break;
        }
        if (in_msg_state == true) {
            String cmd = cmdBuffer.append(mMessageSeq).toString();
            resultList.add(new AutoCompleteItem("message", 1.0f, mMessageSeq, cmd));
        } else if (inputBuffer.length() == 0) {
            for (String nextState : (List<String>) mLinks.get(curState)) {
                for (String val : (List<String>) mNodes.get(nextState)) {
                    String cmd = cmdBuffer.toString() + val;
                    resultList.add(new AutoCompleteItem(nextState, 1.0f, val, cmd));
                }
            }
        } else {
            String tempInput = inputBuffer.toString();
            for (String nextState : (List<String>) mLinks.get(curState)) {
                for (String val : (List<String>) mNodes.get(nextState)) {
                    if (val.startsWith(tempInput)) {
                        String cmd = cmdBuffer.toString() + val;
                        resultList.add(new AutoCompleteItem(nextState, 1.0f, val, cmd));
                    }
                }
            }
        }
        BusProvider.getRestBusInstance().post(new GetAutoCompleteItemEvent(resultList));
    }
}
