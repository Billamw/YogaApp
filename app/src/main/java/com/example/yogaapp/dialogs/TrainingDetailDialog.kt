package com.example.yogaapp.dialogs

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yogaapp.adapter.PoseInTrainingAdapter
import com.example.yogaapp.R
import com.example.yogaapp.dataclasses.Training

class TrainingDetailDialog(
    private val context: Context,
    private val training: Training,
    private val onSave: (Training) -> Unit
) {
    private lateinit var adapter: PoseInTrainingAdapter
    private lateinit var touchHelper: ItemTouchHelper
    private val dialog = AlertDialog.Builder(context)
        .setView(R.layout.training_detail_dialog)
        .create()

    fun show() {
        dialog.show()

        val nameEditText = dialog.findViewById<EditText>(R.id.training_name)!!
        val descEditText = dialog.findViewById<EditText>(R.id.training_description)!!
        val recyclerView = dialog.findViewById<RecyclerView>(R.id.poses_recycler_view)!!
        val doneButton = dialog.findViewById<Button>(R.id.done_button)!!
        val cancelButton = dialog.findViewById<Button>(R.id.cancel_button)!!

        // Initialize fields
        nameEditText.setText(training.name)
        descEditText.setText(training.description)

        // Setup drag & drop
        touchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val from = viewHolder.adapterPosition
                val to = target.adapterPosition
                adapter.moveItem(from, to)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
        })



        // Now initialize adapter
        adapter = PoseInTrainingAdapter(
            poses = JsonHelper.getPosesByUUIDs(training.poses_by_UUID).toMutableList(),
            onDragStart = { holder ->
                touchHelper.startDrag(holder)
            },
            onOrderChanged = { newOrder ->
                // Update both the adapter and training's UUID list
                training.poses_by_UUID.clear()
                training.poses_by_UUID.addAll(newOrder)
                Log.d("OrderDebug", "New UUID order: ${training.poses_by_UUID.joinToString()}")
            }
        )

        // Set up RecyclerView after adapter creation
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        touchHelper.attachToRecyclerView(recyclerView)

        // Set up button click listeners last
        doneButton.setOnClickListener {
            val updatedTraining = training.copy(
                name = nameEditText.text.toString(),
                description = descEditText.text.toString(),
                poses_by_UUID = adapter.poses.map { it.uuid }.toMutableList()
            )
            Log.i("FindTrainingError", "Done Button clicked")
            onSave(updatedTraining)
            dialog.dismiss()
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
    }
}