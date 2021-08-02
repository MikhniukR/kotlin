fun box(): String {
    run {
        run {
            var x1 = 0
            run { ++x1 }
            if (x1 == 0) return "fail"
        }

        run {
            var x2 = 0
            run { x2++ }
            if (x2 == 0) return "fail"
        }
    }

    return "OK"
}


// Temporary variable for 'x2++' + store to fake variable marking the outer `run`:
// 2 ISTORE 1

// 0 NEW
// 0 GETFIELD
// 0 PUTFIELD

// JVM_TEMPLATES
// Shared variable slots (x1, x2):
// 4 ILOAD 6
// 4 ISTORE 6

// JVM_IR_TEMPLATES
// No fake variables for @InlineOnly functions
// 0 ILOAD 6
// 0 ISTORE 6
