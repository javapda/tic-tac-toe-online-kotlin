package tictactoeonline.hyperskill

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.util.*

class PersonalizeYourStudyPlanTest {

    /**
     * https://hyperskill.org/diagnostics/step/4693
     * The minimum value of n numbers
     * Medium
     * 4011
     * 3 days ago
     * Write a program that finds the minimum value of N numbers.
     *
     * The first line contains the number N.
     * The other lines contain N numbers.
     * Output an integer number which is the minimum of N numbers.
     *
     * Sample Input 1:
     *
     * 5
     * 5
     * 1
     * 4
     * 2
     * 3
     *
     * Sample Output 1:
     *
     * 1
     */
    @ParameterizedTest
    @ValueSource(
        strings = ["5\n5\n1\n4\n2\n3",
            "5 5 1 4 2 3"
        ]
    )
    fun `The minimum value of N numbers`(input: String) {
        Scanner(input).use { scanner ->
            val numberOfNumbers = scanner.nextInt()
            val numbers = IntArray(numberOfNumbers) { scanner.nextInt() }
            println("MIN=${numbers.min()}")
        }
    }

    fun mySolutionToTheMinimumValueOfNNumbers() {
        IntArray(readln().toInt()) { readln().toInt() }.min().let(::println)
    }

}

fun Xmain() {
    PersonalizeYourStudyPlanTest().mySolutionToTheMinimumValueOfNNumbers()
}