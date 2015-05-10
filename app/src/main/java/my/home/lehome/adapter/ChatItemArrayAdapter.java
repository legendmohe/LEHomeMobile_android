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

package my.home.lehome.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.skyfishjy.library.RippleBackground;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;

import my.home.lehome.R;
import my.home.lehome.util.Constants;
import my.home.model.entities.ChatItem;

public class ChatItemArrayAdapter extends ArrayAdapter<ChatItem> {
    public static final String TAG = ChatItemArrayAdapter.class.getSimpleName();

    private static final int TYPE_CHATTO = 0;
    private static final int TYPE_CHATFROM = 1;

    private ResendButtonClickListener mResendButtonClickListener;
    private ImageClickListener mImageClickListener;
    private TextView chatTextView;

    private DisplayImageOptions options;

    @Override
    public void add(ChatItem object) {
        super.add(object);
    }

    public void setData(List<ChatItem> items) {
        clear();
        setNotifyOnChange(false);
        if (items != null) {
            for (ChatItem item : items)
                add(item);
        }
        setNotifyOnChange(true);
        notifyDataSetChanged();
    }

    public ChatItemArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        options = new DisplayImageOptions.Builder()
//                .showImageOnLoading(R.drawable.ic_stub)
//                .showImageForEmptyUri(R.drawable.ic_empty)
                .resetViewBeforeLoading(true)
                .showImageOnFail(R.drawable.left_chatitem_disconnect)
//                .showImageOnLoading(R.drawable.left_chatitem_loading)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .displayer(new RoundedBitmapDisplayer(10)).build();
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position).isMe()) {
            return TYPE_CHATTO;
        } else {
            return TYPE_CHATFROM;
        }
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ChatItem chatItem = getItem(position);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (chatItem.isMe())
                convertView = inflater.inflate(R.layout.chat_item_onright, parent, false);
            else
                convertView = inflater.inflate(R.layout.chat_item_onleft, parent, false);

            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.rippleBackground = (RippleBackground) convertView.findViewById(R.id.profile_rippleBackground);
            viewHolder.errorButton = (ImageButton) convertView.findViewById(R.id.resend_imagebutton);
            viewHolder.dateTextView = (TextView) convertView.findViewById(R.id.date_textview);
            viewHolder.chatTextView = (TextView) convertView.findViewById(R.id.chat_content_textview);
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.chat_content_imageview);
            viewHolder.progressBar = (ProgressBar) convertView.findViewById(R.id.load_image_progressBar);
            // 先使textview捕获longpress事件
            viewHolder.chatTextView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return false;
                }
            });
            convertView.setTag(viewHolder);
        }

        final ViewHolder viewHolder = (ViewHolder) convertView.getTag();

        if (chatItem.isMe()) {
            viewHolder.rippleBackground.startRippleAnimation();
            if (chatItem.getState() == Constants.CHATITEM_STATE_SUCCESS
                    || !chatItem.isMe()) {
                viewHolder.errorButton.setVisibility(View.GONE);
                viewHolder.rippleBackground.stopRippleAnimation();
            } else if (chatItem.getState() == Constants.CHATITEM_STATE_PENDING) {
                viewHolder.errorButton.setVisibility(View.GONE);
                viewHolder.rippleBackground.startRippleAnimation();
            } else {
                viewHolder.errorButton.setVisibility(View.VISIBLE);
                viewHolder.rippleBackground.stopRippleAnimation();
                if (mResendButtonClickListener != null) {
                    final int pos = position;
                    viewHolder.errorButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mResendButtonClickListener.onResendButtonClicked(pos);
                        }
                    });
                }
            }
            viewHolder.chatTextView.setText(chatItem.getContent());
        }
        if (chatItem.isServerImageItem()) {
            viewHolder.imageView.setVisibility(View.VISIBLE);
            viewHolder.chatTextView.setVisibility(View.GONE);

            final String image_url = chatItem.getContent();
            viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mImageClickListener != null) {
                        String path = ImageLoader.getInstance().getDiskCache().get(image_url).getAbsolutePath();
                        String fileName = new File(image_url).getName();
                        mImageClickListener.onImageViewClicked(path, fileName);
                    }
