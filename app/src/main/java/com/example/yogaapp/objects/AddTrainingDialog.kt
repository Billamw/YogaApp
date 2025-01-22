package com.example.yogaapp.objects


import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yogaapp.R
import com.example.yogaapp.TrainingAdapter
import com.example.yogaapp.dataclasses.Pose
import com.example.yogaapp.dataclasses.Training

object AddTrainingDialog {
    fun showTrainingDialog(pose: Pose, context: Context) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.training_adding_dialog, null)
        val dialog = AlertDialog.Builder(context).create()
        dialog.setView(dialogView)

        val trainingsList = dialogView.findViewById<RecyclerView>(R.id.trainings_list)
        val addNewTrainingButton = dialogView.findViewById<Button>(R.id.add_new_training_button)

        val trainings = JsonHelper.loadTrainingsFromJson(context)
        val trainingAdapter = TrainingAdapter(
            trainings,
            onTrainingClick = { /* Implement if needed */ },
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

    private fun showNewTrainingDialog(context: Context, onTrainingAdded: (Training) -> Unit) {
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
