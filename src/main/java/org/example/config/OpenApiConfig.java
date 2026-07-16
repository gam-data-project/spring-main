package org.example.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI springMainOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("spring-main API")
                        .description("Spring Main 프로젝트 API 문서")
                        .version("v1"));
    }

    @Bean
    public GroupedOpenApi renewalApi() {
        return GroupedOpenApi.builder()
                .group("renewal")
                .pathsToMatch("/renewal/**")
                .build();
    }
}
