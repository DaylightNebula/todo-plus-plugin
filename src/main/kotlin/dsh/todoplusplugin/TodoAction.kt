package dsh.todoplusplugin

import dsh.todoplusplugin.utils.UUIDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
sealed class TodoAction {

    abstract fun render(id: UUID, map: MutableMap<UUID, TodoItem>)

    @Serializable
    @SerialName("create")
    data class Create(
        val title: String
    ): TodoAction() {
        override fun render(id: UUID, map: MutableMap<UUID, TodoItem>) {
            map[id] = TodoItem(id, title)
        }
    }

    @Serializable
    @SerialName("set_completed")
    data class SetCompleted(
        val completed: Boolean
    ): TodoAction() {
        override fun render(id: UUID, map: MutableMap<UUID, TodoItem>) {
            map[id]?.completed = completed
        }
    }

    @Serializable
    @SerialName("delete")
    class Delete: TodoAction() {
        override fun render(id: UUID, map: MutableMap<UUID, TodoItem>) {
            map.remove(id)
        }
    }
}