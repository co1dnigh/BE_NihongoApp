package com.example.nihongo_app.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình Swagger UI / OpenAPI cho toàn bộ API.
 *
 * <p>Đăng nhập qua {@code POST /api/v1/auth/login} lấy {@code accessToken}, rồi bấm nút
 * "Authorize" trên Swagger UI, dán token vào (không cần gõ tiền tố "Bearer ") để gọi thử
 * các API cần xác thực.</p>
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI nihongoAppOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Nihongo App API")
                        .description("API cho ứng dụng học tiếng Nhật: lộ trình học, bài học, "
                                + "năng lượng, streak, coin và rương thưởng.")
                        .version("v1"))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME_NAME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME_NAME));
    }
}
