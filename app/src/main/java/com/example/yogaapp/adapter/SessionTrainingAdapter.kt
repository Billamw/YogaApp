package com.example.yogaapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.yogaapp.R
import com.example.yogaapp.dataclasses.Training

class SessionTrainingAdapter(
    private val trainings: List<Training>,
    private val onOrderChanged: (List<Training>) -> Unit
) : RecyclerView.Adapter<SessionTrainingAdapter.ViewHolder>() {

    private val selectedTrainings = mutableListOf<Training>()
    private val selectionOrder = mutableListOf<Int>()

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkBox: CheckBox = view.findViewById(R.id.checkbox)
        val orderText: TextView = view.findViewById(R.id.order_text)
        val trainingName: TextView = view.findViewById(R.id.training_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.session_training_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val training = trainings[position]

        holder.checkBox.isChecked = selectedTrainings.contains(training)
        holder.trainingName.text = training.name

        val order = selectionOrder.indexOf(position) + 1
        holder.orderText.text = if (order > 0) order.toString() else ""

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedTrainings.add(training)
                selectionOrder.add(position)
            } else {
                selectedTrainings.remove(training)
                selectionOrder.remove(position)
            }
            updateOrderDisplay()
            onOrderChanged(getOrderedTrainings())
        }
    }

    private fun updateOrderDisplay() {
        notifyDataSetChanged()
    }

    fun getOrderedTrainings(): List<Training> {
        return selectionOrder.map { trainings[it] }
    }

    override fun getItemCount() = trainings.size
}