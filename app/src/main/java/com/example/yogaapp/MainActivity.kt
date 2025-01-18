package com.example.yogaapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.semantics.text
import androidx.recyclerview.widget.LinearLayoutManager

class MainActivity : AppCompatActivity() {

    private lateinit var exercisesButton: Button
    private lateinit var trainingsButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        exercisesButton = findViewById(R.id.exercises_button)

        exercisesButton.setOnClickListener {
            val intent = Intent(this, ExercisesActivity::class.java)
            startActivity(intent)
        }
    }
}