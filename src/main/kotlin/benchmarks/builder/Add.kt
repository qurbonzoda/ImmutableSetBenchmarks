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
open class Add {
    @Param(BM_1, BM_10, BM_100, BM_1000, BM_10000, BM_100000, BM_1000000)
    var listSize: Int = 0

    @Param("builder")
    var implementation = ""

    private val emptySet: ImmutableSet<ElementWrapper<Int>> = persistentHashSetOf()
    private val random = Random(40)

    private val distinctKeys = mutableListOf<ElementWrapper<Int>>()
    private var randomKeys = mutableListOf<ElementWrapper<Int>>()
    private var collisionKeys = mutableListOf<ElementWrapper<Int>>()

    @Setup(Level.Trial)
    fun prepare() {
        if (implementation != "builder") {
            throw AssertionError("Unknown implementation: $implementation")
        }

        distinctKeys.clear()
        randomKeys.clear()
        collisionKeys.clear()
        repeat(times = listSize) { index ->
            distinctKeys.add(ElementWrapper(index, index))
            randomKeys.add(ElementWrapper(index, random.nextInt()))
            collisionKeys.add(ElementWrapper(index, random.nextInt((listSize + 1) / 2)))
        }
    }

    @Benchmark
    fun addDistinct(): ImmutableSet.Builder<ElementWrapper<Int>> {
        val builder = this.emptySet.builder()
        repeat(times = this.listSize) { index ->
            builder.add(distinctKeys[index])
        }
        return builder
    }

    @Benchmark
    fun addRandom(): ImmutableSet.Builder<ElementWrapper<Int>> {
        val builder = this.emptySet.builder()
        repeat(times = this.listSize) { index ->
            builder.add(randomKeys[index])
        }
        return builder
    }

    @Benchmark
    fun addCollision(): ImmutableSet.Builder<ElementWrapper<Int>> {
        val builder = this.emptySet.builder()
        repeat(times = this.listSize) { index ->
            builder.add(collisionKeys[index])
        }
        return builder
    }

//    @Benchmark
//    fun addAndContainsDistinct(bh: Blackhole): ImmutableSet.Builder<ElementWrapper<Int>> {
//        val builder = this.emptySet.builder()
//        repeat(times = this.listSize) { index ->
//            builder.add(distinctKeys[index])
//        }
//        repeat(times = this.listSize) { index ->
//            bh.consume(builder.contains(distinctKeys[index]))
//        }
//        return builder
//    }
//
//    @Benchmark
//    fun addAndContainsRandom(bh: Blackhole): ImmutableSet.Builder<ElementWrapper<Int>> {
//        val builder = this.emptySet.builder()
//        repeat(times = this.listSize) { index ->
//            builder.add(randomKeys[index])
//        }
//        repeat(times = this.listSize) { index ->
//            bh.consume(builder.contains(randomKeys[index]))
//        }
//        return builder
//    }
//
//    @Benchmark
//    fun addAndContainsCollision(bh: Blackhole): ImmutableSet.Builder<ElementWrapper<Int>> {
//        val builder = this.emptySet.builder()
//        repeat(times = this.listSize) { index ->
//            builder.add(collisionKeys[index])
//        }
//        repeat(times = this.listSize) { index ->
//            bh.consume(builder.contains(collisionKeys[index]))
//        }
//        return builder
//    }
}