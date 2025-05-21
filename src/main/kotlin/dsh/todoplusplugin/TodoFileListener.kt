package dsh.todoplusplugin

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileEvent
import com.intellij.openapi.vfs.VirtualFileListener
import kotlinx.serialization.json.Json

class TodoFileListener(
    private val project: Project
): VirtualFileListener {
    override fun contentsChanged(event: VirtualFileEvent) {
        if (event.file.name == "todo.json") {
            val json = event.file.contentsToByteArray().toString(Charsets.UTF_8)
            val todos = Json.decodeFromString<List<TodoItem>>(json)
            TodoToolWindowFactory.updateTodos(project, todos)
        }
    }
}