package benchmarks.builder

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

    @Param("builder")
    var implementation = ""

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
        if (implementation != "builder") {
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

        val emptySet = persistentHashSetOf<ElementWrapper<Int>>()
        distinctSet = emptySet
        randomSet = emptySet
        collisionSet = emptySet
        repeat(times = this.listSize) { index ->
            distinctSet = distinctSet.add(distinctElements[index])
            randomSet = randomSet.add(randomElements[index])
            collisionSet = collisionSet.add(collisionElements[index])
        }
    }

    @Benchmark
    fun containsDistinct(bh: Blackhole): ImmutableSet.Builder<ElementWrapper<Int>> {
        val builder = distinctSet.builder()
        repeat(times = this.listSize) { index ->
            bh.consume(builder.contains(distinctElements[index]))
        }
        return builder
    }

    @Benchmark
    fun containsRandom(bh: Blackhole): ImmutableSet.Builder<ElementWrapper<Int>> {
        val builder = randomSet.builder()
        repeat(times = this.listSize) { index ->
            bh.consume(builder.contains(randomElements[index]))
        }
        return builder
    }

    @Benchmark
    fun containsCollision(bh: Blackhole): ImmutableSet.Builder<ElementWrapper<Int>> {
        val builder = collisionSet.builder()
        repeat(times = this.listSize) { index ->
            bh.consume(builder.contains(collisionElements[index]))
        }
        return builder
    }

    @Benchmark
    fun containsNonExisting(bh: Blackhole): ImmutableSet.Builder<ElementWrapper<Int>> {
        val builder = randomSet.builder()
        repeat(times = this.listSize) { index ->
            bh.consume(builder.contains(anotherRandomElements[index]))
        }
        return builder
    }
}