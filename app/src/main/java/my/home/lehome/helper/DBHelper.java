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

package my.home.lehome.helper;

import android.content.Context;
import android.util.Log;

import java.util.List;

import de.greenrobot.dao.query.DeleteQuery;
import de.greenrobot.dao.query.QueryBuilder;
import my.home.model.entities.ChatItem;
import my.home.model.entities.ChatItemDao;
import my.home.model.entities.DaoMaster;
import my.home.model.entities.DaoMaster.OpenHelper;
import my.home.model.entities.DaoSession;
import my.home.model.entities.Shortcut;
import my.home.model.entities.ShortcutDao;

public class DBHelper {
    private static final String TAG = DBHelper.class.getName();
    private static DaoMaster daoMaster;
    private static DaoSession daoSession;

    public static void initHelper(Context context) {
        if (daoMaster == null) {
            OpenHelper helper = new DaoMaster.DevOpenHelper(context, "lehome_db", null);
            daoMaster = new DaoMaster(helper.getWritableDatabase());
            daoSession = daoMaster.newSession();
        }
    }

    public static DaoMaster getDaoMaster(Context context) {
        initHelper(context);
        if (daoMaster == null) {
            Log.w(TAG, "initHelper must be call first.");
        }
        return daoMaster;
    }

    public static DaoSession getDaoSession(Context context) {
        initHelper(context);
        if (daoSession == null) {
            Log.w(TAG, "initHelper must be call first.");
        }
        return daoSession;
    }

    public static void addChatItem(Context context, ChatItem entity) {
        getDaoSession(context).getChatItemDao().insert(entity);
    }

    public static void updateChatItem(Context context, ChatItem entity) {
        getDaoSession(context).getChatItemDao().update(entity);
    }

    public static List<ChatItem> getAllChatItems(Context context) {
        return getDaoSession(context).getChatItemDao().loadAll();
    }

    public static List<ChatItem> loadLatest(Context context, int limit) {
        if (limit <= 0) {
            Log.w(TAG, "loadAfter invaild limit.");
            return null;
        }
        QueryBuilder<ChatItem> queryBuilder = getDaoSession(context).getChatItemDao().queryBuilder();
        return queryBuilder
                .orderDesc(ChatItemDao.Properties.Id)
                .limit(limit)
                .list();
    }

    public static List<ChatItem> loadBefore(Context context, long id, int limit) {
        if (limit <= 0) {
            Log.w(TAG, "loadAfter invaild limit.");
            return null;
        }
        QueryBuilder<ChatItem> queryBuilder = getDaoSession(context).getChatItemDao().queryBuilder();
        return queryBuilder
                .where(ChatItemDao.Properties.Id.lt(id))
                .orderDesc(ChatItemDao.Properties.Id)
                .limit(limit)
                .list();
    }

    public static void addShortcut(Context context, Shortcut shortcut) {
        if (shortcut.getId() == null || !hasShortcut(context, shortcut)) {
            getDaoSession(context).getShortcutDao().insert(shortcut);
        }
    }

    public static void updateShortcut(Context context, Shortcut shortcut) {
        getDaoSession(context).getShortcutDao().update(shortcut);
    }

    public static boolean hasShortcut(Context context, Shortcut shortcut) {
        QueryBuilder<ChatItem> queryBuilder = getDaoSession(context).getChatItemDao().queryBuilder();
        queryBuilder.where(ChatItemDao.Properties.Content.eq(shortcut.getContent()))
                .limit(1);
        return queryBuilder.buildCount().count() > 0 ? true : false;
    }

    public static List<Shortcut> getAllShortcuts(Context context) {
        return getDaoSession(context).getShortcutDao().loadAll();
    }

    public static void deleteShortcut(Context context, long Id) {
        QueryBuilder<Shortcut> qb = getDaoSession(context).getShortcutDao().queryBuilder();
        DeleteQuery<Shortcut> bd = qb.where(ShortcutDao.Properties.Id.eq(Id)).buildDelete();
        bd.executeDeleteWithoutDetachingEntities();
    }

    public static void destory() {
        if (daoSession != null) {
            daoSession.clear();
        }
        daoMaster = null;
        daoSession = null;
    }
}
