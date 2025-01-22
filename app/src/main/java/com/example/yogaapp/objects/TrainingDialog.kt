package com.example.yogaapp.objects

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yogaapp.R
import com.example.yogaapp.TrainingAdapter
import com.example.yogaapp.dataclasses.Pose
import com.example.yogaapp.dataclasses.Training

object TrainingDialog {

    fun showTrainingDialog(context: Context, pose: Pose) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.training_adding_dialog, null)
        val dialog = AlertDialog.Builder(context).create()
        dialog.setView(dialogView)

        val trainingsList = dialogView.findViewById<RecyclerView>(R.id.trainings_list)
        val addTrainingButton = dialogView.findViewById<Button>(R.id.add_new_training_button)

        val trainings = JsonHelper.loadTrainingsFromJson(context)
        val trainingAdapter = TrainingAdapter(trainings) { training ->
            addPoseToTraining(training, pose, context)
        }
        trainingsList.adapter = trainingAdapter
        trainingsList.layoutManager = LinearLayoutManager(context)

        addTrainingButton.setOnClickListener {
            NewTrainingDialog.showNewTrainingDialog(context) { newTraining ->
                trainings.add(newTraining)
                JsonHelper.saveTrainingsToJson(trainings, context)
                trainingAdapter.notifyDataSetChanged()
            }
        }

        dialog.show()
    }

    private fun addPoseToTraining(training: Training, pose: Pose, context: Context) {
        if (!training.poses.any { it.name == pose.name }) {
            training.poses.add(pose)
            JsonHelper.saveTrainingsToJson(JsonHelper.loadTrainingsFromJson(context), context)
            Toast.makeText(context, "Pose added to ${training.name}", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Pose already in training", Toast.LENGTH_SHORT).show()
        }
    }
}
