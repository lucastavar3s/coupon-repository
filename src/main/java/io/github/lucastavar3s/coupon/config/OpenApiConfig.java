package io.github.lucastavar3s.coupon.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

	@Bean
	OpenAPI couponOpenApi() {
		return new OpenAPI().info(new Info()
				.title("Coupon API")
				.description("Cadastro, consulta e exclusão lógica de cupons.")
				.version("1.0.0"));
	}

}
