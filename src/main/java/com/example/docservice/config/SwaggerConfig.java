package com.example.docservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI docServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Doc Service API")
                        .description("")
                        .version("1.0.0"));
    }
}
