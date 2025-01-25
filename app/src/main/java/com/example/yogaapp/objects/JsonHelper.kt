package com.example.yogaapp.objects

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import android.widget.Toast
import com.example.yogaapp.PoseActivity
import com.example.yogaapp.MainActivity
import com.example.yogaapp.PoseAdapter
import com.example.yogaapp.dataclasses.Pose
import com.example.yogaapp.dataclasses.Training
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

object JsonHelper {

    private var file = File(MainActivity.poseDataFilePath)

    fun copyPoseDataToLocalStorageOnce(context: Context, assets: AssetManager) {
        val sharedPrefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val isDataCopied = sharedPrefs.getBoolean("isPoseDataCopied", false)
        if (!isDataCopied) {
            try {
                val inputStream = assets.open(MainActivity.POSE_DATA_FILENAME)
                val jsonString = inputStream.bufferedReader().use { it.readText() }
                val jsonObject = JSONObject(jsonString)
                val posesArray = jsonObject.getJSONArray("poses")

                for (i in 0 until posesArray.length()) {
                    val poseObject = posesArray.getJSONObject(i)
                    val englishName = poseObject.getString("english_name")
                    val svgUrl = poseObject.getString("url_svg")
                    val uuid = UUID.randomUUID().toString()

                    // Update JSON object
                    poseObject.put("uuid", uuid)
                    poseObject.put("pose_name", englishName)
                    poseObject.put("local_svg_path", "")
                    poseObject.remove("id")
                    poseObject.remove("sanskrit_name_adapted")
                    poseObject.remove("sanskrit_name")
                    poseObject.remove("translation_name")
                    poseObject.remove("url_svg")
                    poseObject.remove("url_png")
                    poseObject.remove("url_svg_alt")
                }

                // Save modified JSON
                val outputFile = File(context.filesDir, MainActivity.POSE_DATA_FILENAME)
                outputFile.writeText(jsonObject.toString(4))

                MainActivity.poseDataFilePath = outputFile.absolutePath
                Log.i("copyPoseDataToLocalStorageOnce", "poseDataFilePath: ${MainActivity.poseDataFilePath}")

                // Set the flag to true after successful copy
                sharedPrefs.edit().putBoolean("isPoseDataCopied", true).apply()
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("copyPoseDataToLocalStorageOnce", "Error processing pose data: ${e.message}")
            }
        } else {
            MainActivity.poseDataFilePath = File(context.filesDir, MainActivity.POSE_DATA_FILENAME).absolutePath
        }
    }

    private fun getJsonObject(): JSONObject {
        val json = file.readText()
        return JSONObject(json)
    }

    //############### Pose helper ###############//

    fun savePoses(poses: MutableList<Pose>, categories: MutableList<JSONObject>) {
        try {
            val jsonObject = getJsonObject() // Load existing data first
            val existingTrainings = loadTrainingsFromJson() // Preserve existing trainings

            // Update poses array
            val posesArray = JSONArray().apply {
                poses.forEach { pose ->
                    JSONObject().apply {
                        put("uuid", pose.uuid)
                        put("english_name", pose.name)
                        put("pose_description", pose.description)
                        put("pose_benefits", pose.benefits)
                        put("category_name", JSONArray(pose.categories))
                        put("local_svg_path", pose.localImagePath)
                    }.also { put(it) }
                }
            }
            jsonObject.put("poses", posesArray)

            // Update categories
            val categoriesArray = JSONArray(categories)
            jsonObject.put("categories", categoriesArray)

            // Preserve existing trainings
            val trainingsArray = jsonObject.optJSONArray("trainings") ?: JSONArray()
            jsonObject.put("trainings", trainingsArray)

            File(MainActivity.poseDataFilePath).writeText(jsonObject.toString(4))
            Log.i("Save", "Data saved successfully")
        } catch (e: Exception) {
            Log.e("Error", "Save failed: ${e.stackTraceToString()}")
        }
    }

