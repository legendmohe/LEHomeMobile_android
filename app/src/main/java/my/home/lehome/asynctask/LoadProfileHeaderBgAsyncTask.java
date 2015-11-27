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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.graphics.BitmapCompat;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import my.home.common.FileUtil;
import my.home.common.ImageUtil;
import my.home.common.PrefUtil;

/**
 * Created by legendmohe on 15/9/22.
 */
public class LoadProfileHeaderBgAsyncTask extends AsyncTask<Uri, String, Bitmap> {
    public static final String TAG = "LoadProfileHeader";

    public static final String THUMBNAIL_SAVE_DIR = "LEHome" + File.separator + "thumb" + File.separator;
    public static final String PREF_KEY_PROFILE_IMAGE = "PREF_KEY_PROFILE_IMAGE";
    private final WeakReference<Context> mContext;
    WeakReference<ImageView> mImageView;
    private Uri mUri;
    private int mWidth;
    private int mHeight;
    private ImageView.ScaleType mScaleType;

    public LoadProfileHeaderBgAsyncTask(Context context, ImageView imageView) {
        this.mContext = new WeakReference<>(context);
        this.mImageView = new WeakReference<>(imageView);
    }

    @Override
    protected void onPreExecute() {
        ImageView imageView = mImageView.get();
        if (imageView != null) {
            imageView.setImageURI(null);
            this.mWidth = imageView.getWidth();
            this.mHeight = imageView.getHeight();
            this.mScaleType = imageView.getScaleType();
        }
    }

    @Override
    protected Bitmap doInBackground(Uri... uris) {
        if (uris == null || mImageView.get() == null || this.mContext.get() == null)
            return null;
        this.mUri = uris[0];
        Bitmap resultBitmap = null;
        String saveURL = FileUtil.getPathFromUri(this.mContext.get(), mUri);

        if (!saveURL.endsWith("nav_thumb.png")) {
            Log.d(TAG, "need to create thumbnail: " + saveURL);
            Bitmap bitmap;
            BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
            InputStream imageStream = null;
            try {
                imageStream = this.mContext.get().getContentResolver().openInputStream(this.mUri);
                bitmap = BitmapFactory.decodeStream(imageStream, null, decodeOptions);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            } finally {
                if (imageStream != null) {
                    try {
                        imageStream.close();
                    } catch (IOException e) {
                        Log.e(TAG, "could not close imageStream ", e);
                    }
                }
            }

            String path = FileUtil.getPathFromUri(mContext.get(), this.mUri);
            resultBitmap = ImageUtil.scaleAndRotateImageFile(
                bitmap,
                path,
                this.mWidth,
                this.mHeight,
                this.mScaleType
            );
            Log.d(TAG, "scaled bitmap from "
                    + BitmapCompat.getAllocationByteCount(bitmap) + "byte to "
                    + BitmapCompat.getAllocationByteCount(resultBitmap) + "byte");
            bitmap.recycle();

            try {
                if (FileUtil.isExternalStorageWritable()) {
                    File dstDir = FileUtil.getPictureStorageDir(THUMBNAIL_SAVE_DIR);
                    saveURL = dstDir.getAbsolutePath() + File.separator + "nav_thumb.png";
                    ImageUtil.saveBitmapToFile(resultBitmap, saveURL, Bitmap.CompressFormat.PNG, 100);
                } else {
                    Log.w(TAG, "isExternalStorageWritable false");
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            PrefUtil.setStringValue(mContext.get(), PREF_KEY_PROFILE_IMAGE, saveURL);
        } else {
            Log.d(TAG, "use thumbnail: " + saveURL);
            resultBitmap = ImageUtil.loadBitmapFromFile(saveURL);
        }
        return resultBitmap;
    }

    @Override
    protected void onPostExecute(Bitmap resultBitmap) {
        ImageView imageView = mImageView.get();
        if (imageView != null) {
            // recycle previous bitmap
            Drawable drawable = imageView.getDrawable();
            if (drawable != null && drawable instanceof BitmapDrawable) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                Bitmap bitmap = bitmapDrawable.getBitmap();
                if (bitmap != null)
                    bitmap.recycle();
                imageView.setImageBitmap(null);
            }
            imageView.setImageBitmap(resultBitmap);
        }
    }
}
