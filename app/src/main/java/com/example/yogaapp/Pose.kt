package com.example.yogaapp

data class Pose(
    var name: String,
    var description: String,
    var benefits: String,
    var groups: List<String>,
    var imageUrl: String
)
