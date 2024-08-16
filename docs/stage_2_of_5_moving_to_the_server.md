# [Stage 2/5 : Moving to the server](https://hyperskill.org/projects/366/stages/2167/implement) | [main README.md](../readme.md)

## John's notes
* [Postman](https://www.postman.com/)
* [GET http://localhost:28852/game/status](http://localhost:28852/game/status)
* [POST http://localhost:28852/game](http://localhost:28852/game)
* [Ktor ContentNegotiation and Serialization](https://ktor.io/docs/serialization.html)
* [ContentNegotion Sample](https://github.com/ktorio/ktor-documentation/blob/2.3.9/codeSnippets/snippets/json-kotlinx/src/main/kotlin/jsonkotlinx/Application.kt)
* [Ktor auto-reloading](https://ktor.io/docs/server-auto-reload.html)
* added [logback.xml](../app/src/main/resources/logback.xml)
* added [application.conf](../app/src/main/resources/application.conf)


### Issue
* Problem running tests on Stage 2, getting compilation errors:
There is a test file: TicTacToeOnlineTest
it fails compilation
```
Hyperskill seems to be using the following versions:
hs.kotlin.version : kotlin version 1.8.20")
Ktor version $hs.ktor.version : ktor version 2.3.1")

//hs.ktor.version
```
* would not allow doing Json.encodeToString(...) operation in POST route
```courseignore
16:33:23.099 [eventLoopGroupProxy-4-1] ERROR Application -- Unhandled: POST - /game
kotlinx.serialization.SerializationException: Serializer for class 'Companion' is not found.
Please ensure that class is marked as '@Serializable' and that the serialization compiler plugin is applied.

	at kotlinx.serialization.internal.Platform_commonKt.serializerNotRegistered(Platform.common.kt:91)
	at kotlinx.serialization.SerializersKt__SerializersKt.noCompiledSerializer(Serializers.kt:366)
	at kotlinx.serialization.SerializersKt.noCompiledSerializer(Unknown Source)

```

---
## Description
You have already implemented a console version of tic-tac-toe, which allows two people to play on the same computer. Online games are in great demand, so it's time to move our game to the Internet. It will make your game accessible to players on different devices with internet access.

>> In this stage, you do not need to implement registration/authorization, raise the database and process many games simultaneously. You need to implement interaction with your game not through the console but through Ktor's endpoints.
## Objectives
In this stage, your goal is to run the Ktor application and create several endpoints.

* Create and run the Ktor application on the 28852 port;
  * [Our suggestion for project structure](./suggestion-for-project-structure.md)
  * Implement the following API endpoints:
  ```
  POST /game for starting a new game. This method accepts a player1, player2, and size as JSON.
  ```
### For example:
```
{
"player1": "Carl",
"player2": "Mike",
"size": "4x3"
}
```

### The response should be as follows:

```
Response code: 200 OK
```

### Response body:
```
{
"status": "New game started",
"player1": <Name of 1st player>,
"player2": <Name of 2nd player>,
"size": <Field size>
}
```

>> When validating the response, we won't check formatting and indentation. The main thing is that the JSON object you return has the correct fields and values.
If the user sent invalid data or did not send the appropriate fields at all, then set the default values:

* `Player1` for the `player1` field;
* `Player2` for the `player2` field;
* `3x3` for the `size` field;
```
GET /game/status for getting the current game status. This method does not accept parameters. The response should be as follows:
Response code: 200 OK

Response body:

{
"game_status": <status string>,
"field": <Field array>,
"player1": <Name of 1st player>,
"player2": <Name of 2nd player>,
"size": <Field size>
}
```

`game_status` can have the following values depending on the current state of the game:

* `game not started` (in this case, `field`, `player1`, `player2` and `size` fields may be missing or take the default values)
* `1st player's move`
* `2nd player's move`
* `1st player won`
* `2nd player won`
* `draw`

`field` contains a two-dimensional array that represents the current playing field.

For example, if we have the following playing field:
```
|---|---|---|
| X | O |   |
|---|---|---|
|   | O |   |
|---|---|---|
|   |   | X |
|---|---|---|
```
The corresponding field array in this case:
```
[
["X", "O", " "],
[" ", "O", " "],
[" ", " ", "X"]
]
```
* `POST /game/move` for making a move.
This method should make a move for the player whose turn it is to move. It accepts a move as JSON. For example:
```
{
"move": "(2,3)"
}
```
If the move is successful, the response should be as follows:
```
Response code: 200 OK

Response body:

{
"status": "Move done"
}
```
If the move is incorrect or impossible (or the game has not started), the response should be:
```
Response code: 400 Bad Request

Response body:

{
"status": "Incorrect or impossible move"
}
```
## Examples
### Example 1:

1. `Request: POST /game`
Request body:
```
{
"player1": "Bob",
"player2": "John",
"size": "3x4"
}
```
Response code: `200 OK`
Response body:
```
{
"status": "New game started",
"player1": "Bob",
"player2": "John",
"size": "3x4"
}
```

2. Request: `GET /game/status`
Request body:
```
{ }
```

Response code: 200 OK
Response body:
```
{
"game_status": "1st player's move",
"field": [
[" ", " ", " ", " "],
[" ", " ", " ", " "],
[" ", " ", " ", " "]
],
"player1": "Bob",
"player2": "John",
"size": "3x4"
}
```

3. Request: `POST /game/move`
Request body:
```
{
"move": "(1,1)"
}
```
Response code: `200 OK`
Response body:
```
{
"status": "Move done"
}
```

4. Request: `GET /game/status`
Request body:
```
{ }
```
Response code: `200 OK`
Response body:

```
{
"game_status": "2nd player's move",
"field": [
["X", " ", " ", " "],
[" ", " ", " ", " "],
[" ", " ", " ", " "]
],
"player1": "Bob",
"player2": "John",
"size": "3x4"
}
```

5. Request: `POST /game/move`
Request body:
```
{
"move": "(2,1)"
}
```
Response code: `200 OK`
Response body:
```
{
"status": "Move done"
}
```

6. Request: `GET /game/status`
Request body:
```
{ }
```
Response code: `200 OK`
Response body:
```
{
"game_status": "1st player's move",
"field": [
["X", " ", " ", " "],
["O", " ", " ", " "],
[" ", " ", " ", " "]
],
"player1": "Bob",
"player2": "John",
"size": "3x4"
}
```

7. Request: `POST /game/move`
Request body:
```
{
"move": "(1,2)"
}
```
Response code: `200 OK`
Response body:
```
{
"status": "Move done"
}
```

8. Request: `POST /game/move`
Request body:
```
{
"move": "(2,2)"
}
```
Response code: `200 OK`
Response body:
```
{
"status": "Move done"
}
```
9. Request: `POST /game/move`
Request body:
```
{
"move": "(1,3)"
}
```

Response code: `200 OK`
Response body:
```
{
"status": "Move done"
}
```
10. Request: `POST /game/move`
Request body:
```
{
"move": "(2,3)"
}
```
Response code: `400 Bad Request`
Response body:
```
{
"status": "Incorrect or impossible move"
}
```
11. Request: `GET /game/status`
Request body:
```
{ }
```

Response code: `200 OK`
Response body:
```
{
"game_status": "1st player won",
"field": [
["X", "X", "X", " "],
["O", "O", " ", " "],
[" ", " ", " ", " "]
],
"player1": "Bob",
"player2": "John",
"size": "3x4"
}
```

### Example 2:

1. Request: `GET /game/status`
Request body:
```
{ }
```
Response code: `200 OK`
Response body:
```
{
"game_status": "game not started"
}
```
2. Request: `POST /game`
Request body:
```
{ }
```
Response code: `200 OK`
Response body:
```
{
"status": "New game started",
"player1": "Player1",
"player2": "Player2",
"size": "3x3"
}
```

3. Request: `POST /game/move`
Request body:
```
{
"move": "(1,1)"
}
```
Response code: `200 OK`
Response body:
```
{
"status": "Move done"
}
```
4. Request: `POST /game/move`
Request body:
```
{
"move": "(1,1)"
}
```
Response code: `400 Bad Request`
Response body:
```
{
"status": "Incorrect or impossible move"
}
```

5. Request: `POST /game/move`
Request body:
```
{
"move": "(100,100)"
}
```
Response code: `400 Bad Request`
Response body:
```
{
"status": "Incorrect or impossible move"
}
```
6. Request: `POST /game/move`
Request body:
```
{
"move": "(2,1)"
}
```

Response code: `200 OK`
Response body:
```
{
"status": "Move done"
}
```

7. Request: `POST /game/move`
Request body:
```
{
"move": "(1,2)"
}
```

Response code: `200 OK`
Response body:
```
{
"status": "Move done"
}
```
8. Request: `POST /game/move`
Request body:
```
{
"move": "(2,2)"
}
```

Response code: `200 OK`
Response body:
```
{
"status": "Move done"
}
```
9. Request: `POST /game/move`
Request body:
```
{
"move": "(3,3)"
}
```
Response code: `200 OK`
Response body:
```
{
"status": "Move done"
}
```

10. Request: `POST /game/move`
Request body:
```
{
"move": "(2,3)"
}
```
Response code: `200 OK`
Response body:
```
{
"status": "Move done"
}
```
11. Request: `GET /game/status`
Request body:
```
{ }
```
Response code: `200 OK`
Response body:
```
{
"game_status": "2nd player won",
"field": [
["X", "X", " "],
["O", "O", "O"],
[" ", " ", "X"]
],
"player1": "Player1",
"player2": "Player2",
"size": "3x3"
}
```

12. Request: `POST /game/move`
Request body:
```
{
"move": "(3,1)"
}
```

Response code: `400 Bad Request`
Response body:
```
{
"status": "Incorrect or impossible move"
}
```