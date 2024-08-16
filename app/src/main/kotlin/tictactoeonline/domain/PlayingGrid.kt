package tictactoeonline.domain

import kotlin.math.max

const val MIN_FIELD_WIDTH_OR_HEIGHT = 3
const val CELL_WIDTH = 3
const val CELL_SEPARATOR = '|'

/**
 * Playing field
 * according to the rules the first part of a dimension represents the row,
 * and the second part represents the column
 *
 * @property width
 * @property height
 * @constructor Create empty Playing field
 */
class PlayingGrid(
    var width: Int = MIN_FIELD_WIDTH_OR_HEIGHT,
    var height: Int = MIN_FIELD_WIDTH_OR_HEIGHT
) {
    constructor(fieldDemensionString: String) : this() {
        val (numberOfRows, numberOfColumns) = parseFieldDimensionString(fieldDemensionString)
        width = numberOfColumns
        height = numberOfRows
        initVirtualPlayingGrids()
    }

    private fun initVirtualPlayingGrids() {
        virtualPlayingGrids.clear()
        (0 until max(height - 2, 1)).forEach { x ->
            (0 until max(width - 2, 1)).forEach { y ->
                virtualPlayingGrids.add(VirtualPlayingGrid(CellLocation(x, y, this)))
            }
        }
    }

    val virtualPlayingGrids: MutableList<VirtualPlayingGrid> = mutableListOf()
    private val playerXLocations: MutableSet<CellLocation> = mutableSetOf()
    private val playerOLocations: MutableSet<CellLocation> = mutableSetOf()

    /**
     * Grid separator line
     * these are the hyphenated lines between rows of cells
     * and above and below the cell rows
     */
    internal fun gridSeparatorLine(cols: Int = width, suffix: String = ""): String {
        return buildString {
            repeat(cols) {
                append(CELL_SEPARATOR)
                append(renderCell('-'))
            }
            append(CELL_SEPARATOR)
            append(suffix)
        }
    }

    internal fun headerLinesY(cellWidth: Int = 3, cellCharacter: Char = ' ', suffix: String = ""): String =
        buildString {
            val cellContent = if (cellCharacter != '-') {
                val half = cellWidth / 2
                " ".repeat(half) + cellCharacter + " ".repeat(cellWidth - half - 1)
            } else {
                cellCharacter.toString().repeat(cellWidth)
            }
            repeat(width) { num ->
                append("|").append(cellContent)
                    .also {
                        if (num == width - 1) {
                            append("|")
                            if (suffix.isNotEmpty()) {
                                append(suffix)
                            }
                        }
                    }
            }
        }

    fun renderCell(cellChar: Char = ' ') =
        if (cellChar != '-') {
            val half = CELL_WIDTH / 2
            " ".repeat(half) + cellChar + " ".repeat(CELL_WIDTH - half - 1)
        } else {
            cellChar.toString().repeat(CELL_WIDTH)
        }


    fun render(
        playerXLocations: Set<CellLocation> = this.playerXLocations,
        playerOLocations: Set<CellLocation> = this.playerOLocations
    ): String {
        val emptyField = playerXLocations.isEmpty() && playerOLocations.isEmpty()
        return buildString {
            appendLine(gridSeparatorLine(suffix = if (emptyField) "-y" else ""))
            for (row in 0 until height) {
                for (col in 0 until width) {
                    val loc = CellLocation(row, col, this@PlayingGrid)
                    val cellChar = if (loc in playerXLocations) 'X' else if (loc in playerOLocations) 'O' else ' '
                    append(CELL_SEPARATOR)
                    append(renderCell(cellChar))
                }
                appendLine(CELL_SEPARATOR)
                appendLine(gridSeparatorLine())
            }
            if (emptyField) {
                appendLine("|")
                append("x")
            }
        }
    }

    fun isValidCellLocation(cellLocation: CellLocation): Boolean {
        return cellLocation.x in (0 until height) && cellLocation.y in (0 until width)
    }

    fun isWinner(): Boolean {
        return virtualPlayingGrids.any { it -> it.isWinner() }
    }

    fun winningPlayer(): Int {
        return virtualPlayingGrids.filter(VirtualPlayingGrid::isWinner).first().winningPlayer()
    }

    fun isDraw(): Boolean {
        return !isWinner() && allCellsOccupied()
    }

    private fun allCellsOccupied(): Boolean {
        return playerXLocations.size + playerOLocations.size >= width * height
    }

    fun isCellLocationAvailable(cellLocation: CellLocation): Boolean {
        return cellLocation !in playerXLocations && cellLocation !in playerOLocations
    }
    fun isCellLocationOccupied(cellLocation: CellLocation): Boolean {
        return !isCellLocationAvailable(cellLocation)
    }

    fun setPlayerCell(cellLocation: CellLocation, playerNumber: Int) {
        if (playerNumber == 1) {
            playerXLocations.add(cellLocation)
        } else if (playerNumber == 2) {
            playerOLocations.add(cellLocation)
        } else {
            throw IllegalArgumentException("Unknown player number '$playerNumber'")
        }
        virtualPlayingGrids.forEach { vpg -> vpg.setPlayerCell(cellLocation, playerNumber) }
    }

    fun info(): String {
        return """
            INFO TIME ${this::class.simpleName}
            no. X:      ${playerXLocations.size}
            no. O:      ${playerOLocations.size}
            -------------------------------------
            X: ${playerXLocations.joinToString(", ", transform = CellLocation::toString)}
            X: ${playerOLocations.joinToString(", ", transform = CellLocation::toString)}
            -------------------------------------
            no. total:  ${width * height}
            isWinner:   ${isWinner()}
            isDraw:     ${isDraw()}
        """.trimIndent()
//        println("""
//            no. x:  ${field.lo}
//        """.trimIndent())

    }

    fun vpgInfo(): Any {

        return buildString {
            virtualPlayingGrids.forEachIndexed { idx, vpg ->
                appendLine("${idx + 1}. Virtual Playing Grid")
                appendLine(vpg.info())
            }
        }
    }

    fun renderFieldTo2DArray(): List<List<String>> {
        val container: MutableList<List<String>> = mutableListOf()
        repeat(height) { x ->
            val row = mutableListOf<String>()
            repeat(width) { y ->
                val cellLocation = CellLocation(x, y, this)
                val marker = if (cellLocation in playerXLocations) {
                    "X"
                } else if (cellLocation in playerOLocations) {
                    "O"
                } else {
                    " "
                }
                row.add(marker)

            }
            container.add(row.toList())
        }
        return container.toList()
    }

    companion object {
        /**
         * Is valid field dimension string
         * A wrong size is a size that does not match the AxB pattern, where A and B are positive integers. Second, the map size cannot be less than 3 in two directions simultaneously. In other words, at least one size must be greater than or equal to 3: A>=3 || B>=3. For example, 1x10 is the correct size, and 2x2 is the incorrect one.
         * @param fieldDimensionString
         * @return
         */
        fun isValidFieldDimensionString(fieldDimensionString: String): Boolean {
            val list = parseFieldDimensionString(fieldDimensionString)
            if (list.size != 2) return false
            val (width, height) = list
            return width > 0 && height > 0 && (width >= MIN_FIELD_WIDTH_OR_HEIGHT || height >= MIN_FIELD_WIDTH_OR_HEIGHT)
        }

        fun parseFieldDimensionString(fieldDimensionString: String): List<Int> {
            val regex = "([0-9]+)[xX]([0-9]+)".toRegex()
            if (regex.matches(fieldDimensionString)) {
                val found = regex.find(fieldDimensionString)
                if (found?.groups?.size == 3) {
                    val x = found.groups[1]?.value?.toIntOrNull()
                        ?: throw Exception("invalid field dimension String $fieldDimensionString")
                    val y = found.groups[2]?.value?.toIntOrNull()
                        ?: throw Exception("invalid field dimension String $fieldDimensionString")
                    return listOf(x, y)
                }
            }
            return emptyList()
        }
    }
}
