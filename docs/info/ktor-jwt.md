# ktor-jwt | [main README.md](../README.md)

* [Ktor on JWT](https://ktor.io/docs/server-jwt.html)
* [Ktor 1.6.8 on JWT](https://ktor.io/docs/old/welcome.html)
* [JWT : JSON Web Token](https://jwt.io/)

### dependencies
* build.gradle.kts
```
dependencies {
    implementation("io.ktor:ktor-server-auth:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jwt:$ktor_version")
}
```

### Code examples

* [auth-jwt-hs256 : signed with HS256 algorithm](https://github.com/ktorio/ktor-documentation/tree/2.3.12/codeSnippets/snippets/auth-jwt-hs256)
* [auth-jwt-rs256 : signed with public/private key pair](https://github.com/ktorio/ktor-documentation/tree/2.3.12/codeSnippets/snippets/auth-jwt-rs256)

