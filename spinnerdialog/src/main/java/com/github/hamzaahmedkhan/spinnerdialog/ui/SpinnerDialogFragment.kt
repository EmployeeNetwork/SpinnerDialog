package com.github.hamzaahmedkhan.spinnerdialog.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.github.hamzaahmedkhan.spinnerdialog.R
import com.github.hamzaahmedkhan.spinnerdialog.callbacks.OnSpinnerItemCheckboxClickListener
import com.github.hamzaahmedkhan.spinnerdialog.callbacks.OnSpinnerItemClickListener
import com.github.hamzaahmedkhan.spinnerdialog.callbacks.OnSpinnerOKPressedListener
import com.github.hamzaahmedkhan.spinnerdialog.enums.SpinnerSelectionType
import com.github.hamzaahmedkhan.spinnerdialog.models.SpinnerModel
import com.github.hamzaahmedkhan.spinnerdialog.ui.multi.SpinnerDialogMultiSelectAdapter
import com.github.hamzaahmedkhan.spinnerdialog.ui.single.SpinnerDialogSingleSelectAdapter
import kotlinx.android.synthetic.main.fragment_spinner_popup.*
import kotlin.collections.ArrayList

/**
 * Created by khanhamza on 21-Feb-17.
 */

class SpinnerDialogFragment : androidx.fragment.app.DialogFragment(),
    OnSpinnerItemClickListener, View.OnClickListener, OnSpinnerItemCheckboxClickListener {

    private var singleSelectAdapter: SpinnerDialogSingleSelectAdapter? = null
    private var multiSelectAdapter: SpinnerDialogMultiSelectAdapter? = null
    private var arrData: ArrayList<SpinnerModel> = ArrayList()
    private var arrFilteredData: ArrayList<SpinnerModel> = ArrayList()
    private var onSpinnerOKPressedListener: OnSpinnerOKPressedListener? = null
    private var scrollToPosition: Int = 0
    private var selectedPosition = 0


    // Properties
    var title = ""
    var subtitle: String? = ""
    var searchbarHint = "type here to search..."
    var themeColorResId: Int = -1
    var buttonText: String = "OK"
    var showSearchBar = true
    var spinnerSelectionType =
        SpinnerSelectionType.SINGLE_SELECTION
    private var imageWidth: Int? = null
    private var imageHeight: Int? = null
    private var dialogHeight = ViewGroup.LayoutParams.MATCH_PARENT
    private var showDescription: Boolean = false
    private var showChoiceImage: Boolean = false


    override fun onStart() {
        super.onStart()

        dialog?.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dialogHeight
            )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
            STYLE_NO_TITLE,
            R.style.DialogTheme
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_spinner_popup, container)
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setListeners()


        // Assign color to header and Ok button if provided by user
        if (themeColorResId != -1) {
            contHeader.setBackgroundColor(themeColorResId)
            btnOK.setBackgroundColor(themeColorResId)
        }


        // Show Search bar if true
        if (showSearchBar) {
            contSearchBar.visibility = View.VISIBLE
        } else {
            contSearchBar.visibility = View.GONE
        }

        // Set text to OK Button
        btnOK.text = buttonText

        // Set text of title
        txtTitle.text = title
        subtitle?.let {
            txtSubtitle.text = it
            txtSubtitle.visibility = VISIBLE
        } ?: run {
            txtSubtitle.visibility = GONE
        }

        edtSearch.hint = searchbarHint

        // init Adapter
        if (spinnerSelectionType == SpinnerSelectionType.SINGLE_SELECTION) {
            singleSelectAdapter =
                SpinnerDialogSingleSelectAdapter(
                    context!!,
                    arrFilteredData,
                    this,
                    showDescription,
                    showChoiceImage,
                    imageWidth = imageWidth,
                    imageHeight = imageHeight
                )
        } else {
            multiSelectAdapter = SpinnerDialogMultiSelectAdapter(context!!, arrFilteredData, this, showDescription, showChoiceImage, imageWidth, imageHeight)
        }

        bindView()
    }

    private fun setListeners() {
        btnOK.setOnClickListener(this)

        edtSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                setFilterData(s?.toString())
            }
        })
    }

    private fun bindView() {
        val mLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(
            context,
            androidx.recyclerview.widget.LinearLayoutManager.VERTICAL,
            false
        )
        recyclerView.layoutManager = mLayoutManager
        (recyclerView.itemAnimator as androidx.recyclerview.widget.DefaultItemAnimator).supportsChangeAnimations =
            false
        val resId =
            R.anim.layout_animation_fall_bottom
        val animation = AnimationUtils.loadLayoutAnimation(context, resId)
        recyclerView.layoutAnimation = animation
        recyclerView.adapter =
            if (spinnerSelectionType == SpinnerSelectionType.SINGLE_SELECTION) singleSelectAdapter else multiSelectAdapter
        scrollToPosition(scrollToPosition)
    }

    fun scrollToPosition(scrollToPosition: Int) {
        if (scrollToPosition > -1) {
            recyclerView.scrollToPosition(scrollToPosition)
        } else {
            recyclerView.scrollToPosition(0)
        }

    }

    fun setFilterData(filterText: String?) {
        if (filterText.isNullOrEmpty()) {
            arrFilteredData.clear()
            arrFilteredData.addAll(arrData)
        } else {
            arrFilteredData.clear()
            arrFilteredData.addAll(arrData.filter {
                it.text.contains(
                    filterText,
                    true
                ) || it.description.contains(filterText, true)
            })
        }
        if (spinnerSelectionType == SpinnerSelectionType.SINGLE_SELECTION) {
            singleSelectAdapter?.notifyDataSetChanged()
        } else {
            multiSelectAdapter?.notifyDataSetChanged()
        }
    }


    override fun onClick(v: View?) {
        if (onSpinnerOKPressedListener != null) {
            arrData.filter { it.isSelected }.let {
                if (it.size > 1) {
                    onSpinnerOKPressedListener!!.onMultiSelection(
                        it,
                        selectedPosition
                    )
                } else if (it.size == 1) {
                    onSpinnerOKPressedListener!!.onSingleSelection(
                        it.first(),
                        selectedPosition
                    )
                }
            }
        }
        this.dismiss()
    }

    override fun onItemClick(
        position: Int,
        anyObject: Any,
        singleSelectAdapter: SpinnerDialogSingleSelectAdapter
    ) {
        selectedPosition = arrData.indexOf(anyObject as SpinnerModel)

        // Set selected from all data
        arrData.forEach { it.isSelected = false }
        arrData[selectedPosition].isSelected = true

        // Set selected in Filtered data
        arrFilteredData.forEach { it.isSelected = false }
        arrFilteredData[position].isSelected = true

        singleSelectAdapter.notifyDataSetChanged()
    }

    companion object {
        fun newInstance(
            spinnerSelectionType: SpinnerSelectionType,
            title: String,
            subtitle: String?,
            arrData: ArrayList<SpinnerModel>,
            onSpinnerOKPressedListener: OnSpinnerOKPressedListener,
            scrollToPosition: Int,
            imageWidth: Int? = null,
            imageHeight: Int? = null
        ): SpinnerDialogFragment {
            val frag =
                SpinnerDialogFragment()
            val args = Bundle()
            frag.title = title
            frag.subtitle = subtitle
            frag.arrData.addAll(arrData)
            frag.arrFilteredData.addAll(arrData)
            frag.scrollToPosition = scrollToPosition
            frag.onSpinnerOKPressedListener = onSpinnerOKPressedListener
            frag.imageWidth = imageWidth
            frag.imageHeight = imageHeight
            frag.arguments = args
            frag.spinnerSelectionType = spinnerSelectionType

            return frag
        }
    }

    override fun onItemClick(
        position: Int,
        anyObject: Any,
        adapter: SpinnerDialogMultiSelectAdapter
    ) {

    }

    /**
     * @param height provide height in Integer
     *
     * Provide value in dp to set height of dialog
     * default value is ViewGroup.LayoutParams.MATCH_PARENT
     * use ViewGroup.LayoutParams.WRAP_CONTENT to choose height as wrap content.
     *
     * If provided height is less than 0, then it will be assigned as MATCH_PARENT.
     *
     * You can use extension #{IntegerExtension.Int.dp} to convert integer value into dp. Example 500.dp
     */
    fun setDialogHeight(height: Int) {
        dialogHeight =
            if (height == ViewGroup.LayoutParams.MATCH_PARENT || height == ViewGroup.LayoutParams.WRAP_CONTENT) {
                height
            } else if (height < 0) {
                ViewGroup.LayoutParams.MATCH_PARENT
            } else {
                height
            }
    }

    /**
     *  Set true if you want to show description, else false.
     *
     *  If true, it will show description which is provided in {@link SpinnerModel#description}
     *
     *  Default value is false
     *
     */
    fun showDescription(showDescription: Boolean) {
        this.showDescription = showDescription
    }


    /**
     *  Set true if you want to show Image for choices, else false.
     *
     *  If true, it will show image which is provided in {@link SpinnerModel#imagePath}. User can also set ImageType.
     *
     *  Default value is false
     *
     */
    fun showImage(showChoiceImage: Boolean) {
        this.showChoiceImage = showChoiceImage
    }


}

