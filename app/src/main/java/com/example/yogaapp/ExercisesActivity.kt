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
        poseAdapter = PoseAdapter(poses)
        recyclerView.adapter = poseAdapter

//        val assetManager: AssetManager = assets
//        val inputStream: InputStream = assetManager.open("poses.json")
//        val jsonString = inputStream.bufferedReader().use { it.readText() }
//        val gson = Gson()
//        val typeToken = object : TypeToken<List<Pose>>() {}.type
//        poses = gson.fromJson(jsonString, typeToken)

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

            val inputStream = assets.open("poses_data.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val json = String(buffer)
            val jsonObject = JSONObject(json)
            val jsonArray = jsonObject.getJSONArray("poses")
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
            Log.i("Load", "added all poses")
            poseAdapter.notifyDataSetChanged()
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("Error", "Load failed")
        }
        finally{
            Log.i("Load", "Load successful")
        }
    }
}