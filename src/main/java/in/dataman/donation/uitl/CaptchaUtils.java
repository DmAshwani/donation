package in.dataman.donation.uitl;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class CaptchaUtils {

    private static RedisTemplate<String, String> redisTemplate;

    private static final long CAPTCHA_EXPIRATION_MINUTES = 15; // Expiration time

   
    public CaptchaUtils(RedisTemplate<String, String> redisTemplate) {
        CaptchaUtils.redisTemplate = redisTemplate;
    }

    // Generate Random CAPTCHA Text
    public static String generateCaptchaText() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz123456789";
        Random random = new Random();
        StringBuilder captcha = new StringBuilder();
        
        while (captcha.length() < 4) {
            int index = random.nextInt(chars.length());
            captcha.append(chars.charAt(index));
        }
        return captcha.toString();
    }

    // Generate CAPTCHA Image
    public static BufferedImage generateCaptchaImage(String text) {
        int width = 160, height = 60;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // Background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        // Font & Text
        g2d.setFont(new Font("Arial", Font.BOLD, 40));
        g2d.setColor(Color.BLACK);
        g2d.drawString(text, 20, 45);

        g2d.dispose();
        return image;
    }

    // Split CAPTCHA Image into Upper and Lower Parts
    public static String[] splitCaptchaImage(BufferedImage image) throws IOException {
        int width = image.getWidth();
        int height = image.getHeight();
        int halfHeight = height / 2;

        // Upper Half
        BufferedImage upperHalf = image.getSubimage(0, 0, width, halfHeight);
        String upperBase64 = encodeImageToBase64(upperHalf);

        // Lower Half
        BufferedImage lowerHalf = image.getSubimage(0, halfHeight, width, height - halfHeight);
        String lowerBase64 = encodeImageToBase64(lowerHalf);

        return new String[]{upperBase64, lowerBase64};
    }

    // Encode Image to Base64
    private static String encodeImageToBase64(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    // Store CAPTCHA in Redis
    public static void storeCaptcha(String captchaKey, String captchaText) {
        redisTemplate.opsForValue().set(captchaKey, captchaText, CAPTCHA_EXPIRATION_MINUTES, TimeUnit.MINUTES);
    }

    // Verify CAPTCHA from Redis
    public static boolean verifyCaptcha(String captchaKey, String captchaText) {
        String storedCaptcha = redisTemplate.opsForValue().get(captchaKey);
        if (storedCaptcha != null && storedCaptcha.equalsIgnoreCase(captchaText)) {
            redisTemplate.delete(captchaKey); // Remove after verification
            return true;
        }
        return false;
    }
}
