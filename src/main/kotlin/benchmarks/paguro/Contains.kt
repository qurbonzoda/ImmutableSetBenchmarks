package benchmarks.paguro

import benchmarks.*
import kotlinx.collections.immutable.implementations.immutableSet.ElementWrapper
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import org.organicdesign.fp.collections.BaseSet
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

    @Param(PAGURO_HASH_SET, PAGURO_TREE_SET)
    var implementation = ""

    private val random = Random(40)

    private val distinctElements = mutableListOf<ElementWrapper<Int>>()
    private val randomElements = mutableListOf<ElementWrapper<Int>>()
    private val collisionElements = mutableListOf<ElementWrapper<Int>>()

    private val anotherRandomElements = mutableListOf<ElementWrapper<Int>>()

    private var distinctSet = emptyPSet<ElementWrapper<Int>>(PAGURO_HASH_SET)
    private var randomSet = emptyPSet<ElementWrapper<Int>>(PAGURO_HASH_SET)
    private var collisionSet = emptyPSet<ElementWrapper<Int>>(PAGURO_HASH_SET)

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
            distinctSet = distinctSet.put(distinctElements[index])
            randomSet = randomSet.put(randomElements[index])
            collisionSet = collisionSet.put(collisionElements[index])
        }
    }

    @Benchmark
    fun containsDistinct(bh: Blackhole): BaseSet<ElementWrapper<Int>> {
        val set = distinctSet
        repeat(times = this.listSize) { index ->
            bh.consume(set.contains(distinctElements[index]))
        }
        return set
    }

    @Benchmark
    fun containsRandom(bh: Blackhole): BaseSet<ElementWrapper<Int>> {
        val set = randomSet
        repeat(times = this.listSize) { index ->
            bh.consume(set.contains(randomElements[index]))
        }
        return set
    }

    @Benchmark
    fun containsCollision(bh: Blackhole): BaseSet<ElementWrapper<Int>> {
        val set = collisionSet
        repeat(times = this.listSize) { index ->
            bh.consume(set.contains(collisionElements[index]))
        }
        return set
    }

    @Benchmark
    fun containsNonExisting(bh: Blackhole): BaseSet<ElementWrapper<Int>> {
        val set = randomSet
        repeat(times = this.listSize) { index ->
            bh.consume(set.contains(anotherRandomElements[index]))
        }
        return set
    }
}