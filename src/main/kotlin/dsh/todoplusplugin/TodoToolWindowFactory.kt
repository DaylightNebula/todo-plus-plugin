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
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import java.util.LinkedList
import java.util.UUID
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JMenuItem
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JPopupMenu

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
        todoFile = if (sourceFile?.exists() == true) TodoFile.deserialize(file.readText()) else null

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
        if (todoFile != null && sourceFile?.exists() == true) drawFileUI(panel)
        // otherwise, if no contents and no to do file, add a button to create one
        else drawNoFileUI(panel)

        // render a new tool window
        val renderedContents = ContentFactory.getInstance().createContent(panel, "", false)
        toolWindow.contentManager.addContent(renderedContents)
    }

    private fun drawFileUI(panel: JPanel) {
        val todoFile = this.todoFile ?: return

        // add header
        val header = JPanel()
        header.layout = BoxLayout(header, BoxLayout.X_AXIS)
        header.alignmentX = 0.0f
        panel.add(header)
        header.add(JLabel(" TODOs: "))
        header.add(JButton("New Item").apply {
            alignmentX = 1.0f
            addActionListener { newItem(panel) }
        })

        // render to do items
        todoFile.render().forEach { item -> renderItem(panel, item) }
    }

    private fun drawNoFileUI(panel: JPanel) {
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

    private fun renderItem(panel: JPanel, item: TodoItem) {
        // setup row panel
        val row = JPanel()
        row.layout = BoxLayout(row, BoxLayout.X_AXIS)
        row.alignmentX = 0.0f
        panel.add(row)

        // add checkbox
        row.add(JCheckBox().apply {
            isSelected = item.completed
            addActionListener { updateCheckbox(this, item) }
        })

        // add label
        row.add(JLabel(item.title))

        val popupMenu = JPopupMenu().apply {
            add(JMenuItem("Rename").apply {
                addActionListener { renameItem(panel, item) }
            })
            add(JMenuItem("Delete").apply {
                addActionListener { deleteItem(item) }
            })
        }

        row.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) { if (e.isPopupTrigger) showMenu(e) }
            override fun mouseReleased(e: MouseEvent) { if (e.isPopupTrigger) showMenu(e) }
            private fun showMenu(e: MouseEvent) { popupMenu.show(e.component, e.x, e.y) }
        })
    }

    private fun newItem(panel: JPanel) {
        val todoFile = this.todoFile ?: return

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

    private fun updateCheckbox(cbox: JCheckBox, item: TodoItem) {
        val todoFile = this.todoFile ?: return

        // remove last set-completed action if peek last and this item, otherwise, add a new set-completed action
        if (
            todoFile.peekLast()?.third is TodoAction.SetCompleted &&
            todoFile.peekLast()?.second == item.id
        ) todoFile.dropLast()
        else todoFile.newAction(
            id = item.id,
            action = TodoAction.SetCompleted(cbox.isSelected)
        )

        drain()
    }

    private fun renameItem(panel: JPanel, item: TodoItem) {
        val todoFile = this.todoFile ?: return

        // ask the user for the new name
        val name = JOptionPane.showInputDialog(
            panel,
            "Enter the new item name:",
            item.title,
            JOptionPane.PLAIN_MESSAGE
        )

        if (!name.isNullOrBlank())
            todoFile.newAction(item.id, TodoAction.SetTitle(name))

        refreshUI()
        drain()
    }

    private fun deleteItem(item: TodoItem) {
        val todoFile = this.todoFile ?: return

        // remove last create if matches this item, otherwise, add delete action
        if (
            todoFile.peekLast()?.third is TodoAction.Create &&
            todoFile.peekLast()?.second == item.id
        ) todoFile.dropLast()
        else todoFile.newAction(
                id = item.id,
                action = TodoAction.Delete()
            )

        // save and update UI
        refreshUI()
        drain()
    }

    fun drain() = sourceFile?.let { sourceFile ->
        ignoreCount++
        todoFile?.drain(sourceFile)
        VfsUtil.markDirtyAndRefresh(true, false, false, sourceFile)
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        ApplicationManager.getApplication().runWriteAction {
            val todoFile = File(project.basePath, "todo")
            initialize(project, todoFile)
        }
    }
}