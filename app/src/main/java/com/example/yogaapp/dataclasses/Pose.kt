package com.example.yogaapp.dataclasses

data class Pose(
    var name: String,
    var description: String,
    var benefits: String,
    var categories: List<String>,
    var localImagePath: String? = null
)
