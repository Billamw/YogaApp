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
import java.net.URL

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

                    // Download SVG
                    val svgFileName = "${englishName.replace(" ", "_")}.svg"
                    val svgFile = File(context.filesDir, svgFileName)
                    downloadFile(svgUrl, svgFile)

                    // Update JSON object
                    poseObject.put("pose_name", englishName)
                    poseObject.put("local_svg_path", svgFile.absolutePath)
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

    private fun downloadFile(url: String, file: File) {
        try {
            val inputStream = URL(url).openStream()
            file.outputStream().use { output ->
                inputStream.copyTo(output)
            }
        } catch (e: Exception) {
            Log.e("downloadFile", "Error downloading file: ${e.message}")
        }
    }

    private fun getJsonObject(): JSONObject {
        val json = file.readText()
        return JSONObject(json)
    }

    fun updatePoseInJson(updatedPose: Pose, oldName: String, context: Context) {
        try {
            val jsonObject = getJsonObject()
            val jsonArray = jsonObject.getJSONArray("poses")

            // Find and update the pose by name
            for (i in 0 until jsonArray.length()) {
                val poseObject = jsonArray.getJSONObject(i)
                if (poseObject.getString("english_name") == oldName) {
                    Log.i("updatePoseInJson", "Updating pose: $oldName")
                    poseObject.put("english_name", updatedPose.name)
                    poseObject.put("pose_description", updatedPose.description)
                    poseObject.put("pose_benefits", updatedPose.benefits)
                    break
                }
            }

            // Save the updated JSON back to internal storage
            context.openFileOutput(MainActivity.POSE_DATA_FILENAME, Context.MODE_PRIVATE).use { output ->
                output.write(jsonObject.toString(4).toByteArray())
            }

            Toast.makeText(context, "JSON file updated!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to update JSON", Toast.LENGTH_SHORT).show()
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

    fun loadTrainingsFromJson(context: Context): MutableList<Training> {
        val trainings = mutableListOf<Training>()
        try {
            val jsonObject = getJsonObject()

            if (jsonObject.has("trainings")) {
                val jsonArray = jsonObject.getJSONArray("trainings")
                for (i in 0 until jsonArray.length()) {
                    val trainingObject = jsonArray.getJSONObject(i)
                    val name = trainingObject.getString("name")
                    val description = trainingObject.getString("description")
                    val posesArray = trainingObject.getJSONArray("poses")
                    val poses = mutableListOf<Pose>()
                    for (j in 0 until posesArray.length()) {
                        val poseObject = posesArray.getJSONObject(j)
                        val poseName = poseObject.getString("english_name")
                        val poseDescription = poseObject.getString("pose_description")
                        val poseBenefits = poseObject.getString("pose_benefits")
                        val poseCategories = poseObject.getJSONArray("category_name").let { categoryArray ->
                            List(categoryArray.length()) { categoryArray.getString(it) }
                        }
                        val localSvgPath = poseObject.getString("local_svg_path")
                        poses.add(Pose(poseName, poseDescription, poseBenefits, poseCategories, localSvgPath))
                    }
                    trainings.add(Training(name, description, poses))
                }
            }
        } catch (e: Exception) {
            Log.e("JsonHelper", "Error loading trainings: ${e.message}")
            Toast.makeText(context, "Failed to load trainings", Toast.LENGTH_SHORT).show()
        }
        return trainings
    }

    fun saveTrainingsToJson(trainings: List<Training>, context: Context) {
        try {
            val jsonObject = getJsonObject()
            val trainingsArray = JSONArray()

            trainings.forEach { training ->
                val trainingObject = JSONObject()
                trainingObject.put("name", training.name)
                trainingObject.put("description", training.description)
                trainingObject.put("poses", JSONArray(training.poses))
                trainingsArray.put(trainingObject)
            }

            jsonObject.put("trainings", trainingsArray)

            file.writeText(jsonObject.toString(4))
            Toast.makeText(context, "Trainings saved successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("JsonHelper", "Error saving trainings: ${e.message}")
            Toast.makeText(context, "Failed to save trainings", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateTrainingInJson(training: Training, context: Context) {
        try {
            val file = File(MainActivity.poseDataFilePath)
            val json = file.readText()
            val jsonObject = JSONObject(json)
            val trainingsArray = jsonObject.getJSONArray("trainings")

            for (i in 0 until trainingsArray.length()) {
                val trainingObject = trainingsArray.getJSONObject(i)
                if (trainingObject.getString("name") == training.name) {
                    trainingObject.put("description", training.description)
                    trainingObject.put("poses", JSONArray(training.poses))
                    break
                }
            }

            file.writeText(jsonObject.toString(4))
            Toast.makeText(context, "Training updated successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("JsonHelper", "Error updating training: ${e.message}")
            Toast.makeText(context, "Failed to update training", Toast.LENGTH_SHORT).show()
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