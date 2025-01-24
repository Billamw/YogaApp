package com.example.yogaapp.objects

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import com.example.yogaapp.R
import com.example.yogaapp.dataclasses.Pose
import org.json.JSONObject

class AddPoseDialog(
    private val context: Context,
    private val existingPoses: List<Pose>,
    private val categories: MutableList<JSONObject>,
    private val listener: OnPoseAddedListener
) {
    private lateinit var dialog: AlertDialog
    private lateinit var dialogView: View

    interface OnPoseAddedListener {
        fun onPoseAdded(newPose: Pose, updatedCategories: List<JSONObject>)
    }

    fun show() {
        createDialog()
        setupViews()
        dialog.show()
    }

    private fun createDialog() {
        dialogView = LayoutInflater.from(context).inflate(R.layout.pose_adding_dialog, null)
        dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setTitle("Add New Pose")
            .setPositiveButton("Add", null)
            .setNegativeButton("Cancel", null)
            .create()
    }

    private fun setupViews() {
        setupCategorySelection()
        setupAddButton()
    }

    private fun setupCategorySelection() {
        val categoriesLayout = dialogView.findViewById<LinearLayout>(R.id.ll_categories)
        categoriesLayout.removeAllViews()

        // Create two columns for existing categories
        val (leftColumn, rightColumn) = createCategoryColumns()
        populateExistingCategories(leftColumn, rightColumn)
        addNewCategoryInput(categoriesLayout)
    }

    private fun createCategoryColumns(): Pair<LinearLayout, LinearLayout> {
        val columnLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        return Pair(
            LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            },
            LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
        ).also { (left, right) ->
            columnLayout.addView(left)
            columnLayout.addView(right)
        }
    }

    private fun populateExistingCategories(left: LinearLayout, right: LinearLayout) {
        categories.forEachIndexed { index, category ->
            val checkBox = CheckBox(context).apply {
                text = category.getString("category_name")
            }
            if (index % 2 == 0) left.addView(checkBox) else right.addView(checkBox)
        }
    }

    private fun addNewCategoryInput(container: LinearLayout) {
        val (checkBox, editText) = createNewCategoryInput()
        val inputContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(checkBox)
            addView(editText)
        }
        container.addView(inputContainer)
    }

    private fun createNewCategoryInput(): Pair<CheckBox, EditText> {
        return Pair(
            CheckBox(context),
            EditText(context).apply {
                hint = "Add new category"
                setOnFocusChangeListener { _, hasFocus ->
                    (parent as? LinearLayout)?.getChildAt(0)?.let { checkBox ->
                        (checkBox as CheckBox).isChecked = hasFocus
                    }
                }
            }
        )
    }

    private fun setupAddButton() {
        dialog.setOnShowListener {
            val addButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            addButton.setOnClickListener {
                val newPose = validateInput() ?: return@setOnClickListener
                val (selectedCategories, newCategories) = getSelectedCategories()

                listener.onPoseAdded(
                    newPose.copy(categories = selectedCategories),
                    categories.apply { addAll(newCategories) }
                )
                dialog.dismiss()
            }
        }
    }

    private fun validateInput(): Pose? {
        val name = dialogView.findViewById<EditText>(R.id.et_pose_name).text.toString()
        val description = dialogView.findViewById<EditText>(R.id.et_pose_description).text.toString()
        val benefits = dialogView.findViewById<EditText>(R.id.et_pose_benefits).text.toString()

        return when {
            existingPoses.any { it.name.equals(name, true) } -> {
                showError("Pose name already exists!")
                null
            }
            name.isBlank() || description.isBlank() || benefits.isBlank() -> {
                showError("Please fill all required fields")
                null
            }
            else -> Pose(name, description, benefits, emptyList(), "")
        }
    }

    private fun getSelectedCategories(): Pair<List<String>, List<JSONObject>> {
        val selected = mutableListOf<String>()
        val newCategories = mutableListOf<JSONObject>()

        dialogView.findViewById<LinearLayout>(R.id.ll_categories).traverseViews { view ->
            if (view is CheckBox && view.isChecked) {
                val categoryName = when (view.parent) {
                    is LinearLayout -> (view.parent as LinearLayout)
                        .getChildAt(1)?.let { (it as? EditText)?.text?.toString() }
                        ?: view.text.toString()
                    else -> view.text.toString()
                }

                if (categoryName.isNotBlank()) {
                    selected.add(categoryName)
                    if (categories.none { it.getString("category_name") == categoryName }) {
                        newCategories.add(JSONObject().apply {
                            put("category_name", categoryName)
                            put("category_description", "")
                        })
                    }
                }
            }
        }

        return Pair(selected, newCategories)
    }

    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun ViewGroup.traverseViews(action: (View) -> Unit) {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child is ViewGroup) child.traverseViews(action) else action(child)
        }
    }
}