package com.example.tasknotifier.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tasknotifier.data.entities.Task
import com.example.tasknotifier.domain.model.TaskPriority
import com.example.tasknotifier.domain.usecase.TaskUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskUseCases: TaskUseCases
) : ViewModel() {

    val tasks = taskUseCases.getAllTasks()

    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    fun loadTask(taskId: Long) {
        viewModelScope.launch {
            val task = taskUseCases.getTaskById(taskId)
            _uiState.value = _uiState.value.copy(
                editingTask = task,
                title = task?.title ?: "",
                description = task?.description ?: "",
                priority = task?.priority ?: TaskPriority.MEDIUM.name
            )
        }
    }

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun updatePriority(priority: String) {
        _uiState.value = _uiState.value.copy(priority = priority)
    }

    fun saveTask() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val task = currentState.editingTask?.copy(
                title = currentState.title,
                description = currentState.description,
                priority = currentState.priority
            ) ?: Task(
                title = currentState.title,
                description = currentState.description,
                priority = currentState.priority
            )

            if (currentState.editingTask == null) {
                taskUseCases.createTask(task)
            } else {
                taskUseCases.updateTask(task)
            }
            clearState()
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskUseCases.deleteTask(task)
        }
    }

    private fun clearState() {
        _uiState.value = TaskUiState()
    }
}

data class TaskUiState(
    val editingTask: Task? = null,
    val title: String = "",
    val description: String = "",
    val priority: String = TaskPriority.MEDIUM.name
)