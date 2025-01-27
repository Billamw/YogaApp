package com.example.yogaapp.dialogs

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.yogaapp.R
import com.example.yogaapp.dataclasses.Training

object NewTrainingDialog {

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
