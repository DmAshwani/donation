package in.dataman.donation.comcontroller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import dataman.dmbase.encryptiondecryptionutil.EncryptionDecryptionUtilNew;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;

import in.dataman.donation.comentity.UserMast;
import in.dataman.donation.comrepository.UserMastRepository;
import in.dataman.donation.comservice.OTPService;
import in.dataman.donation.jwt.JwtHelper;
import in.dataman.donation.uitl.AuthKeyUtil;
import in.dataman.donation.uitl.CaptchaUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = { "http://192.168.7.57:3000" }, originPatterns = "**", allowCredentials = "true", exposedHeaders = { "authKey", "token", "secretKey" })
@Tag(name = "Authentication", description = "APIs for user authentication using OTP and CAPTCHA")
public class LoginController {

    @Autowired
    private OTPService otpService;

    @Autowired
    private JwtHelper jwtHelper;


    @Autowired
    private EncryptionDecryptionUtilNew encryptionDecryptionUtilNew;

    @Autowired
    private AuthKeyUtil authKeyUtil;

    
    @Autowired
    private UserMastRepository userMastRepository;


    private static final Pattern MOBILE_PATTERN = Pattern.compile("^[6-9]\\d{9}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    @Operation(summary = "Send OTP", description = "Sends OTP to a mobile number or email after CAPTCHA verification for emails.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OTP sent successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content(schema = @Schema(implementation = String.class)))
            })
    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody JsonNode request) {
        String data = request.has("data") ? request.get("data").asText() : null;
        Integer nature = request.has("nature") ? request.get("nature").asInt() : null;

        if (data == null || nature == null) {
            return ResponseEntity.badRequest().body("{\"message\": \"Invalid request data\"}");
        }

        if (nature == 0) { // Mobile OTP
            if (!MOBILE_PATTERN.matcher(data).matches()) {
                return ResponseEntity.badRequest().body("{\"message\": \"Invalid Indian mobile number\"}");
            }
        } else if (nature == 1) { // Email OTP requires CAPTCHA verification
            if (!EMAIL_PATTERN.matcher(data).matches()) {
                return ResponseEntity.badRequest().body("{\"message\": \"Invalid email format\"}");
            }
        } else {
            return ResponseEntity.badRequest().body("{\"message\": \"Invalid nature value\"}");
        }

        otpService.generateAndSendOtp(data, nature);
        return ResponseEntity.ok("{\"message\": \"OTP sent successfully\"}");
    }

    @Operation(summary = "Generate CAPTCHA", description = "Generates and stores CAPTCHA in Redis.")
    @GetMapping("/generate-captcha")
    public Map<String, String> generateCaptcha() {
        Map<String, String> response = new HashMap<>();
        try {
            String captchaText = CaptchaUtils.generateCaptchaText();
            String captchaKey = UUID.randomUUID().toString();
            System.out.println("Captcha Text "+captchaText);
            // Store in Redis
            CaptchaUtils.storeCaptcha(captchaKey, captchaText);

            String[] captchaParts = CaptchaUtils.splitCaptchaImage(CaptchaUtils.generateCaptchaImage(captchaText));
            response.put("captchaKey", captchaKey);
            response.put("upperImage", "data:image/png;base64," + captchaParts[0]);
            response.put("lowerImage", "data:image/png;base64," + captchaParts[1]);
            response.put("message", "CAPTCHA generated successfully!");
        } catch (Exception e) {
            response.put("error", "Failed to generate CAPTCHA: " + e.getMessage());
        }
        return response;
    }

    
    @Operation(summary = "Verify CAPTCHA", description = "Verifies CAPTCHA stored in Redis.")
    @PostMapping("/verify-captcha")
    public ResponseEntity<?> verifyCaptcha(@RequestParam String captchaKey, @RequestParam String captchaText) {
        boolean isValid = CaptchaUtils.verifyCaptcha(captchaKey, captchaText);
        if (isValid) {
            return ResponseEntity.ok("CAPTCHA verified successfully!");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired CAPTCHA.");
        }
    }


    @Operation(summary = "Verify OTP", description = "Verifies the OTP and returns a JWT token if successful.")
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody JsonNode request) throws Exception {
        String data = request.has("data") ? request.get("data").asText() : null;
        String otp = request.has("otp") ? request.get("otp").asText() : null;

        if (data == null || otp == null) {
            return ResponseEntity.badRequest().body("{\"message\": \"Data and OTP are required\"}");
        }

        boolean isValid = otpService.verifyOtp(data, "Login", otp);
        if (!isValid) {
            return ResponseEntity.status(401).body("{\"message\": \"Invalid or expired OTP\"}");
            
         
        }
        String jwtToken = jwtHelper.generateToken(data);
        HttpHeaders headers = new HttpHeaders();
        String id = authKeyUtil.generateAuthKey();
        authKeyUtil.storeAuthKey(id, 60*60*1000);
        String encryptedAuthKey = encryptionDecryptionUtilNew.encrypt(id);

        headers.add("secretKey", String.valueOf(encryptionDecryptionUtilNew.getSecretKey()));
        headers.add("authKey", encryptedAuthKey);
        headers.add("token", jwtToken);

        
        UserMast user = userMastRepository.findByEmailOrMobile(data,data);
        String role = (user != null) ? user.getUser_Role() : null;

        Map<String, Object> response = new HashMap<>();
        response.put("jwtToken", jwtToken);
        response.put("role", role);
        response.put("secretKey", encryptionDecryptionUtilNew.getSecretKey());


        return ResponseEntity.ok()
                .headers(headers)
                .body(response);
    }

    @Operation(summary = "Logout", description = "Invalidates the JWT session token.")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            boolean isLoggedOut = jwtHelper.invalidateSession(token);

            if (isLoggedOut) {
                return ResponseEntity.ok("{\"message\": \"Logged out successfully\"}");
            } else {
                return ResponseEntity.status(500).body("{\"message\": \"Logout failed\"}");
            }
        }
        return ResponseEntity.badRequest().body("{\"message\": \"Invalid token\"}");
    }

    @PostMapping("/get-secret-key")
    public ResponseEntity<?> getSecretKey(){
        Map<String, Object> response = new HashMap<>();
        response.put("secretKey1", encryptionDecryptionUtilNew.getSecretKey());
        response.put("secretKey2", encryptionDecryptionUtilNew.getSecretKey());
        response.put("secretKey3", encryptionDecryptionUtilNew.getSecretKey());
        response.put("secretKey4", encryptionDecryptionUtilNew.getSecretKey());
        response.put("secretKey5", encryptionDecryptionUtilNew.getSecretKey());
        response.put("secretKey6", encryptionDecryptionUtilNew.getSecretKey());
        response.put("secretKey7", encryptionDecryptionUtilNew.getSecretKey());

        System.out.println(response.get("secretKey1"));
        return ResponseEntity.ok(response.get("secretKey1"));
    }
}
