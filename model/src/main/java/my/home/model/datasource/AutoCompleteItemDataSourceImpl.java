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

package my.home.model.datasource;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import my.home.common.BusProvider;
import my.home.model.entities.AutoCompleteCountHolder;
import my.home.model.entities.AutoCompleteItem;
import my.home.model.entities.AutoCompleteToolItem;
import my.home.model.events.MConfAutoCompleteItemEvent;
import my.home.model.events.MGetAutoCompleteItemEvent;
import my.home.model.events.MSaveAutoCompleteLocalHistoryEvent;

/**
 * Created by legendmohe on 15/2/13.
 */
public class AutoCompleteItemDataSourceImpl implements AutoCompleteItemDataSource {

    public static final String TAG = "ACItemDataSource";

    public static final float DEFAULT_AUTOCOMPLETE_WEIGHT = 0.0f;

    public static AutoCompleteItemDataSourceImpl INSTANCE;

    private Map<String, List<String>> mNodes;
    private Map<String, List<String>> mLinks;
    private String mMessageSeq;
    private String mTimeSeq;
    private String mInitState;
    private boolean mLoadSuccess = false;
    private Map<String, AutoCompleteCountHolder> mAutoCompleteCountHolderMap;
    private HashMap<String, Integer> mWeightDivides;

    private static Comparator<AutoCompleteItem> mResultComparator;

