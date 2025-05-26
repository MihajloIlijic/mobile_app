package at.fhjoanneum.multiplechoicequiz

/*
Main.kt
Description: Randomly pulling question objects from the pool then presenting them to the user for input and checking correctness.
Author: Mihajlo Ilijic
Last Change: 12.05.2025 by Mihajlo Ilijic
*/

fun main() {
    val numberOfQuestions = 3
    var numberOfCorrectAnswers = 0
    val pool = setupPool()
    val quiz = pool.shuffled().take(numberOfQuestions)

    val welcomeText = "Geben Sie die zutreffende Antwort als Zahl an. \n" +
            "Wenn mehrere Antworten zutreffen, geben Sie \n" +
            "bitte alle Antworten in einer Zeile an und \n" +
            "trennen diese durch Kommas. z.B. '2,4'. \n" +
            "Quiz mit $numberOfQuestions Fragen ..."
    println(welcomeText)

    for (q in quiz) {
        //Antworten neu mischen
        val question = Question(q.txt, q.choices.shuffled())
        println()
        question.show()
        print("Ihre Antwort: ")
        val answer = readLine()!!

        if (question.analyzeAnswer(answer)) {
            println("Korrekt, super!")
            numberOfCorrectAnswers++
        } else {
            println("Sorry, das war falsch. Korrekt wäre:")
            question.showCorrectAnswers()
        }
    }
    println()
    println("Sie haben $numberOfCorrectAnswers von $numberOfQuestions Fragen richtig beantwortet.")
}

fun setupPool(): List<Question> = listOf(
    Question(
        "Was ist die Hauptstadt von Frankreich?",
        listOf(
            Choice("Paris", true),
            Choice("Belgrad"),
            Choice("Madrid"),
            Choice("Wien")
        )
    ),
    Question(
        "Wie viele Beine hat eine Spinne?",
        listOf(
            Choice("8", true),
            Choice("2"),
            Choice("3"),
            Choice("4"),
        )
    ),
    Question(
        "In welcher Programmiersprache ist dieser Quiz implementiert",
        listOf(
            Choice("Java Script"),
            Choice("Kotlin", true),
            Choice("Ruby"),
            Choice("Python"),

            )
    ),
    Question(
        "Welche der folgenden sind Programmiersprachen?",
        listOf(
            Choice("Kotlin", true),
            Choice("Python", true),
            Choice("HTML"),
            Choice("Java", true)
        )
    ),
    Question(
        "Wählen Sie alle geraden Zahlen aus:",
        listOf(
            Choice("1"),
            Choice("2", true),
            Choice("3"),
            Choice("4", true)
        )
    )
)