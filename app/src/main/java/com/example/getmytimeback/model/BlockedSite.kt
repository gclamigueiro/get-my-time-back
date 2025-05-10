package com.example.getmytimeback.model

data class BlockedSite(
    val domain: String,
    var allowedTime: Int,
    var consumedTime: Long
)
