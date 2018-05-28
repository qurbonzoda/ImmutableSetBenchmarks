package benchmarks.cyclops

import benchmarks.*
import kotlinx.collections.immutable.implementations.immutableSet.ElementWrapper
import org.openjdk.jmh.annotations.*
import org.pcollections.PSet
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

    @Param(SCALA_HASH_SET, SCALA_TREE_SET,
            JAVASLANG_SET, PCOLLECTIONS)
    var implementation = ""

    private val random = Random(40)

    private val distinctElements = mutableListOf<ElementWrapper<Int>>()
    private val randomElements = mutableListOf<ElementWrapper<Int>>()
    private val collisionElements = mutableListOf<ElementWrapper<Int>>()

    private val anotherRandomElements = mutableListOf<ElementWrapper<Int>>()

    private var distinctSet = emptyPSet<ElementWrapper<Int>>(SCALA_HASH_SET)
    private var randomSet = emptyPSet<ElementWrapper<Int>>(SCALA_HASH_SET)
    private var collisionSet = emptyPSet<ElementWrapper<Int>>(SCALA_HASH_SET)

    @Setup(Level.Trial)
    fun prepare() {
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

        val emptySet = emptyPSet<ElementWrapper<Int>>(implementation)
        distinctSet = emptySet
        randomSet = emptySet
        collisionSet = emptySet
        repeat(times = this.listSize) { index ->
            distinctSet = distinctSet.plus(distinctElements[index])
            randomSet = randomSet.plus(randomElements[index])
            collisionSet = collisionSet.plus(collisionElements[index])
        }
    }

    @Benchmark
    fun removeDistinct(): PSet<ElementWrapper<Int>> {
        var set = distinctSet
        repeat(times = this.listSize) { index ->
            set = set.minus(distinctElements[index])
        }
        return set
    }

    @Benchmark
    fun removeRandom(): PSet<ElementWrapper<Int>> {
        var set = randomSet
        repeat(times = this.listSize) { index ->
            set = set.minus(randomElements[index])
        }
        return set
    }

    @Benchmark
    fun removeCollision(): PSet<ElementWrapper<Int>> {
        var set = collisionSet
        repeat(times = this.listSize) { index ->
            set = set.minus(collisionElements[index])
        }
        return set
    }

    @Benchmark
    fun removeNonExisting(): PSet<ElementWrapper<Int>> {
        var set = randomSet
        repeat(times = this.listSize) { index ->
            set = set.minus(anotherRandomElements[index])
        }
        return set
    }
}