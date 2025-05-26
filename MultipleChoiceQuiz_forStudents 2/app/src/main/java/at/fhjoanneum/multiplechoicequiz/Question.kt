package at.fhjoanneum.multiplechoicequiz

/*
Question.kt
Description: representing a quiz question object with several answers
Author: Mihajlo Ilijic
Last Change: 12.05.2025 by Mihajlo Ilijic
*/

class Question(val txt: String, val choices: List<Choice>) {

    fun show() {
        println(txt)
        choices.forEachIndexed { index, choice ->
            println("${index + 1}) ${choice.txt}")
        }
    }

    fun showCorrectAnswers() {
        choices.filter { it.ok }
            .forEach { println("  (*) ${it.txt}") }
    }

    fun analyzeAnswer(answer: String): Boolean {
        val given = answer
            .split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .toSet()

        val correct = choices
            .mapIndexedNotNull { i, c -> if (c.ok) i + 1 else null }
            .toSet()

        return given == correct && given.isNotEmpty()
    }
}

