package com.pharmacy.usermanagement.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(new Info()
                        .title("User Management API")
                        .description("API Documentation for User Management Microservice")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("N W K Kamal Indika")
                                .email("mit23112@pgu.uwu.ac.lk"))
                       );
    }
}
