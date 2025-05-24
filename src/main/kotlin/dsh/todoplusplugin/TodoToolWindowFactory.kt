package dsh.todoplusplugin

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.platform.ide.progress.ModalTaskOwner.project
import com.intellij.ui.content.ContentFactory
import java.awt.BorderLayout
import java.io.File
import java.util.LinkedList
import java.util.UUID
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel

object TodoToolWindowFactory: ToolWindowFactory {

    lateinit var project: Project
    lateinit var toolWindow: ToolWindow
    var todoFile: TodoFile? = null
    var sourceFile: File? = null
    var ignoreCount = 0

    fun initialize(inProject: Project, file: File?) {
        project = inProject
        sourceFile = file
        val contents = if (file != null && file.exists()) file.readText() else null
        todoFile = contents?.let { TodoFile.deserialize(it) }

        // make sure a tool window is found
        if (!this::toolWindow.isInitialized)
            toolWindow = ToolWindowManager.getInstance(project).getToolWindow("TODOs")!!

        refreshUI()
    }

    fun fileChange(file: File) {
        // make sure we have a source file, and the ignored count is less than 1
        if (ignoreCount-- > 0 && sourceFile != null) return

        // update source file and todo file
        sourceFile = file
        todoFile = TodoFile.deserialize(file.readText())

        // update UI
        refreshUI()
    }

    private fun refreshUI() {
        // clear the old tool window
        toolWindow.contentManager.removeAllContents(true)

        // create to do the main panel
        val panel = JPanel(BorderLayout())
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)

        // load contents into a linked list if necessary
        if (todoFile != null) {
            val todoFile = this.todoFile!!

            // add header
            val header = JPanel()
            header.layout = BoxLayout(header, BoxLayout.X_AXIS)
            header.alignmentX = 0.0f
            panel.add(header)
            header.add(JLabel(" TODOs: "))
            header.add(JButton("New Item").apply {
                alignmentX = 1.0f
                addActionListener { event ->
                    // request name from the user
                    val name = JOptionPane.showInputDialog(
                        panel,
                        "Enter item name:",
                        "New TODO",
                        JOptionPane.PLAIN_MESSAGE
                    )

                    // if the user gave a name, create a new item
                    if (!name.isNullOrBlank()) {
                        todoFile.newAction(
                            id = UUID.randomUUID(),
                            action = TodoAction.Create(name)
                        )

                        drain()
                        refreshUI()
                    }
                }
            })

            // render to do items
            todoFile.render().forEach { item ->
                // setup row panel
                val row = JPanel()
                row.layout = BoxLayout(row, BoxLayout.X_AXIS)
                row.alignmentX = 0.0f
                panel.add(row)

                // add checkbox 
                row.add(JCheckBox().apply {
                    isSelected = item.completed
                    addActionListener { event ->
                        // remove last set-completed action if peek last and this item, otherwise, add a new set-completed action
                        if (
                            todoFile.peekLast()?.third is TodoAction.SetCompleted &&
                            todoFile.peekLast()?.second == item.id
                        ) todoFile.dropLast()
                        else todoFile.newAction(
                                id = item.id,
                                action = TodoAction.SetCompleted(isSelected)
                            )

                        drain()
                    }
                })

                // add label
                row.add(JLabel(item.title))

                // add right aligned delete button
                row.add(Box.createHorizontalGlue())
                row.add(JButton("Delete").apply {
                    alignmentX = 1.0f
                    addActionListener { event ->
                        if (
                            todoFile.peekLast()?.third is TodoAction.Create &&
                            todoFile.peekLast()?.second == item.id
                        ) todoFile.dropLast()
                        else todoFile.newAction(
                                id = item.id,
                                action = TodoAction.Delete()
                            )

                        refreshUI()
                        drain()
                    }
                })
            }
        }

        // otherwise, if no contents and no to do file, add a button to create one
        if (todoFile == null) {
            val button = JButton("Create TODO file")
            button.addActionListener { event ->
                ApplicationManager.getApplication().runWriteAction {
                    sourceFile = File(project.basePath, "todo")
                    sourceFile!!.createNewFile()
                    todoFile = TodoFile(LinkedList())
                    val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(sourceFile!!)
                    if (virtualFile != null) VfsUtil.saveText(virtualFile, "")
                }
            }
            panel.add(button, BorderLayout.NORTH)
        }

        // render a new tool window
        val renderedContents = ContentFactory.getInstance().createContent(panel, "", false)
        toolWindow.contentManager.addContent(renderedContents)
    }

    fun drain() = sourceFile?.let { ignoreCount++; todoFile?.drain(it) }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        ApplicationManager.getApplication().runWriteAction {
            val todoFile = File(project.basePath, "todo")
            initialize(project, todoFile)
        }
    }
}