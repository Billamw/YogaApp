package com.example.yogaapp

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yogaapp.adapter.TrainingAdapter
import com.example.yogaapp.dataclasses.Training
import com.example.yogaapp.dialogs.AddTrainingDialog
import com.example.yogaapp.dialogs.JsonHelper
import com.example.yogaapp.dialogs.TrainingDetailDialog

class TrainingsActivity : AppCompatActivity() {

    private lateinit var trainingsRecyclerView: RecyclerView
    private lateinit var addTrainingButton: Button
    private lateinit var trainingsAdapter: TrainingAdapter
    private val trainings = mutableListOf<Training>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trainings)

        trainingsRecyclerView = findViewById(R.id.trainings_recycler_view)
        addTrainingButton = findViewById(R.id.add_training_button)

        loadTrainings()
        setupTrainingsList()

        addTrainingButton.setOnClickListener {
            AddTrainingDialog.showNewTrainingDialog(this) { newTraining ->
                trainings.add(newTraining)
                JsonHelper.saveTrainingsToJson(trainings, this)
                trainingsAdapter.updateData(trainings)
            }
        }
    }

    private fun loadTrainings() {
        trainings.clear()
        trainings.addAll(JsonHelper.loadTrainingsFromJson(this))
    }

    private fun setupTrainingsList() {
        trainingsAdapter = TrainingAdapter(trainings) { training ->
            TrainingDetailDialog(this, training) { updatedTraining ->
                val index = trainings.indexOfFirst { it.name == training.name }
                if (index != -1) {
                    trainings[index] = updatedTraining
                    JsonHelper.saveTrainingsToJson(trainings, this)
                    trainingsAdapter.updateData(trainings)
                }
            }.show()
        }
        trainingsRecyclerView.adapter = trainingsAdapter
        trainingsRecyclerView.layoutManager = LinearLayoutManager(this)
    }
}