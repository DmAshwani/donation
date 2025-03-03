package in.dataman.donation.uitl;

import java.security.SecureRandom;

public class OTPUtil {
    private static final SecureRandom random = new SecureRandom();
    
    public static String generateOTP() {
        int otp = 1000 + random.nextInt(9000);  // Generate a 4-digit OTP
        return String.valueOf(otp);
    }
}