    static {
        mResultComparator = new Comparator<AutoCompleteItem>() {
            @Override
            public int compare(AutoCompleteItem lhs, AutoCompleteItem rhs) {
                return Float.compare(rhs.getWeight(), lhs.getWeight());
            }
        };
    }


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
            BusProvider.getRestBusInstance().post(new MConfAutoCompleteItemEvent(MConfAutoCompleteItemEvent.ERROR));
            return;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(CONF_KEY_GET_AUTOCOMPLETE_ITEM, confJSONString);
        editor.apply();
        BusProvider.getRestBusInstance().post(new MConfAutoCompleteItemEvent(MConfAutoCompleteItemEvent.SUCCESS));
    }

    @Override
    public void loadConf(Context context) {
        if (context == null) {
            Log.e(TAG, "invaild mcontext.");
            BusProvider.getRestBusInstance().post(new MConfAutoCompleteItemEvent(MConfAutoCompleteItemEvent.ERROR));
            return;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE);
        if (sharedPreferences == null) {
            Log.e(TAG, "invaild share preference.");
            BusProvider.getRestBusInstance().post(new MConfAutoCompleteItemEvent(MConfAutoCompleteItemEvent.ERROR));
            return;
        }
        String confSrc = sharedPreferences.getString(CONF_KEY_GET_AUTOCOMPLETE_ITEM, "{}");
        if (initConf(confSrc)) {
            loadAutoCompleteLocalHistory(context);
            BusProvider.getRestBusInstance().post(new MConfAutoCompleteItemEvent(MConfAutoCompleteItemEvent.SUCCESS));
        } else {
            BusProvider.getRestBusInstance().post(new MConfAutoCompleteItemEvent(MConfAutoCompleteItemEvent.ERROR));
        }
    }

    private boolean initConf(String confSrc) {
        try {
            JSONObject confJSONObject = new JSONObject(confSrc);

            mNodes = new HashMap<>();
            mLinks = new HashMap<>();

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
            mTimeSeq = confJSONObject.getString("time_seq");
            mInitState = confJSONObject.getString("init_state");
            mNodes.put("message", new ArrayList<String>(Arrays.asList(mMessageSeq)));
            mNodes.put("time", new ArrayList<String>(Arrays.asList(mTimeSeq)));
            mNodes.put("trigger", new ArrayList<String>());
            mNodes.put("finish", new ArrayList<String>());

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
        if (!mLoadSuccess) {
            BusProvider.getRestBusInstance().post(new MGetAutoCompleteItemEvent(new ArrayList()));
            return;
        }

        boolean in_msg_or_time_ind_state = false;
        boolean in_msg_or_time_state = false;
        boolean in_if_or_while_state = false;
        String leftString = "";
        String lastString = "";
        String lastState = "";
        StringBuffer cmdBuffer = new StringBuffer();
        StringBuffer inputBuffer = new StringBuffer(currentInput);
        String curState = mInitState;
        while (inputBuffer.length() > 0) {
            if (in_msg_or_time_ind_state) {
                if (inputBuffer.toString().startsWith(mMessageSeq)
                        || inputBuffer.toString().startsWith(mTimeSeq)) {
                    in_msg_or_time_ind_state = false;
                }
                cmdBuffer.append(inputBuffer.charAt(0));
                inputBuffer.deleteCharAt(0);
                continue;
            }
            boolean found = false;
            String tempInput = inputBuffer.toString();
            if (!mLinks.containsKey(curState)) {
                BusProvider.getRestBusInstance().post(new MGetAutoCompleteItemEvent(new ArrayList()));
                return;
            }
            for (String nextState : mLinks.get(curState)) {
                if (!in_if_or_while_state) {
                    if (nextState.equals("then")
                            || nextState.equals("compare")
                            || nextState.equals("logical"))
                        continue;
                }
                for (String val : mNodes.get(nextState)) {
                    if (tempInput.startsWith(val)) {
                        lastString = val;
                        leftString = inputBuffer.toString();
                        curState = nextState;
                        lastState = nextState;
                        inputBuffer.delete(0, val.length());
                        cmdBuffer.append(val);
                        found = true;
                        in_msg_or_time_state = false;

                        if (curState.equals("message") || curState.equals("time")) {
                            in_msg_or_time_ind_state = true;
                        } else if (curState.equals("if") || curState.equals("while")) {
                            in_if_or_while_state = true;
                        } else if (curState.equals("then")) {
                            in_if_or_while_state = false;
                        }
                        break;
                    } else if (nextState.equals("message") && val.startsWith(tempInput)) {
                        lastState = nextState;
                    }
                }
                if (inputBuffer.length() == 0)
                    break;
            }
            if (inputBuffer.length() == 0)
                break;
            if (found)
                continue;
            if (curState.equals("message") || mLinks.get(curState).contains("message")) {
                in_msg_or_time_state = true;
                found = true;
                curState = "message";
                cmdBuffer.append(inputBuffer.charAt(0));
                inputBuffer.deleteCharAt(0);
            } else if (curState.equals("time") || mLinks.get(curState).contains("time")) {
                in_msg_or_time_state = true;
                found = true;
                curState = "time";
                cmdBuffer.append(inputBuffer.charAt(0));
                inputBuffer.deleteCharAt(0);
            }
            if (!found)
                break;
        }

        String cmdString = cmdBuffer.toString();
        Set<AutoCompleteItem> resultSet = new HashSet<>();

        if (in_msg_or_time_ind_state) {
            String cmd = cmdString + mMessageSeq;
            if (curState.equals("time")) {
                addTimeToolItemToResult(resultSet);
                addDateToolItemToResult(resultSet);
            }
            addFavorToolItemToResult(resultSet);
            resultSet.add(new AutoCompleteItem(curState, 1.0f, mMessageSeq, cmd));
        } else if (inputBuffer.length() == 0) {
            String tempLeft = new StringBuilder(leftString).delete(0, lastString.length()).toString();
            if (tempLeft.length() != 0) {
                if (curState.equals("message") || curState.equals("time")) {
                    for (String nextState : mLinks.get(lastState)) {
                        for (String val : mNodes.get(nextState)) {
                            if (val.startsWith(tempLeft)) {
                                String tempCmd = new StringBuilder(val).delete(0, tempLeft.length()).toString();
                                String cmd = cmdString + tempCmd;
                                resultSet.add(new AutoCompleteItem(nextState, Float.MAX_VALUE, val, cmd));
                            }
                        }
                    }
                } else {
                    for (String val : mNodes.get(curState)) {
                        if (val.startsWith(tempLeft) && !val.equals(tempLeft)) {
                            String tempCmd = new StringBuilder(val).delete(0, lastString.length()).toString();
                            String cmd = cmdString + tempCmd;
                            resultSet.add(new AutoCompleteItem(curState, Float.MAX_VALUE, val, cmd));
                        }
                    }
                }
            } else if (leftString.equals(lastString)) {
                for (String val : mNodes.get(curState)) {
                    if (val.startsWith(leftString) && val.length() != leftString.length()) {
                        String tempCmd = new StringBuilder(val).delete(0, lastState.length()).toString();
                        String cmd = cmdString + tempCmd;
                        resultSet.add(new AutoCompleteItem(curState, Float.MAX_VALUE, val, cmd));
                    }
                }
            }

            if (in_msg_or_time_state) {
                addTimeToolItemToResult(resultSet);
                addDateToolItemToResult(resultSet);
                addFavorToolItemToResult(resultSet);
            }
            for (String nextState : mLinks.get(curState)) {
                if (nextState.equals("then")) {
                    if (in_if_or_while_state) {
                        for (String val : mNodes.get(nextState)) {
                            String cmd = cmdString + val;
                            resultSet.add(new AutoCompleteItem(nextState, 1.0f, val, cmd));
                        }
                    }
                } else {
                    if (nextState.equals("compare") || nextState.equals("logical"))
                        if (!in_if_or_while_state)
                            continue;
                    if (nextState.equals("message") || nextState.equals("time")) {
                        addTimeToolItemToResult(resultSet);
                        addDateToolItemToResult(resultSet);
                        addFavorToolItemToResult(resultSet);
                    }
                    for (String val : mNodes.get(nextState)) {
                        String cmd = cmdString + val;
                        resultSet.add(
                                autoCompleteItemWithWeight(new AutoCompleteItem(nextState, DEFAULT_AUTOCOMPLETE_WEIGHT, val, cmd), lastString)
                        );
                    }
                }
            }
        } else {
            String tempInput = inputBuffer.toString();
            if (tempInput.length() != 0) {
                for (String nextState : mLinks.get(curState)) {
                    for (String val : mNodes.get(nextState)) {
                        if (val.startsWith(tempInput)) {
                            String cmd = cmdString + val;
                            resultSet.add(new AutoCompleteItem(nextState, 1.0f, val, cmd));
                        }
                    }
                }
            }
        }
        List<AutoCompleteItem> resultList = new ArrayList<>(resultSet);
        Collections.sort(resultList, mResultComparator);
        BusProvider.getRestBusInstance().post(new MGetAutoCompleteItemEvent(resultList));
    }

    private void addFavorToolItemToResult(Set<AutoCompleteItem> result) {
        result.add(new AutoCompleteToolItem("tool", "[收藏]", AutoCompleteToolItem.SPEC_TYPE_FAVOR));
    }

    private void addDateToolItemToResult(Set<AutoCompleteItem> result) {
        result.add(new AutoCompleteToolItem("tool", "[日期]", AutoCompleteToolItem.SPEC_TYPE_DATE));
    }

    private void addTimeToolItemToResult(Set<AutoCompleteItem> result) {
        result.add(new AutoCompleteToolItem("tool", "[时间]", AutoCompleteToolItem.SPEC_TYPE_TIME));
    }

    private void loadAutoCompleteLocalHistory(Context context) {
        mAutoCompleteCountHolderMap = loadLocalHistory(context);
        if (mAutoCompleteCountHolderMap != null) {
            mWeightDivides = getWeightDivides(mAutoCompleteCountHolderMap);
        }
    }

    private AutoCompleteItem autoCompleteItemWithWeight(AutoCompleteItem toItem, String from) {
        String key = from + toItem.getContent();
        AutoCompleteCountHolder countHolder = mAutoCompleteCountHolderMap.get(key);
        if (countHolder == null) {
            toItem.setWeight(DEFAULT_AUTOCOMPLETE_WEIGHT);
            return toItem;
        }
        float weight = mAutoCompleteCountHolderMap.get(key).count * 1.0f / mWeightDivides.get(from);
        toItem.setWeight(weight);

        Log.d(TAG, "from: " + from + " to: " + toItem.getContent() + " weight: " + weight);
        return toItem;
    }

    private void incAutoCompleteItemWeight(String from, String to) {
        String key = from + to;
        AutoCompleteCountHolder countHolder = mAutoCompleteCountHolderMap.get(key);
        if (countHolder == null) {
            countHolder = new AutoCompleteCountHolder(from, to, 0);
            mAutoCompleteCountHolderMap.put(key, countHolder);
            if (!mWeightDivides.containsKey(from)) {
                mWeightDivides.put(from, 0);
            }
        }
        countHolder.count = countHolder.count + 1;
        mWeightDivides.put(from, mWeightDivides.get(from) + 1);
        Log.d(TAG, "from: " + from + ", to: " + to + " count: " + countHolder.count + " div: " + mWeightDivides.get(from));
    }

    public void markCurrentInput(String inputString) {
        Log.d(TAG, "mark: " + inputString);
        StringBuffer inputBuffer = new StringBuffer(inputString);
        boolean in_if_or_while_state = false;
        boolean in_msg_or_time_ind_state = false;
        String lastString = null;
        String curState = mInitState;
        while (inputBuffer.length() > 0) {
            if (in_msg_or_time_ind_state) {
                if (inputBuffer.toString().startsWith(mMessageSeq)
                        || inputBuffer.toString().startsWith(mTimeSeq)) {
                    in_msg_or_time_ind_state = false;
                }
                inputBuffer.deleteCharAt(0);
                continue;
            }
            boolean found = false;
            String tempInput = inputBuffer.toString();
            if (!mLinks.containsKey(curState)) {
                return;
            }
            for (String nextState : mLinks.get(curState)) {
                if (!in_if_or_while_state) {
                    if (nextState.equals("then")
                            || nextState.equals("compare")
                            || nextState.equals("logical"))
                        continue;
                }
                for (String val : mNodes.get(nextState)) {
                    if (tempInput.startsWith(val)) {
                        if (lastString != null)
                            incAutoCompleteItemWeight(lastString, val);

                        lastString = val;
                        curState = nextState;
                        inputBuffer.delete(0, val.length());
                        found = true;
                        if (curState.equals("message") || curState.equals("time")) {
                            in_msg_or_time_ind_state = true;
                        } else if (curState.equals("if") || curState.equals("while")) {
                            in_if_or_while_state = true;
                        } else if (curState.equals("then")) {
                            in_if_or_while_state = false;
                        }
                        break;
                    }
                }
                if (found || inputBuffer.length() == 0)
                    break;
            }
            if (inputBuffer.length() == 0)
                break;
            if (found)
                continue;
            if (curState.equals("message") || mLinks.get(curState).contains("message")) {
                found = true;
                curState = "message";
                inputBuffer.deleteCharAt(0);
            } else if (curState.equals("time") || mLinks.get(curState).contains("time")) {
                found = true;
                curState = "time";
                inputBuffer.deleteCharAt(0);
            }
            if (!found) break;
        }
    }

    @Override
    public void saveLocalHistory(Context context) {
        if (saveLocalHistory(context, mAutoCompleteCountHolderMap)) {
            BusProvider.getRestBusInstance().post(
                    new MSaveAutoCompleteLocalHistoryEvent(MSaveAutoCompleteLocalHistoryEvent.SUCCESS)
            );
        } else {
            BusProvider.getRestBusInstance().post(
                    new MSaveAutoCompleteLocalHistoryEvent(MSaveAutoCompleteLocalHistoryEvent.ERROR)
            );
        }
    }

    private boolean saveLocalHistory(Context context, Map<String, AutoCompleteCountHolder> localHistorys) {
        if (context == null) {
            Log.e(TAG, "invaild mcontext.");
            return false;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String objJson = null;
        if (localHistorys != null) {
            objJson = new Gson().toJson(localHistorys);
        }
        editor.putString(CONF_KEY_SAVE_AUTOCOMPLETE_LOCAL_HISTORY, objJson);
        editor.apply();
        return true;
    }

    private Map<String, AutoCompleteCountHolder> loadLocalHistory(Context context) {
        if (context == null) {
            Log.e(TAG, "invaild mcontext.");
            return null;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE);
        if (sharedPreferences == null) {
            Log.e(TAG, "invaild share preference.");
            return null;
        }
        String savedJson = sharedPreferences.getString(CONF_KEY_SAVE_AUTOCOMPLETE_LOCAL_HISTORY, null);
        if (savedJson == null || savedJson.equals("null"))
            return new HashMap<String, AutoCompleteCountHolder>();

        Type type = new TypeToken<Map<String, AutoCompleteCountHolder>>() {
        }.getType();
        Map<String, AutoCompleteCountHolder> result = new Gson().fromJson(savedJson, type);
        if (result != null) {
            return result;
        }
        return null;
    }

    public static HashMap<String, Integer> getWeightDivides(Map<String, AutoCompleteCountHolder> weightHolders) {
        HashMap<String, Integer> result = new HashMap<String, Integer>(weightHolders.size());
        for (AutoCompleteCountHolder holder : weightHolders.values()) {
            String key = holder.from; // only 'from' as key
            Integer val = result.get(key);
            if (val == null) {
                result.put(key, holder.count);
            } else {
                result.put(key, val + holder.count);
            }
        }
        return result;
    }
}
