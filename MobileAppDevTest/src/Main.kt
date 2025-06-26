fun main() {
    var text : String? = null

    println(text?.length)


    fun <T> genericFunction(text: T){
        println(text)
    }

    genericFunction("hello")
    genericFunction(1234)
    genericFunction(1.286638)


}