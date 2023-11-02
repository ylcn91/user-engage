package com.userengage.security;

import com.userengage.exception.IncompleteKeyFileException;
import com.userengage.exception.KeyFileNotFoundException;
import io.smallrye.jwt.auth.principal.JWTAuthContextInfo;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.build.JwtClaimsBuilder;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import io.smallrye.jwt.auth.principal.DefaultJWTCallerPrincipalFactory;
import io.smallrye.jwt.auth.principal.JWTCallerPrincipalFactory;

@Slf4j
@ApplicationScoped
public class TokenProvider {

    @ConfigProperty(name = "jwt.private.key.location")
    String privateKeyLocation;

    @ConfigProperty(name = "jwt.token.expiration")
    long tokenExpiration;

    @ConfigProperty(name = "jwt.key.id")
    String keyId;

    @ConfigProperty(name = "jwt.public.key.location")
    String publicKeyLocation;

    private static final String PRIVATE_KEY_BEGIN = "-----BEGIN PRIVATE KEY-----";
    private static final String PRIVATE_KEY_END = "-----END PRIVATE KEY-----";

    private static final String PUBLIC_KEY_BEGIN = "-----BEGIN PUBLIC KEY-----";
    private static final String PUBLIC_KEY_END = "-----END PUBLIC KEY-----";
    private static final String NEWLINE_REGEX = "\\n";
    private static final String RSA_ALGORITHM = "RSA";

    public String createToken(String username, String[] roles) throws Exception {
        log.info("Creating token for user: {}", username);
        JwtClaimsBuilder claimsBuilder = Jwt.claims()
                .subject(username)
                .issuedAt(System.currentTimeMillis())
                .expiresAt(System.currentTimeMillis() + tokenExpiration);
        log.info("Adding roles to the token");
        for (String role : roles) {
            claimsBuilder.groups(role);
        }

        PrivateKey privateKey = getPrivateKey();
        log.info("Signing the token with private key");
        return claimsBuilder.jws().keyId(keyId).sign(privateKey);
    }

    private PrivateKey getPrivateKey() throws Exception {
        byte[] keyBytes = loadKeyFile(privateKeyLocation);
        return generatePrivateKey(parseKeyString(new String(keyBytes)));
    }

    private PublicKey getPublicKey() throws Exception {
        byte[] keyBytes = loadKeyFile(publicKeyLocation);
        return generatePublicKey(parseKeyString(new String(keyBytes)));
    }

    private byte[] loadKeyFile(String keyLocation) throws IOException {
        log.info("Loading key from: {}", keyLocation);
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(keyLocation)) {
            if (inputStream == null) {
                log.error("Key file not found: {}", keyLocation);
                throw new KeyFileNotFoundException(keyLocation);
            }
            byte[] keyBytes = new byte[inputStream.available()];
            int bytesRead = inputStream.read(keyBytes);
            if (bytesRead != keyBytes.length) {
                log.error("Failed to read the entire key file: {} bytes read, expected {}", bytesRead, keyBytes.length);
                throw new IncompleteKeyFileException(keyLocation, bytesRead, keyBytes.length);
            }
            return keyBytes;
        }
    }

    private String parseKeyString(String keyString) {
        log.info("Removing header and footer from the key");
        if (keyString.contains(PUBLIC_KEY_BEGIN)) {
            return keyString.replace(PUBLIC_KEY_BEGIN, "").replace(PUBLIC_KEY_END, "").replaceAll(NEWLINE_REGEX, "");
        } else if (keyString.contains(PRIVATE_KEY_BEGIN)) {
            return keyString.replace(PRIVATE_KEY_BEGIN, "").replace(PRIVATE_KEY_END, "").replaceAll(NEWLINE_REGEX, "");
        } else {
            log.error("Unknown key type");
            throw new IllegalArgumentException("Unknown key type");
        }
    }
    private PrivateKey generatePrivateKey(String keyString) throws Exception {
        log.info("Generating private key from the decoded bytes");
        byte[] decodedKeyBytes = Base64.getDecoder().decode(keyString);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKeyBytes);
        return keyFactory.generatePrivate(keySpec);
    }

    private PublicKey generatePublicKey(String keyString) throws Exception {
        log.info("Generating public key from the decoded bytes");
        byte[] decoded = Base64.getDecoder().decode(keyString);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
        return keyFactory.generatePublic(keySpec);
    }


    public boolean validateToken(String token) {
        try {
            log.info("Validating token");
            JWTAuthContextInfo contextInfo = new JWTAuthContextInfo(getPublicKey(), null);  // 'null' as issuer is not used
            JWTCallerPrincipalFactory factory = DefaultJWTCallerPrincipalFactory.instance();
            factory.parse(token, contextInfo);
            log.info("Token is valid");
            return true;  // Token is valid
        } catch (Exception e) {
            log.error("Invalid token: {}", e.getMessage());
            return false;  // Token is invalid
        }
    }


}
