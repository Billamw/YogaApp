package com.example.yogaapp

//import android.icu.util.TimeUnit
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.concurrent.TimeUnit
import java.io.BufferedReader
import java.io.IOException
import java.net.HttpURLConnection
import kotlinx.coroutines.*
import java.io.InputStreamReader
import java.net.URL
import org.json.JSONObject
import com.bumptech.glide.Glide

class TrainingActivity : AppCompatActivity() {

    private val yogaApiBaseUrl = "https://yoga-api-nzy4.onrender.com/v1/categories?name="
    private lateinit var selectedCategories: List<String>
    private lateinit var selectedDifficulty: String
    private var exerciseAmount: Int = 0
    private var exerciseTime: Float = 0f
    private val allExercises = mutableListOf<YogaPose>()
    private var currentPoseIndex = 0
    private lateinit var poseImageView: ImageView
    private lateinit var poseNameTextView: TextView
    private lateinit var timerTextView: TextView
    private lateinit var prepareTextView: TextView
    private lateinit var nextButton: Button
    private var timer: CountDownTimer? = null
    private var prepareTimer: CountDownTimer? = null
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_training)

        // Initialize UI elements
        poseImageView = findViewById(R.id.poseImageView)
        poseNameTextView = findViewById(R.id.poseNameTextView)
        timerTextView = findViewById(R.id.timerTextView)
        prepareTextView = findViewById(R.id.prepareTextView)
        nextButton = findViewById(R.id.nextButton)

        // Get data from intent
        selectedCategories = intent.getStringArrayListExtra("categories") ?: emptyList()
        selectedDifficulty = intent.getStringExtra("difficulty") ?: ""
        exerciseAmount = intent.getIntExtra("exerciseAmount", 0)
        exerciseTime = intent.getFloatExtra("exerciseTime", 0f)

        // Fetch data for each selected category
        fetchYogaData()

        // Set up click listener for the next button
        nextButton.setOnClickListener {
            showNextPose()
        }
    }

    private fun fetchYogaData() {
        // Use a coroutine scope to manage the asynchronous tasks
        CoroutineScope(Dispatchers.IO).launch {
            selectedCategories.forEach { category ->
                try {
                    var apiUrl = ""
                    if(selectedDifficulty == "") {
                        apiUrl = yogaApiBaseUrl + category
                    } else {
                        apiUrl = yogaApiBaseUrl + category + "&level=" + selectedDifficulty
                    }
                    val jsonResponse = fetchJsonData(apiUrl)
                    if (jsonResponse != null) {
                        // Process the JSON response
                        processYogaData(jsonResponse, category)
                    } else {
                        Log.e("API_ERROR", "Failed to fetch data for category: $category")
                    }
                } catch (e: Exception) {
                    Log.e("API_ERROR", "Error fetching data for category: $category", e)
                }
            }
            // After fetching all data, you can process allExercises here
            withContext(Dispatchers.Main) {
                // Start with the first pose
                showNextPose()
            }
        }
    }

    private fun fetchJsonData(apiUrl: String): String? {
        var connection: HttpURLConnection? = null
        var reader: BufferedReader? = null
        try {
            val url = URL(apiUrl)
            connection = url.openConnection() as HttpURLConnection
            connection.connect()

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = connection.inputStream
                reader = BufferedReader(InputStreamReader(inputStream))
                val stringBuilder = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    stringBuilder.append(line)
                }
                return stringBuilder.toString()
            } else {
                Log.e("API_ERROR", "HTTP Error: ${connection.responseCode} - ${connection.responseMessage}")
                return null
            }
        } catch (e: IOException) {
            Log.e("API_ERROR", "IO Error: ", e)
            return null
        } finally {
            reader?.close()
            connection?.disconnect()
        }
    }

    private fun processYogaData(jsonResponse: String, category: String) {
        try {
            val jsonObject = JSONObject(jsonResponse)
            val posesArray = jsonObject.getJSONArray("poses")
            val numberOfPoses = minOf(exerciseAmount, posesArray.length()) // Ensure we don't exceed available poses
            for (i in 0 until numberOfPoses) {
                val poseObject = posesArray.getJSONObject(i)
                val poseName = poseObject.getString("english_name")
                val poseDescription = poseObject.getString("pose_description")
                val poseImageUrl = poseObject.getString("url_png")
                val yogaPose = YogaPose(poseName, poseDescription, poseImageUrl)
                allExercises.add(yogaPose)
            }
        } catch (e: Exception) {
            Log.e("API_ERROR", "Error parsing JSON for category: $category", e)
        }
    }

    private fun showNextPose() {
        // Cancel any existing timers
        timer?.cancel()
        prepareTimer?.cancel()

        if (currentPoseIndex < allExercises.size) {
            val currentPose = allExercises[currentPoseIndex]

            // Load image using Glide
            Glide.with(this)
                .load(currentPose.urlPng)
                .into(poseImageView)

            // Set pose name
            poseNameTextView.text = currentPose.englishName

            // Start prepare timer
            startPrepareTimer()
        } else {
            // All poses done
            poseImageView.setImageResource(R.drawable.lotus) // Set the logo
            poseNameTextView.text = "Training Complete!"
            timerTextView.text = ""
            prepareTextView.text = ""
            nextButton.isEnabled = true
            playSound(R.raw.training_complete_sound)
            nextButton.setOnClickListener {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun startPrepareTimer() {
        prepareTextView.visibility = TextView.VISIBLE
        nextButton.isEnabled = false // Disable the next button
        var prepareTimeLeft = 10
        prepareTimer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                prepareTextView.text = "Prepare Time: $prepareTimeLeft"
                prepareTimeLeft--
            }

            override fun onFinish() {
                prepareTextView.visibility = TextView.GONE
                startExerciseTimer()
            }
        }.start()
    }

    private fun startExerciseTimer() {
        val timeLeft = exerciseTime.toLong()
        nextButton.isEnabled = false // Disable the next button
        timer = object : CountDownTimer(TimeUnit.SECONDS.toMillis(timeLeft), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(minutes)
                timerTextView.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                timerTextView.text = "00:00"
                playSound(R.raw.finish_sound)
                currentPoseIndex++
                nextButton.isEnabled = true // Enable the next button
                showNextPose()
            }
        }.start()
    }

    private fun playSound(soundResource: Int) {
        mediaPlayer = MediaPlayer.create(this, soundResource)
        mediaPlayer?.start()
        mediaPlayer?.setOnCompletionListener {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        prepareTimer?.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}

data class YogaPose(val englishName: String, val pose: String, val urlPng: String)