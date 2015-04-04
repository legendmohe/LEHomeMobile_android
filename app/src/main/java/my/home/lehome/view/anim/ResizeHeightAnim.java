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

package my.home.lehome.view.anim;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by legendmohe on 15/4/3.
 */
public class ResizeHeightAnim extends Animation {
    View view;
    int startH;
    int endH;
    int diff;

    public ResizeHeightAnim(View v, int newh) {
        view = v;
        startH = v.getLayoutParams().height;
        endH = newh;
        diff = endH - startH;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        view.getLayoutParams().height = startH + (int) (diff * interpolatedTime);
        view.requestLayout();
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }
}
