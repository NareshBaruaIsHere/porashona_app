package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.Subject
import com.example.data.model.Topic

@Database(
    entities = [Subject::class, Topic::class],
    version = 1,
    exportSchema = false
)
abstract class ExamTrackDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun topicDao(): TopicDao

    companion object {
        @Volatile
        private var INSTANCE: ExamTrackDatabase? = null

        fun getDatabase(context: Context): ExamTrackDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ExamTrackDatabase::class.java,
                    "exam_track_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
