package dsh.todoplusplugin

import dsh.todoplusplugin.utils.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
class TodoItem(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    var title: String,
    var completed: Boolean = false
)