package benchmarks.builder

import benchmarks.*
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.implementations.immutableSet.ElementWrapper
import kotlinx.collections.immutable.implementations.immutableSet.persistentHashSetOf
import org.openjdk.jmh.annotations.*
import java.util.*
import java.util.concurrent.TimeUnit

@Fork(1)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
open class Remove {
    @Param(BM_1, BM_10, BM_100, BM_1000, BM_10000, BM_100000, BM_1000000)
    var listSize: Int = 0

    @Param("builder")
    var implementation = ""

    private val emptySet = persistentHashSetOf<ElementWrapper<Int>>()
    private val random = Random(40)

    private val distinctElements = mutableListOf<ElementWrapper<Int>>()
    private val randomElements = mutableListOf<ElementWrapper<Int>>()
    private val collisionElements = mutableListOf<ElementWrapper<Int>>()

    private val anotherRandomElements = mutableListOf<ElementWrapper<Int>>()

    private var distinctElementsSet = persistentHashSetOf<ElementWrapper<Int>>()
    private var randomElementsSet = persistentHashSetOf<ElementWrapper<Int>>()
    private var collisionElementsSet = persistentHashSetOf<ElementWrapper<Int>>()

    @Setup(Level.Trial)
    fun prepare() {
        if (implementation != "builder") {
            throw AssertionError("Unknown implementation: $implementation")
        }

        distinctElements.clear()
        randomElements.clear()
        collisionElements.clear()
        anotherRandomElements.clear()
        repeat(times = listSize) { index ->
            distinctElements.add(ElementWrapper(index, index))
            randomElements.add(ElementWrapper(index, random.nextInt()))
            collisionElements.add(ElementWrapper(index, random.nextInt((listSize + 1) / 2)))
            anotherRandomElements.add(ElementWrapper(random.nextInt(), random.nextInt()))
        }

        distinctElementsSet = this.emptySet
        randomElementsSet = this.emptySet
        collisionElementsSet = this.emptySet
        repeat(times = this.listSize) { index ->
            distinctElementsSet = distinctElementsSet.add(distinctElements[index])
            randomElementsSet = randomElementsSet.add(randomElements[index])
            collisionElementsSet = collisionElementsSet.add(collisionElements[index])
        }
    }

    @Benchmark
    fun removeDistinct(): ImmutableSet.Builder<ElementWrapper<Int>> {
        val builder = distinctElementsSet.builder()
        repeat(times = this.listSize) { index ->
            builder.remove(distinctElements[index])
        }
        return builder
    }

    @Benchmark
    fun removeRandom(): ImmutableSet.Builder<ElementWrapper<Int>> {
        val builder = randomElementsSet.builder()
        repeat(times = this.listSize) { index ->
            builder.remove(randomElements[index])
        }
        return builder
    }

    @Benchmark
    fun removeCollision(): ImmutableSet.Builder<ElementWrapper<Int>> {
        val builder = collisionElementsSet.builder()
        repeat(times = this.listSize) { index ->
            builder.remove(collisionElements[index])
        }
        return builder
    }

    @Benchmark
    fun removeNonExisting(): ImmutableSet.Builder<ElementWrapper<Int>> {
        val builder = randomElementsSet.builder()
        repeat(times = this.listSize) { index ->
            builder.remove(anotherRandomElements[index])
        }
        return builder
    }
}