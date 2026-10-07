package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val examDate: Long, // Epoch millis representing exam date
    val colorTag: String, // Hex string, e.g. "#0D9488"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "topics",
    foreignKeys = [
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["subjectId"])]
)
data class Topic(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val subjectId: String,
    val title: String,
    val isDone: Boolean = false,
    val order: Int = 0
)

data class SubjectWithTopics(
    val subject: Subject,
    val topics: List<Topic> = emptyList()
) {
    val totalTopics: Int get() = topics.size
    val completedTopics: Int get() = topics.count { it.isDone }
    val progress: Float
        get() = if (totalTopics > 0) completedTopics.toFloat() / totalTopics.toFloat() else 0f
    val progressPercentage: Int
        get() = (progress * 100).toInt()
}
