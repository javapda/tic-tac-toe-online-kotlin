package com.javapda.tictactoeonline

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class TicTacToeOnlineTest {

    @ParameterizedTest
    @ValueSource(strings = ["(2,3)"])
    fun isValidMoveFormat(moveCandidate: String) {
        assertTrue(TicTacToeOnline().isValidMoveFormat(moveCandidate))
    }
    @ParameterizedTest
    @ValueSource(strings = ["(2,3):2:3","(4 , 5):4:5"])
    fun parseMoveFormat(data: String) {
        val (moveCandidate,expectedX,expectedY) = data.split(":")
        assertTrue(TicTacToeOnline().isValidMoveFormat(moveCandidate))
        val (x,y) = TicTacToeOnline().parseMoveFormat(moveCandidate)
        assertEquals(expectedX,x.toString())
        assertEquals(expectedY,y.toString())
    }
}