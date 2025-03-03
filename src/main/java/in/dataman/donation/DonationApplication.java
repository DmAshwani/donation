package in.dataman.donation;

import dataman.dmbase.debug.Debug;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import in.dataman.donation.config.ExternalConfig;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"in.dataman", "dataman.dmbase.encryptiondecryptionutil"})
@EnableConfigurationProperties(ExternalConfig.class)
@OpenAPIDefinition(
			info = @Info(
						title = "Donation",
						version = "1.0",
						description = "Donation Application!"
					)
		)
public class DonationApplication {

	public static void main(String[] args) {
		SpringApplication.run(DonationApplication.class, args);
	}

}
