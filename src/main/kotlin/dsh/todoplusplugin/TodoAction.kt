package dsh.todoplusplugin

import dsh.todoplusplugin.utils.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
sealed class TodoAction {
    @Serializable
    data class TodoCreate(
        @Serializable(with = UUIDSerializer::class)
        val id: UUID,
        val item: TodoItem
    ): TodoAction()

    @Serializable
    data class TodoUpdate(
        @Serializable(with = UUIDSerializer::class)
        val id: UUID,
        val item: TodoItem
    ): TodoAction()

    @Serializable
    data class TodoDelete(
        @Serializable(with = UUIDSerializer::class)
        val id: UUID
    ): TodoAction()
}