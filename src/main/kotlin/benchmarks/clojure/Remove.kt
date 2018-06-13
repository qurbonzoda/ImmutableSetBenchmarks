package benchmarks.clojure

import benchmarks.*
import clojure.lang.IPersistentCollection
import clojure.lang.IPersistentSet
import clojure.lang.PersistentHashSet
import kotlinx.collections.immutable.implementations.immutableSet.ElementWrapper
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

    @Param("clojure")
    var implementation = ""

    private val random = Random(40)

    private val randomElements = mutableListOf<ElementWrapper<Int>>()

    private var randomSet: IPersistentCollection = PersistentHashSet.EMPTY

    @Setup(Level.Trial)
    fun prepare() {
        if (implementation != "clojure") {
            throw AssertionError("Unknown implementation: $implementation")
        }

        randomElements.clear()
        repeat(times = this.listSize) { index ->
            randomElements.add(ElementWrapper(index, random.nextInt()))
        }

        randomSet = PersistentHashSet.EMPTY
        repeat(times = this.listSize) { index ->
            randomSet = randomSet.cons(randomElements[index])
        }
    }

    @Benchmark
    fun removeRandom(): IPersistentSet {
        var set: IPersistentSet = randomSet as PersistentHashSet
        repeat(times = this.listSize) { index ->
            set = set.disjoin(randomElements[index])
        }
        return set
    }
}