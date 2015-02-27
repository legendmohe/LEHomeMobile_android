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

package my.home.lehome.util;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.ArrayList;

/**
 * Created by legendmohe on 15/2/8.
 */
public class UIUtils {

    public static AnimatorSet getShowViewScaleAnimatorSet(View target, long duration) {
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(duration);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        ArrayList<Animator> animatorList = new ArrayList<Animator>();
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(target, "ScaleX", 0f, 1.2f, 1f);
        animatorList.add(scaleXAnimator);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(target, "ScaleY", 0f, 1.2f, 1f);
        animatorList.add(scaleYAnimator);
        animatorSet.playTogether(animatorList);
        return animatorSet;
    }

    public static AnimatorSet getDismissViewScaleAnimatorSet(View target, long duration) {
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(duration);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        ArrayList<Animator> animatorList = new ArrayList<Animator>();
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(target, "ScaleX", 1f, 1.2f, 0f);
        animatorList.add(scaleXAnimator);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(target, "ScaleY", 1f, 1.2f, 0f);
        animatorList.add(scaleYAnimator);
        animatorSet.playTogether(animatorList);
        return animatorSet;
    }
}
