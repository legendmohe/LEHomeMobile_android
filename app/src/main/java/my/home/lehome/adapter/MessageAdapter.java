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

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.List;

import my.home.lehome.R;

/**
 * Created by legendmohe on 15/11/30.
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private final List<String> mData;
    private WeakReference<IMessageItemClickListener> mListener;

    public MessageAdapter(List<String> data, IMessageItemClickListener listener) {
        this.mListener = new WeakReference<>(listener);
        this.mData = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.draw_item, viewGroup, false);
        return new ViewHolder(view, i, mListener.get());
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
//            if (this.mIcons != null && this.mIcons.length != 0) {
//                viewHolder.mImageView.setImageResource(this.mIcons[i]);
//            }
        viewHolder.mTextView.setText("demo");
        viewHolder.mIndex = i;
    }

    @Override
    public int getItemCount() {
        return this.mData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        protected TextView mTextView;
        //            protected ImageView mImageView;
        protected int mIndex;

        public ViewHolder(View itemView, int index, final IMessageItemClickListener listener) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.title_textview);
//                mImageView = (ImageView) itemView.findViewById(R.id.icon_imageview);
            mIndex = index;
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null)
                        listener.onDrawerItemClick(mIndex);
                }
            });
        }
    }

    public static interface IMessageItemClickListener {
        public void onDrawerItemClick(int i);
    }
}
