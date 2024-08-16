package tictactoeonline

const val DEFAULT_PLAYER_X_NAME = "Player1"
const val DEFAULT_PLAYER_O_NAME = "Player2"
const val DEFAULT_FIELD_DIMENSIONS = "3x3"

enum class GameState(val description: String) {
    NOT_STARTED("game not started"),
    PLAYER_MOVE_1("1st player's move"),
    PLAYER_MOVE_2("2nd player's move"),
    GAME_OVER_WINNER_1("1st player won"),
    GAME_OVER_WINNER_2("2nd player won"),
    GAME_OVER_DRAW("draw"),
}

interface Game {
    fun initializeField(size: String)
}


class TicTacToeOnline(val verbose: Boolean = false) : Game {
    lateinit var field: PlayingGrid
    lateinit var playerX: Player
    lateinit var playerO: Player
    lateinit var currentPlayer: Player
    var moveCount = 0
    var state: GameState = GameState.NOT_STARTED
    fun addPlayer(player: Player) {
        if (this::playerX.isInitialized && playerX.name.isNotEmpty()) {
            playerO = Player(player.name, 'O')
        } else {
            playerX = Player(player.name, 'X')

        }
        if (this::playerX.isInitialized && this::playerO.isInitialized
            && playerX.name.isNotEmpty() && playerO.name.isNotEmpty()
        ) {
            initGame()
        }
    }

    fun initGame() {
        state = GameState.PLAYER_MOVE_1
        currentPlayer = playerX
        println(
            """
            ${"%".repeat(80)}
            GAME INITIALIZED
            playerX:        $playerX
            playerO:        $playerO
            currentPlayer:  $currentPlayer
            field:          ${fieldSize()}
            state:          $state
            ${"%".repeat(80)}
        """.trimIndent()
        )
    }

    fun startGame() {
        print("Enter the first player's name ($DEFAULT_PLAYER_X_NAME by default)\n> ")
        val player1 = readln().trim().let { if (it.trim().isEmpty()) DEFAULT_PLAYER_X_NAME else it }
        println("First player's name: $player1")
        print("Enter the second player's name ($DEFAULT_PLAYER_O_NAME by default)\n> ")
        val player2 = readln().trim().let { if (it.trim().isEmpty()) DEFAULT_PLAYER_O_NAME else it }
        println("Second player's name: $player2")
        print("Enter the field size ($DEFAULT_FIELD_DIMENSIONS by default)\n> ")
        var fieldDimensions = readln().trim().let { if (it.trim().isEmpty()) DEFAULT_PLAYER_O_NAME else it }
        if (!PlayingGrid.isValidFieldDimensionString(fieldDimensions)) {
            fieldDimensions = DEFAULT_FIELD_DIMENSIONS
        }
        if (verbose) {
            println(
                """
            player1:          $player1
            player2:          $player2
            fieldDimensions:  $fieldDimensions
        """.trimIndent()
            )
        }
        playerX = Player(player1)
        playerO = Player(player2)
        field = PlayingGrid(fieldDimensions)
        currentPlayer = playerX
        println("Field size: ${field.height}x${field.width}")
        println(field.render(playerXLocations = playerX.locations, playerOLocations = playerO.locations))
        gameLoop()
    }


    private fun gameLoop() {
        fun Int.isPlayerOnesMove() = this % 2 == 0
        var moveCount = 0
        while (gameStillPlaying()) {
            val currentPlayer = if (moveCount.isPlayerOnesMove()) playerX else playerO
            print("Enter ${currentPlayer.name}'s move as (x,y)\n> ")
            val move = readln().trim()
            if (move == "?" || move == "help") {
                help()
            } else if (move == "info") {
                println(field.info())
                println("no. moves:  $moveCount")
            } else if (move == "vpg") {
                println(field.vpgInfo())
            } else if (move == "show") {
                println(field.render())
            } else if (isValidMoveFormat(move)) {
                val (x, y) = parseMoveFormat(move)
                val cellLocation = CellLocation(x - 1, y - 1, field)
                if (field.isValidCellLocation(cellLocation) && field.isCellLocationAvailable(cellLocation)) {
                    field.setPlayerCell(cellLocation, if (moveCount.isPlayerOnesMove()) 1 else 2)
                    moveCount++
                    println(field.render())
                } else if (!field.isCellLocationAvailable(cellLocation)) {
                    println("Wrong move entered")
                } else {
                    println("Wrong move entered")
                }
            } else {
                println("Wrong move entered")
            }
        }
        if (field.isWinner()) {
            val winningPlayer = if (field.winningPlayer() == 1) playerX else playerO
            println("${winningPlayer.name} wins!")
        } else if (field.isDraw()) {
            println("Draw!")
        } else {
            throw Exception("No Winner? and No Draw? Should not be here")
        }
    }

