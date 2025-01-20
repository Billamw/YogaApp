package com.example.yogaapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var exercisesButton: Button
    private lateinit var trainingsButton: Button
    private lateinit var oldCallButton: Button

    companion object {
        const val POSE_DATA_FILENAME = "poses_data.json"
        lateinit var poseDataFilePath: String
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Copy pose_data.json to local storage
        copyPoseDataToLocalStorageOnce()

        exercisesButton = findViewById(R.id.exercises_button)
        trainingsButton = findViewById(R.id.trainings_button)
        oldCallButton = findViewById(R.id.old_call_button)

        exercisesButton.setOnClickListener {
            val intent = Intent(this, ExercisesActivity::class.java)
            startActivity(intent)
        }

        oldCallButton.setOnClickListener {
            val intent = Intent(this, OldApiCallActivity::class.java)
            startActivity(intent)
        }
    }

    private fun copyPoseDataToLocalStorageOnce() {
        val sharedPrefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val isDataCopied = sharedPrefs.getBoolean("isPoseDataCopied", false)
        if (!isDataCopied) {
            try {
                val inputStream = assets.open(POSE_DATA_FILENAME)
                val outputFile = File(filesDir, POSE_DATA_FILENAME)

                FileOutputStream(outputFile).use { output ->
                    inputStream.copyTo(output)
                }

                poseDataFilePath = outputFile.absolutePath
                Log.i("copyPoseDataToLocalStorageOnce", "poseDataFilePath: $poseDataFilePath")

                // Set the flag to true after successful copy
                sharedPrefs.edit().putBoolean("isPoseDataCopied", true).apply()
            } catch (e: IOException) {
                e.printStackTrace()
                // Handle the error
                Log.e("copyPoseDataToLocalStorageOnce", "Error copying pose data: ${e.message}")
            }
        } else {
            // File has already been copied, just set the path
            poseDataFilePath = File(filesDir, POSE_DATA_FILENAME).absolutePath
        }
    }
}
