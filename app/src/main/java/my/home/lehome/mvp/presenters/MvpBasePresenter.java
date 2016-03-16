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

package my.home.lehome.mvp.presenters;

/**
 * Created by legendmohe on 16/3/14.
 */

import java.lang.ref.WeakReference;

import my.home.lehome.mvp.views.MvpBaseView;

public abstract class MvpBasePresenter<T extends MvpBaseView> {

    private WeakReference<T> mView;

    public MvpBasePresenter(T view) {
        mView = new WeakReference<T>(view);
    }

    public boolean isViewAttached() {
        return mView != null && mView.get() != null;
    }

    public T getView() {
        if (mView != null) {
            return mView.get();
        }
        return null;
    }

    public abstract void start();

    public abstract void stop();
}
