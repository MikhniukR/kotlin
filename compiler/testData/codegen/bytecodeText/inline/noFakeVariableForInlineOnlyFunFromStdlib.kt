// WITH_RUNTIME

import kotlin.math.sin

fun test(x: Double) = sin(x)

// JVM_TEMPLATES
// 0 LDC 0
// 1 ICONST_0
// 1 ISTORE

// JVM_IR_TEMPLATES
// 0 LDC 0
// 0 ICONST_0
// 0 ISTORE
