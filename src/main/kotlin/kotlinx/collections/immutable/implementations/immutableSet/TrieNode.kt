package kotlinx.collections.immutable.implementations.immutableSet


internal const val MAX_BRANCHING_FACTOR = 32
internal const val LOG_MAX_BRANCHING_FACTOR = 5
internal const val MAX_BRANCHING_FACTOR_MINUS_ONE = MAX_BRANCHING_FACTOR - 1
internal const val ENTRY_SIZE = 2
internal const val MAX_SHIFT = 30
internal const val NULL_HASH_CODE = 0


internal class TrieNode<E>(var bitmap: Int,
                           var buffer: Array<Any?>,
                           var marker: Marker?) {

    constructor(bitmap: Int, buffer: Array<Any?>) : this(bitmap, buffer, null)

    fun makeMutableFor(mutator: PersistentHashSetBuilder<*>): TrieNode<E> {
        if (marker === mutator.marker) { return this }
        return TrieNode(bitmap, buffer.copyOf(), mutator.marker)
    }

    private fun ensureMutableBy(mutator: PersistentHashSetBuilder<*>) {
        if (marker !== mutator.marker) {
            throw IllegalStateException("Markers expected to be same")
        }
    }

    private fun isNullCellAt(position: Int): Boolean {
        return bitmap and position == 0
    }

    private fun indexOfCellAt(position: Int): Int {
        return Integer.bitCount(bitmap and (position - 1))
    }

    private fun elementAtIndex(index: Int): E {
        return buffer[index] as E
    }

    private fun nodeAtIndex(index: Int): TrieNode<E> {
        return buffer[index] as TrieNode<E>
    }

    private fun bufferAddElementAtIndex(index: Int, element: E): Array<Any?> {
        val newBuffer = arrayOfNulls<Any?>(buffer.size + 1)
        System.arraycopy(buffer, 0, newBuffer, 0, index)
        System.arraycopy(buffer, index, newBuffer, index + 1, buffer.size - index)
        newBuffer[index] = element
        return newBuffer
    }

    private fun addElementAt(position: Int, element: E): TrieNode<E> {
//        assert(isNullCellAt(position))

        val index = indexOfCellAt(position)
        val newBuffer = bufferAddElementAtIndex(index, element)
        return TrieNode(bitmap or position, newBuffer)
    }

    private fun mutableAddElementAt(position: Int, element: E) {
//        assert(isNullCellAt(position))

        val index = indexOfCellAt(position)
        buffer = bufferAddElementAtIndex(index, element)
        bitmap = bitmap or position
    }

    private fun updateNodeAtIndex(nodeIndex: Int, newNode: TrieNode<E>): TrieNode<E> {
//        assert(buffer[nodeIndex] !== newNode)

        val newBuffer = buffer.copyOf()
        newBuffer[nodeIndex] = newNode
        return TrieNode(bitmap, newBuffer)
    }

    private fun mutableUpdateNodeAtIndex(nodeIndex: Int, newNode: TrieNode<E>) {
        buffer[nodeIndex] = newNode
    }

    private fun makeNodeAtIndex(elementIndex: Int, newElementHash: Int, newElement: E,
                                shift: Int, mutatorMarker: Marker?): TrieNode<E> {
        val storedElement = elementAtIndex(elementIndex)
        val storedElementHash = storedElement?.hashCode() ?: NULL_HASH_CODE
        return makeNode(storedElementHash, storedElement,
                newElementHash, newElement, shift + LOG_MAX_BRANCHING_FACTOR, mutatorMarker)
    }

    private fun moveElementToNode(elementIndex: Int, newElementHash: Int, newElement: E,
                                  shift: Int): TrieNode<E> {
//        assert(!isNullCellAt(position))

        val newBuffer = buffer.copyOf()
        newBuffer[elementIndex] = makeNodeAtIndex(elementIndex, newElementHash, newElement, shift, null)
        return TrieNode(bitmap, newBuffer)
    }

    private fun mutableMoveElementToNode(elementIndex: Int, newElementHash: Int, newElement: E,
                                         shift: Int, mutator: PersistentHashSetBuilder<*>) {
//        assert(!isNullCellAt(position))

        buffer[elementIndex] = makeNodeAtIndex(elementIndex, newElementHash, newElement, shift, mutator.marker)
    }

    private fun makeNode(elementHash1: Int, element1: E, elementHash2: Int, element2: E,
                         shift: Int, mutatorMarker: Marker?): TrieNode<E> {
        if (shift > MAX_SHIFT) {
//            assert(element1 != element2)
            return TrieNode<E>(0, arrayOf(element1, element2), mutatorMarker)
        }

        val setBit1 = (elementHash1 shr shift) and MAX_BRANCHING_FACTOR_MINUS_ONE
        val setBit2 = (elementHash2 shr shift) and MAX_BRANCHING_FACTOR_MINUS_ONE

        if (setBit1 != setBit2) {
            val nodeBuffer =  if (setBit1 < setBit2) {
                arrayOf<Any?>(element1, element2)
            } else {
                arrayOf<Any?>(element2, element1)
            }
            return TrieNode((1 shl setBit1) or (1 shl setBit2), nodeBuffer, mutatorMarker)
        }
        val node = makeNode(elementHash1, element1, elementHash2, element2, shift + LOG_MAX_BRANCHING_FACTOR, mutatorMarker)
        return TrieNode<E>(1 shl setBit1, arrayOf(node), mutatorMarker)
    }

    private fun bufferRemoveCellAtIndex(cellIndex: Int): Array<Any?> {
        val newBuffer = arrayOfNulls<Any?>(buffer.size - 1)
        System.arraycopy(buffer, 0, newBuffer, 0, cellIndex)
        System.arraycopy(buffer, cellIndex + 1, newBuffer, cellIndex, buffer.size - cellIndex - 1)
        return newBuffer
    }

    private fun removeCellAtIndex(cellIndex: Int, position: Int): TrieNode<E>? {
//        assert(!isNullCellAt(position))
        if (buffer.size == 1) { return null }

        val newBuffer = bufferRemoveCellAtIndex(cellIndex)
        return TrieNode(bitmap xor position, newBuffer)
    }

    private fun mutableRemoveCellAtIndex(cellIndex: Int, position: Int) {
//        assert(!isNullCellAt(position))
        buffer = bufferRemoveCellAtIndex(cellIndex)
        bitmap = bitmap xor position
    }

    private fun collisionRemoveElementAtIndex(i: Int): TrieNode<E>? {
        if (buffer.size == 1) { return null }

        val newBuffer = bufferRemoveCellAtIndex(i)
        return TrieNode(0, newBuffer)
    }

    private fun mutableCollisionRemoveElementAtIndex(i: Int) {
        buffer = bufferRemoveCellAtIndex(i)
    }

    private fun collisionContainsElement(element: E): Boolean {
        return buffer.contains(element)
    }

    private fun collisionAdd(element: E): TrieNode<E> {
        if (collisionContainsElement(element)) { return this }
        val newBuffer = bufferAddElementAtIndex(0, element)
        return TrieNode(0, newBuffer)
    }

    private fun mutableCollisionAdd(element: E, mutator: PersistentHashSetBuilder<*>): Boolean {
        if (collisionContainsElement(element)) { return false }
        mutator.size++
        buffer = bufferAddElementAtIndex(0, element)
        return true
    }

    private fun collisionRemove(element: E): TrieNode<E>? {
        val index = buffer.indexOf(element)
        if (index != -1) {
            return collisionRemoveElementAtIndex(index)
        }
        return this
    }

    private fun mutableCollisionRemove(element: E, mutator: PersistentHashSetBuilder<*>): Boolean {
        val index = buffer.indexOf(element)
        if (index != -1) {
            mutator.size--
            mutableCollisionRemoveElementAtIndex(index)
            return true
        }
        return false
    }

    fun contains(elementHash: Int, element: E, shift: Int): Boolean {
        val cellPosition = 1 shl ((elementHash shr shift) and MAX_BRANCHING_FACTOR_MINUS_ONE)

        if (isNullCellAt(cellPosition)) { // element is absent
            return false
        }

        val cellIndex = indexOfCellAt(cellPosition)
        if (buffer[cellIndex] is TrieNode<*>) { // element may be in node
            val targetNode = nodeAtIndex(cellIndex)
            if (shift == MAX_SHIFT) {
                return targetNode.collisionContainsElement(element)
            }
            return targetNode.contains(elementHash, element, shift + LOG_MAX_BRANCHING_FACTOR)
        }
        // element is directly in buffer
        return element == buffer[cellIndex]
    }

    fun add(elementHash: Int, element: E, shift: Int): TrieNode<E> {
        val cellPosition = 1 shl ((elementHash shr shift) and MAX_BRANCHING_FACTOR_MINUS_ONE)

        if (isNullCellAt(cellPosition)) { // element is absent
            return addElementAt(cellPosition, element)
        }

        val cellIndex = indexOfCellAt(cellPosition)
        if (buffer[cellIndex] is TrieNode<*>) { // element may be in node
            val targetNode = nodeAtIndex(cellIndex)
            val newNode = if (shift == MAX_SHIFT) {
                targetNode.collisionAdd(element)
            } else {
                targetNode.add(elementHash, element, shift + LOG_MAX_BRANCHING_FACTOR)
            }
            if (targetNode === newNode) { return this }
            return updateNodeAtIndex(cellIndex, newNode)
        }
        // element is directly in buffer
        if (element == buffer[cellIndex]) { return this }
        return moveElementToNode(cellIndex, elementHash, element, shift)
    }

    fun mutableAdd(elementHash: Int, element: E, shift: Int, mutator: PersistentHashSetBuilder<*>): Boolean {
        ensureMutableBy(mutator)
        val cellPosition = 1 shl ((elementHash shr shift) and MAX_BRANCHING_FACTOR_MINUS_ONE)

        if (isNullCellAt(cellPosition)) { // element is absent
            mutator.size++
            mutableAddElementAt(cellPosition, element)
            return true
        }

        val cellIndex = indexOfCellAt(cellPosition)
        if (buffer[cellIndex] is TrieNode<*>) { // element may be in node
            val targetNode = nodeAtIndex(cellIndex).makeMutableFor(mutator)
            mutableUpdateNodeAtIndex(cellIndex, targetNode)
            return if (shift == MAX_SHIFT) {
                targetNode.mutableCollisionAdd(element, mutator)
            } else {
                targetNode.mutableAdd(elementHash, element, shift + LOG_MAX_BRANCHING_FACTOR, mutator)
            }
        }
        // element is directly in buffer
        if (element == buffer[cellIndex]) { return false }
        mutator.size++
        mutableMoveElementToNode(cellIndex, elementHash, element, shift, mutator)
        return true
    }

    fun remove(elementHash: Int, element: E, shift: Int): TrieNode<E>? {
        val cellPosition = 1 shl ((elementHash shr shift) and MAX_BRANCHING_FACTOR_MINUS_ONE)

        if (isNullCellAt(cellPosition)) { // element is absent
            return this
        }

        val cellIndex = indexOfCellAt(cellPosition)
        if (buffer[cellIndex] is TrieNode<*>) { // element may be in node
            val targetNode = nodeAtIndex(cellIndex)
            val newNode = if (shift == MAX_SHIFT) {
                targetNode.collisionRemove(element)
            } else {
                targetNode.remove(elementHash, element, shift + LOG_MAX_BRANCHING_FACTOR)
            }
            if (targetNode === newNode) { return this }
            if (newNode == null) { return removeCellAtIndex(cellIndex, cellPosition) }
            return updateNodeAtIndex(cellIndex, newNode)
        }
        // element is directly in buffer
        if (element == buffer[cellIndex]) {
            return removeCellAtIndex(cellIndex, cellPosition)
        }
        return this
    }

    fun mutableRemove(elementHash: Int, element: E, shift: Int, mutator: PersistentHashSetBuilder<*>): Boolean {
        ensureMutableBy(mutator)
        val cellPosition = 1 shl ((elementHash shr shift) and MAX_BRANCHING_FACTOR_MINUS_ONE)

        if (isNullCellAt(cellPosition)) { // element is absent
            return false
        }

        val cellIndex = indexOfCellAt(cellPosition)
        if (buffer[cellIndex] is TrieNode<*>) { // element may be in node
            val targetNode = nodeAtIndex(cellIndex).makeMutableFor(mutator)
            mutableUpdateNodeAtIndex(cellIndex, targetNode)
            val result = if (shift == MAX_SHIFT) {
                targetNode.mutableCollisionRemove(element, mutator)
            } else {
                targetNode.mutableRemove(elementHash, element, shift + LOG_MAX_BRANCHING_FACTOR, mutator)
            }
            if (targetNode.buffer.isEmpty()) { mutableRemoveCellAtIndex(cellIndex, cellPosition) }
            return result
        }
        // element is directly in buffer
        if (element == buffer[cellIndex]) {
            mutator.size--
            mutableRemoveCellAtIndex(cellIndex, cellPosition)   // check is empty
            return true
        }
        return false
    }

    internal companion object {
        internal val EMPTY = TrieNode<Nothing>(0, emptyArray())
    }
}