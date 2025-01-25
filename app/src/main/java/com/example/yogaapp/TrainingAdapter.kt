package com.example.yogaapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.yogaapp.dataclasses.Training


class TrainingAdapter(
    private var trainings: List<Training>,
    private val onItemClick: (Training) -> Unit
) : RecyclerView.Adapter<TrainingAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.training_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.training_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val training = trainings[position]
        holder.textView.text = training.name
        holder.itemView.setOnClickListener { onItemClick(training) }
    }

    override fun getItemCount() = trainings.size

    fun updateData(newTrainings: List<Training>) {
        trainings = newTrainings
        notifyDataSetChanged()
    }
}