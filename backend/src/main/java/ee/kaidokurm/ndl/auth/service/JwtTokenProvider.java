package ee.kaidokurm.ndl.auth.service;

import ee.kaidokurm.ndl.auth.config.AuthProperties;
import ee.kaidokurm.ndl.auth.user.model.UserEntity;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/**
 * Service for generating JWT tokens for email/password authentication. Uses
 * Spring Security's JwtEncoder for token generation. Configuration is
 * centralized in AuthProperties.
 */
@Service
public class JwtTokenProvider {

    private final JwtEncoder jwtEncoder;
    private final AuthProperties authProperties;

    public JwtTokenProvider(JwtEncoder jwtEncoder, AuthProperties authProperties) {
        this.jwtEncoder = jwtEncoder;
        this.authProperties = authProperties;
    }

    /**
     * Generate an access token for a user. Token contains: sub (user ID), email,
     * iss, exp, iat
     */
    public String generateAccessToken(UserEntity user) {
        Instant now = Instant.now();
        long expiryMinutes = authProperties.getJwt().getAccessTokenExpiryMinutes();
        Instant expiresAt = now.plus(expiryMinutes, ChronoUnit.MINUTES);

        JwtClaimsSet claims = JwtClaimsSet.builder().issuer(authProperties.getJwt().getIssuer())
                .subject(user.getId().toString()).claim("email", user.getEmail()).issuedAt(now).expiresAt(expiresAt)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
