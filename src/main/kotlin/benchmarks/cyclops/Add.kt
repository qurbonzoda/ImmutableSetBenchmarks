package benchmarks.cyclops

import benchmarks.*
import kotlinx.collections.immutable.implementations.immutableSet.ElementWrapper
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import org.pcollections.PSet
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

    @Param(SCALA_HASH_SET, SCALA_TREE_SET,
            JAVASLANG_SET, PCOLLECTIONS)
    var implementation = ""

    private var emptySet = emptyPSet<ElementWrapper<Int>>(SCALA_HASH_SET)
    private val random = Random(40)

    private val distinctElements = mutableListOf<ElementWrapper<Int>>()
    private var randomElements = mutableListOf<ElementWrapper<Int>>()
    private var collisionElements = mutableListOf<ElementWrapper<Int>>()

    @Setup(Level.Trial)
    fun prepare() {
        emptySet = emptyPSet(implementation)

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
    fun addDistinct(): PSet<ElementWrapper<Int>> {
        var set = this.emptySet
        repeat(times = this.listSize) { index ->
            set = set.plus(distinctElements[index])
        }
        return set
    }

    @Benchmark
    fun addRandom(): PSet<ElementWrapper<Int>> {
        var set = this.emptySet
        repeat(times = this.listSize) { index ->
            set = set.plus(randomElements[index])
        }
        return set
    }

    @Benchmark
    fun addCollision(): PSet<ElementWrapper<Int>> {
        var set = this.emptySet
        repeat(times = this.listSize) { index ->
            set = set.plus(collisionElements[index])
        }
        return set
    }

//    @Benchmark
//    fun addAndContainsDistinct(bh: Blackhole): PSet<ElementWrapper<Int>> {
//        var set = this.emptySet
//        repeat(times = this.listSize) { index ->
//            set = set.plus(distinctElements[index])
//        }
//        repeat(times = this.listSize) { index ->
//            bh.consume(set.contains(distinctElements[index]))
//        }
//        return set
//    }
//
//    @Benchmark
//    fun addAndContainsRandom(bh: Blackhole): PSet<ElementWrapper<Int>> {
//        var set = this.emptySet
//        repeat(times = this.listSize) { index ->
//            set = set.plus(randomElements[index])
//        }
//        repeat(times = this.listSize) { index ->
//            bh.consume(set.contains(randomElements[index]))
//        }
//        return set
//    }
//
//    @Benchmark
//    fun addAndContainsCollision(bh: Blackhole): PSet<ElementWrapper<Int>> {
//        var set = this.emptySet
//        repeat(times = this.listSize) { index ->
//            set = set.plus(collisionElements[index])
//        }
//        repeat(times = this.listSize) { index ->
//            bh.consume(set.contains(collisionElements[index]))
//        }
//        return set
//    }
}

