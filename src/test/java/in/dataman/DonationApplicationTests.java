package in.dataman;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = in.dataman.donation.DonationApplication.class)
@TestPropertySource(locations = "file:///C:\\ActiveProject\\config.properties")
class DonationApplicationTests {
    @Test
    void contextLoads() {
    }
}


