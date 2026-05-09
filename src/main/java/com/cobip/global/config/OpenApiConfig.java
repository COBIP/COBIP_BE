package com.cobip.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI cobipOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("COBIP API")
                        .version("v1")
                        .description("COBIP backend API documentation")
                        .license(new License().name("Internal")))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .name(BEARER_AUTH)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

    @Bean
    public GroupedOpenApi authApi() {
        return group("auth", "/api/v1/auth/**");
    }

    @Bean
    public GroupedOpenApi userApi() {
        return group("user", "/api/v1/users/me/**", "/api/v1/users/me");
    }

    @Bean
    public GroupedOpenApi templateApi() {
        return group("template", "/api/v1/templates/**", "/api/v1/templates");
    }

    @Bean
    public GroupedOpenApi grammarApi() {
        return group("grammar", "/api/v1/grammar-templates/**", "/api/v1/grammar-templates");
    }

    @Bean
    public GroupedOpenApi codingApi() {
        return group("coding", "/api/v1/coding-workbooks/**", "/api/v1/coding-problems/**");
    }

    @Bean
    public GroupedOpenApi communityApi() {
        return group("community", "/api/v1/community/**");
    }

    @Bean
    public GroupedOpenApi subscriptionApi() {
        return group("subscription", "/api/v1/subscription-plans/**", "/api/v1/subscription-plans");
    }

    @Bean
    public GroupedOpenApi labApi() {
        return group("lab", "/api/v1/lab/**");
    }

    @Bean
    public GroupedOpenApi reportApi() {
        return group("report", "/api/v1/reports/**");
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return group("admin", "/api/v1/admin/**");
    }

    @Bean
    public GroupedOpenApi runtimeApi() {
        return group("runtime", "/api/run/**", "/api/judge/**");
    }

    private GroupedOpenApi group(String group, String... paths) {
        return GroupedOpenApi.builder()
                .group(group)
                .pathsToMatch(paths)
                .build();
    }
}
