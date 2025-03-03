package in.dataman.donation.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import in.dataman.donation.jwt.JwtAuthenticationEntryPoint;
import in.dataman.donation.jwt.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Autowired
	private JwtAuthenticationFilter authenticationFilter;

	@Autowired
	private JwtAuthenticationEntryPoint authenticationEntryPoint;

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http.csrf(csrf -> csrf.disable()).cors(cors -> cors.disable()).authorizeHttpRequests(req -> req
				.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				.requestMatchers("/api/v1/auth/send-otp", "/api/v1/auth/verify-otp", "/api/v1/states", "/api/v1/auth/get-secret-key",
						"/api/v1/patient", "/api/v1/getDonation", "/api/v1/getPatient", "/api/v1/countries",
						"/api/v1/cities", "/api/v1/cities-name", "/api/v1/submit", "/api/v1/order", "/api/v1/verify",
						"/api/v1/payment-gateway", "/api/v1/admin-donation-transactions",
						"/api/v1/admin-patient-transactions", "/api/v1/transactions", "/api/v1/PatientTransactions",
						"/api/v1/payment-receipts", "/api/v1/patient-receipts", "/api/v1/auth/generate-captcha","/encrypt-object-and-send-map","/decrypt-and-convert-to-dto","/decrypt-request","/encryption-request",
						"/api/v1/auth/verify-captcha", "/swagger-ui/**", "/v3/api-docs/**")
				.permitAll().anyRequest().authenticated())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint))
				.addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class).build();
	}

}
