fun main() {
    val foo = Foo("str")
    print(foo.firstProperty + " = " + foo.secondProperty)
    true!!.and(false)
}

class Foo(name: String) {
    val firstProperty = "First property: $name".also(::println)

    init {
        println("First initializer block that prints $name")
    }

    val secondProperty = "Second property: ${name.length}".also(::println)

    init {
        println("Second initializer block that prints ${name.length}")
    }
}
