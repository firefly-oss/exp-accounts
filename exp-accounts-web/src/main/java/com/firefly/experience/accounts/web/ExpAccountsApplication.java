package com.firefly.experience.accounts.web;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.web.reactive.config.EnableWebFlux;

@SpringBootApplication(
    scanBasePackages = {
        "com.firefly.experience.accounts",
        "org.fireflyframework.web"
    }
)
@EnableWebFlux
@ConfigurationPropertiesScan(basePackages = "com.firefly.experience.accounts")
@OpenAPIDefinition(
    info = @Info(
        title = "${spring.application.name}",
        version = "${spring.application.version}",
        description = "${spring.application.description}",
        contact = @Contact(
            name = "${spring.application.team.name}",
            email = "${spring.application.team.email}"
        )
    ),
    servers = {
        @Server(url = "http://experience.getfirefly.io/exp-accounts", description = "Development Environment"),
        @Server(url = "/", description = "Local Development Environment")
    }
)
public class ExpAccountsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExpAccountsApplication.class, args);
    }
}
