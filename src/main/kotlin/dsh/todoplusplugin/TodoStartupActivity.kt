package dsh.todoplusplugin

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.vfs.VirtualFileManager

class TodoStartupActivity: StartupActivity {
    override fun runActivity(project: Project) {
        val listener = TodoFileListener(project)
        VirtualFileManager.getInstance().addVirtualFileListener(listener, project)
    }
}