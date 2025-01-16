package com.example.yogaapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.semantics.text

class MainActivity : AppCompatActivity() {

    // UI Elements
    private lateinit var seatedCheckbox: CheckBox
    private lateinit var strengtheningCheckbox: CheckBox
    private lateinit var chestCheckbox: CheckBox
    private lateinit var armCheckbox: CheckBox
    private lateinit var standingCheckbox: CheckBox
    private lateinit var hipCheckbox: CheckBox
    private lateinit var backbendCheckbox: CheckBox
    private lateinit var forwardCheckbox: CheckBox
    private lateinit var inversionCheckbox: CheckBox
    private lateinit var restorativeCheckbox: CheckBox
    private lateinit var balancingCheckbox: CheckBox
    private lateinit var beginnerCheckbox: CheckBox
    private lateinit var intermediateCheckbox: CheckBox
    private lateinit var minusButton: Button
    private lateinit var plusButton: Button
    private lateinit var exerciseAmountInput: EditText
    private lateinit var exerciseTimeInput: EditText
    private lateinit var startButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI elements
        initUI()

        // Set up button click listeners
        setupClickListeners()
    }

    private fun initUI() {
        // Initialize all UI elements by their IDs
        seatedCheckbox = findViewById(R.id.seated_checkbox)
        strengtheningCheckbox = findViewById(R.id.strengthening_checkbox)
        chestCheckbox = findViewById(R.id.chest_checkbox)
        armCheckbox = findViewById(R.id.arm_checkbox)
        standingCheckbox = findViewById(R.id.standing_checkbox)
        hipCheckbox = findViewById(R.id.hip_checkbox)
        backbendCheckbox = findViewById(R.id.backbend_checkbox)
        forwardCheckbox = findViewById(R.id.forward_checkbox)
        inversionCheckbox = findViewById(R.id.inversion_checkbox)
        restorativeCheckbox = findViewById(R.id.restorative_checkbox)
        balancingCheckbox = findViewById(R.id.balancing_checkbox)
        beginnerCheckbox = findViewById(R.id.beginner_checkbox)
        intermediateCheckbox = findViewById(R.id.intermediate_checkbox)
        minusButton = findViewById(R.id.minus_button)
        plusButton = findViewById(R.id.plus_button)
        exerciseAmountInput = findViewById(R.id.exercise_amount_input)
        exerciseTimeInput = findViewById(R.id.exercise_time_input)
        startButton = findViewById(R.id.start_button)
    }
    private fun setupClickListeners() {
        // Set click listener for the minus button
        minusButton.setOnClickListener {
            decrementExerciseAmount()
        }

        // Set click listener for the plus button
        plusButton.setOnClickListener {
            incrementExerciseAmount()
        }

        // Set click listener for the start button
        startButton.setOnClickListener {
            startTraining()
        }
    }

    private fun decrementExerciseAmount() {
        // Decrement the exercise amount if it's greater than 1
        val currentAmount = exerciseAmountInput.text.toString().toIntOrNull() ?: 1
        if (currentAmount > 1) {
            exerciseAmountInput.setText((currentAmount - 1).toString())
        }
    }

    private fun incrementExerciseAmount() {
        // Increment the exercise amount
        val currentAmount = exerciseAmountInput.text.toString().toIntOrNull() ?: 1
        exerciseAmountInput.setText((currentAmount + 1).toString())
    }

    private fun startTraining() {
        // Get selected categories
        val selectedCategories = getSelectedCategories()

        // Get selected difficulty
        val selectedDifficulty = getSelectedDifficulty()

        // Get exercise amount and time
        val exerciseAmount = exerciseAmountInput.text.toString().toIntOrNull() ?: 0
        val exerciseTime = exerciseTimeInput.text.toString().toFloatOrNull() ?: 0f

        // Validate inputs
        if (selectedCategories.isEmpty()) {
            showToast("Please select at least one category.")
            return
        }
        if (exerciseAmount <= 0) {
            showToast("Please enter a valid exercise amount.")
            return
        }
        if (exerciseTime <= 0) {
            showToast("Please enter a valid exercise time.")
            return
        }

        // Start the training activity
        val intent = Intent(this, TrainingActivity::class.java)
        intent.putStringArrayListExtra("categories", ArrayList(selectedCategories))
        intent.putExtra("difficulty", selectedDifficulty)
        intent.putExtra("exerciseAmount", exerciseAmount)
        intent.putExtra("exerciseTime", exerciseTime)
        startActivity(intent)
    }

    private fun getSelectedCategories(): List<String> {
        // Get the selected categories from the checkboxes
        val selectedCategories = mutableListOf<String>()
        if (seatedCheckbox.isChecked) selectedCategories.add("Seated Yoga")
        if (strengtheningCheckbox.isChecked) selectedCategories.add("Strengthening Yoga")
        if (chestCheckbox.isChecked) selectedCategories.add("Chest Opening Yoga")
        if (armCheckbox.isChecked) selectedCategories.add("Arm Yoga")
        if (standingCheckbox.isChecked) selectedCategories.add("Standing Yoga")
        if (hipCheckbox.isChecked) selectedCategories.add("Hip Opening Yoga")
        if (backbendCheckbox.isChecked) selectedCategories.add("Backbend Yoga")
        if (forwardCheckbox.isChecked) selectedCategories.add("Forward Yoga")
        if (inversionCheckbox.isChecked) selectedCategories.add("Inversion Yoga")
        if (restorativeCheckbox.isChecked) selectedCategories.add("Restorative Yoga")
        if (balancingCheckbox.isChecked) selectedCategories.add("Balancing Yoga")
        return selectedCategories
    }

    private fun getSelectedDifficulty(): String {
        // Get the selected difficulty from the checkboxes
        return when {
            beginnerCheckbox.isChecked -> "Beginner"
            intermediateCheckbox.isChecked -> "Intermediate"
            else -> ""
        }
    }

    private fun showToast(message: String) {
        // Show a toast message
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}