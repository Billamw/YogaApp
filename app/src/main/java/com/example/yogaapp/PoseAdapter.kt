package com.example.yogaapp

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.json.JSONObject
import java.io.File

class PoseAdapter(private val poses: List<Pose>, private val context: Context) : RecyclerView.Adapter<PoseAdapter.PoseViewHolder>() {

    class PoseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val poseImage: ImageView = itemView.findViewById(R.id.pose_image)
        val poseName: TextView = itemView.findViewById(R.id.pose_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PoseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.pose, parent, false)
        return PoseViewHolder(view)
    }

    override fun onBindViewHolder(holder: PoseViewHolder, position: Int) {
        val pose = poses[position]

        holder.poseName.text = pose.name
        Glide.with(holder.poseImage.context)
            .load(pose.imageUrl)
            .into(holder.poseImage)
        holder.itemView.setOnClickListener {
            showDetailDialog(pose)
        }
    }

    override fun getItemCount(): Int {
        return poses.size
    }

    private fun showDetailDialog(pose: Pose) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.pose_detail_popup, null)
        val dialog = android.app.AlertDialog.Builder(context).create()
        dialog.setView(dialogView)

        val poseImage = dialogView.findViewById<ImageView>(R.id.detail_pose_image)
        val poseName = dialogView.findViewById<EditText>(R.id.detail_pose_name)
        val editButton = dialogView.findViewById<ImageButton>(R.id.edit_button)
        val poseDescription = dialogView.findViewById<EditText>(R.id.detail_pose_description)
        val poseBenefits = dialogView.findViewById<EditText>(R.id.detail_pose_benefits)

        // Load the pose data into the popup
        Glide.with(context).load(pose.imageUrl).into(poseImage)
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

                updatePoseInJson(pose, oldName) // Save updated pose to poses.json

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

        dialog.show()
    }


    private fun updatePoseInJson(updatedPose: Pose, oldName: String) {
        try {
            // Use the file path from MainActivity
            val file = File(MainActivity.poseDataFilePath)
            val json = file.readText()

            val jsonObject = JSONObject(json)
            val jsonArray = jsonObject.getJSONArray("poses")

            // Find and update the pose by name
            for (i in 0 until jsonArray.length()) {
                val poseObject = jsonArray.getJSONObject(i)
                if (poseObject.getString("english_name") == oldName) {
                    Log.i("updatePoseInJson", "Updating pose: $oldName")
                    poseObject.put("english_name", updatedPose.name)
                    poseObject.put("pose_description", updatedPose.description)
                    poseObject.put("pose_benefits", updatedPose.benefits)
                    break
                }
            }

            // Save the updated JSON back to internal storage
            context.openFileOutput(MainActivity.POSE_DATA_FILENAME, Context.MODE_PRIVATE).use { output ->
                output.write(jsonObject.toString().toByteArray())
            }

            Toast.makeText(context, "JSON file updated!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to update JSON", Toast.LENGTH_SHORT).show()
        }
    }
}