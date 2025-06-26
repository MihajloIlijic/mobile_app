fun main() {
    var listA = mutableSetOf(1,2,3,4,5)
    var listB = listA.add(6)
    println(listB)
    println(listA)


    var something : Any = 4;
    when (something) {
        1 -> println("var is 1")
        2 -> println("var is 2")
        in 3..5 -> println("var is between 3 and 5")
        else -> println("else")
    }
    if (something == 3..10) {
        println(3..10)
    }
    fun <T> printGeneric(item : T) {
        println(item)
    }
    printGeneric(5)
    printGeneric("12")



}