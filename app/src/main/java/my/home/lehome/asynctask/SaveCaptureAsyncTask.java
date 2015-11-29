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
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import my.home.common.util.FileUtil;
import my.home.lehome.R;

/**
 * Created by legendmohe on 15/5/7.
 */
public class SaveCaptureAsyncTask extends AsyncTask<String, String, String> {
    public static final String TAG = "SaveCaptureAsyncTask";

    public static final String SAVE_DIR = "LEHome" + File.separator;

    private WeakReference<Context> mContext;

    public SaveCaptureAsyncTask(Context context) {
        this.mContext = new WeakReference<Context>(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mContext.get() != null) {
            Toast.makeText(mContext.get(), R.string.toast_saving_capture, Toast.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (mContext.get() != null) {
            if (TextUtils.isEmpty(result)) {
                Toast.makeText(mContext.get(), R.string.toast_saving_capture_faild, Toast.LENGTH_LONG)
                        .show();
            } else {
                String successToast = mContext.get().getString(R.string.toast_saving_capture_success) + "\n" + result;
                Toast.makeText(mContext.get(), successToast, Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    @Override
    protected String doInBackground(String... params) {
        if (!FileUtil.isExternalStorageWritable()) {
            Log.w(TAG, "isExternalStorageWritable false");
            return null;
        }
        String srcPath = params[0];
        String dstFilename = params[1];
        File srcFile = new File(srcPath);
        if (srcFile.exists() && srcFile.isFile()) {
            File dstDir = FileUtil.getPictureStorageDir(SAVE_DIR);
            File dstFile = new File(dstDir, dstFilename);
            try {
                FileUtil.copy(srcFile, dstFile);
                Log.d(TAG, "save file from:" + srcFile.getAbsolutePath() + " to:" + dstFile.getAbsolutePath());
                return dstFile.getAbsolutePath();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
