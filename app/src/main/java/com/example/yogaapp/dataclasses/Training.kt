package com.example.yogaapp.dataclasses

data class Training(
    var name: String,
    var description: String,
    var poses_by_UUID: MutableList<String> = mutableListOf<String>()
)
