package in.dataman.donation.uitl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class AuthKeyUtil {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // Store authKey in Redis with an expiration time
    public void storeAuthKey(String authKey, long expirationTime) {
        long expiry = (expirationTime > 0) ? expirationTime : TimeUnit.MINUTES.toMillis(30);
        redisTemplate.opsForValue().set(authKey, "authKey", expiry, TimeUnit.MILLISECONDS);
        System.out.println("Stored session: key=" + authKey + ", token=" + "authKey" + ", expirationTime=" + expiry);
    }

    // Delete authKey from Redis
    public void deleteAuthKey(String authKey) {
        redisTemplate.delete(authKey);
        System.out.println("Deleted session: key=" + authKey);
    }

    public String getAuthKey(String authKey) {
        String storedValue = redisTemplate.opsForValue().get(authKey);
        if (storedValue != null) {
            System.out.println("Retrieved session: key=" + authKey + ", token=" + storedValue);
        } else {
            System.out.println("AuthKey not found for key: " + authKey);
        }
        return storedValue;
    }
    
    public String generateAuthKey() {
    	return UUID.randomUUID().toString();
    }

}
