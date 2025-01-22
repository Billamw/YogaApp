package com.example.yogaapp.objects

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yogaapp.PoseAdapter
import com.example.yogaapp.PosesInTrainingAdapter
import com.example.yogaapp.R
import com.example.yogaapp.dataclasses.Training

object TrainingDetailDialog {
    fun showDialog(context: Context, training: Training, onUpdateTraining: (Training) -> Unit) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.training_detail_dialog, null)
        val dialog = AlertDialog.Builder(context).create()
        dialog.setView(dialogView)

        val nameEditText = dialogView.findViewById<EditText>(R.id.training_name)
        val descriptionEditText = dialogView.findViewById<EditText>(R.id.training_description)
        val posesRecyclerView = dialogView.findViewById<RecyclerView>(R.id.poses_recycler_view)
        val updateButton = dialogView.findViewById<Button>(R.id.update_button)
        val deleteButton = dialogView.findViewById<Button>(R.id.delete_button)

        // Populate existing data
        nameEditText.setText(training.name)
        descriptionEditText.setText(training.description)

        // Setup poses RecyclerView
        val posesAdapter = PosesInTrainingAdapter(training.poses.toMutableList()) { updatedPoses ->
            training.poses = updatedPoses
        }
        posesRecyclerView.adapter = posesAdapter
        posesRecyclerView.layoutManager = LinearLayoutManager(context)

        updateButton.setOnClickListener {
            val updatedTraining = training.copy(
                name = nameEditText.text.toString(),
                description = descriptionEditText.text.toString(),
                poses = posesAdapter.poses
            )
            onUpdateTraining(updatedTraining)
            dialog.dismiss()
        }

        deleteButton.setOnClickListener {
            // Implement delete logic
            val trainings = JsonHelper.loadTrainingsFromJson(context)
            trainings.removeAll { it.name == training.name }
            JsonHelper.saveTrainingsToJson(trainings, context)
            dialog.dismiss()
        }

        dialog.show()
    }
}

