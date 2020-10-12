package com.pghaz.revery.animation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.*
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
        fadeIn(view, TranslationDirection.FROM_BOTTOM_TO_TOP, duration, startOffset)
    }

    fun fadeOut(view: View, duration: Long, startOffset: Long) {
        fadeOut(view, TranslationDirection.FROM_TOP_TO_BOTTOM, duration, startOffset)
    }

    private fun fadeOut(
        view: View,
        direction: TranslationDirection,
        duration: Long,
        startOffset: Long
    ) {
        val animationSet = AnimationSet(true)
        animationSet.interpolator = OvershootInterpolator()
        animationSet.startOffset = startOffset
        animationSet.fillAfter = true
        animationSet.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                view.visibility = View.INVISIBLE
            }

            override fun onAnimationRepeat(animation: Animation?) {
            }
        })

        val y = if (direction == TranslationDirection.FROM_BOTTOM_TO_TOP) {
            (-view.height).toFloat()
        } else {
            view.height.toFloat()
        }

        val translateAnimation = TranslateAnimation(0f, 0f, 0f, y)
        translateAnimation.duration = duration

        val alphaAnimation = AlphaAnimation(1f, 0f)
        alphaAnimation.duration = duration

        animationSet.addAnimation(translateAnimation)
        animationSet.addAnimation(alphaAnimation)

        view.startAnimation(animationSet)

        view.visibility = View.INVISIBLE
    }

    private fun fadeIn(
        view: View,
        direction: TranslationDirection,
        duration: Long,
        startOffset: Long
    ) {
        val animationSet = AnimationSet(true)
        animationSet.interpolator = OvershootInterpolator()
        animationSet.startOffset = startOffset
        animationSet.fillAfter = true
        animationSet.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                view.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animation?) {
            }

            override fun onAnimationRepeat(animation: Animation?) {
            }
        })

        val y = if (direction == TranslationDirection.FROM_BOTTOM_TO_TOP) {
            view.height.toFloat()
        } else {
            (-view.height).toFloat()
        }

        val translateAnimation = TranslateAnimation(0f, 0f, y, 0f)
        translateAnimation.duration = duration

        val alphaAnimation = AlphaAnimation(0f, 1f)
        alphaAnimation.duration = duration

        animationSet.addAnimation(translateAnimation)
        animationSet.addAnimation(alphaAnimation)

        view.startAnimation(animationSet)
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

        restoreFinalVisibility(view, isEnterAnimation)

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
        animatorSet.interpolator = OvershootInterpolator()
        animatorSet.play(initialStateAnimator).before(mainAnimator)

        bindAnimator(animatorSet, view)

        return animatorSet
    }

    private fun restoreFinalVisibility(view: View, isEnterAnimation: Boolean) {
        if (isEnterAnimation) {
            view.visibility = View.VISIBLE
        } else {
            view.visibility = View.INVISIBLE
        }
    }

    private fun bindAnimator(animator: Animator?, target: View?) {
        if (animator == null || target == null) {
            return
        }
        animator.addListener(animatorCallback)
        animator.setTarget(target)
    }

    private val animatorCallback: Animator.AnimatorListener = object : AnimatorListenerAdapter() {
        override fun onAnimationStart(animator: Animator) {
            updateTargetView(animator, View.VISIBLE)
        }
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