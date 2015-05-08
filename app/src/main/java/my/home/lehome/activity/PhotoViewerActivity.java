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

package my.home.lehome.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import my.home.lehome.R;
import my.home.lehome.asynctask.SaveCaptureAsyncTask;

public class PhotoViewerActivity extends Activity {
    public static final String TAG = "PhotoViewerActivity";

    public static final String EXTRA_IMAGE_URL = "EXTRA_IMAGE_URL";
    public static final String EXTRA_IMAGE_NAME = "EXTRA_IMAGE_NAME";

    private String mImageUrl = "";
    private String mImageName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_viewer);
        setupViews(getIntent());
    }

    private void setupViews(Intent intent) {
        mImageUrl = intent.getStringExtra(EXTRA_IMAGE_URL);
        mImageName = intent.getStringExtra(EXTRA_IMAGE_NAME);
        if (!TextUtils.isEmpty(mImageUrl)) {
            SubsamplingScaleImageView imageView = (SubsamplingScaleImageView) findViewById(R.id.scale_imageView);
            imageView.setImage(ImageSource.uri(mImageUrl));
        }

        if (getActionBar() != null)
            getActionBar().setTitle(mImageName);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_photo_viewer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save_photo) {
            new SaveCaptureAsyncTask(getApplicationContext()).execute(mImageUrl, mImageName);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
