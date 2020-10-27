package com.pghaz.revery

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.CallSuper
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.core.view.ViewCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.pghaz.revery.util.Arguments
import kotlinx.android.synthetic.main.header_bottom_sheet.*


abstract class BaseBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private var dialogTitle: String? = null

    @DrawableRes
    private var closeButtonDrawableRes: Int = R.drawable.ic_close

    @LayoutRes
    abstract fun getLayoutResId(): Int
    abstract fun configureViews(savedInstanceState: Bundle?)

    override fun getTheme(): Int {
        return R.style.ReveryBottomSheetDialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            parseArguments(savedInstanceState)
        } else {
            parseArguments(arguments)
        }
    }

    @CallSuper
    open fun parseArguments(arguments: Bundle?) {
        closeButtonDrawableRes =
            arguments?.getInt(Arguments.ARGS_DIALOG_CLOSE_ICON, R.drawable.ic_close) ?: 0
        dialogTitle = arguments?.getString(Arguments.ARGS_DIALOG_TITLE, "")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(Arguments.ARGS_DIALOG_CLOSE_ICON, closeButtonDrawableRes)
        outState.putString(Arguments.ARGS_DIALOG_TITLE, dialogTitle)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(getLayoutResId(), container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        closeButton.setImageResource(closeButtonDrawableRes)
        closeButton.setOnClickListener {
            dismiss()
        }

        dialogTitleTextView.text = dialogTitle

        configureViews(savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        bottomSheetDialog.dismissWithAnimation = true

        bottomSheetDialog.setOnShowListener { dialog ->
            configureBottomSheetDialog(dialog as BottomSheetDialog)
        }

        return bottomSheetDialog
    }

    override fun onStop() {
        super.onStop()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            dialog?.window?.setWindowAnimations(-1)
        }
    }

    private fun configureBottomSheetDialog(bottomSheetDialog: BottomSheetDialog) {
        val bottomSheet =
            bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)

        // Make dialog top corners rounded when dialog is expanded
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    //In the EXPANDED STATE apply a new MaterialShapeDrawable with rounded corners
                    val newMaterialShapeDrawable = createMaterialShapeDrawable(bottomSheet)
                    ViewCompat.setBackground(bottomSheet, newMaterialShapeDrawable)
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // do nothing
            }
        })

        // This must be called after ´addBottomSheetCallback()´
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetBehavior.isDraggable = false
    }

    private fun createMaterialShapeDrawable(bottomSheet: View): MaterialShapeDrawable {
        val shapeAppearanceModel =
            //Create a ShapeAppearanceModel with the same shapeAppearanceOverlay used in the style
            ShapeAppearanceModel.builder(context, 0, R.style.ReveryShapeAppearanceBottomSheetDialog)
                .build()

        //Create a new MaterialShapeDrawable (you can't use the original MaterialShapeDrawable in the BottoSheet)
        val currentMaterialShapeDrawable = bottomSheet.background as MaterialShapeDrawable
        val newMaterialShapeDrawable = MaterialShapeDrawable(shapeAppearanceModel)
        //Copy the attributes in the new MaterialShapeDrawable
        newMaterialShapeDrawable.initializeElevationOverlay(context)
        newMaterialShapeDrawable.fillColor = currentMaterialShapeDrawable.fillColor
        newMaterialShapeDrawable.tintList = currentMaterialShapeDrawable.tintList
        newMaterialShapeDrawable.elevation = currentMaterialShapeDrawable.elevation
        newMaterialShapeDrawable.strokeWidth = currentMaterialShapeDrawable.strokeWidth
        newMaterialShapeDrawable.strokeColor = currentMaterialShapeDrawable.strokeColor
        return newMaterialShapeDrawable
    }
}