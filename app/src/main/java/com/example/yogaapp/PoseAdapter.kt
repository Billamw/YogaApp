package com.example.yogaapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.ui.semantics.text
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PoseAdapter(private val poses: List<Pose>) : RecyclerView.Adapter<PoseAdapter.PoseViewHolder>() {

    class PoseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val poseImage: ImageView = itemView.findViewById(R.id.pose_image)
        val poseName: TextView = itemView.findViewById(R.id.pose_name)
        val poseGroup: TextView = itemView.findViewById(R.id.pose_groups)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PoseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.pose, parent, false)
        return PoseViewHolder(view)
    }

    override fun onBindViewHolder(holder: PoseViewHolder, position: Int) {
        val pose = poses[position]

        holder.poseName.text = pose.name
        holder.poseGroup.text = pose.description
        Glide.with(holder.poseImage.context)
            .load(pose.imageUrl)
            .into(holder.poseImage)
    }

    override fun getItemCount(): Int {
        return poses.size
    }
}