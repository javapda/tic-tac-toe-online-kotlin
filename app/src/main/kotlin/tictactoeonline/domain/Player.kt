package tictactoeonline.domain

class Player(val name: String, val marker: Char = '?') {
    val locations = mutableSetOf<CellLocation>()

    override fun toString(): String {
        return "Player: name=$name, marker=$marker, no. locations: ${locations.size}"
//        return """
//            Player  - HERE
//            name:       $name
//            marker:    $marker
//            locations:  $locations
//        """.trimIndent()
    }

}

