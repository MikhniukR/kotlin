fun main() {
    print("\r\n")
    val foo = Foo("str")
    print(foo.firstProperty + " = " + foo.secondProperty)
    tryFunctionInAnotherFile(foo.firstProperty + " = " + foo.secondProperty)
    tryFunctionInAnotherFile(123)
    testHover("test")
    testHoverWithReturnType(12)
    testGeneric("generics", 12)
    true!!.and(false)
}

class Foo(name: String) {
    val firstProperty = "First property: $name".also(::println)

    init {
        println("First initializer block that prints ${name}")
    }

    val secondProperty = "Second property: ${name.length}".also(::println)

    init {
        println("Second initializer block that prints ${name.length}")
    }
}

fun testHover(s: String) {
    val ss = s + s
    println(ss)
}

fun testHoverWithReturnType(x: Int): String {
    return x
}

fun <T: Comparable<T>, S> testGeneric(t: T, s: S) {
}
