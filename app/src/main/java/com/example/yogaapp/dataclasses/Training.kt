package com.example.yogaapp.dataclasses

data class Training(
    var name: String,
    var description: String,
    var poses: MutableList<Pose>
)
