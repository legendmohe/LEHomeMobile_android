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

package my.home.domain.usecase;

import android.content.Context;
import android.util.Log;

import java.io.File;

import my.home.common.util.FileUtil;
import my.home.model.manager.DBStaticManager;

/**
 * Created by legendmohe on 15/12/5.
 */
public class CleanMsgUsecaseImpl implements CleanMsgUsecase {
    private String mPrefix;
    private Context mContext;

    public CleanMsgUsecaseImpl(Context context, String prefix) {
        this.mContext = context;
        this.mPrefix = prefix;
    }

    @Override
    public void execute() {
        if (mContext != null) {
            String path = FileUtil.getDiskCacheDir(mContext);
            File deleteFolder = new File(path);
            if (deleteFolder.isDirectory()) {
                String[] children = deleteFolder.list();
                for (int i = 0; i < children.length; i++) {
                    File deleteFile = new File(deleteFolder, children[i]);
                    if (deleteFile.getName().startsWith(this.mPrefix)) {
                        Log.d(TAG, "delete message:" + deleteFile.getName());
                        deleteFile.delete();
                    }
                }
                DBStaticManager.deleteAllMessages(mContext);
            }
        }
    }
}
