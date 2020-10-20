package com.pghaz.revery.animation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.OvershootInterpolator
import androidx.annotation.NonNull

object AnimatorUtils {

    enum class AnimatorProperties(val value: String) {
        ALPHA("alpha"),
        TRANSLATION_Y("translationY"),
        TRANSLATION_X("translationX"),
        TEXT_SIZE("textSize"),
        PIVOT_X("pivotX"),
        PIVOT_Y("pivotY"),
        SCALE_X("scaleX"),
        SCALE_Y("scaleY")
    }

    enum class TranslationDirection {
        FROM_TOP_TO_BOTTOM,
        FROM_BOTTOM_TO_TOP,
        FROM_RIGHT_TO_LEFT,
        FROM_LEFT_TO_RIGHT
    }

    enum class TranslationAxis(val type: String) {
        HORIZONTAL(AnimatorProperties.TRANSLATION_X.value),
        VERTICAL(AnimatorProperties.TRANSLATION_Y.value);
    }

    fun fadeIn(view: View, duration: Long, startOffset: Long) {
        view.animate().alpha(1f).setStartDelay(startOffset).setDuration(duration)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    super.onAnimationStart(animation)
                    view.visibility = View.VISIBLE
                }
            })
    }

    fun fadeOut(view: View, duration: Long, startOffset: Long) {
        view.animate().alpha(0f).setStartDelay(startOffset).setDuration(duration)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    view.visibility = View.GONE
                }
            })
    }

    @NonNull
    fun getTranslationAnimatorSet(
        view: View?,
        isEnterAnimation: Boolean,
        axis: TranslationAxis,
        direction: TranslationDirection,
        fade: Boolean,
        initialStateDuration: Long,
        animationDuration: Long
    ): AnimatorSet {
        if (view == null) {
            return AnimatorSet()
        }

        val startPosition = if (AnimatorProperties.TRANSLATION_X.value.equals(axis.type, true)) {
            if (direction == TranslationDirection.FROM_RIGHT_TO_LEFT) {
                view.measuredWidth
            } else {
                -view.measuredWidth
            }
        } else {
            if (direction == TranslationDirection.FROM_BOTTOM_TO_TOP) {
                view.measuredHeight
            } else {
                -view.measuredHeight
            }
        }

        val toPosition = if (AnimatorProperties.TRANSLATION_X.value.equals(axis.type, true)) {
            if (direction == TranslationDirection.FROM_RIGHT_TO_LEFT) {
                -view.measuredWidth
            } else {
                view.measuredWidth
            }
        } else {
            if (direction == TranslationDirection.FROM_BOTTOM_TO_TOP) {
                -view.measuredHeight
            } else {
                view.measuredHeight
            }
        }

        val translation = axis.type

        // initially, set the view outside of its container and transparent immediately if necessary
        // depending on params
        val initialStateAnimator = AnimatorSet()
        initialStateAnimator.duration = initialStateDuration

        initialStateAnimator.play(
            ObjectAnimator.ofFloat(
                view,
                translation,
                if (isEnterAnimation) startPosition.toFloat() else 0f,
                if (isEnterAnimation) startPosition.toFloat() else 0f
            )
        ).with(
            ObjectAnimator.ofFloat(
                view,
                AnimatorProperties.ALPHA.value,
                if (isEnterAnimation) 0f else 1f,
                if (isEnterAnimation) 0f else 1f
            )
        )

        // build the real animation
        val mainAnimator = AnimatorSet()
        mainAnimator.duration = animationDuration

        mainAnimator.play(
            ObjectAnimator.ofFloat(
                view,
                translation,
                if (isEnterAnimation) startPosition.toFloat() else 0f,
                if (isEnterAnimation) 0f else toPosition.toFloat()
            )
        ).with(
            ObjectAnimator.ofFloat(
                view, AnimatorProperties.ALPHA.value,
                if (fade && isEnterAnimation) 0f else 1f,
                if (fade && isEnterAnimation) 1f else 0f
            )
        )

        val animatorSet = AnimatorSet()
        animatorSet.interpolator =
            if (isEnterAnimation) OvershootInterpolator() else AnticipateOvershootInterpolator()
        animatorSet.play(initialStateAnimator).before(mainAnimator)

        bindAnimator(animatorSet, view, isEnterAnimation)

        return animatorSet
    }

    private fun bindAnimator(animator: Animator?, target: View?, isEnterAnimation: Boolean) {
        if (animator == null || target == null) {
            return
        }
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animator: Animator) {
                updateTargetView(animator, View.VISIBLE)
            }

            override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
                if (!isEnterAnimation) {
                    updateTargetView(animator, View.INVISIBLE)
                }
            }
        })
        animator.setTarget(target)
    }

    private fun updateTargetView(animator: Animator, visibility: Int) {
        if (animator is ObjectAnimator) {
            val target = animator.target
            if (target is View) {
                target.visibility = visibility
            }
        } else if (animator is AnimatorSet) {
            for (childAnimator in animator.childAnimations) {
                updateTargetView(childAnimator, visibility)
            }
        }
    }
}