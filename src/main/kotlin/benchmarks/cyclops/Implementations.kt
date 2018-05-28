package benchmarks.cyclops

//import com.aol.cyclops.clojure.collections.ClojureHashPSet
import com.aol.cyclops.javaslang.collections.JavaSlangPSet
import com.aol.cyclops.scala.collections.ScalaHashPSet
import com.aol.cyclops.scala.collections.ScalaTreePOrderedSet
import org.pcollections.PSet

//const val CLOJURE_HASH_SET      = "CLOJURE_HASH_SET"
const val SCALA_HASH_SET        = "SCALA_HASH_SET"
const val SCALA_TREE_SET        = "SCALA_TREE_SET"
const val JAVASLANG_SET         = "JAVASLANG_SET"
const val PCOLLECTIONS          = "PCOLLECTIONS"

fun <E: Comparable<E>> emptyPSet(implementation: String): PSet<E> {
    return when(implementation) {
//        CLOJURE_HASH_SET        -> ClojureHashPSet.empty()
        SCALA_HASH_SET          -> ScalaHashPSet.empty()
        SCALA_TREE_SET          -> ScalaTreePOrderedSet.empty()
        JAVASLANG_SET           -> JavaSlangPSet.emptyPSet()
        PCOLLECTIONS            -> org.pcollections.HashTreePSet.empty()
        else -> throw AssertionError("Unknown implementation: $implementation")
    }
}