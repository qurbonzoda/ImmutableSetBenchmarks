package kotlinx.collections.immutable.implementations.immutableSet

internal class PersistentHashSetMutableIterator<E>(private val builder: PersistentHashSetBuilder<E>)
    : PersistentHashSetIterator<E>(builder.node), MutableIterator<E> {
    var lastReturned: E? = null
    var nextWasInvoked = false

    override fun next(): E {
        val next = super.next()
        lastReturned = next
        nextWasInvoked = true
        return next
    }

    override fun remove() {
        if (!nextWasInvoked) {
            throw IllegalStateException()
        }
        if (hasNext()) {
            val currentElement = currentElement()

            assert(builder.remove(lastReturned))
            resetPath(currentElement?.hashCode() ?: NULL_HASH_CODE, builder.node, currentElement, 0)
        } else {
            assert(builder.remove(lastReturned))
        }

        lastReturned = null
        nextWasInvoked = false
    }

    private fun resetPath(hashCode: Int, node: TrieNode<*>, element: E, pathIndex: Int) {
        if (isCollision(node)) {
            val index = node.buffer.indexOf(element)
            assert(index != -1)
            path[pathIndex].reset(node.buffer, index)
            pathLastIndex = pathIndex
            return
        }

        val position = 1 shl ((hashCode shr (pathIndex * LOG_MAX_BRANCHING_FACTOR)) and MAX_BRANCHING_FACTOR_MINUS_ONE)
        val index = Integer.bitCount(node.bitmap and (position - 1))

        path[pathIndex].reset(node.buffer, index)

        val cell = node.buffer[index]
        if (cell is TrieNode<*>) {
            resetPath(hashCode, cell, element, pathIndex + 1)
        } else {
            assert(cell == element)
            pathLastIndex = pathIndex
        }
    }

    private fun isCollision(node: TrieNode<*>): Boolean {
        return node.bitmap == 0
    }
}