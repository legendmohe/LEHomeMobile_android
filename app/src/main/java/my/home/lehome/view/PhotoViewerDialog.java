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
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.lang.ref.WeakReference;

import my.home.lehome.R;
import my.home.lehome.asynctask.SaveCaptureAsyncTask;
import my.home.lehome.util.CommonUtils;

/**
 * Created by legendmohe on 15/5/10.
 */
public class PhotoViewerDialog extends Dialog {
    public static final String TAG = "PhotoViewerDialog";

    private String mImageUrl = "";
    private ProgressBar mProgressBar;
    private final DisplayImageOptions options;

    private final WeakReference<Activity> mContextActivity;
    private Intent mExtraIntent;
    private String mExtraTitle;

    public PhotoViewerDialog(Activity context) {
        super(context, R.style.PhotoViewerAcvitity);
        LayoutInflater inflater = context.getLayoutInflater();
        View contentView = inflater.inflate(R.layout.dialog_photo_viewer, null);
        setContentView(contentView);

        mContextActivity = new WeakReference<>(context);
        mProgressBar = (ProgressBar) contentView.findViewById(R.id.dialog_image_progressBar);
        options = new DisplayImageOptions.Builder()
                .resetViewBeforeLoading(true)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .build();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mContextActivity.get() != null) {
            mContextActivity.get().getMenuInflater().inflate(R.menu.menu_photo_viewer, menu);
        }
        if (mExtraIntent != null) {
            menu.getItem(1).setTitle(mExtraTitle);
            menu.getItem(1).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save_photo && mContextActivity.get() != null) {
            if (TextUtils.isEmpty(mImageUrl)) {
                return super.onMenuItemSelected(featureId, item);
            }
            String fileName = CommonUtils.getDateFormatString("yyyy-MM-dd_hh-mm-ss") + ".jpg";
            new SaveCaptureAsyncTask(mContextActivity.get().getApplicationContext()).execute(mImageUrl, fileName);
            return true;
        } else if (id == R.id.action_photo_extra_intent && mContextActivity.get() != null) {
            try {
                mContextActivity.get().startActivity(mExtraIntent);
            } catch (ActivityNotFoundException exception) {
                Toast.makeText(getContext(), R.string.toast_no_such_app, Toast.LENGTH_SHORT).show();
            }
        }
        return super.onMenuItemSelected(featureId, item);
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        if (id == R.id.action_save_photo && mContextActivity.get() != null) {
//            if (TextUtils.isEmpty(mImageUrl) || TextUtils.isEmpty(mImageName)) {
//                return super.onOptionsItemSelected(item);
//            }
//            new SaveCaptureAsyncTask(mContextActivity.get().getApplicationContext()).execute(mImageUrl, mImageName);
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    public void setTarget(String imageURL, Intent extraIntent, String extraTitle) {
        if (extraIntent != null) {
            mExtraIntent = extraIntent;
            mExtraTitle = extraTitle;
        }

        File imageFile = ImageLoader.getInstance().getDiskCache().get(imageURL);
        if (imageFile != null) {
            mImageUrl = imageFile.getAbsolutePath();
        }
        final View contentImageView = findViewById(R.id.scale_imageView);
        if (imageFile != null) {
            SubsamplingScaleImageView imageView = (SubsamplingScaleImageView) contentImageView;
            imageView.setImage(ImageSource.uri(mImageUrl));
            imageView.setVisibility(View.VISIBLE);
        } else {
            ImageLoader.getInstance().loadImage(imageURL, null, options, new SimpleImageLoadingListener() {

                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    mProgressBar.setProgress(0);
                    mProgressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view,
                                            FailReason failReason) {
                    Log.w(TAG, failReason.toString());
                    mProgressBar.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingComplete(String imageUri,
                                              View view, Bitmap loadedImage) {
                    mProgressBar.setVisibility(View.GONE);
                    contentImageView.setVisibility(View.VISIBLE);
                    SubsamplingScaleImageView imageView = (SubsamplingScaleImageView) findViewById(R.id.scale_imageView);
                    imageView.setImage(ImageSource.bitmap(loadedImage));

                    File imageFile = ImageLoader.getInstance().getDiskCache().get(imageUri);
                    if (imageFile != null) {
                        mImageUrl = imageFile.getAbsolutePath();
                    }
                }
            }, new ImageLoadingProgressListener() {
                @Override
                public void onProgressUpdate(String imageUri, View view, int current, int total) {
                    mProgressBar.setProgress(Math.round(100.0f * current / total));
                }
            });
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
