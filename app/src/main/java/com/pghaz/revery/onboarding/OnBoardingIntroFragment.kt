package com.pghaz.revery.onboarding

import android.os.Bundle
import com.pghaz.revery.BaseFragment
import com.pghaz.revery.R
import kotlinx.android.synthetic.main.fragment_on_boarding_intro.*

class OnBoardingIntroFragment : BaseFragment() {

    override fun getLayoutResId(): Int {
        return R.layout.fragment_on_boarding_intro
    }

    override fun configureViews(savedInstanceState: Bundle?) {
        titleTextView.text = String.format(
            getString(R.string.on_boarding_intro_title),
            getString(R.string.app_name)
        )
    }
}