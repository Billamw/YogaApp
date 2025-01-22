package com.example.yogaapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yogaapp.dataclasses.Training
import com.example.yogaapp.objects.JsonHelper
import com.example.yogaapp.objects.TrainingDetailDialog

class TrainingsActivity : AppCompatActivity() {

    private lateinit var trainingsRecyclerView: RecyclerView
    private lateinit var trainingsAdapter: TrainingAdapter
    private val trainings = mutableListOf<Training>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trainings)

        trainingsRecyclerView = findViewById(R.id.trainings_recycler_view)

        loadTrainings()
        setupTrainingsList()
    }

    private fun loadTrainings() {
        trainings.clear()
        trainings.addAll(JsonHelper.loadTrainingsFromJson(this))
    }

    private fun setupTrainingsList() {
        trainingsAdapter = TrainingAdapter(trainings) { training ->
            showTrainingDetailDialog(training)
        }
        trainingsRecyclerView.adapter = trainingsAdapter
        trainingsRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun showTrainingDetailDialog(training: Training) {
        TrainingDetailDialog.showDialog(this, training) { updatedTraining ->
            // Update the training in the list and save
            val index = trainings.indexOfFirst { it.name == training.name }
            if (index != -1) {
                trainings[index] = updatedTraining
                JsonHelper.saveTrainingsToJson(trainings, this)
                trainingsAdapter.notifyItemChanged(index)
            }
        }
    }
}
