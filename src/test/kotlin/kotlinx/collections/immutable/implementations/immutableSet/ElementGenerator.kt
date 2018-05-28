package kotlinx.collections.immutable.implementations.immutableSet

import java.util.*

class ElementGenerator<K: Comparable<K>>(private val hashCodeUpperBound: Int) {
    private val elementMap = hashMapOf<K, ElementWrapper<K>>()
    private val hashCodeMap = hashMapOf<Int, MutableList<ElementWrapper<K>>>()
    private val random = Random()

    fun wrapper(element: K): ElementWrapper<K> {
        val existing = elementMap[element]
        if (existing != null) {
            return existing
        }
        val hashCode = random.nextInt(hashCodeUpperBound)
        val wrapper = ElementWrapper(element, hashCode)
        elementMap[element] = wrapper

        val wrappers = hashCodeMap.getOrDefault(hashCode, mutableListOf())
        wrappers.add(wrapper)
        hashCodeMap[hashCode] = wrappers

        return wrapper
    }

    fun wrappersByHashCode(hashCode: Int): List<ElementWrapper<K>> {
        return hashCodeMap.getOrDefault(hashCode, mutableListOf())
    }
}