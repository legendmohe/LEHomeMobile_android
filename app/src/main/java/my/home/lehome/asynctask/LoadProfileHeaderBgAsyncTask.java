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
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import my.home.common.FileUtil;
import my.home.common.ImageUtil;
import my.home.common.PrefUtil;

/**
 * Created by legendmohe on 15/9/22.
 */
public class LoadProfileHeaderBgAsyncTask extends AsyncTask<Uri, String, Bitmap> {

    public static final String PREF_KEY_PROFILE_IMAGE = "PREF_KEY_PROFILE_IMAGE";
    private final WeakReference<Context> mContext;
    WeakReference<ImageView> mImageView;
    private Uri mUri;
    private int mWidth;
    private int mHeight;
    private ImageView.ScaleType mScaleType;

    public LoadProfileHeaderBgAsyncTask(Context context, ImageView imageView) {
        this.mContext = new WeakReference<>(context);
        this.mImageView = new WeakReference<ImageView>(imageView);
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
        Uri uri = uris[0];
        this.mUri = uris[0];

        Bitmap bitmap;
        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        InputStream imageStream;
        try {
            imageStream = this.mContext.get().getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        bitmap = BitmapFactory.decodeStream(imageStream, null, decodeOptions);
        String path = FileUtil.getPathFromUri(mContext.get(), uri);
        Bitmap tmpBitmap = ImageUtil.scaleAndRotateImageFile(
                bitmap,
                path,
                this.mWidth,
                this.mHeight,
                this.mScaleType
        );
        bitmap.recycle();
//            try {
//                bitmap = ImageUtil.scaleImageFile(this,
//                        selectedImageUri, iconImageView.getWidth(),
//                        iconImageView.getHeight(),
//                        iconImageView.getScaleType()
//                );
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }

//            if (bitmap != null) {
//                String path = FileUtil.getPathFromUri(this, selectedImageUri);
//                bitmap = ImageUtil.rotateBitmapToNormal(bitmap, path);
//            }

        return tmpBitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        ImageView imageView = mImageView.get();
        if (imageView != null) {
            imageView.setImageBitmap(bitmap);
//            resetNavProfileName();
            PrefUtil.setStringValue(mContext.get(), PREF_KEY_PROFILE_IMAGE, mUri.toString());
        }
    }
}
