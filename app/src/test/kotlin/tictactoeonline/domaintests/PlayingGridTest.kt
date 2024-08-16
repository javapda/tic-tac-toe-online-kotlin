package tictactoeonline.domaintests

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import tictactoeonline.domain.CellLocation
import tictactoeonline.domain.PlayingGrid

class PlayingGridTest {


    @Test
    fun renderCell() {
        assertEquals("---", field.renderCell('-'))
        assertEquals(" X ", field.renderCell('X'))
        assertEquals("   ", field.renderCell())
    }

    @Test
    fun gridSeparatorLine() {
        assertEquals("|---|---|---|", field.gridSeparatorLine())
        assertEquals("|---|---|---|-y", field.gridSeparatorLine(suffix = "-y"))
    }

    @Test
    fun headerLinesY() {
//        assertEquals("|   |   |   |",PlayingField("3x3").headerLinesY(cellCharacter = 'X'))
        assertEquals("|   |   |   |", PlayingGrid("3x3").headerLinesY(cellCharacter = ' '))
        assertEquals("|---|---|---|", PlayingGrid("3x3").headerLinesY(cellCharacter = '-'))
        assertEquals("|---|---|---|---|", PlayingGrid("3x4").headerLinesY(cellCharacter = '-'))
        assertEquals("|---|---|---|-y", PlayingGrid("3x3").headerLinesY(cellCharacter = '-', suffix = "-y"))
        assertEquals("|---|---|---|---|-y", PlayingGrid("3x4").headerLinesY(cellCharacter = '-', suffix = "-y"))
    }


    @Test
    fun `empty playing field`() {
        val expected = """
            |---|---|---|-y
            |   |   |   |
            |---|---|---|
            |   |   |   |
            |---|---|---|
            |   |   |   |
            |---|---|---|
            |
            x
        """.trimIndent()
        assertEquals(expected, PlayingGrid().render())
    }

    @ParameterizedTest
    @ValueSource(strings = ["3x3", "3x4"])
    fun isValidFieldDimensionString(fieldDimensionString: String) {
        assertTrue(PlayingGrid.isValidFieldDimensionString(fieldDimensionString))
    }

    @ParameterizedTest
    @ValueSource(strings = ["1x2", "2x1", "0x4"])
    fun isInvalidFieldDimensionString(fieldDimensionString: String) {
        assertFalse(PlayingGrid.isValidFieldDimensionString(fieldDimensionString))
    }

    @ParameterizedTest
    @ValueSource(strings = ["3x3:0:0", "3x3:2:2"])
    fun isValidCellLocation(info: String) {
        val (fieldDimensionText, cellXText, cellYText) = info.split(":")
        val (cellX, cellY) = listOf(cellXText, cellYText).map(String::toInt)
        val field = PlayingGrid(fieldDimensionText)
        assertTrue(field.isValidCellLocation(CellLocation(cellX, cellY, field)))
    }

    @ParameterizedTest
    @ValueSource(strings = ["3x3:4:0", "3x3:1:14"])
    fun isInvalidCellLocation(info: String) {
        val (fieldDimensionText, cellXText, cellYText) = info.split(":")
        val (cellX, cellY) = listOf(cellXText, cellYText).map(String::toInt)
        val field = PlayingGrid(fieldDimensionText)
        assertFalse(field.isValidCellLocation(CellLocation(cellX, cellY, field)))
    }

    companion object {
        @JvmStatic
        var field = PlayingGrid()

        @JvmStatic
        fun noWinnerCellLocations(): List<Set<CellLocation>> =
            listOf(
                setOf(CellLocation(0, 0, field), CellLocation(1, 1, field)),
                setOf(CellLocation(0, 0, field), CellLocation(1, 2, field)),
            )

        @JvmStatic
        fun winnerCellLocations(): List<Set<CellLocation>> =
            listOf(
                setOf(CellLocation(0, 0, field), CellLocation(1, 1, field)),
                setOf(CellLocation(0, 0, field), CellLocation(1, 1, field)),
                setOf(CellLocation(0, 0, field), CellLocation(1, 2, field)),
            )

    }

}
