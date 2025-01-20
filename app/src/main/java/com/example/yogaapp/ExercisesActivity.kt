package com.example.yogaapp

import android.content.res.AssetManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.input.key.type
import com.google.gson.Gson
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.io.InputStream

class ExercisesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var poseAdapter: PoseAdapter
    private var poses = mutableListOf<Pose>()
    private lateinit var addPoseButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercises)

        recyclerView = findViewById(R.id.exercises_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        poseAdapter = PoseAdapter(poses, this)
        recyclerView.adapter = poseAdapter

        addPoseButton = findViewById(R.id.add_pose_button)
        addPoseButton.setOnClickListener {
            // Hier kannst du die Logik zum Hinzufügen einer neuen Pose implementieren
            // Zum Beispiel:
            // val newPose = Pose("Neue Pose", "Beschreibung", "neue_pose.jpg")
            // poses.add(newPose)
            // poseAdapter.notifyDataSetChanged()
            // Oder du startest eine neue Activity, um eine neue Pose hinzuzufügen
        }

        loadPoses()
    }

    private fun loadPoses() {
        try {
            val file = File(MainActivity.poseDataFilePath)
            val json = file.readText()

            val jsonObject = JSONObject(json)
            val jsonArray = jsonObject.getJSONArray("poses")

            poses.clear() // Clear existing poses before loading

            for (i in 0 until jsonArray.length()) {
                val poseObject = jsonArray.getJSONObject(i)
                val name = poseObject.getString("english_name")
                val description = poseObject.getString("pose_description")
                val benefits = poseObject.getString("pose_benefits")
                val groups = poseObject.getJSONArray("category_name").let { groupsArray ->
                    List(groupsArray.length()) { groupsArray.getString(it) }
                }
                val imageUrl = poseObject.getString("url_png")
                poses.add(Pose(name, description, benefits, groups, imageUrl))
            }

            Log.i("Load", "Added all poses")
            poseAdapter.notifyDataSetChanged()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("Error", "Load failed: ${e.message}")
        } finally {
            Log.i("Load", "Load process completed")
        }
    }

}