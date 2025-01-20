package com.example.yogaapp

data class Training(
    var name: String,
    var description: String,
    var imageUrl: String,
    var exercises: List<Pose>
)
