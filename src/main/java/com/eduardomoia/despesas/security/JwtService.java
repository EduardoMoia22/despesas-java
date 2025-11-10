package com.eduardomoia.despesas.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.eduardomoia.despesas.config.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    private Algorithm getAlgorithm() {
        return Algorithm.HMAC256(jwtProperties.secret());
    }

    public String generateToken(UserDetails userDetails) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusMillis(jwtProperties.expirationMs());

        return JWT.create()
                .withSubject(userDetails.getUsername())
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(expiresAt))
                .sign(getAlgorithm());
    }

    public String extractUsername(String token) throws JWTVerificationException {
        JWTVerifier verifier = JWT.require(getAlgorithm()).build();
        DecodedJWT decodedJWT = verifier.verify(token);
        return decodedJWT.getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            return username.equals(userDetails.getUsername());
        } catch (JWTVerificationException ex) {
            return false;
        }
    }
}
