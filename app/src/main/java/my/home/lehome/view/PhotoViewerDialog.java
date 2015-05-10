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

package my.home.lehome.view;

import android.app.Activity;
import android.app.Dialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.lang.ref.WeakReference;

import my.home.lehome.R;
import my.home.lehome.asynctask.SaveCaptureAsyncTask;

/**
 * Created by legendmohe on 15/5/10.
 */
public class PhotoViewerDialog extends Dialog {
    public static final String TAG = "PhotoViewerDialog";

    private String mImageUrl = "";
    private String mImageName = "";

    private final WeakReference<Activity> mContextActivity;

    public PhotoViewerDialog(Activity context) {
        super(context, R.style.PhotoViewerAcvitity);
        LayoutInflater inflater = context.getLayoutInflater();
        View contentView = inflater.inflate(R.layout.dialog_photo_viewer, null);
        setContentView(contentView);

        mContextActivity = new WeakReference<>(context);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mContextActivity.get() != null) {
            mContextActivity.get().getMenuInflater().inflate(R.menu.menu_photo_viewer, menu);
        }
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        new SaveCaptureAsyncTask(getContext()).execute(mImageUrl, mImageName);
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_save_photo && mContextActivity.get() != null) {
            new SaveCaptureAsyncTask(mContextActivity.get().getApplicationContext()).execute(mImageUrl, mImageName);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setTarget(String path, String fileName) {
        mImageUrl = path;
        mImageName = fileName;
        if (!TextUtils.isEmpty(mImageUrl)) {
            SubsamplingScaleImageView imageView = (SubsamplingScaleImageView) findViewById(R.id.scale_imageView);
            imageView.setImage(ImageSource.uri(mImageUrl));
        }

        if (getActionBar() != null)
            getActionBar().setTitle(mImageName);
    }
}
