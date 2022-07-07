package server

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import java.security.Key
import java.util.*

object JWT {
    private val key: Key = Keys.secretKeyFor(SignatureAlgorithm.HS256)
    fun createJWT(username: String?): String {
        return Jwts.builder()
            .setSubject(username)
            .signWith(key).compact()
    }

    fun extractUserName(jwt: String?): String {
        return Jwts.parserBuilder().setSigningKey(key).build()
            .parseClaimsJws(jwt)
            .getBody()
            .getSubject()
    }
}
