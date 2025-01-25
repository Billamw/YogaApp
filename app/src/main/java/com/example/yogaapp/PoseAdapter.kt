package com.example.yogaapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.yogaapp.dataclasses.Pose
import com.example.yogaapp.objects.AddTrainingDialog
import com.example.yogaapp.objects.PoseDetailDialog

/**
 * An adapter for displaying a list of yoga poses in a RecyclerView.
 *
 * This adapter handles the creation and binding of views for each pose item in the list.
 * It uses Glide to load images and provides functionality for displaying pose details,
 * editing pose information, and deleting poses.
 *
 * @param poses The list of poses to display.
 * @param context The application context.
 */
class PoseAdapter(val poses: MutableList<Pose>, private val context: Context) : RecyclerView.Adapter<PoseAdapter.PoseViewHolder>() {

    class PoseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val poseImage: ImageView = itemView.findViewById(R.id.pose_image)
        val poseName: TextView = itemView.findViewById(R.id.pose_name)
        val addButton: ImageButton = itemView.findViewById(R.id.add_to_training_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PoseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.pose_item, parent, false)
        return PoseViewHolder(view)
    }

    override fun onBindViewHolder(holder: PoseViewHolder, position: Int) {
        val pose = poses[position]

        holder.poseName.text = pose.name

        // Check if localImagePath is valid and not empty
        if (!pose.localImagePath.isNullOrBlank()) {
            Glide.with(holder.poseImage.context)
                .load(pose.localImagePath)
                .error(R.drawable.baseline_image_not_supported_24) // Fallback image if load fails
                .into(holder.poseImage)
        } else {
            // Set default image if no local path exists
            holder.poseImage.setImageResource(R.drawable.baseline_image_not_supported_24)
        }

        holder.itemView.setOnClickListener {
            PoseDetailDialog.showDetailDialog(pose, context, poses, this)
        }

        holder.addButton.setOnClickListener {
            AddTrainingDialog.showTrainingDialog(context, pose)
        }
    }

    override fun getItemCount(): Int {
        return poses.size
    }

}