    private fun help() {
        println(
            """
            Tic-Tac-Toe Online
            ?, help:  show this help
            info:     show info
            show:     show the playing field in its current state
        """.trimIndent()
        )
    }

    fun isValidMove(move: String): Boolean {
        return gameStarted() && isValidMoveFormat(move) && isValidCellLocation(move)
    }

    fun isOccupied(move: String): Boolean {
        return gameStarted() && isValidMoveFormat(move) && isValidCellLocation(move) && isOccupiedCellLocation(move)
    }

    private fun isValidCellLocation(move: String): Boolean {
        val (x, y) = parseMoveFormat(move)
        return x - 1 in (0 until field.height) && y - 1 in (0 until field.width)
    }

    private fun isOccupiedCellLocation(move: String): Boolean {
        val (x, y) = parseMoveFormat(move)
        val cellLocation = CellLocation(x - 1, y - 1, field)
        return field.isCellLocationOccupied(cellLocation)
    }

    fun gameStarted(): Boolean {
        return state !in setOf(
            GameState.NOT_STARTED,
            GameState.GAME_OVER_WINNER_1,
            GameState.GAME_OVER_WINNER_2,
            GameState.GAME_OVER_DRAW
        )
    }

    fun isValidMoveFormat(move: String): Boolean {
        val regex = """\(\s*\d+\s*,\s*\d+\s*\)""".toRegex()
        return regex.matches(move)
    }

    fun parseMoveFormat(move: String): Pair<Int, Int> {
        val regex = """\(\s*(\d+)\s*,\s*(\d+)\s*\)""".toRegex()
        if (regex.matches(move)) {
            val matchResult = regex.find(move)
            val x = matchResult?.groupValues!![1].toInt()
            val y = matchResult.groupValues[2].toInt()
            return x to y
        }
        throw IllegalArgumentException("move '$move' in wrong format")
    }

    private fun gameStillPlaying(): Boolean {
        return !field.isWinner() && !field.isDraw()
    }

    fun newGame(player1: String, player2: String, size: String): Boolean {

        if (PlayingGrid.isValidFieldDimensionString(size)) {
            field = PlayingGrid(size)
            playerX = Player(player1)
            playerO = Player(player2)
            state = GameState.PLAYER_MOVE_1
            moveCount = 0
            currentPlayer = playerX
            return true
        } else {
            throw IllegalArgumentException("Invalid field Dimensions '$size'")
        }

    }

    fun fieldSize(): String = "${field.height}x${field.width}"
    fun renderFieldTo2DArray(): List<List<String>>? {
        return if (this::field.isInitialized)
            field.renderFieldTo2DArray()
        else null
    }

    fun move(move: String): Boolean {
        val (x, y) = parseMoveFormat(move)
        val cellLocation = CellLocation(x - 1, y - 1, field)
        return if (field.isCellLocationAvailable(cellLocation)) {
            field.setPlayerCell(cellLocation, currentPlayerNumber())
            moveCount++
            currentPlayer = if (moveCount % 2 == 0) playerX else playerO
            state =
                if (field.isDraw()) {
                    GameState.GAME_OVER_DRAW
                } else if (field.isWinner()) {
                    if (field.winningPlayer() == 1) GameState.GAME_OVER_WINNER_1 else GameState.GAME_OVER_WINNER_2
                } else if (currentPlayer == playerX) {
                    GameState.PLAYER_MOVE_1
                } else {
                    GameState.PLAYER_MOVE_2
                }
            true
        } else {
            false
        }

    }

    private fun currentPlayerNumber(): Int =
        if (currentPlayer == playerX) 1 else 2

    override fun initializeField(size: String) {
        field = PlayingGrid(
            if (PlayingGrid.isValidFieldDimensionString(size)) size else DEFAULT_FIELD_DIMENSIONS
        )
    }

    fun playerXName(): String {
        return if (this::playerX.isInitialized) playerX.name else ""
    }
    fun playerOName(): String {
        return if (this::playerO.isInitialized) playerO.name else ""
    }


}

fun main() {
    TicTacToeOnline().startGame()
}