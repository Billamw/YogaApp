package com.example.yogaapp

data class Pose(
    val name: String,
    val description: String,
    val benefits: String,
    val groups: List<String>,
    val imageUrl: String
)
