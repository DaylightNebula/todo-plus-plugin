//package dsh.todoplusplugin.oldeditor
//
//import com.intellij.openapi.fileEditor.FileEditor
//import com.intellij.openapi.fileEditor.FileEditorPolicy
//import com.intellij.openapi.fileEditor.FileEditorProvider
//import com.intellij.openapi.project.DumbAware
//import com.intellij.openapi.project.Project
//import com.intellij.openapi.vfs.VirtualFile
//
//class TodoFileEditorProvider: FileEditorProvider, DumbAware {
//    override fun accept(project: Project, file: VirtualFile): Boolean {
//        return file.name == "todo"
//    }
//
//    override fun createEditor(project: Project, file: VirtualFile): FileEditor {
//        return TodoFileEditor(file, project)
//    }
//
//    override fun getEditorTypeId(): String = "todo-json-editor"
//    override fun getPolicy(): FileEditorPolicy = FileEditorPolicy.HIDE_DEFAULT_EDITOR
//}