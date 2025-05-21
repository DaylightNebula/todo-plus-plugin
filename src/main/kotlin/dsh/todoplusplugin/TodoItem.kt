package dsh.todoplusplugin

import kotlinx.serialization.Serializable

@Serializable
class TodoItem(
    val title: String,
    val category: String = "Uncategorized",
    val priority: String = "Medium",
    val description: String = "",
    var completed: Boolean = false
)