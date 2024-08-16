package tictactoeonline.domaintests

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import tictactoeonline.domain.Player

class PlayerTest {

    @Test
    fun getMarker() {
        assertEquals("X", Player("Jed", 'X').marker.toString())
        assertEquals("Jed", Player("Jed", 'X').name)
    }
}