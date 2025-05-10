package com.example.getmytimeback.model

data class BlockedSite(
    val domain: String,
    val allowedTime: Int,
    var consumedTime: Long
)
