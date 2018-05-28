package benchmarks.persistentHashSet

import benchmarks.*
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.implementations.immutableSet.ElementWrapper
import kotlinx.collections.immutable.implementations.immutableSet.persistentHashSetOf
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import java.util.*
import java.util.concurrent.TimeUnit

@Fork(1)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
open class Contains {
    @Param(BM_1, BM_10, BM_100, BM_1000, BM_10000, BM_100000, BM_1000000)
    var listSize: Int = 0

    @Param("persistentHashSet")
    var implementation = ""

    private val emptyMap = persistentHashSetOf<ElementWrapper<Int>>()
    private val random = Random(40)

    private val distinctElements = mutableListOf<ElementWrapper<Int>>()
    private val randomElements = mutableListOf<ElementWrapper<Int>>()
    private val collisionElements = mutableListOf<ElementWrapper<Int>>()

    private val anotherRandomElements = mutableListOf<ElementWrapper<Int>>()

    private var distinctSet = persistentHashSetOf<ElementWrapper<Int>>()
    private var randomSet = persistentHashSetOf<ElementWrapper<Int>>()
    private var collisionSet = persistentHashSetOf<ElementWrapper<Int>>()

    @Setup(Level.Trial)
    fun prepare() {
        if (implementation != "persistentHashSet") {
            throw AssertionError("Unknown implementation: $implementation")
        }

        distinctElements.clear()
        randomElements.clear()
        collisionElements.clear()
        anotherRandomElements.clear()
        repeat(times = this.listSize) { index ->
            distinctElements.add(ElementWrapper(index, index))
            randomElements.add(ElementWrapper(index, random.nextInt()))
            collisionElements.add(ElementWrapper(index, random.nextInt((listSize + 1) / 2)))
            anotherRandomElements.add(ElementWrapper(random.nextInt(), random.nextInt()))
        }

        distinctSet = this.emptyMap
        randomSet = this.emptyMap
        collisionSet = this.emptyMap
        repeat(times = this.listSize) { index ->
            distinctSet = distinctSet.add(distinctElements[index])
            randomSet = randomSet.add(randomElements[index])
            collisionSet = collisionSet.add(collisionElements[index])
        }
    }

    @Benchmark
    fun containsDistinct(bh: Blackhole): ImmutableSet<ElementWrapper<Int>> {
        val set = distinctSet
        repeat(times = this.listSize) { index ->
            bh.consume(set.contains(distinctElements[index]))
        }
        return set
    }

    @Benchmark
    fun containsRandom(bh: Blackhole): ImmutableSet<ElementWrapper<Int>> {
        val set = randomSet
        repeat(times = this.listSize) { index ->
            bh.consume(set.contains(randomElements[index]))
        }
        return set
    }

    @Benchmark
    fun containsCollision(bh: Blackhole): ImmutableSet<ElementWrapper<Int>> {
        val set = collisionSet
        repeat(times = this.listSize) { index ->
            bh.consume(set.contains(collisionElements[index]))
        }
        return set
    }

    @Benchmark
    fun containsNonExisting(bh: Blackhole): ImmutableSet<ElementWrapper<Int>> {
        val set = randomSet
        repeat(times = this.listSize) { index ->
            bh.consume(set.contains(anotherRandomElements[index]))
        }
        return set
    }
}