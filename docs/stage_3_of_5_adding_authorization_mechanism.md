# [Stage 3/5 : Adding an authorization mechanism](https://hyperskill.org/projects/366/stages/2168/implement) | [main README.md](../readme.md)

## John's notes
* [Postman](https://www.postman.com/)
* [GET http://localhost:28852/game/status](http://localhost:28852/game/status)
* [POST http://localhost:28852/game](http://localhost:28852/game)
* [Ktor ContentNegotiation and Serialization](https://ktor.io/docs/serialization.html)
* [ContentNegotion Sample](https://github.com/ktorio/ktor-documentation/blob/2.3.9/codeSnippets/snippets/json-kotlinx/src/main/kotlin/jsonkotlinx/Application.kt)
* [Ktor auto-reloading](https://ktor.io/docs/server-auto-reload.html)
* added [logback.xml](../app/src/main/resources/logback.xml)
* added [application.conf](../app/src/main/resources/application.conf)

## My Steps
* DONE record description and format in markdown
* DONE create postman for [stage 2](postman/Tic-Tac-Toe Online Stage 2.postman_collection.json) (the previous stage) and for this one, [stage 3](postman/Tic-Tac-Toe Online Stage 3.postman_collection.json)
* DONE partition route into its own kotlin file: [configureRoutes.kt](../app/src/main/kotlin/tictactoeonline/configureRoutes.kt)
* DONE add help() - JSON
* DONE add info() - JSON
* DONE add [testing](https://ktor.io/docs/server-testing.html) - via io.ktor.server.testing.testApplication [ApplicationTest.kt](../app/src/test/kotlin/tictactoeonline/ApplicationTest.kt)
  * test failed registration /signup
    * DONE user already exists
    * DONE user provided with missing email
    * DONE user provided with missing password
  * test flow add user(s) and check user store
  * test failed authorization 
    * /games
    * /game/{game_id}/move
    * /game/{game_id}/status
* Strategy for managing Games? Rooms?
```
dependency {
  testImplementation("io.ktor:ktor-server-test-host:$ktor_version")
  testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlin_version")  // assertions + utility functions
}
```
* attempt retrofit dependencies (update [libs.versions.toml](../gradle/libs.versions.toml), btw, saved stage 2 [libs.versions.toml](support/stage_2_of_5_libs.versions.toml), [build.gradle.kts here](support/stage_2_of_5_build.gradle.kts)) for Hyperskill based on [build.gradle for stage 3](support/stage_3_of_5_build.gradle)
* add JWT

### Issue
_none yet_
---

### Description
We already have a Ktor game server that allows two players to play tic-tac-toe through requests to Ktor endpoints. So far, we can maintain only one simultaneous game. Besides, any user on the Internet can make moves. It's time to fix that.

### Objectives
In this stage, add authorization and the room mechanism to the game. This will allow players to log into the game under their accounts and create or join game rooms. Each game room will have its game state.


> At this stage, we are still not introducing persistent data storage (like a database or files), so the tests assume that all application data is reset on restart. To pass the tests store data in regular variables and do not use files, databases or other external storage.
Implement the following API endpoints:

* `POST /signup` for registration.
This method accepts an `email` and `password` as JSON. If the registration was successful, the response should be the following:

Response code: `200 OK`

Response body:

>{
"status": "Signed Up"
}


If the registration failed, the response should be:

Response code: `403 Forbidden`

Response body:

>{
"status": "Registration failed"
}

Registration is unsuccessful if:

* email or password in the request is blank (or missing)
* there is an attempt to register a user with an existing email

For simplicity, you don't need to check the email's password strength and correctness.

* `POST /signin` for authorization.
This method accepts an `email` and a `password` as JSON. If an authorization was successful, the response is:

Response code: `200 OK`

Response body:

>{
"status": "Signed In",
"token": "`JWT token`"
}

If the authorization failed, this response follows:

Response code: `403 Forbidden`

Response body:

>{
"status": "Authorization failed"
}

To pass the check, use the following parameters to generate JWT tokens:

* Token header:
> {
"alg": "HS256",
"typ": "JWT"
}

* Token payload:

> {
"email": <user's email>
}

_Payload usually contains token expiration time in real projects, but we will not include it for simplicity._

* Token signature:

>HMACSHA256(
> base64UrlEncode(header) + "." + 
> base64UrlEncode(payload), 
> ut920BwH09AOEDx5
> )

> Where `ut920BwH09AOEDx5` is the JWT secret key. It is only used in this educational project. When you make your applications, use your key instead!
With the received token, the client will get access to all endpoints (except `/signup` and `/signin`) using the `Authorization` header:

> Authorization: Bearer <token>

* Handling authorization:
Add the JWT token validation to all endpoints (except `/signup` and `/signin`) so that unauthorized users cannot access them. If the token check fails, the response should be as follows:

Response code: `401 Unauthorized`

Response body:

>{
"status": "Authorization failed"
}

* Edit `POST /game`
This method accepts a `player1` or `player2`, and `size` as JSON. 
But now the `player1` and `player2` parameters should contain the 
player's email instead of an arbitrary name. If a player wants to 
create a game and play for crosses (`X`), they put their email in 
`player1`, leaving `player2` empty. For example:

>{
>"player1": "carl@example.com",
>"player2": "",
>"size": "4x3"
>}

If a player wants to create a game and play for noughts (`O`), they put the 
email in `player2`, leaving `player1` empty. For example:

>{
>"player1": "",
>"player2": "carl@example.com",
>"size": "4x3"
>}


In the response body, add the `game_id` of the created game room. The `game_id` 
should be assigned by your server and should represent the sequence number of 
the game created (the first game created should have `game_id = 1`, the second 
game should have `game_id = 2`, etc.):

>{
>"game_id": <new game id>,
>"status": "New game started",
>"player1": <email of 1st player or "">,
>"player2": <email of 2nd player or "">,
>"size": <field size>
>}

If the operation fails, this response follows:

Response code: `403 Forbidden`

Response body:

> {
> "status": "Creating a game failed"
> }

Creating a game is considered unsuccessful if `player1` and `player2` do not 
contain the email of the user who is trying to develop the game

* `GET /games` for getting a list of all games (game rooms).
This method does not accept parameters. The response should be as follows:

Response code: `200 OK`

Response body:

> [
> {
> "game_id": <game id>,
> "player1": <email of 1st player or "">,
> "player2": <email of 2nd player or "">,
> "size": <field size>
> },
> ...
> ]


If a room is created, but the opponent has not joined it, the 
field of the corresponding player should be empty (`""`)

* `POST /game/<game_id>/join` for joining a game created by another player.
Where `<game_id>` is the path parameter containing the id of the game room the player wants to join. The request body does not have parameters. If joining the game room was successful, the response should be as follows:

Response code: `200 OK`

Response body:

> {
> "status": "Joining the game succeeded"
> }

If the operation fails, this response follows:

Response code: `403 Forbidden`

Response body:

> {
> "status": "Joining the game failed"
> }

Joining a game is considered unsuccessful if a game room with this id does not 
exist or the game room is already occupied (`player1` and `player2` fields do 
not contain the empty string `""`)

* `Edit GET /game/status`

Change the address of the endpoint so that it contains the id of the game whose status you want to get:

`GET /game/<game_id>/status`

Also, add this id to the body of the response:

> {
> "game_id": <game id>,
> "game_status": <status string>,
> "field": <field array>,
> "player1": <email of 1st player or "">,
> "player2": <email of 2nd player or "">,
> "size": <field size>
> }

If the operation fails, this response follows:

Response code: `403 Forbidden`

Response body:

> {
> "status": "Failed to get game status"
> }

Getting game status is considered unsuccessful if the user who is trying to 
get the status has an email different from `player1` and `player2` or the 
game with the given id does not exist

* Edit `POST /game/move`

The query body parameter remains the same. Change the endpoint address 
so that it contains the id of the game in which you want to make your 
move: `POST /game/<game_id>/move`

If a player tries to make a move for another player (with an email different from his own) or the game with the specified id does not exist, the server's response should be as follows:

Response code: `403 Forbidden`

Response body:

> {
> "status": "You have no rights to make this move"
> }

If the user has the right to make a move, but the move is incorrect or impossible, the response should be:

Response code: `400 Bad Request`

Response body:

> {
> "status": "Incorrect or impossible move"
> }

If after a move a winning combination or a draw is formed, the game status 
should be set accordingly. So that at the next
`GET /game/<game_id>/status` call the game_status field will contain the 
appropriate string (`"1st player won"` or `"2nd player won"` or `"draw"`). 
In this case the game is considered completed and all subsequent requests 
to make a move in such a game must be responded to as follows:

Response code: `403 Forbidden`

Response body:

> {
> "status": "You have no rights to make this move"
> }

> > As a reminder, all data should be stored in the server's RAM to pass the tests.

* Your project structure might look like this:
![Project structure](images/stage_3_of_5_project_structure.webp)
  * [application.conf](support/stage_3_of_5_application.conf)
  * [Application.kt](support/stage_3_of_5_Application.kt)
  * [configurePlugins.kt](support/stage_3_of_5_configurePlugins.kt)
  * [configureRoutes.kt](support/stage_3_of_5_configureRoutes.kt)
  * [build.gradle](support/stage_3_of_5_build.gradle)

## Examples
### Example 1:

1. Request: `POST /game`
Request body:
```
{
 "player1": "carl@example.com",
 "player2": "",
 "size": "4x3"
}
```
Response code: `401 Unauthorized`
Response body:
```
{
 "status": "Authorization failed"
 }
```
2. Request: `POST /signup`
Request body:
```
{
 "email": "carl@example.com",
 "password": "1111"
}
```
Response code: `200 OK`
Response body:
```
{
 "status": "Signed Up"
}
```
3. Request: `POST /signup`
Request body:
```
{
 "email": "mike@example.com",
 "password": "2222"
}
```
Response code: `200 OK`
Response body:
```
{
 "status": "Signed Up"
}
```
4. Request: `POST /signin`
Request body:
```
{
 "email": "carl@example.com",
 "password": "1111"
}
```
Response code: `200 OK`
Response body:
```
{
 "status": "Signed In",
 "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6ImNhcmxAZXhhbXBsZS5jb20ifQ.Zt8gtkUjEffgODauxx9gwQ1cnKueML-5ESBkJW2g5AY"
}
```
5. Request: `POST /signin`
Request body:
```
{
 "email": "mike@example.com",
 "password": "2222"
}
```

Response code: `200 OK`
Response body:
```
{
 "status": "Signed In",
 "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6Im1pa2VAZXhhbXBsZS5jb20ifQ.yw_YW7lY77UncwcPNOxego1l1TcP80o1gHhrdWeg0JU"
}
```
6. Request: `POST /game`
Authorization header:
> Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6ImNhcmxAZXhhbXBsZS5jb20ifQ.Zt8gtkUjEffgODauxx9gwQ1cnKueML-5ESBkJW2g5AY

Request body:
```
{
 "player1": "carl@example.com",
 "player2": "",
 "size": "4x3"
}
```

Response code: `200 OK`
Response body:
> {
> "game_id": 1,
> "status": "New game started",
> "player1": "carl@example.com",
> "player2": "",
> "size": "4x3"
> }

7. Request: `POST /game/1/join`
Authorization header:
> Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6Im1pa2VAZXhhbXBsZS5jb20ifQ.yw_YW7lY77UncwcPNOxego1l1TcP80o1gHhrdWeg0JU

Request body:
> { }

Response code: `200 OK`
Response body:
> {
> "status": "Joining the game succeeded"
> }

8. Request: `GET /game/1/status`
Authorization header:
> Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6ImNhcmxAZXhhbXBsZS5jb20ifQ.Zt8gtkUjEffgODauxx9gwQ1cnKueML-5ESBkJW2g5AY

Request body:
> { }

Response code: `200 OK`
Response body:
```
{
"game_id": 1,
"game_status": "1st player's move",
"field": [
[" ", " ", " "],
[" ", " ", " "],
[" ", " ", " "],
[" ", " ", " "]
],
"player1": "carl@example.com",
"player2": "mike@example.com",
"size": "4x3"
}
```

9. Request: `POST /game/1/move`
Authorization header:
> Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6ImNhcmxAZXhhbXBsZS5jb20ifQ.Zt8gtkUjEffgODauxx9gwQ1cnKueML-5ESBkJW2g5AY

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

10. Request: `POST /game/1/move`
Request body:
```
{
"move": "(1,1)"
}
```

Response code: `401 Unauthorized`
Response body:
```
{
"status": "Authorization failed"
}
```

11. Request: `POST /game/1/move`
Authorization header:
> Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6ImNhcmxAZXhhbXBsZS5jb20ifQ.Zt8gtkUjEffgODauxx9gwQ1cnKueML-5ESBkJW2g5AY

Request body:
```
{
"move": "(1,1)"
}
```
Response code: `403 Forbidden`
Response body:
```
{
"status": "You have no rights to make this move"
}
```

12. Request: `POST /game/1/move`
Authorization header:
> Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6Im1pa2VAZXhhbXBsZS5jb20ifQ.yw_YW7lY77UncwcPNOxego1l1TcP80o1gHhrdWeg0JU

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

13. Request: `POST /game/1/move`
Authorization header:
> Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6Im1pa2VAZXhhbXBsZS5jb20ifQ.yw_YW7lY77UncwcPNOxego1l1TcP80o1gHhrdWeg0JU

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

14. Request: `POST /game/1/move`
Authorization header:
> Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6ImNhcmxAZXhhbXBsZS5jb20ifQ.Zt8gtkUjEffgODauxx9gwQ1cnKueML-5ESBkJW2g5AY

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

15. Request: `POST /game/1/move`
Authorization header:
> Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6Im1pa2VAZXhhbXBsZS5jb20ifQ.yw_YW7lY77UncwcPNOxego1l1TcP80o1gHhrdWeg0JU

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

16. Request: `POST /game/1/move`
Authorization header:
> Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6ImNhcmxAZXhhbXBsZS5jb20ifQ.Zt8gtkUjEffgODauxx9gwQ1cnKueML-5ESBkJW2g5AY

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

17. Request: `GET /game/1/status`
Authorization header:
> Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6ImNhcmxAZXhhbXBsZS5jb20ifQ.Zt8gtkUjEffgODauxx9gwQ1cnKueML-5ESBkJW2g5AY

Request body:
```
{ }
```

Response code: `200 OK`
Response body:
```
{
"game_id": 1,
"game_status": "1st player won",
"field": [
["X", "X", "X"],
["O", "O", " "],
[" ", " ", " "],
[" ", " ", " "]
],
"player1": "carl@example.com",
"player2": "mike@example.com",
"size": "4x3"
}
```

### Example 2:

1. Request: POST /signup
Request body:
```
{
"email": "artem@hyperskill.org"
}
```

Response code: `403 Forbidden`
Response body:
```
{
"status": "Registration failed"
}
```
2. Request: `POST /signup`
Request body:
```
{
"email": "artem@hyperskill.org",
"password": "1234"
}
```
Response code: `200 OK`
Response body:
```
{
"status": "Signed Up"
}
```
3. Request: `POST /signin`
Request body:
```
{
"email": "artem@hyperskill.org",
"password": "11111111"
}
```
Response code: `403 Forbidden`
Response body:
```
{
"status": "Authorization failed"
}
```
4. Request: `POST /signin`
Request body:
```
{
"email": "artem@hyperskill.org",
"password": "1234"
}
```
Response code: `200 OK`
Response body:
```
{
"status": "Signed In",
"token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6ImFydGVtQGh5cGVyc2tpbGwub3JnIn0.EYNWizxGYKbvbF1ah4EC4TTnyqbquElEzpJqE5jdwrM"
}
```
5. Request: `POST /game`
Authorization header:
> Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6ImFydGVtQGh5cGVyc2tpbGwub3JnIn0.EYNWizxGYKbvbF1ah4EC4TTnyqbquElEzpJqE5jdwrM

Request body:
```
{
"player1": "",
"player2": "artem@hyperskill.org",
"size": "3x3"
}
```
Response code: `200 OK`
Response body:
```
{
"game_id": 1,
"status": "New game started",
"player1": "",
"player2": "artem@hyperskill.org",
"size": "3x3"
}
```
6. Request: POST /game/1/join
Authorization header:
> Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6ImFydGVtQGh5cGVyc2tpbGwub3JnIn0.EYNWizxGYKbvbF1ah4EC4TTnyqbquElEzpJqE5jdwrM

Request body:
```
{ }
```

Response code: `200 OK`
Response body:
```
{
"status": "Joining the game succeeded"
}
```

7. Request: `POST /game/1/move`
Authorization header:
> Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6ImFydGVtQGh5cGVyc2tpbGwub3JnIn0.EYNWizxGYKbvbF1ah4EC4TTnyqbquElEzpJqE5jdwrM

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
8. Request: `POST /game/1/move`
Authorization header:
> Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6ImFydGVtQGh5cGVyc2tpbGwub3JnIn0.EYNWizxGYKbvbF1ah4EC4TTnyqbquElEzpJqE5jdwrM

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

9. Request: `POST /game/1/move`
Authorization header:
> Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6ImFydGVtQGh5cGVyc2tpbGwub3JnIn0.EYNWizxGYKbvbF1ah4EC4TTnyqbquElEzpJqE5jdwrM

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
10. Request: `POST /game/1/move`
Authorization header:
> Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6ImFydGVtQGh5cGVyc2tpbGwub3JnIn0.EYNWizxGYKbvbF1ah4EC4TTnyqbquElEzpJqE5jdwrM

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
11. Request: `POST /game/1/move`
Authorization header:
> Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6ImFydGVtQGh5cGVyc2tpbGwub3JnIn0.EYNWizxGYKbvbF1ah4EC4TTnyqbquElEzpJqE5jdwrM

Request body:
```
{
"move": "(3,1)"
}
```
Response code: `200 OK`
Response body:
```
{
"status": "Move done"
}
```

12. Request: `GET /game/1/status`
Authorization header:
> Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6ImFydGVtQGh5cGVyc2tpbGwub3JnIn0.EYNWizxGYKbvbF1ah4EC4TTnyqbquElEzpJqE5jdwrM

Request body:
```
{ }
```
Response code: `200 OK`
Response body:
```
{
"game_id": 1,
"game_status": "1st player won",
"field": [
["X", "O", " "],
["X", "O", " "],
["X", " ", " "]
],
"player1": "artem@hyperskill.org",
"player2": "artem@hyperskill.org",
"size": "3x3"
}
```
### HINT : [Eric Joest](https://hyperskill.org/projects/366/stages/2168/implement)
```courseignore
After completing the project, I feel a little silly; the "game not started" 
status does indeed have a place here, but I don't think it's explained very well. 

When a game is created via the post("/game") path, the response for THAT CALL ONLY 
should have a status of "New game started". Immediately after, it should switch to 
"game not started" until a second player joins. The first response should really 
be changed to "New game created", rather than "started," and I think that would 
make it clearer.
```