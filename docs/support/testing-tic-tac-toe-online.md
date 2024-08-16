# testing-tic-tac-toe-online | [main README.md](../../readme.md)

## thoughts
when I began the Tic-Tac-Toe Online I started using [Postman](https://www.postman.com/) as the way to communicate with the application.
[Postman](https://www.postman.com/) is a good tool for simple requests but gets more interesting as you develop a workflow, or a sequence
of interactions with a [RESTful](https://restfulapi.net/) site.

at some point it grew tiresome and I began looking for an alternative way to test. That's when I discovered, something,
that up-to-now, had not been covered (or at least I don't recall it being covered) - testing a Ktor application.
Specifically [server-side testing](https://ktor.io/docs/server-testing.html)

It was really pretty simple to get going.

You can see the bulk of this testing in [ApplicationTest.kt](../../app/src/test/kotlin/tictactoeonline/ApplicationTest.kt)
