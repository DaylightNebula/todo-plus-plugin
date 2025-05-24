package dsh.todoplusplugin

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.LinkedList
import java.util.UUID

class TodoFile(
    val sortedActions: LinkedList<Triple<Long, UUID, TodoAction>>,
    var dirty: Boolean = false
) {
    companion object {
        val json = Json {}

        fun deserialize(content: String): TodoFile {
            var dirty = false
            val output = LinkedList<Triple<Long, UUID, TodoAction>>()
            val iterator = content.iterator()

            while(iterator.hasNext()) {
                // loads a timestamp, a UUID, and a TodoAction
                val timestamp = iterator.nextUntil('|').toLong()
                val id = UUID.fromString(iterator.nextUntil('|'))
                val action = json.decodeFromString<TodoAction>(iterator.nextUntilClose('{', '}'))

                // check if this action can go at the end of the sorted output list
                if (timestamp >= (output.lastOrNull()?.first ?: Long.MIN_VALUE))
                    output.addLast(Triple(timestamp, id, action))
                // otherwise, add to where the timestamp is greater than the last timestamp in the list
                else {
                    dirty = true
                    val iter = output.listIterator()
                    while(iter.hasPrevious()) {
                        val previous = iter.previous()
                        if (timestamp <= previous.first) {
                            iter.next()
                            iter.add(Triple(timestamp, id, action))
                            break
                        }
                    }
                }
            }

            return TodoFile(output, dirty)
        }
    }

    fun serialize() = sortedActions.joinToString(separator = "\n") { (timestamp, id, action) ->
        val actionJson = json.encodeToString(action)
        "$timestamp|$id|$actionJson"
    }

    fun render(): Collection<TodoItem> {
        val map = mutableMapOf<UUID, TodoItem>()
        sortedActions.forEach { (_, id, action) -> action.render(id, map) }
        return map.values
    }

    fun newAction(id: UUID, action: TodoAction) {
        sortedActions.addLast(Triple(System.currentTimeMillis(), id, action))
        dirty = true
    }

    fun dropLast() {
        sortedActions.removeLast()
        dirty = true
    }

    fun peekLast() = sortedActions.lastOrNull()
}

fun Iterator<Char>.nextUntil(char: Char): String {
    val builder = StringBuilder()
    while (hasNext()) {
        val next = next()
        if (next == char) break
        builder.append(next)
    }
    return builder.toString()
}

fun Iterator<Char>.nextUntilClose(increment: Char, decrement: Char): String {
    var counter = 0
    val builder = StringBuilder()
    while(hasNext()) {
        val next = next()
        if (next == increment) counter++
        if (next == decrement) counter--
        builder.append(next)
        if (counter == 0) break
    }
    return builder.toString()
}
