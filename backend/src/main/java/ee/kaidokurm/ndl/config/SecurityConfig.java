package ee.kaidokurm.ndl.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSAlgorithmFamilyJWSKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;

@Configuration
public class SecurityConfig {

    @Value("${auth0.audience}")
    private String audience;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()).cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // allow health and swagger in local
                        .requestMatchers("/actuator/health", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                        // allow ping for quick checks
                        .requestMatchers(HttpMethod.GET, "/api/ping").permitAll()
                        // allow public auth endpoints
                        .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login", "/api/auth/refresh")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/email-verification/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/password-reset/**").permitAll()
                        // everything else under /api requires auth
                        .requestMatchers("/api/**").authenticated().anyRequest().permitAll())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }

    /**
     * Generate RSA key pair for signing local JWT tokens (email/password auth).
     * Created as a bean so it's consistent across encoder and decoder.
     */
    @Bean
    public JWKSource<SecurityContext> localJwkSource() throws Exception {
        RSAKey key = new RSAKeyGenerator(2048).generate();
        JWKSet jwkSet = new JWKSet(key);
        return new ImmutableJWKSet<>(jwkSet);
    }

    /**
     * Validates that Auth0 JWT contains the expected audience.
     * Auth0 access tokens include "aud" to prevent token misuse across APIs.
     */
    private OAuth2TokenValidatorResult validateAudience(Jwt jwt) {
        List<String> audiences = jwt.getAudience();
        if (audiences != null && audiences.contains(audience)) {
            return OAuth2TokenValidatorResult.success();
        }
        OAuth2Error err = new OAuth2Error("invalid_token", "The required audience is missing", null);
        return OAuth2TokenValidatorResult.failure(err);
    }

    /**
     * Decoder for Auth0 OAuth2 tokens.
     * Validates issuer, signature, and audience against Auth0 configuration.
     */
    @Bean
    public JwtDecoder auth0Decoder(@Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuer) {
        NimbusJwtDecoder decoder = JwtDecoders.fromIssuerLocation(issuer);
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);
        OAuth2TokenValidator<Jwt> withAudience = this::validateAudience;
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withIssuer, withAudience));
        return decoder;
    }

    /**
     * Decoder for local email/password JWT tokens.
     * Validates issuer and signature using the local RSA key.
     */
    @Bean
    public JwtDecoder localJwtDecoder(
            JWKSource<SecurityContext> localJwkSource,
            @Value("${app.jwt.issuer}") String localIssuer) {
        DefaultJWTProcessor<SecurityContext> processor = new DefaultJWTProcessor<>();
        JWSKeySelector<SecurityContext> keySelector = new JWSAlgorithmFamilyJWSKeySelector<SecurityContext>(
                JWSAlgorithm.Family.RSA, localJwkSource);
        processor.setJWSKeySelector(keySelector);

        NimbusJwtDecoder decoder = new NimbusJwtDecoder(processor);
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(localIssuer);
        decoder.setJwtValidator(withIssuer);
        return decoder;
    }

    /**
     * Multi-issuer JWT decoder that routes tokens to the appropriate decoder.
     * - Auth0 tokens (issuer: Auth0 domain) → auth0Decoder
     * - Local tokens (issuer: "numbers-dont-lie") → localJwtDecoder
     * 
     * This allows supporting both OAuth2 and email/password authentication.
     * Marked as @Primary so Spring Security uses this one when requesting
     * JwtDecoder.
     */
    @Bean
    @org.springframework.context.annotation.Primary
    public JwtDecoder jwtDecoder(
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String auth0Issuer,
            @Value("${app.jwt.issuer}") String localIssuer,
            JwtDecoder auth0Decoder,
            JwtDecoder localJwtDecoder) {
        return new MultiIssuerJwtDecoder(auth0Issuer, localIssuer, auth0Decoder, localJwtDecoder);
    }

    /**
     * Bean for encoding (generating) JWTs for email/password login.
     * Uses the same RSA key as the localJwtDecoder for validation.
     */
    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> localJwkSource) {
        return new NimbusJwtEncoder(localJwkSource);
    }
}
