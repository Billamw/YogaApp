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
import com.example.yogaapp.objects.AddPoseDialog
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class PoseActivity : AppCompatActivity(), AddPoseDialog.OnPoseAddedListener {

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

    private fun showAddPoseDialog() {
        AddPoseDialog(
            context = this,
            existingPoses = poses,
            categories = categories,
            listener = this
        ).show()
    }

    override fun onPoseAdded(newPose: Pose, updatedCategories: List<JSONObject>) {
        poses.add(newPose)
        categories.addAll(updatedCategories)
        poseAdapter.notifyDataSetChanged()
        savePoses()
    }
}
