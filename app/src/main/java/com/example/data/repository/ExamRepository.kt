package com.example.data.repository

import com.example.data.local.SubjectDao
import com.example.data.local.TopicDao
import com.example.data.model.Subject
import com.example.data.model.SubjectWithTopics
import com.example.data.model.Topic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ExamRepository(
    private val subjectDao: SubjectDao,
    private val topicDao: TopicDao
) {
    val allSubjectsWithTopics: Flow<List<SubjectWithTopics>> = combine(
        subjectDao.getAllSubjects(),
        topicDao.getAllTopics()
    ) { subjects, topics ->
        val topicsBySubject = topics.groupBy { it.subjectId }
        subjects.map { subject ->
            SubjectWithTopics(
                subject = subject,
                topics = topicsBySubject[subject.id]?.sortedBy { it.order } ?: emptyList()
            )
        }
    }

    fun getSubjectWithTopics(subjectId: String): Flow<SubjectWithTopics?> = combine(
        subjectDao.getSubjectById(subjectId),
        topicDao.getTopicsForSubject(subjectId)
    ) { subject, topics ->
        subject?.let {
            SubjectWithTopics(
                subject = it,
                topics = topics.sortedBy { t -> t.order }
            )
        }
    }

    suspend fun getSubjectById(subjectId: String): Subject? {
        return subjectDao.getSubjectByIdSync(subjectId)
    }

    suspend fun getAllSubjects(): List<Subject> {
        return subjectDao.getAllSubjectsSync()
    }

    suspend fun insertSubject(subject: Subject) {
        subjectDao.insertSubject(subject)
    }

    suspend fun updateSubject(subject: Subject) {
        subjectDao.updateSubject(subject)
    }

    suspend fun deleteSubject(subject: Subject) {
        topicDao.deleteTopicsForSubject(subject.id)
        subjectDao.deleteSubject(subject)
    }

    suspend fun deleteSubjectById(subjectId: String) {
        topicDao.deleteTopicsForSubject(subjectId)
        subjectDao.deleteSubjectById(subjectId)
    }

    suspend fun insertTopic(topic: Topic) {
        topicDao.insertTopic(topic)
    }

    suspend fun updateTopic(topic: Topic) {
        topicDao.updateTopic(topic)
    }

    suspend fun deleteTopicById(topicId: String) {
        topicDao.deleteTopicById(topicId)
    }

    suspend fun updateTopicsOrder(topics: List<Topic>) {
        topics.forEachIndexed { index, topic ->
            if (topic.order != index) {
                topicDao.updateTopic(topic.copy(order = index))
            }
        }
    }
}
