package in.dataman.donation.jwt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class SessionManagementService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String SESSION_PREFIX = "ACTIVE_SESSION:";

    // Store the active session for the user
    public void storeSession(String userId, String token, long expirationTime) {
        String redisKey = SESSION_PREFIX + userId.toLowerCase(); // Convert to lowercase

        // Check if a session already exists
        String existingToken = redisTemplate.opsForValue().get(redisKey);
        if (existingToken != null) {
            System.out.println("Invalidating existing session for userId: " + userId);
            invalidateSession(userId);
        }

        // Store the new session
        redisTemplate.opsForValue().set(redisKey, token, expirationTime > 0 ? expirationTime : 30 * 60 * 1000, TimeUnit.MILLISECONDS);
        System.out.println("Stored session: key=" + redisKey + ", token=" + token + ", expirationTime=" + expirationTime);
    }

    // Invalidate the active session
    public void invalidateSession(String userId) {
        String redisKey = SESSION_PREFIX + userId.toLowerCase(); // Convert to lowercase
        System.out.println("Invalidating session for userId: " + userId);
        redisTemplate.delete(redisKey);
    }

    // Validate the session during requests
    public boolean validateSession(String userId, String token) {
        String redisKey = SESSION_PREFIX + userId.toLowerCase(); // Convert to lowercase
        // Check if userId or token is null
        if (userId == null || token == null) {
            System.out.println("Validation failed: userId or token is null");
            return false;
        }

        String activeToken = redisTemplate.opsForValue().get(redisKey);
        System.out.println("Validating session: key=" + redisKey + ", retrievedToken=" + activeToken);
        boolean tokens = token.equals(activeToken);
        System.out.println(tokens);
        return tokens ;
    }
}


