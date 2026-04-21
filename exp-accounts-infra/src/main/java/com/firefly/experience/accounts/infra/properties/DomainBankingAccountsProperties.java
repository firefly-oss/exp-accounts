package com.firefly.experience.accounts.infra.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Configuration properties for the Domain Banking Accounts API.
 * <p>
 * Binds to {@code api-configuration.domain-platform.banking-accounts} in application.yaml.
 */
@Component
@ConfigurationProperties(prefix = "api-configuration.domain-platform.banking-accounts")
@Data
public class DomainBankingAccountsProperties {

    /** Base URL of the Domain Banking Accounts service (e.g. {@code http://localhost:8080}). */
    private String basePath;

    /** Read/connect timeout for SDK calls. Defaults to 5 seconds. */
    private Duration timeout = Duration.ofSeconds(5);
}
