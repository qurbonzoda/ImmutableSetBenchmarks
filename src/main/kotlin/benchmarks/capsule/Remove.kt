package benchmarks.capsule

import benchmarks.*
import org.openjdk.jmh.annotations.*
import io.usethesource.capsule.Set
import kotlinx.collections.immutable.implementations.immutableSet.ElementWrapper
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

    @Param(CAPSULE_TRIE_SET)
    var implementation = ""

    private val random = Random(40)

    private val distinctElements = mutableListOf<ElementWrapper<Int>>()
    private val randomElements = mutableListOf<ElementWrapper<Int>>()
    private val collisionElements = mutableListOf<ElementWrapper<Int>>()

    private val anotherRandomElements = mutableListOf<ElementWrapper<Int>>()

    private var distinctSet = emptyPSet<ElementWrapper<Int>>(CAPSULE_TRIE_SET)
    private var randomSet = emptyPSet<ElementWrapper<Int>>(CAPSULE_TRIE_SET)
    private var collisionSet = emptyPSet<ElementWrapper<Int>>(CAPSULE_TRIE_SET)

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
            distinctSet = distinctSet.__insert(distinctElements[index])
            randomSet = randomSet.__insert(randomElements[index])
            collisionSet = collisionSet.__insert(collisionElements[index])
        }
    }

    @Benchmark
    fun removeDistinct(): Set.Immutable<ElementWrapper<Int>> {
        var set = distinctSet
        repeat(times = this.listSize) { index ->
            set = set.__remove(distinctElements[index])
        }
        return set
    }

    @Benchmark
    fun removeRandom(): Set.Immutable<ElementWrapper<Int>> {
        var set = randomSet
        repeat(times = this.listSize) { index ->
            set = set.__remove(randomElements[index])
        }
        return set
    }

    @Benchmark
    fun removeCollision(): Set.Immutable<ElementWrapper<Int>> {
        var set = collisionSet
        repeat(times = this.listSize) { index ->
            set = set.__remove(collisionElements[index])
        }
        return set
    }

    @Benchmark
    fun removeNonExisting(): Set.Immutable<ElementWrapper<Int>> {
        var set = randomSet
        repeat(times = this.listSize) { index ->
            set = set.__remove(anotherRandomElements[index])
        }
        return set
    }
}