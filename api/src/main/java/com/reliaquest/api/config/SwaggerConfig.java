package com.reliaquest.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI employeeAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Employee API")
                        .description("Employee management system")
                        .version("v2.0"));
    }
}