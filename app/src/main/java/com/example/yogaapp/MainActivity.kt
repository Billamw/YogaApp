package com.example.yogaapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.yogaapp.objects.JsonHelper
import com.example.yogaapp.oldcode.OldApiCallActivity
import java.io.File

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
        poseDataFilePath = File(filesDir, POSE_DATA_FILENAME).absolutePath
        JsonHelper.copyPoseDataToLocalStorageOnce(this, assets)

        exercisesButton = findViewById(R.id.exercises_button)
        trainingsButton = findViewById(R.id.trainings_button)
        oldCallButton = findViewById(R.id.old_call_button)

        exercisesButton.setOnClickListener {
            val intent = Intent(this, PoseActivity::class.java)
            startActivity(intent)
        }

        trainingsButton.setOnClickListener {
            val intent = Intent(this, TrainingsActivity::class.java)
            startActivity(intent)
        }

        oldCallButton.setOnClickListener {
            val intent = Intent(this, OldApiCallActivity::class.java)
            startActivity(intent)
        }
    }

}
