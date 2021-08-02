// WITH_RUNTIME

fun test() = run {
    var tmp = 0
    "OK"
}

// 2 LOCALVARIABLE
// 1 LOCALVARIABLE tmp I
// 1 LOCALVARIABLE \$i\$a\$-run-NoFakeVariableForInlineOnlyFunWithLambdaKt\$test\$1 I

// JVM_TEMPLATES
// 0 LDC 0
// 4 ICONST_0
// 4 ISTORE

// JVM_IR_TEMPLATES
// 0 LDC 0
// 2 ICONST_0
// 2 ISTORE
