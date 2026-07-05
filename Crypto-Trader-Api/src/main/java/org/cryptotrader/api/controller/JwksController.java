package org.cryptotrader.api.controller;

import jakarta.annotation.security.PermitAll;
import org.cryptotrader.api.library.services.rsa.RsaKeyService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Exposes the server's JSON Web Key Set (JWKS).
 *
 * This endpoint publishes the RSA public key used to sign access tokens (RS256). Clients can retrieve the key
 * at the well-known path and verify JWT signatures using the matching kid.
 *
 * Path: /.well-known/jwks.json
 * Content-Type: application/json
 *
 * The returned JSON has the shape: { "keys": [ { "kty":"RSA", "kid":"...", "alg":"RS256", "use":"sig", "n":"...", "e":"..." } ] }
 * where:
 * - kty: key type (RSA)
 * - kid: key identifier corresponding to the JWT header kid
 * - alg: intended signing algorithm (RS256)
 * - use: public key use (sig)
 * - n: modulus (base64url without leading sign byte)
 * - e: exponent (base64url)
 */
@RestController
@PermitAll
public class JwksController {
    private final RsaKeyService rsaKeyService;

    public JwksController(RsaKeyService rsaKeyService) {
        this.rsaKeyService = rsaKeyService;
    }

    /**
     * Return the current JWKS containing a single RSA signing key.
     *
     * @return a JSON object with a "keys" array compatible with RFC 7517
     */
    @GetMapping(value = "/.well-known/jwks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> jwks() {
        RSAPublicKey publicKey = this.rsaKeyService.getPublicKey();
        Map<String, Object> jwk = new HashMap<>();
        jwk.put("kty", "RSA");
        jwk.put("kid", this.rsaKeyService.getKid());
        jwk.put("alg", "RS256");
        jwk.put("use", "sig");
        jwk.put("n", base64urlUnsigned(publicKey.getModulus()));
        jwk.put("e", base64urlUnsigned(publicKey.getPublicExponent()));
        return Map.of("keys", List.of(jwk));
    }

    /**
     * Base64url-encode a BigInteger without sign byte, as required by JWK fields.
     */
    private static String base64urlUnsigned(BigInteger big) {
        byte[] bytes = toUnsigned(big);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Convert a BigInteger to an unsigned big-endian byte array (strip leading 0x00 if present).
     */
    private static byte[] toUnsigned(BigInteger big) {
        byte[] bytes = big.toByteArray();
        if (bytes.length > 1 && bytes[0] == 0x00) {
            byte[] trimmed = new byte[bytes.length - 1];
            System.arraycopy(bytes, 1, trimmed, 0, trimmed.length);
            return trimmed;
        }
        return bytes;
    }
}
