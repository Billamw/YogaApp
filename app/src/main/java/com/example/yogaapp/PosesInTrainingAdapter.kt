package com.example.yogaapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.yogaapp.dataclasses.Pose
import java.util.Collections

class PosesInTrainingAdapter(
    val poses: MutableList<Pose>,
    private val onPosesUpdated: (MutableList<Pose>) -> Unit
) : RecyclerView.Adapter<PosesInTrainingAdapter.PoseViewHolder>() {

    class PoseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val poseName: TextView = itemView.findViewById(R.id.pose_name)
        val moveUpButton: ImageButton = itemView.findViewById(R.id.move_up_button)
        val moveDownButton: ImageButton = itemView.findViewById(R.id.move_down_button)
        val deleteButton: ImageButton = itemView.findViewById(R.id.delete_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PoseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.pose_in_training_item, parent, false)
        return PoseViewHolder(view)
    }

    override fun onBindViewHolder(holder: PoseViewHolder, position: Int) {
        val pose = poses[position]
        holder.poseName.text = pose.name

        holder.moveUpButton.isEnabled = position > 0
        holder.moveDownButton.isEnabled = position < poses.size - 1

        holder.moveUpButton.setOnClickListener {
            if (position > 0) {
                Collections.swap(poses, position, position - 1)
                notifyItemMoved(position, position - 1)
                onPosesUpdated(poses)
            }
        }

        holder.moveDownButton.setOnClickListener {
            if (position < poses.size - 1) {
                Collections.swap(poses, position, position + 1)
                notifyItemMoved(position, position + 1)
                onPosesUpdated(poses)
            }
        }

        holder.deleteButton.setOnClickListener {
            poses.removeAt(position)
            notifyItemRemoved(position)
            onPosesUpdated(poses)
        }
    }

    override fun getItemCount() = poses.size
}
