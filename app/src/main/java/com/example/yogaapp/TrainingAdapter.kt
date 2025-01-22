package com.example.yogaapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.yogaapp.dataclasses.Training


class TrainingAdapter(
    private val trainings: List<Training>,
    private val onTrainingClick: (Training) -> Unit
) : RecyclerView.Adapter<TrainingAdapter.TrainingViewHolder>() {

    class TrainingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val trainingName: TextView = itemView.findViewById(R.id.training_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrainingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.training_item, parent, false)
        return TrainingViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrainingViewHolder, position: Int) {
        val training = trainings[position]
        holder.trainingName.text = training.name
        holder.itemView.setOnClickListener { onTrainingClick(training) }
    }

    override fun getItemCount() = trainings.size
}