    fun updatePoseInJson(updatedPose: Pose, oldUUID: String, context: Context) {
        try {
            // 1. Load COMPLETE existing data first
            val jsonObject = getJsonObject()
            val categoriesArray = jsonObject.getJSONArray("categories")
            val trainingsArray = jsonObject.optJSONArray("trainings") ?: JSONArray()

            // 2. Update pose in poses array
            val posesArray = jsonObject.getJSONArray("poses")
            for (i in 0 until posesArray.length()) {
                val poseObject = posesArray.getJSONObject(i)
                if (poseObject.getString("uuid") == oldUUID) {
                    poseObject.apply {
                        put("english_name", updatedPose.name)
                        put("pose_description", updatedPose.description)
                        put("pose_benefits", updatedPose.benefits)
                        put("category_name", JSONArray(updatedPose.categories))
                    }
                    break
                }
            }

            // 3. Preserve all data sections when saving
            jsonObject.apply {
                put("poses", posesArray)
            put("categories", categoriesArray)
                put("trainings", trainingsArray)
            }

            // 4. Save complete data
            context.openFileOutput(MainActivity.POSE_DATA_FILENAME, Context.MODE_PRIVATE).use {
                it.write(jsonObject.toString(4).toByteArray())
            }

            Toast.makeText(context, "Pose updated successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("JsonHelper", "Update error: ${e.stackTraceToString()}")
            Toast.makeText(context, "Update failed", Toast.LENGTH_SHORT).show()
        }
    }
    fun getPosesByUUIDs(poseUUIDS: List<String>): MutableList<Pose> {
        val allPoses = loadAllPoses()
        return poseUUIDS.mapNotNull { uuid ->
            allPoses.firstOrNull { it.uuid == uuid }
        }.toMutableList()
    }

    fun getPoseByUUID(uuid: String): Pose? {
        val allPoses = loadAllPoses()
        return allPoses.firstOrNull { it.uuid == uuid }
    }

    private fun loadAllPoses(): List<Pose> {
        return try {
            val jsonObject = getJsonObject()
            jsonObject.getJSONArray("poses").let { posesArray ->
                List(posesArray.length()) { i ->
                    val poseJson = posesArray.getJSONObject(i)
                    Pose(
                        uuid = poseJson.getString("uuid"),
                        name = poseJson.getString("english_name"),
                        description = poseJson.getString("pose_description"),
                        benefits = poseJson.getString("pose_benefits"),
                        categories = poseJson.getJSONArray("category_name").let {
                            List(it.length()) { idx -> it.getString(idx) }
                        },
                        localImagePath = poseJson.getString("local_svg_path")
                    )
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun deletePoseFromJson(pose: Pose, context: Context, poses: MutableList<Pose>, poseAdapter: PoseAdapter) {
        try {
            val jsonObject = getJsonObject()
            val jsonArray = jsonObject.getJSONArray("poses")

            // Find and remove the pose
            for (i in 0 until jsonArray.length()) {
                val poseObject = jsonArray.getJSONObject(i)
                if (poseObject.getString("english_name") == pose.name) {
                    jsonArray.remove(i)
                    break
                }
            }

            // Save the updated JSON back to internal storage
            file.writeText(jsonObject.toString(4))

            // Remove the pose from the local list and update the adapter
            (context as? PoseActivity)?.let { activity ->
                poses.remove(pose)
                poseAdapter.notifyDataSetChanged()
            }

            Toast.makeText(context, "Pose deleted successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to delete pose", Toast.LENGTH_SHORT).show()
        }
    }

    //############### Training helper ###############//

    fun loadTrainingsFromJson(context: Context? = null): MutableList<Training> {
        val trainings = mutableListOf<Training>()
        try {
            val jsonObject = getJsonObject()

            if (jsonObject.has("trainings")) {
                val jsonArray = jsonObject.getJSONArray("trainings")
                for (i in 0 until jsonArray.length()) {
                    val trainingObject = jsonArray.getJSONObject(i)
                    trainings.add(
                        Training(
                            name = trainingObject.getString("name"),
                            description = trainingObject.optString("description", ""),
                            poses_by_UUID = trainingObject.getJSONArray("poses").let { posesArray ->
                                List(posesArray.length()) { j -> posesArray.getString(j) }.toMutableList()
                            }
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("JsonHelper", "Error loading trainings: ${e.message}")
            context?.apply {
                Toast.makeText(this, "Failed to load trainings", Toast.LENGTH_SHORT).show()
            }
        }
        return trainings
    }


    fun saveTrainingsToJson(trainings: List<Training>, context: Context) {
        try {
            val jsonObject = getJsonObject()
            val trainingsArray = JSONArray()

            trainings.forEach { training ->
                JSONObject().apply {
                    put("name", training.name)
                    put("description", training.description)
                    put("poses", JSONArray(training.poses_by_UUID))
                    trainingsArray.put(this)
                }
            }

            jsonObject.put("trainings", trainingsArray)
            file.writeText(jsonObject.toString(4))
        } catch (e: Exception) {
            Log.e("JsonHelper", "Error saving trainings: ${e.message}")
        }
    }

    fun addPoseToTraining(training: Training, newPose: Pose, context: Context) {
        try {
            val jsonObject = getJsonObject()
            val trainingsArray = jsonObject.getJSONArray("trainings")

            for (i in 0 until trainingsArray.length()) {
                val trainingObject = trainingsArray.getJSONObject(i)
                if (trainingObject.getString("name") == training.name) {
                    // Get existing poses array
                    val posesArray = trainingObject.getJSONArray("poses")
                    posesArray.put(newPose.uuid)
                    trainingObject.put("poses", posesArray)
                    break
                }
            }

            // Save the modified JSON back to file
            file.writeText(jsonObject.toString(4))
        } catch (e: Exception) {
            Log.e("JsonHelper", "Error adding pose: ${e.message}")
            Toast.makeText(context, "Failed to add pose", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteTrainingFromJson(training: Training, context: Context) {
        try {
            val jsonObject = getJsonObject()
            val trainingsArray = jsonObject.getJSONArray("trainings")

            for (i in 0 until trainingsArray.length()) {
                val trainingObject = trainingsArray.getJSONObject(i)
                if (trainingObject.getString("name") == training.name) {
                    trainingsArray.remove(i)
                    break
                }
            }

            file.writeText(jsonObject.toString(4))
            Toast.makeText(context, "Training deleted successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("JsonHelper", "Error deleting training: ${e.message}")
            Toast.makeText(context, "Failed to delete training", Toast.LENGTH_SHORT).show()
        }
    }
}