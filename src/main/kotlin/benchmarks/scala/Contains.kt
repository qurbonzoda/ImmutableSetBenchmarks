package benchmarks.scala

import benchmarks.*
import kotlinx.collections.immutable.implementations.immutableSet.ElementWrapper
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import scala.collection.immutable.HashSet
import scala.collection.immutable.`HashSet$`
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

    @Param("scala")
    var implementation = ""

    private val random = Random(40)

    private val randomElements = mutableListOf<ElementWrapper<Int>>()

    private var randomSet = `HashSet$`.`MODULE$`.empty<ElementWrapper<Int>>() as HashSet<ElementWrapper<Int>>

    @Setup(Level.Trial)
    fun prepare() {
        if (implementation != "scala") {
            throw AssertionError("Unknown implementation: $implementation")
        }

        randomElements.clear()
        repeat(times = this.listSize) { index ->
            randomElements.add(ElementWrapper(index, random.nextInt()))
        }

        randomSet = `HashSet$`.`MODULE$`.empty<ElementWrapper<Int>>() as HashSet<ElementWrapper<Int>>
        repeat(times = this.listSize) { index ->
            randomSet = randomSet.`$plus`(randomElements[index])
        }
    }

    @Benchmark
    fun containsRandom(bh: Blackhole): HashSet<ElementWrapper<Int>> {
        val set = randomSet
        repeat(times = this.listSize) { index ->
            bh.consume(set.contains(randomElements[index]))
        }
        return set
    }
}