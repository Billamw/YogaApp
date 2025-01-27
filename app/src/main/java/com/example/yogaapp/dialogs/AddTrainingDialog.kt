package com.example.yogaapp.dialogs

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yogaapp.R
import com.example.yogaapp.adapter.TrainingAdapter
import com.example.yogaapp.dataclasses.Pose
import com.example.yogaapp.dataclasses.Training

object AddTrainingDialog {

    fun showTrainingDialog(context: Context, pose: Pose?) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.training_adding_dialog, null)
        val dialog = AlertDialog.Builder(context).create()
        dialog.setView(dialogView)

        val trainingsList = dialogView.findViewById<RecyclerView>(R.id.trainings_list)
        val addNewTrainingButton = dialogView.findViewById<Button>(R.id.add_new_training_button)

        val trainings = JsonHelper.loadTrainingsFromJson(context)
        val trainingAdapter = TrainingAdapter(
            trainings,
            onItemClick = { selectedTraining ->
                pose?.let { nonNullPose ->
                    // Add pose to training if it's not null
                    selectedTraining.poses_by_UUID.add(nonNullPose.uuid)
                    JsonHelper.addPoseToTraining(selectedTraining, nonNullPose, context)

                    // Optional: Show visual feedback
                    Toast.makeText(context, "Pose added to ${selectedTraining.name}", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
        )

        trainingsList.adapter = trainingAdapter
        trainingsList.layoutManager = LinearLayoutManager(context)

        addNewTrainingButton.setOnClickListener {
            showNewTrainingDialog(context) { newTraining ->
                trainings.add(newTraining)
                JsonHelper.saveTrainingsToJson(trainings, context)
                trainingAdapter.notifyDataSetChanged()
            }
        }

        dialog.show()
    }

    fun showNewTrainingDialog(context: Context, onTrainingAdded: (Training) -> Unit) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.training_new_training_dialog, null)
        val dialog = AlertDialog.Builder(context).create()
        dialog.setView(dialogView)

        val nameEditText = dialogView.findViewById<EditText>(R.id.training_name)
        val descriptionEditText = dialogView.findViewById<EditText>(R.id.training_description)
        val addButton = dialogView.findViewById<Button>(R.id.add_training_button)

        addButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val description = descriptionEditText.text.toString()
            if (name.isNotBlank() && description.isNotBlank()) {
                val newTraining = Training(name, description, mutableListOf())
                onTrainingAdded(newTraining)
                dialog.dismiss()
            } else {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }
}
