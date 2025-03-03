package in.dataman.donation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.razorpay.RazorpayClient;

@Configuration
public class RazorpayConfig {

	private static final String RAZORPAY_KEY_ID = "rzp_test_INsEDomC8rRAol";
    private static final String RAZORPAY_KEY_SECRET = "LBD4KkcvlMVh4lgFnvmrGq2b";

    @Bean
    RazorpayClient razorpayClient() throws Exception {
        return new RazorpayClient(RAZORPAY_KEY_ID, RAZORPAY_KEY_SECRET);
    }
}
