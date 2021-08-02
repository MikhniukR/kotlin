// IGNORE_BACKEND_FIR: JVM_IR
val xs = listOf<Any>()

fun box(): String {
    val s = StringBuilder()
    for ((index, x) in xs.withIndex()) {
        return "Loop over empty array should not be executed"
    }
    return "OK"
}

// 0 withIndex
// 1 iterator
// 1 hasNext
// 1 next
// 0 component1
// 0 component2

// JVM_TEMPLATES
// - fake variable for @InlineOnly 'listOf';
// - Initializing the index in the lowered for-loop.
// 2 ICONST_0

// JVM_IR_TEMPLATES
// - Initializing the index in the lowered for-loop.
// 1 ICONST_0
