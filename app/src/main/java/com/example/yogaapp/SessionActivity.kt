package com.example.yogaapp

import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.yogaapp.dataclasses.Pose
import com.example.yogaapp.dataclasses.Training
import com.example.yogaapp.dialogs.JsonHelper

class SessionActivity : AppCompatActivity() {
    private lateinit var trainings: ArrayList<Training>
    private var duration: Int = 0
    private var currentTrainingIndex = 0
    private var currentPoseIndex = 0
    private var isPreparing = true
    private var timer: CountDownTimer? = null
    private var prepareTime = 10
    private var finished: Boolean = false

    private lateinit var trainingNameTextView: TextView
    private lateinit var poseImageView: ImageView
    private lateinit var poseNameTextView: TextView
    private lateinit var timerTextView: TextView
    private lateinit var prepareTextView: TextView
    private lateinit var nextButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session)

        trainings = intent.getParcelableArrayListExtra<Training>("trainings") ?: arrayListOf()
        duration = intent.getIntExtra("duration", 0)

        trainingNameTextView = findViewById(R.id.training_name)
        poseImageView = findViewById(R.id.poseImageView)
        poseNameTextView = findViewById(R.id.poseNameTextView)
        timerTextView = findViewById(R.id.timerTextView)
        prepareTextView = findViewById(R.id.prepareTextView)
        nextButton = findViewById(R.id.nextButton)


        nextButton.setOnClickListener {
            Log.d("SessionActivity", "current trainigsindex $currentTrainingIndex")
            if(finished) finish()
            else {
                updateUI()
                if (currentTrainingIndex < trainings.size) {
                    moveToNextPose()
                }
            }

        }

        startSession()
    }


    private fun moveToNextPose() {
        timer?.cancel()
        currentPoseIndex++

        Log.d("SessionActivity", "$currentTrainingIndex of ${trainings.size}")
        if (currentPoseIndex >= trainings[currentTrainingIndex].poses_by_UUID.size) {
            currentTrainingIndex++
            currentPoseIndex = 0
        }
        if (currentTrainingIndex < trainings.size) {
            startPrepareTimer()
        } else {
            finished = true
            finishTraining()
        }
    }


    private fun startSession() {
        updateUI()
        startPrepareTimer()
    }

    private fun updateUI() {
        Log.d("SessionActivity", "Updating UI with training index: $currentTrainingIndex of ${trainings.size}")
        if (currentTrainingIndex < trainings.size) {
            val currentTraining = trainings[currentTrainingIndex]
            trainingNameTextView.text = currentTraining.name

            if (currentPoseIndex < currentTraining.poses_by_UUID.size) {
                val currentPose: Pose? = JsonHelper.getPoseByUUID(currentTraining.poses_by_UUID[currentPoseIndex])
                if (currentPose != null) {
                    poseNameTextView.text = currentPose.name
                    loadPoseImage(currentPose.localImagePath)
                }

            }
        }
    }

    private fun loadPoseImage(imagePath: String?) {
        if (!imagePath.isNullOrBlank()) {
            Glide.with(this)
                .load(imagePath)
                .error(R.drawable.baseline_image_not_supported_24) // Fallback image if load fails
                .into(poseImageView)
        } else {
            // Set default image if no local path exists
            poseImageView.setImageResource(R.drawable.baseline_image_not_supported_24)
        }
    }

    private fun startPrepareTimer() {
        isPreparing = true
        prepareTextView.visibility = View.VISIBLE
        timerTextView.visibility = View.VISIBLE

        timer = object : CountDownTimer((prepareTime * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                prepareTextView.text = buildString {
                    append("Prepare Time: ")
                    append(secondsRemaining)
                }
            }

            override fun onFinish() {
                prepareTextView.visibility = View.GONE
                startPoseTimer()
            }
        }.start()
    }

    private fun startPoseTimer() {
        isPreparing = false
        timer = object : CountDownTimer((duration * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                timerTextView.text = String.format("%02d:%02d", secondsRemaining / 60, secondsRemaining % 60)
            }

            override fun onFinish() {
                playSound(R.raw.finish_sound)
            }
        }.start()
    }

    private fun finishTraining() {
        playSound(R.raw.training_complete_sound)
        poseImageView.setImageResource(R.drawable.lotus)
        poseNameTextView.text = "Training Finished"
        trainingNameTextView.text = ""
        timerTextView.visibility = View.INVISIBLE
        nextButton.text = "Done"
    }

    private fun playSound(soundResourceId: Int) {
        MediaPlayer.create(this, soundResourceId).apply {
            setOnCompletionListener { mp -> mp.release() }
            start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}