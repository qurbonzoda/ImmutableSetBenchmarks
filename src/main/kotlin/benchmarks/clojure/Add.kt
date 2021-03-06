package benchmarks.clojure

import benchmarks.*
import clojure.lang.IPersistentCollection
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
open class Add {
    @Param(BM_1, BM_10, BM_100, BM_1000, BM_10000, BM_100000, BM_1000000)
    var listSize: Int = 0

    @Param("clojure")
    var implementation = ""

    private val random = Random(40)

    private var randomElements = mutableListOf<ElementWrapper<Int>>()

    @Setup(Level.Trial)
    fun prepare() {
        if (implementation != "clojure") {
            throw AssertionError("Unknown implementation: $implementation")
        }

        randomElements.clear()
        repeat(times = listSize) { index ->
            randomElements.add(ElementWrapper(index, random.nextInt()))
        }
    }

    @Benchmark
    fun addRandom(): IPersistentCollection {
        var set: IPersistentCollection = PersistentHashSet.EMPTY
        repeat(times = this.listSize) { index ->
            set = set.cons(randomElements[index])
        }
        return set
    }
}