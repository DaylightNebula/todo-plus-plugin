//package dsh.todoplusplugin.oldeditor
//
//import com.intellij.openapi.application.ApplicationManager
//import com.intellij.openapi.fileEditor.FileEditor
//import com.intellij.openapi.fileEditor.FileEditorLocation
//import com.intellij.openapi.fileEditor.FileEditorState
//import com.intellij.openapi.project.Project
//import com.intellij.openapi.util.UserDataHolderBase
//import com.intellij.openapi.vfs.VfsUtil
//import com.intellij.openapi.vfs.VirtualFile
//import com.intellij.ui.components.JBScrollPane
//import dsh.todoplusplugin.TodoItem
//import kotlinx.serialization.encodeToString
//import kotlinx.serialization.json.Json
//import java.awt.BorderLayout
//import java.beans.PropertyChangeListener
//import java.beans.PropertyChangeSupport
//import java.io.IOException
//import javax.swing.BoxLayout
//import javax.swing.JCheckBox
//import javax.swing.JComponent
//import javax.swing.JLabel
//import javax.swing.JPanel
//
//class TodoFileEditor(private val file: VirtualFile, private val project: Project) : UserDataHolderBase(), FileEditor {
//    private val propertyChangeSupport = PropertyChangeSupport(this)
//    private val panel = JPanel(BorderLayout())
//    private val todoItems = mutableListOf<TodoItem>()
//    private val checkboxes = mutableListOf<JCheckBox>()
//
//    private val PROP_MODIFIED = "modified"
//    private var modified = false
//
//    init {
//        val scroll = JBScrollPane(buildUI(file))
//        panel.add(scroll, BorderLayout.CENTER)
//    }
//
//    private fun buildUI(file: VirtualFile): JComponent {
//        val content = String(file.contentsToByteArray(), Charsets.UTF_8)
//        checkboxes.clear()
//
////        if (TodoToolWindowFactory.todo)
//        try {
//            val todos = Json.decodeFromString<List<TodoItem>>(content)
//            todoItems.addAll(todos)
//        } catch (e: Exception) {
//            return JLabel("Invalid TODO JSON format. $e")
//        }
//
//        val wrapper = JPanel()
//        wrapper.layout = BoxLayout(wrapper, BoxLayout.Y_AXIS)
//
////        todoItems.groupBy { it.category }.forEach { (category, items) ->
////            wrapper.add(JLabel("<html><h3>$category</h3></html>"))
////            items.forEach { todo ->
////                val checkbox = JCheckBox(todo.title, todo.completed)
////                checkbox.addActionListener {
////                    todo.completed = checkbox.isSelected
////                    modified = true
////                    firePropertyChange(PROP_MODIFIED, false, true) // notify IDE about the modified state
////                    save()
////                }
////                checkboxes.add(checkbox)
////                wrapper.add(checkbox)
////            }
////        }
//
//        return wrapper
//    }
//
//    fun save() {
//        if (!modified) return
//
//        val json = Json.encodeToString(todoItems)
//        ApplicationManager.getApplication().runWriteAction {
//            try {
//                VfsUtil.saveText(file, json)
//                modified = false
//                firePropertyChange(PROP_MODIFIED, true, false)
//            } catch (e: IOException) {
//                // handle save error: notify user or log
//                e.printStackTrace()
//            }
//        }
//    }
//
//    override fun getFile() = file
//    override fun getComponent(): JComponent = panel
//    override fun getPreferredFocusedComponent(): JComponent = panel
//    override fun getName(): String = "TODO Viewer"
//    override fun dispose() {}
//    override fun getCurrentLocation(): FileEditorLocation? = null
//    override fun isValid(): Boolean = true
//    override fun isModified(): Boolean = modified
//
//    fun firePropertyChange(propertyName: String, oldValue: Boolean, newValue: Boolean) =
//        propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue)
//
//    override fun addPropertyChangeListener(listener: PropertyChangeListener) =
//        propertyChangeSupport.addPropertyChangeListener(listener)
//
//    override fun removePropertyChangeListener(listener: PropertyChangeListener) =
//        propertyChangeSupport.removePropertyChangeListener(listener)
//
//    override fun setState(state: FileEditorState) {}
//}
