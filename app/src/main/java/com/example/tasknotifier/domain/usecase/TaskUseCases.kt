package com.example.tasknotifier.domain.usecase

import com.example.tasknotifier.data.entities.Task
import com.example.tasknotifier.data.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TaskUseCases @Inject constructor(
    private val repository: TaskRepository
) {
    fun getAllTasks(): Flow<List<Task>> = repository.getAllTasks()

    suspend fun getTaskById(taskId: Long): Task? = repository.getTaskById(taskId)

    suspend fun createTask(task: Task): Long = repository.insertTask(task)

    suspend fun updateTask(task: Task) = repository.updateTask(task)

    suspend fun deleteTask(task: Task) = repository.deleteTask(task)

    fun getActiveTasks(): Flow<List<Task>> = repository.getActiveTasks()
}