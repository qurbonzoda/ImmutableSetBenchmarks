package kotlinx.collections.immutable.implementations.immutableSet

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

class PersistentHashSetBuilderTest {
    @Test
    fun isEmptyTests() {
        val builder = persistentHashSetOf<Int>().builder()

        assertTrue(builder.isEmpty())

        val elementsToAdd = 1000000
        repeat(times = elementsToAdd) { index ->
            builder.add(index)
            assertFalse(builder.isEmpty())
        }
        repeat(times = elementsToAdd - 1) { index ->
            builder.remove(index)
            assertFalse(builder.isEmpty())
        }
        builder.remove(elementsToAdd - 1)
        assertTrue(builder.isEmpty())
    }

    @Test
    fun sizeTests() {
        val builder = persistentHashSetOf<Int>().builder()

        assertTrue(builder.size == 0)

        val elementsToAdd = 100000
        repeat(times = elementsToAdd) { index ->
            builder.add(index)
            assertEquals(index + 1, builder.size)

            builder.add(index)
            assertEquals(index + 1, builder.size)
        }
        repeat(times = elementsToAdd) { index ->
            builder.remove(index)
            assertEquals(elementsToAdd - index - 1, builder.size)

            builder.remove(index)
            assertEquals(elementsToAdd - index - 1, builder.size)
        }
    }

    @Test
    fun storedElementsTests() {
        val builder = persistentHashSetOf<Int>().builder()
        assertTrue(builder.isEmpty())

        val mutableSet = mutableSetOf<Int>()
        val random = Random()

        val elementsToAdd = 2000
        repeat(times = elementsToAdd) {
            val element = random.nextInt()
            mutableSet.add(element)
            builder.add(element)

            assertEquals(builder.sorted(), mutableSet.sorted())
        }

        mutableSet.toMutableSet().forEach { element ->
            mutableSet.remove(element)
            builder.remove(element)

            assertEquals(builder.sorted(), mutableSet.sorted())
        }

        assertTrue(builder.isEmpty())
    }

    @Test
    fun iteratorTests() {
        val builder = persistentHashSetOf<Int>().builder()
        assertFalse(builder.iterator().hasNext())

        val mutableSet = mutableSetOf<Int>()
        val random = Random()

        val elementsToAdd = 2000
        repeat(times = elementsToAdd) {
            val element = random.nextInt()
            mutableSet.add(element)
            builder.add(element)
        }

        var iterator = builder.iterator()
        mutableSet.toMutableSet().forEach { element ->
            mutableSet.remove(element)

            var didRemove = false
            for (i in 0..1) {
                while (!didRemove && iterator.hasNext()) {
                    if (iterator.next() == element) {
                        iterator.remove()
                        didRemove = true
                        break
                    }
                }
                if (!didRemove) {
                    iterator = builder.iterator()
                }
            }
            assertTrue(didRemove)

            assertEquals(mutableSet.size, builder.size)
            assertEquals(mutableSet.sorted(), builder.sorted())
        }

        assertTrue(builder.isEmpty())
    }

    @Test
    fun removeTests() {
        val builder = persistentHashSetOf<Int>().builder()

        val elementsToAdd = 1000000
        repeat(times = elementsToAdd) { index ->
            builder.add(index)
        }
        repeat(times = elementsToAdd) { index ->
            assertEquals(elementsToAdd - index, builder.size)

            assertTrue(builder.contains(index))
            builder.remove(index)
            assertFalse(builder.contains(index))
        }
    }

    @Test
    fun containsTests() {
        val builder = persistentHashSetOf<String>().builder()

        val elementsToAdd = 10000
        repeat(times = elementsToAdd) { index ->
            builder.add(index.toString())

            for (i in 0..index) {
                assertTrue(builder.contains(i.toString()))
            }
        }
        repeat(times = elementsToAdd) { index ->
            for (i in elementsToAdd - 1 downTo index) {
                assertTrue(builder.contains(i.toString()))
            }

            builder.remove(index.toString())
        }
    }

