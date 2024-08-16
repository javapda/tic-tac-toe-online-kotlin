package tictactoeonline.domain

import kotlin.math.min

typealias PlayerCounts = Pair<Int, Int>


/**
 * Virtual playing grid
 * NOTE: this grid must have 1 dimension that is at least 3 cells long
 * So, a 1x10 grid would be broken down into (A-2)*(B-2) : MAX(A-2,1) * MAX(B-2,1)
 * @property gridCellLocation
 * @property width
 * @property height
 * @constructor Create empty Virtual playing grid
 */
class VirtualPlayingGrid(
    val gridCellLocation: CellLocation,
    private val width: Int = MIN_FIELD_WIDTH_OR_HEIGHT,
    private val height: Int = MIN_FIELD_WIDTH_OR_HEIGHT
) {

    /**
     * Cell locations
     * These are locations as projected onto the main grid,
     * so each is adjusted to the main grid based on the
     * grid cell location.
     * For the virtual grid located at (0,0) there will be
     * 9 cell locations identical to the upper left corner
     * of the main grid
     * For other locations, there will be 9 cell locations adjusted
     * by the grid cell location
     * @return
     */
    fun cellLocations(): List<CellLocation> {
        val cellLocations = mutableListOf<CellLocation>()
        val mainGrid = gridCellLocation.grid
        (0 until height).forEach { x ->
            (0 until width).forEach { y ->
                cellLocations.add(
                    CellLocation(
                        x + gridCellLocation.x,
                        y + gridCellLocation.y,
                        mainGrid
                    )
                )
            }
        }
        return cellLocations.toList()
    }

    private var vertical0: PlayerCounts = PlayerCounts(0, 0)
    private var vertical1: PlayerCounts = PlayerCounts(0, 0)
    private var vertical2: PlayerCounts = PlayerCounts(0, 0)
    private var horizontal0: PlayerCounts = PlayerCounts(0, 0)
    private var horizontal1: PlayerCounts = PlayerCounts(0, 0)
    private var horizontal2: PlayerCounts = PlayerCounts(0, 0)
    private var diagonal: PlayerCounts = PlayerCounts(0, 0)
    private var antiDiagonal: PlayerCounts = PlayerCounts(0, 0)
    fun setPlayerCell(cellLocation: CellLocation, playerNumber: Int) {
        if (isCellLocationLocal(cellLocation)) {
            val normalizedCellLocation = CellLocation(
                min(cellLocation.x - gridCellLocation.x,2),
                min(cellLocation.y - gridCellLocation.y,2),
                cellLocation.grid
            )
            if (onDiagonal(normalizedCellLocation)) {
                diagonal =
                    if (playerNumber == 1) diagonal.copy(first = diagonal.first + 1) else diagonal.copy(second = diagonal.second + 1)
            }
            if (onAntiDiagonal(normalizedCellLocation)) {
                antiDiagonal =
                    if (playerNumber == 1) antiDiagonal.copy(first = antiDiagonal.first + 1) else antiDiagonal.copy(
                        second = antiDiagonal.second + 1
                    )
            }
            when (normalizedCellLocation.x) {
                0 -> vertical0 =
                    if (playerNumber == 1) vertical0.copy(first = vertical0.first + 1) else vertical0.copy(second = vertical0.second + 1)

                1 -> vertical1 =
                    if (playerNumber == 1) vertical1.copy(first = vertical1.first + 1) else vertical1.copy(second = vertical1.second + 1)

                2 -> vertical2 =
                    if (playerNumber == 1) vertical2.copy(first = vertical2.first + 1) else vertical2.copy(second = vertical2.second + 1)
            }
            when (normalizedCellLocation.y) {
                0 -> horizontal0 =
                    if (playerNumber == 1) horizontal0.copy(first = horizontal0.first + 1) else horizontal0.copy(second = horizontal0.second + 1)

                1 -> horizontal1 =
                    if (playerNumber == 1) horizontal1.copy(first = horizontal1.first + 1) else horizontal1.copy(second = horizontal1.second + 1)

                2 -> horizontal2 =
                    if (playerNumber == 1) horizontal2.copy(first = horizontal2.first + 1) else horizontal2.copy(second = horizontal2.second + 1)
            }
        }
    }

    /**
     * Winning player
     *
     * @return 1=Player1 [X], 2=Player2 [O]
     */
    fun winningPlayer(): Int {
        return winningPlayer(
            vertical0,
            vertical1,
            vertical2,
            horizontal0,
            horizontal1,
            horizontal2,
            diagonal,
            antiDiagonal
        )
    }


    fun winningPlayer(vararg playerCounts: PlayerCounts): Int {
        return if (isWinner(*playerCounts)) { // spread operator '*'
            if (playerCounts.map(PlayerCounts::first).any { it >= 3 }) 1 else 2
        } else {
            throw Exception("no winning player yet, so cannot get winningPlayer")
        }
    }

    fun isWinner(): Boolean {
        return isWinner(vertical0, vertical1, vertical2, horizontal0, horizontal1, horizontal2, diagonal, antiDiagonal)
    }


    fun isWinner(vararg playerCounts: PlayerCounts): Boolean {
        val result = playerCounts.map(PlayerCounts::first).any { it >= 3 } ||
                playerCounts.map(PlayerCounts::second).any { it >= 3 }
        return result
    }

    fun isCellLocationLocal(cellLocation: CellLocation): Boolean {
        return cellLocation.x in gridCellLocation.x..gridCellLocation.x + height - 1
                &&
                cellLocation.y in gridCellLocation.y..gridCellLocation.y + width - 1
    }

    fun onDiagonal(normalizedCellLocation: CellLocation): Boolean {
        val pair = normalizedCellLocation.x to normalizedCellLocation.y
        return pair in listOf(
            0 to 0,
            1 to 1,
            2 to 2
        )
    }

    fun onAntiDiagonal(normalizedCellLocation: CellLocation): Boolean {
        val pair = normalizedCellLocation.x to normalizedCellLocation.y
        return pair in listOf(
            2 to 0,
            1 to 1,
            0 to 2
        )
    }

    fun info(): Any {
        return buildString {
            appendLine("gridCellLocation:  $gridCellLocation")
            appendLine("vertical0:         $vertical0")
            appendLine("vertical1:         $vertical1")
            appendLine("vertical2:         $vertical2")
            appendLine("horizontal0:       $horizontal0")
            appendLine("horizontal1:       $horizontal1")
            appendLine("horizontal2:       $horizontal2")
            appendLine("diagonal:          $diagonal")
            appendLine("antiDiagonal:      $antiDiagonal")
            appendLine("cell locations:    ${cellLocations().joinToString(", ", transform = CellLocation::toString)}")
        }
    }


}