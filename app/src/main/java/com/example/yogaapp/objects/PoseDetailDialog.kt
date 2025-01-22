package com.example.yogaapp.objects

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.yogaapp.MainActivity
import com.example.yogaapp.PoseAdapter
import com.example.yogaapp.R
import com.example.yogaapp.dataclasses.Pose
import org.json.JSONObject
import java.io.File

object PoseDetailDialog{

    fun showDetailDialog(pose: Pose, context: Context, poses: MutableList<Pose>, poseAdapter: PoseAdapter) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.pose_detail_popup, null)
        val dialog = android.app.AlertDialog.Builder(context).create()
        dialog.setView(dialogView)

        val poseImage = dialogView.findViewById<ImageView>(R.id.detail_pose_image)
        val poseName = dialogView.findViewById<EditText>(R.id.detail_pose_name)
        val editButton = dialogView.findViewById<ImageButton>(R.id.edit_button)
        val deleteButton = dialogView.findViewById<ImageButton>(R.id.delete_button)
        val poseDescription = dialogView.findViewById<EditText>(R.id.detail_pose_description)
        val poseBenefits = dialogView.findViewById<EditText>(R.id.detail_pose_benefits)

        // Load the pose data into the popup
        Glide.with(context)
            .load(pose.localImagePath)
            .error(R.drawable.baseline_image_not_supported_24) // Fallback image if load fails
            .into(poseImage)
        poseName.setText(pose.name)
        poseDescription.setText(pose.description)
        poseBenefits.setText(pose.benefits)

        poseName.isEnabled = false
        poseDescription.isEnabled = false
        poseBenefits.isEnabled = false

        var isEditing = false // Track editing state

        editButton.setOnClickListener {
            if (isEditing) {
                // Save the changes and update the JSON file
                val oldName = pose.name
                pose.name = poseName.text.toString()
                pose.description = poseDescription.text.toString()
                pose.benefits = poseBenefits.text.toString()

                JsonHelper.updatePoseInJson(pose, oldName, context) // Save updated pose to poses.json

                // Disable editing and revert icon
                poseName.isEnabled = false
                poseDescription.isEnabled = false
                poseBenefits.isEnabled = false
                editButton.setImageResource(R.drawable.baseline_edit_24)

                isEditing = false
            } else {
                // Enable editing and change icon to "done"
                poseName.isEnabled = true
                poseDescription.isEnabled = true
                poseBenefits.isEnabled = true
                editButton.setImageResource(R.drawable.baseline_done_24)
                isEditing = true
            }
        }

        deleteButton.setOnClickListener {
            // Show confirmation dialog
            android.app.AlertDialog.Builder(context)
                .setTitle("Delete Pose")
                .setMessage("Are you sure you want to delete this pose?")
                .setPositiveButton("Yes") { _, _ ->
                    JsonHelper.deletePoseFromJson(pose, context, poses, poseAdapter)
                    dialog.dismiss()
                }
                .setNegativeButton("No", null)
                .show()
        }

        dialog.show()


        dialog.show()
    }
}