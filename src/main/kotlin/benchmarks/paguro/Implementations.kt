package benchmarks.paguro

import org.organicdesign.fp.collections.BaseSet

const val PAGURO_HASH_SET        = "PAGURO_HASH_SET"
const val PAGURO_TREE_SET        = "PAGURO_TREE_SET"

fun <E: Comparable<E>> emptyPSet(implementation: String): BaseSet<E> {
    return when(implementation) {
        PAGURO_HASH_SET          -> org.organicdesign.fp.collections.PersistentHashSet.empty()
        PAGURO_TREE_SET          -> org.organicdesign.fp.collections.PersistentTreeSet.empty()
        else -> throw AssertionError("Unknown implementation: $implementation")
    }
}