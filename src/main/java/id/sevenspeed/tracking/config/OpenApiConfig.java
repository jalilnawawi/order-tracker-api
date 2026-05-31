package id.sevenspeed.tracking.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        Info info = new Info();
        info.title("Order Tracking API");
        info.version("1.0.0");
        info.description(
            """
            Documentation for Order Tracking API.
            
            ### Features
            - Authentication
            """
        );
        info.setLicense(new License().name("Internal Use Only"));
        info.setContact(new Contact().name("SUKU Dev Team"));

        SecurityScheme securityScheme = new SecurityScheme();
        securityScheme.name("bearerAuth");
        securityScheme.type(SecurityScheme.Type.HTTP);
        securityScheme.scheme("bearer");
        securityScheme.bearerFormat("JWT");

        SecurityRequirement securityRequirement = new SecurityRequirement();
        securityRequirement.addList("bearerAuth");

        Components components = new Components();
        components.addSecuritySchemes("bearerAuth", securityScheme);

        Server devServer = new Server();
        devServer.setUrl("http://localhost:8080");
        devServer.setDescription("Development Server");

        Server prodServer = new Server();
        prodServer.setUrl("http://ordertracking.sukudev.net");
        prodServer.setDescription("Production Server");

        return new OpenAPI()
                .info(info)
                .components(components)
                .servers(List.of(devServer, prodServer))
                .addSecurityItem(securityRequirement);
    }
}
