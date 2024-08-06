package tictactoeonline

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VirtualPlayingGridTest {
    lateinit var grid: PlayingGrid

    @BeforeEach
    fun setup() {
        grid = PlayingGrid("5x4")
        assertEquals(5, grid.height)
        assertEquals(4, grid.width)
        assertEquals(6, grid.virtualPlayingGrids.size)
    }



    @Test
    fun `test isWinner - yes`() {
        val vpg = VirtualPlayingGrid(CellLocation(0, 1, grid))
        assertTrue(vpg.isWinner(PlayerCounts(1, 1), PlayerCounts(2, 3), PlayerCounts(0, 1)))
    }

    @Test
    fun `get winning player`() {
        val vpg = VirtualPlayingGrid(CellLocation(0, 1, grid))
        assertEquals(2,vpg.winningPlayer(PlayerCounts(1, 1), PlayerCounts(2, 3), PlayerCounts(0, 1)))
        assertEquals(1,vpg.winningPlayer(PlayerCounts(3, 1), PlayerCounts(2, 0), PlayerCounts(0, 1)))

    }

    @Test
    fun `test isWinner - no`() {
        val vpg = VirtualPlayingGrid(CellLocation(0, 1, grid))
        assertFalse(vpg.isWinner(PlayerCounts(1, 1), PlayerCounts(2, 1), PlayerCounts(0, 1)))
    }

    @ParameterizedTest
    @ValueSource(strings = ["0,0", "1,1", "2,2"])
    fun onDiagonal(xy: String) {
        val (x, y) = xy.split(",").map(String::toInt)
        val vpg = VirtualPlayingGrid(CellLocation(0, 1, grid))
        assertTrue(vpg.onDiagonal(CellLocation(x, y, grid)))
    }

    @ParameterizedTest
    @ValueSource(strings = ["2,0", "1,1", "0,2"])
    fun onAntiDiagonal(xy: String) {
        val (x, y) = xy.split(",").map(String::toInt)
        val vpg = VirtualPlayingGrid(CellLocation(0, 1, grid))
        assertTrue(vpg.onAntiDiagonal(CellLocation(x, y, grid)))
    }

    @ParameterizedTest
    @ValueSource(strings = ["2,1", "2,2", "2,3", "3,1", "3,2", "3,3", "4,1", "4,2", "4,3"])
    fun `valid cellLocationLocal`(xy: String) {
        val (x, y) = xy.split(",").map(String::toInt)
        grid = PlayingGrid("5x4")
        val cellLocation = CellLocation(x, y, grid)
        assertTrue(
            VirtualPlayingGrid(CellLocation(2, 1, grid))
                .isCellLocationLocal(cellLocation)
        )
    }

    @ParameterizedTest
    @ValueSource(strings = ["1,1", "1,2", "1,3"])
    fun `invalid cellLocationLocal`(xy: String) {
        val (x, y) = xy.split(",").map(String::toInt)
        grid = PlayingGrid("5x4")
        val cellLocation = CellLocation(x, y, grid)
        assertFalse(
            VirtualPlayingGrid(CellLocation(2, 1, grid))
                .isCellLocationLocal(cellLocation)
        )
    }

    @Test
    fun testIt() {

        val vpg = VirtualPlayingGrid(CellLocation(0, 1, grid))
        assertEquals(9, vpg.cellLocations().size)
        vpg.cellLocations().forEach(::println)
    }

}