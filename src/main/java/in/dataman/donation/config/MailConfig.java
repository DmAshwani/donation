package in.dataman.donation.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import in.dataman.donation.uitl.DmPasswaord;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;
import java.util.Properties;

@Configuration
public class MailConfig {

    @Autowired
    @Qualifier("companyJdbcTemplate")
    private JdbcTemplate jdbcTemplateCom;

    @Bean
    JavaMailSender javaMailSender() throws Exception {
        // Fetch SMTP credentials dynamically from database
        String sql = "SELECT accountName, password, smtpServer, smtpPort, smtpSsl FROM wjAccountMast WHERE nature = 1 AND isActive = 1";
        Map<String, Object> accountResult = jdbcTemplateCom.queryForMap(sql);

        String username = (String) accountResult.get("accountName");
        String encryptedPassword = (String) accountResult.get("password");
        String smtpServer = (String) accountResult.get("smtpServer");

        // Handling type casting for port and SSL
        // Cast Short to Integer
        int smtpPort = ((Number) accountResult.get("smtpPort")).intValue(); // Convert Short to Integer
        int smtpSsl = ((Short) accountResult.get("smtpSsl")).intValue(); // Convert Short to Integer

        // Decrypt the password (replace with your actual decryption method)
        String password = DmPasswaord.dCodify(encryptedPassword);

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(smtpServer);
        mailSender.setPort(smtpPort);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");

        if (smtpSsl == 1) {
            props.put("mail.smtp.starttls.enable", "false");
            props.put("mail.smtp.ssl.enable", "true");
        } else {
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.ssl.enable", "false");
        }

        return mailSender;
    }
}


