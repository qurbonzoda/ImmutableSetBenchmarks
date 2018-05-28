package benchmarks.capsule

import io.usethesource.capsule.Set

const val CAPSULE_TRIE_SET        = "CAPSULE_TRIE_SET"

fun <E: Comparable<E>> emptyPSet(implementation: String): Set.Immutable<E> {
    return when(implementation) {
        CAPSULE_TRIE_SET          -> io.usethesource.capsule.core.PersistentTrieSet.of()
        else -> throw AssertionError("Unknown implementation: $implementation")
    }
}