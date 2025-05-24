package dsh.todoplusplugin.listeners

import com.intellij.openapi.vfs.VirtualFileEvent
import com.intellij.openapi.vfs.VirtualFileListener
import dsh.todoplusplugin.TodoToolWindowFactory

class TodoFileListener: VirtualFileListener {
    override fun contentsChanged(event: VirtualFileEvent) {
        if (event.file.name == "todo")
            TodoToolWindowFactory.fileChange(event.file.toNioPath().toFile())
    }
}