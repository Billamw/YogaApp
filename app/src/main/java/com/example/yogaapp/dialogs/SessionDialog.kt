package com.example.yogaapp.dialogs

import android.app.AlertDialog
import android.content.Context
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yogaapp.R
import com.example.yogaapp.adapter.SessionTrainingAdapter
import com.example.yogaapp.dataclasses.Training

class SessionDialog(
    private val context: Context,
    private val trainings: List<Training>,
    private val onSessionStart: (List<Training>, Int) -> Unit
) {
    private val dialog = AlertDialog.Builder(context)
        .setView(R.layout.session_dialog)
        .create()

    fun show() {
        dialog.show()

        val recyclerView = dialog.findViewById<RecyclerView>(R.id.trainings_list)!!
        val durationInput = dialog.findViewById<EditText>(R.id.duration_input)!!
        val startButton = dialog.findViewById<Button>(R.id.start_button)!!

        val adapter = SessionTrainingAdapter(trainings) { orderedTrainings ->
            // Order updated
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        startButton.setOnClickListener {
            val duration = durationInput.text.toString().toIntOrNull() ?: 60
            if (duration <= 0) {
                Toast.makeText(context, "Invalid duration", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selected = adapter.getOrderedTrainings()
            if (selected.isEmpty()) {
                Toast.makeText(context, "Select at least one training", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            onSessionStart(selected, duration)
            dialog.dismiss()
        }
    }
}