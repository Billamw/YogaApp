package com.example.yogaapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yogaapp.dataclasses.Pose
import com.example.yogaapp.objects.AddPoseDialog
import com.example.yogaapp.objects.JsonHelper
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
                val uuid = poseObject.getString("uuid")
                val name = poseObject.getString("english_name")
                val description = poseObject.getString("pose_description")
                val benefits = poseObject.getString("pose_benefits")
                val categories = poseObject.getJSONArray("category_name").let { categoryArray ->
                    List(categoryArray.length()) { categoryArray.getString(it) }
                }
                val localSvgPath = poseObject.getString("local_svg_path")
                poses.add(Pose(uuid, name, description, benefits, categories, localSvgPath))
            }

            poseAdapter.notifyDataSetChanged()
            Log.i("Load", "Added all poses")
        } catch (e: Exception) {
            Log.e("Error", "Failed to load poses: ${e.message}")
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AddPoseDialog.IMAGE_PICK_REQUEST && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                // Persist permission for future access
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                AddPoseDialog.handleImageSelected(uri)
            }
        }
    }

    private fun onPoseAdded(newPose: Pose, updatedCategories: List<JSONObject>, imagePath: String?) {
        val savedPath = imagePath?.let { uriString ->
            saveImageToInternalStorage(Uri.parse(uriString), newPose.name)
        }

        poses.add(newPose.copy(localImagePath = savedPath ?: imagePath ?: ""))
        categories.addAll(updatedCategories)
        poseAdapter.notifyDataSetChanged()
        JsonHelper.savePoses(poses, categories)
    }

    private fun saveImageToInternalStorage(uri: Uri, poseName: String): String? {
        return try {
            val imagesDir = File(filesDir, "pose_images").apply { mkdirs() }
            val outputFile = File(imagesDir, "${poseName.replace(" ", "_")}.png")

            contentResolver.openInputStream(uri)?.use { input ->
                outputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            outputFile.absolutePath
        } catch (e: Exception) {
            Log.e("SaveImage", "Error saving image: ${e.message}")
            null
        }
    }
    private fun showAddPoseDialog() {
        AddPoseDialog.show(
            context = this,
            existingPoses = poses,
            categories = categories
        ) { newPose, newCategories, imagePath -> // Receive parameters from dialog
            onPoseAdded(newPose, newCategories, imagePath)
        }
    }
}
