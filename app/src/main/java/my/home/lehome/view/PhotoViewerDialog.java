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
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
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

/**
 * Created by legendmohe on 15/5/10.
 */
public class PhotoViewerDialog extends Dialog {
    public static final String TAG = "PhotoViewerDialog";

    private String mImageUrl = "";
    private String mImageName = "";
    private ProgressBar mProgressBar;
    private final DisplayImageOptions options;

    private final WeakReference<Activity> mContextActivity;

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
        LocationClient locationClient = null;
        BDLocationListener myListener = new MyLocationListener();

        locationClient = new LocationClient(mContextActivity.get().getApplicationContext());     //声明LocationClient类
        locationClient.registerLocationListener(myListener);    //注册监听函数
        locationClient.start();
        locationClient.requestLocation();
    }


    public static class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null)
                return;
            StringBuffer sb = new StringBuffer(256);
            sb.append("time : ");
            sb.append(location.getTime());
            sb.append("\nerror code : ");
            sb.append(location.getLocType());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());
            sb.append("\nradius : ");
            sb.append(location.getRadius());
            if (location.getLocType() == BDLocation.TypeGpsLocation) {
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
            }

            Log.d(TAG, sb.toString());
        }
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
        if (TextUtils.isEmpty(mImageUrl) || TextUtils.isEmpty(mImageName))
            return true;
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

    public void setTarget(String imageURL) {
        File imageFile = ImageLoader.getInstance().getDiskCache().get(imageURL);
        if (imageFile != null) {
            mImageUrl = imageFile.getAbsolutePath();
            mImageName = new File(imageURL).getName();
        }
        if (imageFile != null) {
            SubsamplingScaleImageView imageView = (SubsamplingScaleImageView) findViewById(R.id.scale_imageView);
            imageView.setImage(ImageSource.uri(mImageUrl));
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
                    SubsamplingScaleImageView imageView = (SubsamplingScaleImageView) findViewById(R.id.scale_imageView);
                    imageView.setImage(ImageSource.bitmap(loadedImage));

                    File imageFile = ImageLoader.getInstance().getDiskCache().get(imageUri);
                    if (imageFile != null) {
                        mImageUrl = imageFile.getAbsolutePath();
                        mImageName = new File(imageUri).getName();
                    }
                }
            }, new ImageLoadingProgressListener() {
                @Override
                public void onProgressUpdate(String imageUri, View view, int current, int total) {
                    mProgressBar.setProgress(Math.round(100.0f * current / total));
                }
            });
        }

        if (getActionBar() != null)
            getActionBar().setTitle(mImageName);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
