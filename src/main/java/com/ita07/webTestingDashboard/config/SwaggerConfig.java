package com.ita07.webTestingDashboard.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    /**
     * Configures the OpenAPI (Swagger) documentation for the Web Testing Dashboard API.
     * - Sets API title, version, and description.
     * - Adds HTTP Basic Auth as a security scheme so the Swagger UI 'Authorize' button works.
     * - Applies the security scheme globally to all endpoints.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Web Testing Dashboard API")
                        .version("1.0")
                        .description("API for triggering Selenium automation tests"))
                .addSecurityItem(new SecurityRequirement().addList("basicAuth"))
                .components(new Components().addSecuritySchemes("basicAuth",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic")));
    }
}


