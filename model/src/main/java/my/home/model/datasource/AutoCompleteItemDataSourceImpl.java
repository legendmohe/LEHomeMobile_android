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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import my.home.model.entities.AutoCompleteItem;
import my.home.model.entities.AutoCompleteToolItem;
import my.home.model.entities.HistoryItem;
import my.home.model.entities.MsgHistoryItem;
import my.home.model.events.MConfAutoCompleteItemEvent;
import my.home.model.events.MGetAutoCompleteItemEvent;
import my.home.model.manager.DBStaticManager;

/**
 * Created by legendmohe on 15/2/13.
 */
public class AutoCompleteItemDataSourceImpl implements AutoCompleteItemDataSource {

    public static final String TAG = "ACItemDataSource";

    public static final float DEFAULT_AUTOCOMPLETE_WEIGHT = 0.0f;

    private static AutoCompleteItemDataSourceImpl INSTANCE;

    private Map<String, List<String>> mNodes;
    private Map<String, List<String>> mLinks;
    private String mMessageSeq;
    private String mTimeSeq;
    private String mInitState;
    private boolean mLoadSuccess = false;

    private static Comparator<AutoCompleteItem> mResultComparator;

    static {
        mResultComparator = new Comparator<AutoCompleteItem>() {
            @Override
            public int compare(AutoCompleteItem lhs, AutoCompleteItem rhs) {
                return Float.compare(rhs.getWeight(), lhs.getWeight());
            }
        };
    }

    private static final int HISTORY_ITEM_MAX_NUM = 15;
    private static final int MAX_MSG_HISTORY_NUM = 10;

    private static class SingletonHolder {
        private static final AutoCompleteItemDataSourceImpl INSTANCE = new AutoCompleteItemDataSourceImpl();
    }

