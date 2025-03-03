package in.dataman.donation.comservice;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import in.dataman.donation.comentity.LinkVerification;
import in.dataman.donation.comrepository.LinkVerificationRepository;
import in.dataman.donation.config.ExternalConfig;
import in.dataman.donation.uitl.DmPasswaord;
import in.dataman.donation.uitl.OTPUtil;
import jakarta.mail.internet.MimeMessage;

@Service
public class OTPService {
	
	@Autowired
	private LinkVerificationRepository linkVerificationRepository;
    
	private final long OTP_EXPIRATION_MINUTES = 15;
    
    private final RestTemplate restTemplate = new RestTemplate(); 
   
    @Autowired
    private JavaMailSender javaMailSender;
    

    @Autowired
    @Qualifier("TransactionJdbcTemplate")
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    @Qualifier("companyJdbcTemplate")
    private JdbcTemplate jdbcTemplateCom;



    @Autowired
    private ExternalConfig externalConfig;
    
    
    // Generate and send OTP
    public String generateAndSendOtp(String data, int nature) {
        // Generate OTP (a random 4-digit number)
        String otp = OTPUtil.generateOTP();
        String generatedUUID = UUID.randomUUID().toString();
        String keyField = "Login";

        if(nature == 0) {
        	// Store OTP in the database
            storeOtpInDatabase(data, keyField, otp, generatedUUID);
            System.out.println(otp);
            // Send OTP via SMS
            sendOtpToMobile(data, otp);
        } else if (nature == 1) {
        	storeOtpInDatabase(data, keyField, otp, generatedUUID);
        	System.out.println(otp);
        	sendOtpToEmail(data, otp);
        }
        
        

        return otp;
    }
    
    // Store OTP in the database
    private void storeOtpInDatabase(String mobile, String keyField, String otp, String generatedUUID) {
        LinkVerification linkVerification = new LinkVerification();
        linkVerification.setMobile(mobile);
        linkVerification.setKeyField(keyField);
        linkVerification.setOtp(otp);
        linkVerification.setCode(generatedUUID);
        linkVerification.setPreparedDt(LocalDateTime.now()); // Set creation timestamp
        linkVerificationRepository.save(linkVerification);
    }
    
    


 // Verify OTP and delete after successful verification
    public boolean verifyOtp(String mobile, String keyField, String otp) {
        LinkVerification linkVerification = linkVerificationRepository.findByMobileAndOtp(mobile, otp);

        if (linkVerification != null && linkVerification.getOtp().equals(otp)) {
            // Verify if OTP has expired
            if (linkVerification.getPreparedDt().isBefore(LocalDateTime.now().minusMinutes(OTP_EXPIRATION_MINUTES))) {
                
                linkVerificationRepository.delete(linkVerification); // Remove expired OTP
                return false;
            }

            // Delete the record after verification
            linkVerificationRepository.deleteByMobileAndKeyField(mobile, keyField);
            return true;
        }
        return false;
    }

    private void sendOtpToMobile(String mobile, String otp) {
        try {
            // Fetch API credentials dynamically
            String sqlAccount = "SELECT accountName, password, url, format FROM wjAccountMast WHERE nature = 0 AND isActive = 1";
            Map<String, Object> accountResult = jdbcTemplateCom.queryForMap(sqlAccount);
            
            String username = (String) accountResult.get("accountName");
            String encryptedPassword = (String) accountResult.get("password");
            String smsUrl = (String) accountResult.get("url");
            String format = (String) accountResult.get("format");

            // Decrypt the password if necessary
            String password = DmPasswaord.dCodify(encryptedPassword);
            
            // Step 1: Get vType for OTP category
            String sqlVtype = "SELECT TOP 1 vt.v_Type FROM voucher_Type vt WHERE vt.isActive = 1 AND vt.category = 'OTP'";
            int vType = jdbcTemplate.queryForObject(sqlVtype, Integer.class); // Use queryForObject to get a single result
            
            // Step 2: Get template code using vType
            String sqlTempCode = "SELECT templateCode FROM attachWJTemplates WHERE v_type = ?";
            Long templateCode = jdbcTemplate.queryForObject(sqlTempCode, Long.class, vType); // Pass vType as parameter
            
            // Step 3: Fetch OTP message template using templateCode
            String sqlTemplate = "SELECT message, manualCode FROM wjTemplateMast WHERE code = ?";
            Map<String, Object> templateResult = jdbcTemplate.queryForMap(sqlTemplate, templateCode);
            
            String message = (String) templateResult.get("message");
            String templateId = (String) templateResult.get("manualCode");

            // Replace placeholders in the message
            message = message.replace("{otp}", otp);

            // Dynamically construct the SMS URL based on the format
            String finalUrl = format
                    .replace("{url}", smsUrl)
                    .replace("{user}", username)
                    .replace("{password}", password)
                    .replace("{mobile}", mobile)
                    .replace("{msg}", message)
                    .replace("{template_manual_code}", templateId);
            
            // Send HTTP request
            String response = restTemplate.getForObject(finalUrl, String.class);
            System.out.println("OTP sent successfully. Response: " + response);
        } catch (Exception e) {
            System.out.println("Failed to send OTP: " + e.getMessage());
        }
    }

    
    
    public void sendOtpToEmail(String email, String otp) {
        try {
            // **📌 Step 1: Send Email with the injected MailSender**
            sendEmail(email, otp);

        } catch (Exception e) {
            System.out.println("Failed to send OTP via email: " + e.getMessage());
        }
    }

    private void sendEmail(String to, String otp) {
        try {
            // **📌 Step 1: Create Email Message**
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            // **Fetch the username from the JavaMailSender's credentials (already set in MailConfig)**
            String username = ((JavaMailSenderImpl) javaMailSender).getUsername();

            helper.setFrom(username);
            helper.setTo(to);
            helper.setSubject("Your OTP Code");
            helper.setText("Your OTP code is: " + otp + " It is expir 157 second");

            // **📌 Step 2: Send Email**
            javaMailSender.send(message);
            System.out.println("OTP sent successfully via Email.");

        } catch (Exception e) {
            System.out.println("Error sending email: " + e.getMessage());
        }
    }
    
    
    
	// Periodic task to delete expired OTPs, runs every 15 minutes
    @Scheduled(fixedRate = 15 * 60 * 1000) // 15 minutes in milliseconds
    public void deleteExpiredOtps() {
    	LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(OTP_EXPIRATION_MINUTES);
        linkVerificationRepository.deleteExpiredOtps(expirationTime);
    }
    
    
    
    public Boolean checkMobileNumberInDb(String mobile) {
    	String companyDb = externalConfig.getCompanyDb();
    	// Dynamically construct the query with the schema name
    	String query = String.format("SELECT COUNT(*) FROM %s .. userMast WHERE mobile = ?", companyDb);


        Integer count = jdbcTemplate.queryForObject(query, Integer.class, mobile);
        System.out.println("Bolean "+count != null && count > 0);
        return count != null && count > 0;
    }
    
}

