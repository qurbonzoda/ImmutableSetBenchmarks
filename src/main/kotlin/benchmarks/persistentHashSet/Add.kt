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
open class Add {
    @Param(BM_1, BM_10, BM_100, BM_1000, BM_10000, BM_100000, BM_1000000)
    var listSize: Int = 0

    @Param("persistentHashSet")
    var implementation = ""

    private val emptySet: ImmutableSet<ElementWrapper<Int>> = persistentHashSetOf()
    private val random = Random(40)

    private val distinctElements = mutableListOf<ElementWrapper<Int>>()
    private var randomElements = mutableListOf<ElementWrapper<Int>>()
    private var collisionElements = mutableListOf<ElementWrapper<Int>>()

    @Setup(Level.Trial)
    fun prepare() {
        if (implementation != "persistentHashSet") {
            throw AssertionError("Unknown implementation: $implementation")
        }

        distinctElements.clear()
        randomElements.clear()
        collisionElements.clear()
        repeat(times = listSize) { index ->
            distinctElements.add(ElementWrapper(index, index))
            randomElements.add(ElementWrapper(index, random.nextInt()))
            collisionElements.add(ElementWrapper(index, random.nextInt((listSize + 1) / 2)))
        }
    }

    @Benchmark
    fun addDistinct(): ImmutableSet<ElementWrapper<Int>> {
        var set = this.emptySet
        repeat(times = this.listSize) { index ->
            set = set.add(distinctElements[index])
        }
        return set
    }

    @Benchmark
    fun addRandom(): ImmutableSet<ElementWrapper<Int>> {
        var set = this.emptySet
        repeat(times = this.listSize) { index ->
            set = set.add(randomElements[index])
        }
        return set
    }

    @Benchmark
    fun addCollision(): ImmutableSet<ElementWrapper<Int>> {
        var set = this.emptySet
        repeat(times = this.listSize) { index ->
            set = set.add(collisionElements[index])
        }
        return set
    }

//    @Benchmark
//    fun addAndContainsDistinct(bh: Blackhole): ImmutableSet<ElementWrapper<Int>> {
//        var set = this.emptySet
//        repeat(times = this.listSize) { index ->
//            set = set.add(distinctElements[index])
//        }
//        repeat(times = this.listSize) { index ->
//            bh.consume(set.contains(distinctElements[index]))
//        }
//        return set
//    }
//
//    @Benchmark
//    fun addAndContainsRandom(bh: Blackhole): ImmutableSet<ElementWrapper<Int>> {
//        var set = this.emptySet
//        repeat(times = this.listSize) { index ->
//            set = set.add(randomElements[index])
//        }
//        repeat(times = this.listSize) { index ->
//            bh.consume(set.contains(randomElements[index]))
//        }
//        return set
//    }
//
//    @Benchmark
//    fun addAndContainsCollision(bh: Blackhole): ImmutableSet<ElementWrapper<Int>> {
//        var set = this.emptySet
//        repeat(times = this.listSize) { index ->
//            set = set.add(collisionElements[index])
//        }
//        repeat(times = this.listSize) { index ->
//            bh.consume(set.contains(collisionElements[index]))
//        }
//        return set
//    }
}