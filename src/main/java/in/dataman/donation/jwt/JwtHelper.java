package in.dataman.donation.jwt;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import in.dataman.donation.comcontroller.SseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@SuppressWarnings("deprecation")
@Component
public class JwtHelper {

    public static final long JWT_TOKEN_VALIDITY = 1 * 60 * 60 * 1000; // 1 hour
    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    @Autowired
    private SessionManagementService sessionManagementService;
    @Autowired
    private SseController sseController;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public JwtHelper() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp521r1");
            keyPairGenerator.initialize(ecSpec);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            this.privateKey = keyPair.getPrivate();
            this.publicKey = keyPair.getPublic();
        } catch (Exception e) {
            throw new RuntimeException("Error initializing ECDSA key pair", e);
        }
    }

    public String generateToken(String userId) {
        Map<String, Object> claims = new HashMap<>();
        String sessionId = UUID.randomUUID().toString(); // Generate unique session ID
        claims.put("userId", userId);
        System.out.println("User Id in JWT Claims: "+userId);
        claims.put("sessionId", sessionId);

        String token = Jwts.builder()
                .claims(claims)
                .subject(userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY))
                .signWith(privateKey, SignatureAlgorithm.ES512)
                .compact();

        sessionManagementService.storeSession(userId, token,JWT_TOKEN_VALIDITY);
        sseController.notifyTokenUpdate(userId);
        return token;
    }


    public Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }


    // Retrieve username from JWT token
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }


	/*
	 * private void storeSessionInRedis(String userId, String sessionId, String
	 * token) { String oldSessionId =
	 * redisTemplate.opsForValue().get("USER_SESSION_" + userId); if (oldSessionId
	 * != null) { redisTemplate.delete("SESSION_" + oldSessionId); }
	 * 
	 * redisTemplate.opsForValue().set("USER_SESSION_" + userId, sessionId,
	 * JWT_TOKEN_VALIDITY, TimeUnit.MILLISECONDS);
	 * redisTemplate.opsForValue().set("SESSION_" + sessionId, token,
	 * JWT_TOKEN_VALIDITY, TimeUnit.MILLISECONDS); }
	 */

    public boolean validateToken(String token) {
        try {
            String sessionId = getClaimFromToken(token, claims -> claims.get("sessionId", String.class));
            if (sessionId == null) return false;

            String storedToken = redisTemplate.opsForValue().get("SESSION_" + sessionId);
            return token.equals(storedToken) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        Claims claims = Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claimsResolver.apply(claims);
    }

    public boolean isRateLimited(String userId) {
        String rateLimitKey = "RATE_LIMIT_" + userId;
        Long requestCount = redisTemplate.opsForValue().increment(rateLimitKey);

        if (requestCount == 1) {
            redisTemplate.expire(rateLimitKey, 1, TimeUnit.MINUTES); // Set expiration for rate limit key
        }

        return requestCount > 100;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }
    
    public boolean invalidateSession(String token) {
        try {
            String mobileNumber = getClaimFromToken(token, Claims::getSubject);
            redisTemplate.delete("USER_SESSION_" + mobileNumber);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    
    

    public long getExpirationTimeFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(publicKey) // Use public key for verification
                .build()
                .parseSignedClaims(token)
                .getBody();
        return claims.getExpiration().getTime(); // Return expiration time in milliseconds
    }
}



