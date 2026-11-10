package rw.parkshare.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Interactive API docs at /swagger-ui.html. Log in via /api/auth/login, copy the
 * token, click "Authorize" and paste it. Disable in production with
 * SPRINGDOC_API_DOCS_ENABLED=false and SPRINGDOC_SWAGGER_UI_ENABLED=false.
 */
@Configuration
public class ApiDocsConfig {

    @Bean
    public OpenAPI parkShareOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("ParkShare API")
                        .version("v1")
                        .description("Parking marketplace for Kigali. Amounts in RWF, VAT (18%) included. "
                                + "Log in with POST /api/auth/login, then click Authorize and paste the token."))
                .components(new Components().addSecuritySchemes("bearer",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearer"));
    }

    @RestController
    static class RootRedirect {
        /** Opening the server in a browser shows the API docs instead of a blank page. */
        @GetMapping("/")
        RedirectView root() {
            return new RedirectView("/swagger-ui.html");
        }
    }
}
