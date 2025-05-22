package dsh.todoplusplugin.listeners

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileEvent
import com.intellij.openapi.vfs.VirtualFileListener
import dsh.todoplusplugin.TodoItem
import dsh.todoplusplugin.TodoToolWindowFactory
import kotlinx.serialization.json.Json

class TodoFileListener(
    private val project: Project
): VirtualFileListener {
    override fun contentsChanged(event: VirtualFileEvent) {
        if (event.file.name == "todo") {
            val contents = event.file.contentsToByteArray().toString(Charsets.UTF_8)
            if (TodoToolWindowFactory.waiting) TodoToolWindowFactory.refresh(project, contents)
        }
    }
}