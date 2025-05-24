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
    data class TodoCreate(
        val title: String
    ): TodoAction() {
        override fun render(id: UUID, map: MutableMap<UUID, TodoItem>) {
            map[id] = TodoItem(id, title)
        }
    }

    @Serializable
    @SerialName("delete")
    class TodoDelete: TodoAction() {
        override fun render(id: UUID, map: MutableMap<UUID, TodoItem>) {
            map.remove(id)
        }
    }
}