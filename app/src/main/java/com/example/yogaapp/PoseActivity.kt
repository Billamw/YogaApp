package com.example.yogaapp

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yogaapp.dataclasses.Pose
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class PoseActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var poseAdapter: PoseAdapter
    private var poses = mutableListOf<Pose>()
    private lateinit var addPoseButton: Button
    private var categories = mutableListOf<JSONObject>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_poses)

        recyclerView = findViewById(R.id.exercises_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        poseAdapter = PoseAdapter(poses, this)
        recyclerView.adapter = poseAdapter

        addPoseButton = findViewById(R.id.add_pose_button)
        addPoseButton.setOnClickListener {
            showAddPoseDialog()
        }

        loadCategories()
        loadPoses()
    }

    private fun loadCategories() {
        try {
            val file = File(MainActivity.poseDataFilePath)
            val json = file.readText()
            val jsonObject = JSONObject(json)
            val categoriesArray = jsonObject.getJSONArray("categories")
            for (i in 0 until categoriesArray.length()) {
                val categoryObject = categoriesArray.getJSONObject(i)
                categories.add(categoryObject)
            }
        } catch (e: Exception) {
            Log.e("Error", "Failed to load categories: ${e.message}")
        }
    }

    private fun loadPoses() {
        try {
            val file = File(MainActivity.poseDataFilePath)
            val json = file.readText()
            val jsonObject = JSONObject(json)
            val jsonArray = jsonObject.getJSONArray("poses")

            poses.clear()

            for (i in 0 until jsonArray.length()) {
                val poseObject = jsonArray.getJSONObject(i)
                val name = poseObject.getString("english_name")
                val description = poseObject.getString("pose_description")
                val benefits = poseObject.getString("pose_benefits")
                val categories = poseObject.getJSONArray("category_name").let { categoryArray ->
                    List(categoryArray.length()) { categoryArray.getString(it) }
                }
                val localSvgPath = poseObject.getString("local_svg_path")
                poses.add(Pose(name, description, benefits, categories, localSvgPath))
            }

            poseAdapter.notifyDataSetChanged()
            Log.i("Load", "Added all poses")
        } catch (e: Exception) {
            Log.e("Error", "Failed to load poses: ${e.message}")
        }
    }

    private fun showAddPoseDialog() {
        val dialogView = layoutInflater.inflate(R.layout.pose_adding_dialog, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Add New Pose")
            .setPositiveButton("Add", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val addButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            addButton.setOnClickListener {
                val name = dialogView.findViewById<EditText>(R.id.et_pose_name).text.toString()
                val description = dialogView.findViewById<EditText>(R.id.et_pose_description).text.toString()
                val benefits = dialogView.findViewById<EditText>(R.id.et_pose_benefits).text.toString()
                val selectedCategories = getSelectedCategories(dialogView)
                Log.i("showAddPoseDialog", "selectedCategories: ${selectedCategories.toString()}")

                // Check if the pose name already exists
                if (poses.any { it.name.equals(name, ignoreCase = true) }) {
                    Toast.makeText(this, "Pose name already exists!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (name.isNotBlank() && description.isNotBlank() && benefits.isNotBlank()) {
                    val newPose = Pose(name, description, benefits, selectedCategories?: emptyList(), "")
                    poses.add(newPose)
                    poseAdapter.notifyDataSetChanged()
                    savePoses()
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, "Please fill all fields and select at least one category", Toast.LENGTH_SHORT).show()
                }
            }
        }

        populateCategoriesInDialog(dialogView)
        dialog.show()
    }

    private fun getSelectedCategories(dialogView: View): List<String> {
        val categoriesLayout = dialogView.findViewById<LinearLayout>(R.id.ll_categories)
        val selectedCategories = mutableListOf<String>()

        fun searchCheckboxes(parent: View) {
            if (parent is CheckBox) {
                if (parent.isChecked) {
                    val categoryName = parent.text.toString()
                    selectedCategories.add(categoryName)

                    // Check if this is a new category and add it to the JSON
                    if (!categories.any { it.getString("category_name") == categoryName }) {
                        val newCategory = JSONObject()
                        newCategory.put("category_name", categoryName)
                        newCategory.put("category_description", "")
                        categories.add(newCategory)
                    }
                }
            } else if (parent is ViewGroup) {
                for (i in 0 until parent.childCount) {
                    searchCheckboxes(parent.getChildAt(i))
                }
            }
        }

        searchCheckboxes(categoriesLayout)
        return selectedCategories
    }

    private fun populateCategoriesInDialog(dialogView: View) {
        val categoriesLayout = dialogView.findViewById<LinearLayout>(R.id.ll_categories)
        categoriesLayout.orientation = LinearLayout.VERTICAL

        val columnLayout = LinearLayout(this)
        columnLayout.orientation = LinearLayout.HORIZONTAL
        columnLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val leftColumn = LinearLayout(this)
        leftColumn.orientation = LinearLayout.VERTICAL
        leftColumn.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )

        val rightColumn = LinearLayout(this)
        rightColumn.orientation = LinearLayout.VERTICAL
        rightColumn.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )

        columnLayout.addView(leftColumn)
        columnLayout.addView(rightColumn)

        categories.forEachIndexed { index, category ->
            val checkBox = CheckBox(this)
            checkBox.text = category.getString("category_name")
            if (index % 2 == 0) {
                leftColumn.addView(checkBox)
            } else {
                rightColumn.addView(checkBox)
            }
        }

        categoriesLayout.addView(columnLayout)

        // Add the editable checkbox and EditText at the bottom
        val editableCheckBox = CheckBox(this)
        val editText = EditText(this)
        editText.textSize = 16f
        editText.hint = "Add new category"
        editText.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        editText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                editableCheckBox.isChecked = true
            }
        }
        editableCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && editText.text.isEmpty()) {
                editText.requestFocus()
            }
        }
        val container = LinearLayout(this)
        container.orientation = LinearLayout.HORIZONTAL
        container.addView(editableCheckBox)
        container.addView(editText)
        categoriesLayout.addView(container)
    }

    private fun savePoses() {
        try {
            val jsonObject = JSONObject()
            val posesArray = JSONArray()
            for (pose in poses) {
                val poseObject = JSONObject()
                poseObject.put("english_name", pose.name)
                poseObject.put("pose_description", pose.description)
                poseObject.put("pose_benefits", pose.benefits)
                poseObject.put("category_name", JSONArray(pose.categories))
                poseObject.put("local_svg_path", pose.localImagePath)
                posesArray.put(poseObject)

                // Add any new categories
                pose.categories.forEach { categoryName ->
                    if (!categories.any { it.getString("category_name") == categoryName }) {
                        val newCategory = JSONObject()
                        newCategory.put("category_name", categoryName)
                        newCategory.put("category_description", "")
                        categories.add(newCategory)
                    }
                }
            }
            jsonObject.put("poses", posesArray)

            val categoriesArray = JSONArray(categories)
            jsonObject.put("categories", categoriesArray)

            File(MainActivity.poseDataFilePath).writeText(jsonObject.toString())
            Log.i("Save", "Poses and categories saved successfully")
        } catch (e: Exception) {
            Log.e("Error", "Failed to save poses and categories: ${e.message}")
        }
    }
}
