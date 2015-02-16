package my.home.model.datasource;

import android.content.Context;

/**
 * Created by legendmohe on 15/2/13.
 */
public interface AutoCompleteItemDataSource {
    static final String PREFERENCE_KEY = "GetAutoCompleteItemUsecase_pref_key";
    static final String CONF_KEY = "GetAutoCompleteItemUsecase_conf_key";

    public void saveConf(Context context, String confJSONString);

    public void loadConf(Context context);

    public void getAutoCompleteItems(String currentInput);
}