//                    Intent intent = new Intent(getContext(), PhotoViewerActivity.class);
//                    intent.putExtra(PhotoViewerActivity.EXTRA_IMAGE_URL, path);
//                    intent.putExtra(PhotoViewerActivity.EXTRA_IMAGE_NAME, fileName);
//                    getContext().startActivity(intent);
                }
            });
            viewHolder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return false;
//                    Log.d(TAG, "image view long click");
//                    if (mImageClickListener != null) {
//                        String path = ImageLoader.getInstance().getDiskCache().get(image_url).getAbsolutePath();
//                        String fileName = new File(image_url).getName();
//                        mImageClickListener.onImageViewLongClicked(path, fileName);
//                    }
////                    AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
////                    alert.setTitle(getContext().getResources().getString(R.string.save_capture_to_sdcard));
////                    alert.setMessage(Uri.parse(image_url).getLastPathSegment());
////                    alert.setPositiveButton(getContext().getResources().getString(R.string.com_comfirm)
////                            , new DialogInterface.OnClickListener() {
////                        public void onClick(DialogInterface dialog, int whichButton) {
////                            String path = ImageLoader.getInstance().getDiskCache().get(image_url).getAbsolutePath();
////                            String fileName = new File(image_url).getName();
////                            new SaveCaptureAsyncTask(getContext()).execute(path, fileName);
////                        }
////                    });
////
////                    alert.setNegativeButton(getContext().getResources().getString(R.string.com_cancel),
////                            new DialogInterface.OnClickListener() {
////                                public void onClick(DialogInterface dialog, int whichButton) {
////                                }
////                            });
////
////                    alert.show();
//                    return true;
                }
            });

            ImageAware imageAware = new ImageViewAware(viewHolder.imageView, false);
            ImageLoader.getInstance().displayImage(image_url, imageAware, options, new SimpleImageLoadingListener() {

                        @Override
                        public void onLoadingStarted(String imageUri, View view) {
                            viewHolder.progressBar.setProgress(0);
                            viewHolder.progressBar.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view,
                                                    FailReason failReason) {
                            viewHolder.progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onLoadingComplete(String imageUri,
                                                      View view, Bitmap loadedImage) {
                            viewHolder.progressBar.setVisibility(View.GONE);
                        }
                    }, new ImageLoadingProgressListener() {
                        @Override
                        public void onProgressUpdate(String imageUri, View view, int current, int total) {
                            viewHolder.progressBar.setProgress(Math.round(100.0f * current / total));
                        }
                    }
            );
        } else if (chatItem.isServer()) {
            viewHolder.imageView.setVisibility(View.GONE);
            viewHolder.chatTextView.setVisibility(View.VISIBLE);
            viewHolder.chatTextView.setText(chatItem.getContent());
        }

        String dateString = getTimeWithFormat(position);
        if (dateString != null) {
            viewHolder.dateTextView.setText(dateString);
            viewHolder.dateTextView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.dateTextView.setVisibility(View.INVISIBLE);
        }


        return convertView;
    }

    private String getTimeWithFormat(int position) {
        ChatItem chatItem = getItem(position);
        String formatString = "MM月dd日 hh时mm分";
        SimpleDateFormat df = new SimpleDateFormat(formatString);
        if (position == 0) {
            return df.format(chatItem.getDate());
        } else {
            ChatItem preItem = getItem(position - 1);
            if (chatItem.getDate().getTime() - preItem.getDate().getTime() > Constants.SHOW_DATE_TEXTVIEW_GAP) {
                return df.format(chatItem.getDate());
            } else {
                return null;
            }
        }
    }

    public void setImageClickListener(ImageClickListener mImageClickListener) {
        this.mImageClickListener = mImageClickListener;
    }

    static class ViewHolder {
        ImageView imageView;
        TextView chatTextView;
        ProgressBar progressBar;
        RippleBackground rippleBackground;
        ImageButton errorButton;
        TextView dateTextView;
    }

	/*
     * delegate
	 */

    public void setResendButtonClickListener(
            ResendButtonClickListener mResendButtonClickListener) {
        this.mResendButtonClickListener = mResendButtonClickListener;
    }

    public interface ResendButtonClickListener {
        public void onResendButtonClicked(int pos);
    }

    public interface ImageClickListener {
        public void onImageViewClicked(String path, String fileName);

        public void onImageViewLongClicked(String path, String fileName);
    }
}