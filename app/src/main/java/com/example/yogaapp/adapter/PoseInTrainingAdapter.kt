package com.example.yogaapp.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.yogaapp.R
import com.example.yogaapp.dataclasses.Pose
import java.util.Collections

class PoseInTrainingAdapter(
    var poses: MutableList<Pose>,
    private val onDragStart: (RecyclerView.ViewHolder) -> Unit,
    private val onOrderChanged: (List<String>) -> Unit
) : RecyclerView.Adapter<PoseInTrainingAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val poseImage: ImageView = view.findViewById(R.id.pose_image)
        val poseName: TextView = view.findViewById(R.id.pose_name)
        val dragHandle: ImageButton = view.findViewById(R.id.drag_button)
        val removeButton: ImageButton = view.findViewById(R.id.remove_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.pose_in_training_item, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pose = poses[position]
        holder.poseName.text = pose.name

        holder.dragHandle.setOnTouchListener { _, event ->
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                onDragStart(holder)
            }
            false
        }

        if (!pose.localImagePath.isNullOrBlank()) {
            Glide.with(holder.poseImage.context)
                .load(pose.localImagePath)
                .error(R.drawable.baseline_image_not_supported_24) // Fallback image if load fails
                .into(holder.poseImage)
        } else {
            // Set default image if no local path exists
            holder.poseImage.setImageResource(R.drawable.baseline_image_not_supported_24)
        }

        holder.removeButton.setOnClickListener {
            removeItem(position)
        }
    }


    override fun getItemCount() = poses.size

    fun updateData(newPoses: List<Pose>) {
        poses = newPoses.toMutableList()
        notifyDataSetChanged()
    }

    fun moveItem(from: Int, to: Int) {
        Collections.swap(poses, from, to)
        notifyItemMoved(from, to)

        // Convert to UUID list and notify
        val newOrder = poses.map { it.uuid }
        onOrderChanged(newOrder)
    }

    fun removeItem(position: Int) {
        poses.removeAt(position)
        notifyItemRemoved(position)

        // Update UUID list after removal
        val newOrder = poses.map { it.uuid }
        onOrderChanged(newOrder)
    }
}