    @Test
    fun addTests() {
        val set = persistentHashSetOf<Int>().builder()

        val elementsToAdd = 5000
        repeat(times = elementsToAdd) { index ->
            set.add(index * 2)

            for (i in index downTo 0) {
                assertTrue(set.contains(i + index))
                set.remove(i + index)
                assertFalse(set.contains(i + index))
                assertFalse(set.contains(i + index + 1))
                set.add(i + index + 1)
                assertTrue(set.contains(i + index + 1))
            }
        }
        repeat(times = elementsToAdd) { index ->
            for (i in index until elementsToAdd ) {
                val expected = elementsToAdd - index + i

                assertTrue(set.contains(expected))
                assertFalse(set.contains(expected - 1))
                set.remove(expected)
                set.add(expected - 1)
                assertTrue(set.contains(expected - 1))
            }

            set.remove(elementsToAdd - 1)
        }
    }

    @Test
    fun collisionTests() {
        val builder = persistentHashSetOf<ElementWrapper<Int>>().builder()

        val eGen = ElementGenerator<Int>(20000)
        fun wrapper(element: Int): ElementWrapper<Int> {
            return eGen.wrapper(element)
        }

        val elementsToAdd = 100000   /// should be more than eGen.hashCodeUpperBound
        repeat(times = elementsToAdd) { index ->
            builder.add(wrapper(index))
            assertTrue(builder.contains(wrapper(index)))
            assertEquals(index + 1, builder.size)

            builder.add(wrapper(index))
            assertEquals(index + 1, builder.size)

            val collisions = eGen.wrappersByHashCode(wrapper(index).hashCode)
            assertTrue(collisions.contains(wrapper(index)))

            for (key in collisions) {
                assertTrue(builder.contains(wrapper(index)))
            }
        }
        repeat(times = elementsToAdd) { index ->
            val collisions = eGen.wrappersByHashCode(wrapper(index).hashCode)
            assertTrue(collisions.contains(wrapper(index)))

            if (!builder.contains(wrapper(index))) {
                for (wrapper in collisions) {
                    assertFalse(builder.contains(wrapper))
                }
            } else {
                for (wrapper in collisions) {
                    assertTrue(builder.contains(wrapper))

                    assertFalse(builder.remove(ElementWrapper(wrapper.element, Int.MIN_VALUE)))
                    assertTrue(builder.contains(wrapper))
                    assertTrue(builder.remove(wrapper))
                    assertFalse(builder.contains(wrapper))
                }
            }
        }
        assertTrue(builder.isEmpty())
    }

    @Test
    fun randomOperationsTests() {
        val mapGen = mutableListOf(List(20) { persistentHashSetOf<ElementWrapper<Int>>() })
        val expected = mutableListOf(List(20) { setOf<ElementWrapper<Int>>() })

        repeat(times = 10) {

            val random = Random()
            val builders = mapGen.last().map { it.builder() }
            val sets = builders.map { it.toMutableSet() }

            val operationCount = 200000
            val hashCodes = List(operationCount / 2) { random.nextInt() }
            repeat(times = operationCount) {
                val index = random.nextInt(sets.size)
                val set = sets[index]
                val builder = builders[index]

                val operationType = random.nextDouble()
                val hashCodeIndex = random.nextInt(hashCodes.size)
                val element = ElementWrapper(random.nextInt(), hashCodes[hashCodeIndex])

                val shouldRemove = operationType < 0.30
                when {
                    shouldRemove -> {
                        assertEquals(set.remove(element), builder.remove(element))
                    }
                    else -> {
                        assertEquals(set.add(element), builder.add(element))
                    }
                }

                assertEquals(set.size, builder.size)
                assertEquals(set.contains(element), builder.contains(element))
//                assertEquals(set.sorted(), builder.sorted())
            }

            mapGen.add( builders.map { it.build() } )
            expected.add( mapGen.last().map { it.toMutableSet() } )

            println(sets.maxBy { it.size }?.size)
        }

        mapGen.forEachIndexed { index, maps ->
            assertEquals(expected[index].map { it.sorted() }, maps.map { it.sorted() })
        }
    }
}