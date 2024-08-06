package tictactoeonline

/**
 * Cell location
 *  ODDITY: in this implementation x represents the vertical, and y the horizontal
 * @property x - vertical offset
 * @property y - horizontal offset
 * @property grid
 * @constructor Create empty Cell location
 */
class CellLocation(val x:Int, val y:Int, val grid: PlayingGrid) {
    init {
        //require(grid.isValidCellLocation(this))
    }

    override fun toString(): String {
        return "($x,$y)"
//        return "CellLocation: x=$x, y=$y"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CellLocation

        if (x != other.x) return false
        if (y != other.y) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        return result
    }
}