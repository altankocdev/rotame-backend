package com.altankoc.rotame;

import com.altankoc.rotame.core.config.AwsProperties;
import com.altankoc.rotame.core.config.JwtProperties;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@OpenAPIDefinition(
		info = @Info(
				title = "RotaMe API",
				version = "1.0",
				description = "Konum takip mobil uygulaması backend API"
		)
)
@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, AwsProperties.class})
@EnableScheduling
public class RotameApplication {
	public static void main(String[] args) {
		SpringApplication.run(RotameApplication.class, args);
	}
}