package com.orderflow.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI orderFlowOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("OrderFlow API")
                        .version("1.0.0")
                        .description("""
                                E-commerce order management API.
                                Built with Java, Spring Boot and PostgreSQL.
                                """));
    }
}