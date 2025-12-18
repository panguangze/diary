package com.love.diary.data.model

/**
 * Data class for mood aggregation query results
 * Used for chart data showing average mood scores by date
 */
data class MoodAggregation(
    val date: String,
    val avgScore: Double
)