    public static AutoCompleteItemDataSourceImpl getInstance() {
        return SingletonHolder.INSTANCE;
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
//            loadAutoCompleteLocalHistory(context);
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
    public void getAutoCompleteItems(Context context, String currentInput) {
        if (!mLoadSuccess) {
            BusProvider.getRestBusInstance().post(new MGetAutoCompleteItemEvent(new ArrayList()));
            return;
        }

        boolean in_msg_or_time_ind_state = false;
        boolean in_msg_or_time_state = false;
        boolean in_if_state = false;
        boolean in_if_then_state = false;
        boolean in_while_state = false;
        String leftString = "";
        String lastString = "";
        String lastPreMsg = "";
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
                if (!in_if_state && !in_while_state) {
                    if (nextState.equals("then")
                            || nextState.equals("compare")
                            || nextState.equals("logical"))
                        continue;
                }
                if (!in_if_then_state) {
                    if (nextState.equals("else"))
                        continue;
                }
                for (String val : mNodes.get(nextState)) {
                    if (tempInput.startsWith(val)) {
                        leftString = inputBuffer.toString();
                        lastState = nextState;
                        inputBuffer.delete(0, val.length());
                        cmdBuffer.append(val);
                        found = true;
                        in_msg_or_time_state = false;

                        if (nextState.equals("message") || nextState.equals("time")) {
                            in_msg_or_time_ind_state = true;
                            lastPreMsg = lastString;
                        } else if (nextState.equals("if")) {
                            in_if_state = true;
                        } else if (nextState.equals("while")) {
                            in_while_state = true;
                        } else if (nextState.equals("then")) {
                            if (in_if_state)
                                in_if_then_state = true;
                            in_if_state = false;
                            in_while_state = false;
                        } else if (in_if_then_state && nextState.equals("else")) {
                            in_if_then_state = false;
                        }
                        lastString = val;
                        curState = nextState;
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
            // 1
            addFavorToolItemToResult(resultSet);
            // 2
            addMsgHistoryItemToResult(resultSet, context, cmdString, lastPreMsg, curState);
            // 3
            resultSet.add(new AutoCompleteItem(curState, 1.0f, mMessageSeq, cmd));
        } else if (inputBuffer.length() == 0) {
            ArrayList<AutoCompleteItem> unweightItems = new ArrayList<>();
            String tempLeft = new StringBuilder(leftString).delete(0, lastString.length()).toString();
            if (tempLeft.length() != 0) {
                if (curState.equals("message") || curState.equals("time")) {
                    for (String nextState : mLinks.get(lastState)) {
                        for (String val : mNodes.get(nextState)) {
                            if (val.startsWith(tempLeft)) {
                                String tempCmd = new StringBuilder(val).delete(0, tempLeft.length()).toString();
                                String cmd = cmdString + tempCmd;
//                                resultSet.add(new AutoCompleteItem(nextState, Float.MAX_VALUE, val, cmd));
                                unweightItems.add(new AutoCompleteItem(nextState, DEFAULT_AUTOCOMPLETE_WEIGHT, val, cmd));
                            }
                        }
                    }
                } else {
                    for (String val : mNodes.get(curState)) {
                        if (val.startsWith(tempLeft) && !val.equals(tempLeft)) {
                            String tempCmd = new StringBuilder(val).delete(0, lastString.length()).toString();
                            String cmd = cmdString + tempCmd;
//                            resultSet.add(new AutoCompleteItem(curState, Float.MAX_VALUE, val, cmd));
                            unweightItems.add(new AutoCompleteItem(curState, DEFAULT_AUTOCOMPLETE_WEIGHT, val, cmd));
                        }
                    }
                }
            } else if (leftString.equals(lastString)) {
                for (String val : mNodes.get(curState)) {
                    if (val.startsWith(leftString) && val.length() != leftString.length()) {
                        String tempCmd = new StringBuilder(val).delete(0, lastString.length()).toString();
                        String cmd = cmdString + tempCmd;
//                        resultSet.add(new AutoCompleteItem(curState, DEFAULT_AUTOCOMPLETE_WEIGHT, val, cmd));
                        unweightItems.add(new AutoCompleteItem(curState, DEFAULT_AUTOCOMPLETE_WEIGHT, val, cmd));
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
                    if (in_if_state || in_while_state) {
                        for (String val : mNodes.get(nextState)) {
                            String cmd = cmdString + val;
                            resultSet.add(new AutoCompleteItem(nextState, 1.0f, val, cmd));
                        }
                    }
                } else if (nextState.equals("else")) {
                    if (in_if_then_state) {
                        for (String val : mNodes.get(nextState)) {
                            String cmd = cmdString + val;
                            resultSet.add(new AutoCompleteItem(nextState, 1.0f, val, cmd));
                        }
                    }
                } else {
                    if (nextState.equals("compare") || nextState.equals("logical"))
                        if (!in_if_state && !in_while_state)
                            continue;
                    if (nextState.equals("message") || nextState.equals("time")) {
                        addTimeToolItemToResult(resultSet);
                        addDateToolItemToResult(resultSet);
                        addFavorToolItemToResult(resultSet);
                    }
                    for (String val : mNodes.get(nextState)) {
                        String cmd = cmdString + val;
                        unweightItems.add(new AutoCompleteItem(nextState, DEFAULT_AUTOCOMPLETE_WEIGHT, val, cmd));
                    }
                }
            }
            if (unweightItems.size() != 0) {
                setItemWeight(context, unweightItems, lastString);
                resultSet.addAll(unweightItems);
            }
        } else {
            String tempInput = inputBuffer.toString();
            ArrayList<AutoCompleteItem> unweightItems = new ArrayList<>();
            if (tempInput.length() != 0) {
                for (String nextState : mLinks.get(curState)) {
                    for (String val : mNodes.get(nextState)) {
                        if (val.startsWith(tempInput)) {
                            String cmd = cmdString + val;
                            unweightItems.add(new AutoCompleteItem(nextState, DEFAULT_AUTOCOMPLETE_WEIGHT, val, cmd));
                        }
                    }
                }
            }
            if (unweightItems.size() != 0) {
                setItemWeight(context, unweightItems, tempInput);
                resultSet.addAll(unweightItems);
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

    private void addMsgHistoryItemToResult(Set<AutoCompleteItem> result, Context context, String cmdString, String lastPreMsg, String curState) {
        int indexOfSeq = cmdString.lastIndexOf(mMessageSeq);
        String unfinishedPart = cmdString.substring(indexOfSeq + 1);
        String finishedPart = cmdString.substring(0, indexOfSeq + 1);
        List<MsgHistoryItem> msgHistoryItems = DBStaticManager.getMsgHistoryItems(context, lastPreMsg, MAX_MSG_HISTORY_NUM);
        for (MsgHistoryItem item : msgHistoryItems) {
            if (item.getMsg().startsWith(unfinishedPart) && !item.getMsg().equals(unfinishedPart)) {
                String msg = finishedPart + item.getMsg() + mMessageSeq;
                result.add(new AutoCompleteItem(curState, 1.0f, item.getMsg(), msg));
            }
        }
    }

//    private void loadAutoCompleteLocalHistory(Context context) {
//        mAutoCompleteCountHolderMap = loadLocalHistory(context);
//        if (mAutoCompleteCountHolderMap != null) {
//            mWeightDivides = getWeightDivides(mAutoCompleteCountHolderMap);
//        }
//    }

    // --------------------- auto item weight --------------------

    private void setItemWeight(Context context, ArrayList<AutoCompleteItem> items, String from) {
        List<HistoryItem> historyItems = DBStaticManager.getLatestItems(context, from, HISTORY_ITEM_MAX_NUM);
        if (historyItems == null || historyItems.size() == 0) {
            Log.d(TAG, "no history from: " + from + ". Use default value.");
            return;
        }

        int hLen = historyItems.size();
        /* TODO move to Util class */
        HashMap<String, Integer> counter = new HashMap<>(hLen);
        for (HistoryItem item : historyItems) {
            String key = item.getFrom() + item.getTo();
            if (counter.containsKey(key)) {
                counter.put(key, counter.get(key) + 1);
            } else {
                counter.put(key, 1);
            }
        }

        String lastValue = "";
        float lastWeight = 0.0f;
        HashMap<String, Float> resultMap = new HashMap<>();
        for (int i = 0; i < hLen; i++) {
            HistoryItem cItem = historyItems.get(i);
            String key = cItem.getFrom() + cItem.getTo();
            // pos weight
            float pos_w = (i + 1) * 1.0f / hLen;
            pos_w = pos_w * pos_w;
            // occ weight
            float occ_w = counter.get(key) * 1.0f / hLen;
            // ctu weight
            float ctu_w = 0.0f;
            if (lastValue.equals(key)) {
                ctu_w = lastWeight / 2;
            }
            lastWeight = pos_w + occ_w + ctu_w;
            lastValue = key;

            if (resultMap.containsKey(key)) {
                resultMap.put(key, resultMap.get(key) + lastWeight);
            } else {
                resultMap.put(key, lastWeight);
            }
        }
        if (resultMap.size() != 0) {
            for (AutoCompleteItem item : items) {
                if (resultMap.containsKey(from + item.getContent())) {
                    Log.d(TAG, "item:" + item.getContent() + " | " + resultMap.get(from + item.getContent()));
                    item.setWeight(resultMap.get(from + item.getContent()));
                }
            }
        }
    }

    private void addNewHistoryItem(Context context, String from, String to) {
        Log.d(TAG, "new HistoryItem from: " + from + ", to: " + to);
        HistoryItem newItem = new HistoryItem();
        newItem.setFrom(from);
        newItem.setTo(to);
        DBStaticManager.addHistoryItem(context, newItem);
    }

    public void markCurrentInput(Context context, String inputString) {
        Log.d(TAG, "mark: " + inputString);
        StringBuffer inputBuffer = new StringBuffer(inputString);
        StringBuffer msgBuffer = new StringBuffer();
        boolean in_if_state = false;
        boolean in_if_then_state = false;
        boolean in_while_state = false;
        boolean in_msg_or_time_ind_state = false;
        String lastString = null;
        String preMsgString = null;
        String curState = mInitState;
        while (inputBuffer.length() > 0) {
            if (in_msg_or_time_ind_state) {
                if (inputBuffer.toString().startsWith(mMessageSeq)
                        || inputBuffer.toString().startsWith(mTimeSeq)) {
                    in_msg_or_time_ind_state = false;
                    MsgHistoryItem msgHistoryItem = new MsgHistoryItem();
                    msgHistoryItem.setFrom(preMsgString);
                    msgHistoryItem.setMsg(msgBuffer.toString());
                    DBStaticManager.addMsgHistoryItem(context, msgHistoryItem);

                    msgBuffer.delete(0, msgBuffer.length());
                    preMsgString = null;
                } else {
                    msgBuffer.append(inputBuffer.charAt(0));
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
                if (!in_if_state && !in_while_state) {
                    if (nextState.equals("then")
                            || nextState.equals("compare")
                            || nextState.equals("logical"))
                        continue;
                }
                if (!in_if_then_state) {
                    if (nextState.equals("else"))
                        continue;
                }
                for (String val : mNodes.get(nextState)) {
                    if (tempInput.startsWith(val)) {
                        if (lastString != null) {
                            addNewHistoryItem(context, lastString, val);
                        }

                        inputBuffer.delete(0, val.length());
                        found = true;
                        if (nextState.equals("message") || nextState.equals("time")) {
                            in_msg_or_time_ind_state = true;
                            preMsgString = lastString;
                        } else if (nextState.equals("if")) {
                            in_if_state = true;
                        } else if (nextState.equals("while")) {
                            in_while_state = true;
                        } else if (nextState.equals("then")) {
                            if (in_if_state)
                                in_if_then_state = true;
                            in_if_state = false;
                            in_while_state = false;
                        } else if (in_if_then_state && nextState.equals("else")) {
                            in_if_then_state = false;
                        }
                        lastString = val;
                        curState = nextState;
                        break;
                    }
                }
                if (found || inputBuffer.length() == 0)
                    break;
            }
            if (inputBuffer.length() == 0) {
                break;
            }
            if (found) {
                if (msgBuffer.length() != 0) {
                    MsgHistoryItem msgHistoryItem = new MsgHistoryItem();
                    msgHistoryItem.setFrom(preMsgString);
                    msgHistoryItem.setMsg(msgBuffer.toString());
                    DBStaticManager.addMsgHistoryItem(context, msgHistoryItem);

                    msgBuffer.delete(0, msgBuffer.length());
                    preMsgString = null;
                }
                continue;
            }
            if (curState.equals("message") || mLinks.get(curState).contains("message")) {
                found = true;
                curState = "message";
                preMsgString = lastString;
                msgBuffer.append(inputBuffer.charAt(0));
                inputBuffer.deleteCharAt(0);
            } else if (curState.equals("time") || mLinks.get(curState).contains("time")) {
                found = true;
                curState = "time";
                preMsgString = lastString;
                msgBuffer.append(inputBuffer.charAt(0));
                inputBuffer.deleteCharAt(0);
            }
            if (!found) break;
        }
        if (msgBuffer.length() != 0) {
            MsgHistoryItem msgHistoryItem = new MsgHistoryItem();
            msgHistoryItem.setFrom(preMsgString);
            msgHistoryItem.setMsg(msgBuffer.toString());
            DBStaticManager.addMsgHistoryItem(context, msgHistoryItem);
        }
    }
}

