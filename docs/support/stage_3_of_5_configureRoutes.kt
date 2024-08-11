package tictactoeonline

//imports ...


fun Application.configureRouting() {

    // Game and user storage (the Game and User classes you should implement yourself)
    val GameStore: MutableList<Game> = mutableListOf()
    val UserStore: MutableList<User> = mutableListOf()

    routing {

        // Routes not protected by the JWT
        post("/signup") {
            // ...
        }

        post("/signin") {
            // ...
        }


        // JWT-protected routes
        authenticate("auth-jwt") {

            post("/game") {
                // ...
            }

            get("/games") {
                // ...
            }

            // ...

        }

    }
}