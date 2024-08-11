package tictactoeonline.util

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*
import kotlin.test.assertEquals

/**
 * JWT test
 * used to test and gain better understanding of JWT
 * @constructor Create empty J w t test
 */
class JWTTest {
    val secret = "ut920BwH09AOEDx5"

    @Test
    fun `simple JWT token`() {
        val email = "test@email.com"
        val token = JWT.create()
            .withClaim("email", email)
            .sign(Algorithm.HMAC256(secret))
        println(
            """
            ${"#".repeat(80)}
            token: $token
            ${"#".repeat(80)}
        """.trimIndent()
        )
        assertEquals(3, token.split("\\.".toRegex()).size)
        val header = token.split("\\.".toRegex())[0]
        val payload = token.split("\\.".toRegex())[1]
        val decoder = Base64.getDecoder()
        println(String(decoder.decode(header)))
        assertEquals("""{"alg":"HS256","typ":"JWT"}""", String(decoder.decode(header)))
        assertEquals("""{"email":"test@email.com"}""", String(decoder.decode(payload)))

    }

    @Test
    fun `JWT token with expiration date`() {
        val email = "test@email.com"
        val audience = "myAudience"
        val issuer = "http://javapda.com"
        val tz: ZoneId = ZoneId.systemDefault()
        val tzo: ZoneOffset = ZoneOffset.ofHours(0)
        val issuedDate = LocalDateTime.of(2024, 8, 9, 7, 12)
        val expirationDate = issuedDate.plusMinutes(10) // good for 10 minutes
        val token = JWT.create()
            .withPayload(mapOf("email" to email))
            .withAudience(audience)
            .withIssuer(issuer)
            .withIssuedAt(issuedDate.toInstant(tzo))
            .withExpiresAt(expirationDate.toInstant(tzo))
            .sign(Algorithm.HMAC256(secret))

        println(
            """
            ${"#".repeat(80)}
            token: $token
            ${"#".repeat(80)}
        """.trimIndent()
        )
        assertEquals(3,token.split("\\.".toRegex()).size)
        val header = token.split("\\.".toRegex())[0]
        val payload = token.split("\\.".toRegex())[1]
        val decoder = Base64.getDecoder()
        println(String(decoder.decode(header)))
        assertEquals("""{"alg":"HS256","typ":"JWT"}""",String(decoder.decode(header)))
        assertEquals("""{"email":"test@email.com","aud":"myAudience","iss":"http://javapda.com","iat":1723187520,"exp":1723188120}""",String(decoder.decode(payload)))

    }
}