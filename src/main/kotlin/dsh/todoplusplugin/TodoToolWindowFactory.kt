package dsh.todoplusplugin

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.treeStructure.Tree
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeModel

object TodoToolWindowFactory: ToolWindowFactory {
//    companion object {
//        private val todoData = mutableMapOf<Project, List<TodoItem>>()
//
//        fun getTodos() = todoData
//
//        fun updateTodos(project: Project, todos: List<TodoItem>) {
//            todoData[project] = todos
//            val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("TODOs")
//            toolWindow?.contentManager?.removeAllContents(true)
//            toolWindow?.contentManager?.addContent(createContent(project, todos))
//        }
//
//        private fun createContent(project: Project, todos: List<TodoItem>): Content {
//            val panel = JPanel(BorderLayout())
//            val tree = Tree(buildTreeModel(todos))
//            panel.add(JScrollPane(tree), BorderLayout.CENTER)
//            return ContentFactory.getInstance().createContent(panel, "", false)
//        }
//
//        private fun buildTreeModel(todos: List<TodoItem>): TreeModel {
//            val root = DefaultMutableTreeNode("TODOs")
//            todos.groupBy { it.category }.forEach { (category, items) ->
//                val catNode = DefaultMutableTreeNode(category)
//                items.forEach {
//                    val itemText = if (it.completed) "[✓] ${it.title}" else it.title
//                    catNode.add(DefaultMutableTreeNode(itemText))
//                }
//                root.add(catNode)
//            }
//            return DefaultTreeModel(root)
//        }
//    }

    lateinit var toolWindow: ToolWindow
    var waiting = true

    fun refresh(project: Project, contents: String? = null) {
        // make sure tool window is found
        if (!this::toolWindow.isInitialized)
            toolWindow = ToolWindowManager.getInstance(project).getToolWindow("TODOs")!!

        // clear old tool window
        toolWindow.contentManager.removeAllContents(true)

        // load contents into linked list if necessary

        // otherwise, if no contents and no to do file, add button to create one

        // render new tool window
        val panel = JPanel(BorderLayout())
        val renderedContents = ContentFactory.getInstance().createContent(panel, "", false)
        toolWindow.contentManager.addContent(renderedContents)
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        refresh(project)
    }
}