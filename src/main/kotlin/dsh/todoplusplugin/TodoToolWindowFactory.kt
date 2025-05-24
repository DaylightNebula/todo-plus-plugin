package dsh.todoplusplugin

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.ContentFactory
import java.awt.BorderLayout
import java.io.File
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JPanel

// todo save on todo file dirty without calling refresh
// todo remove task button
// todo add new task button

object TodoToolWindowFactory: ToolWindowFactory {

    lateinit var toolWindow: ToolWindow
    lateinit var todoFile: TodoFile
    var waiting = true

    fun refresh(project: Project, contents: String?) {
        // make sure a tool window is found
        if (!this::toolWindow.isInitialized)
            toolWindow = ToolWindowManager.getInstance(project).getToolWindow("TODOs")!!

        // clear the old tool window
        toolWindow.contentManager.removeAllContents(true)

        // create to do main panel
        val panel = JPanel(BorderLayout())

        // load contents into a linked list if necessary
        if (contents != null && !this::todoFile.isInitialized)
            todoFile = TodoFile.deserialize(contents)
        if (contents != null && this::todoFile.isInitialized) {
            panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
            panel.add(JLabel("TODOs: ")) // todo add "add item button"
            val toRender = todoFile.render()
            toRender.forEach { item ->
                // setup row panel
                val row = JPanel()
                row.layout = BoxLayout(row, BoxLayout.X_AXIS)
                row.alignmentX = 0.0f
                panel.add(row)

                // add checkbox 
                val cbox = JCheckBox()
                cbox.addActionListener { event ->
                    // remove last set-completed action if peek last and this item, otherwise, add a new set-completed action
                    if (todoFile.peekLast()?.third is TodoAction.SetCompleted && todoFile.peekLast()?.second == item.id)
                        todoFile.dropLast()
                    else
                        todoFile.newAction(
                            id = item.id,
                            action = TodoAction.SetCompleted(cbox.isSelected)
                        )
                }
                row.add(cbox)

                // add label
                row.add(JLabel(item.title))
            }
        }

        // otherwise, if no contents and no to do file, add a button to create one
        if (contents == null) {
            val button = JButton("Create TODO file")
            button.addActionListener { event ->
                ApplicationManager.getApplication().runWriteAction {
                    val todoFile = File(project.basePath, "todo")
                    todoFile.createNewFile()
                    val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(todoFile)
                    if (virtualFile != null) {
                        VfsUtil.saveText(virtualFile, "")
                        refresh(project, "")
                    }
                }
            }
            panel.add(button, BorderLayout.NORTH)
        }

        // render a new tool window
        val renderedContents = ContentFactory.getInstance().createContent(panel, "", false)
        toolWindow.contentManager.addContent(renderedContents)
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        ApplicationManager.getApplication().runWriteAction {
            val todoFile = File(project.basePath, "todo")
            val contents = if (todoFile.exists()) todoFile.readText() else null
            refresh(project, contents)
        }
    }
}