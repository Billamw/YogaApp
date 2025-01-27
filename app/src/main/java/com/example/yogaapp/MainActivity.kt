package com.example.yogaapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.yogaapp.dialogs.JsonHelper
import com.example.yogaapp.dialogs.SessionDialog
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var exercisesButton: Button
    private lateinit var trainingsButton: Button
    private lateinit var oldCallButton: Button
    private lateinit var startSessionButton: Button

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
        startSessionButton = findViewById(R.id.start_session_button)

        exercisesButton.setOnClickListener {
            val intent = Intent(this, PoseActivity::class.java)
            startActivity(intent)
        }

        trainingsButton.setOnClickListener {
            val intent = Intent(this, TrainingsActivity::class.java)
            startActivity(intent)
        }

        startSessionButton.setOnClickListener {
            val trainings = JsonHelper.loadTrainingsFromJson(this)
            SessionDialog(this, trainings) { selectedTrainings, duration ->
                // Start the session with these trainings and duration
                val intent = Intent(this, SessionActivity::class.java).apply {
                    putParcelableArrayListExtra("trainings", ArrayList(selectedTrainings))
                    putExtra("duration", duration)
                }
                startActivity(intent)
            }.show()
        }
    }

}
