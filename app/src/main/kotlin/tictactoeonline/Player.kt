package tictactoeonline

class Player(val name: String, val marker: Char = '?') {
    val locations = mutableSetOf<CellLocation>()

    override fun toString(): String {
        return """
            Player  - HERE
            name:       $name
            marker:    $marker
            locations:  $locations
        """.trimIndent()
    }

}

