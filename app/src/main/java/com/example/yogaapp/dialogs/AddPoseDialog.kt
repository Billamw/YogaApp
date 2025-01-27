package com.example.yogaapp.dialogs

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.yogaapp.R
import com.example.yogaapp.dataclasses.Pose
import org.json.JSONObject
import java.util.UUID

@SuppressLint("StaticFieldLeak")
object AddPoseDialog {
    const val IMAGE_PICK_REQUEST = 1
    private var currentImageView: ImageView? = null
    var selectedImageUri: Uri? = null
    private var currentContext: Context? = null

    fun handleImageSelected(uri: Uri) {

        currentImageView?.let { imageView ->
            currentContext?.let { context ->
                // Load image using Glide (recommended)
                Glide.with(context)
                    .load(uri)
                    .into(imageView)

                selectedImageUri = uri
            }
        }
    }

    fun show(
        context: Context,
        existingPoses: List<Pose>,
        categories: List<JSONObject>,
        onPoseAdded: (Pose, List<JSONObject>, String?) -> Unit // Correct parameter types
    ) {
        currentContext = context
        val dialogView = LayoutInflater.from(context).inflate(R.layout.pose_adding_dialog, null)
        val dialog = AlertDialog.Builder(context).create()
        dialog.setView(dialogView)

        currentImageView = dialogView.findViewById(R.id.iv_pose_image)


        val selectImageButton = dialogView.findViewById<Button>(R.id.btn_select_image)
        val nameEditText = dialogView.findViewById<EditText>(R.id.et_pose_name)
        val descEditText = dialogView.findViewById<EditText>(R.id.et_pose_description)
        val benefitsEditText = dialogView.findViewById<EditText>(R.id.et_pose_benefits)
        val categoriesContainer = dialogView.findViewById<LinearLayout>(R.id.ll_categories)
        val addButton = dialogView.findViewById<Button>(R.id.btn_add_pose)

        // Populate categories
        populateCategories(currentContext ?: context, categoriesContainer, categories)

        selectImageButton.setOnClickListener {
            // Start image picker
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/svg+xml", "image/png", "image/jpeg"))
            }
            (context as Activity).startActivityForResult(intent, IMAGE_PICK_REQUEST)
        }

        addButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val description = descEditText.text.toString()
            val benefits = benefitsEditText.text.toString()
            val selectedCategories = getSelectedCategories(categoriesContainer)
            val newCategories = getNewCategories(categoriesContainer, categories)

            if (validateInput(name, existingPoses, context)) {
                val newPose = Pose(
                    uuid = UUID.randomUUID().toString(),
                    name = name,
                    description = description,
                    benefits = benefits,
                    categories = selectedCategories,
                    localImagePath = selectedImageUri?.toString()
                )
                onPoseAdded(newPose, newCategories, selectedImageUri?.toString())
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun populateCategories(context: Context, container: LinearLayout, categories: List<JSONObject>) {
        container.removeAllViews()
        val columnLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val leftColumn = createCategoryColumn(container)
        val rightColumn = createCategoryColumn(container)

        categories.forEachIndexed { index, category ->
            val checkBox = CheckBox(context).apply {
                text = category.getString("category_name")
            }
            if (index % 2 == 0) leftColumn.addView(checkBox) else rightColumn.addView(checkBox)
        }

        columnLayout.addView(leftColumn)
        columnLayout.addView(rightColumn)
        container.addView(columnLayout)

        addNewCategoryInput(context, container)
    }

    private fun addNewCategoryInput(context: Context, container: LinearLayout) {
        val (checkBox, editText) = createNewCategoryInput(context)
        val inputContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(checkBox)
            addView(editText)
            container.addView(this)
        }
    }

    private fun createNewCategoryInput(context: Context): Pair<CheckBox, EditText> {
        return Pair(
            CheckBox(context),
            EditText(context).apply {
                hint = "Add new category"
                setOnFocusChangeListener { _, hasFocus ->
                    (parent as? LinearLayout)?.getChildAt(0)?.let { checkBox ->
                        (checkBox as CheckBox).isChecked = hasFocus
                    }
                }
            })
    }


    private fun createCategoryColumn(container: LinearLayout): LinearLayout {
        return LinearLayout(container.context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }
    }

    private fun getSelectedCategories(container: LinearLayout): List<String> {
        val selected = mutableListOf<String>()
        for (i in 0 until container.childCount) {
            val column = container.getChildAt(i) as? LinearLayout
            column?.let {
                for (j in 0 until it.childCount) {
                    val checkBox = it.getChildAt(j) as? CheckBox
                    checkBox?.let { cb ->
                        if (cb.isChecked) {
                            selected.add(cb.text.toString())
                        }
                    }
                }
            }
        }
        return selected
    }

    private fun getNewCategories(container: LinearLayout, existingCategories: List<JSONObject>): List<JSONObject> {
        // Implement if you have new category creation in your UI
        return emptyList()
    }

    private fun validateInput(
        name: String,
        existingPoses: List<Pose>,
        context: Context
    ): Boolean {
        return when {
            name.isBlank() -> {
                showError("Please fill out name", context)
                false
            }
            existingPoses.any { it.name.equals(name, true) } -> {
                showError("Pose name already exists!", context)
                false
            }
            else -> true
        }
    }

    private fun showError(message: String, context: Context) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}