package com.firefly.experience.accounts.infra.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "api-configuration.domain-platform.common-notifications")
@Data
public class DomainCommonNotificationsProperties {

    private String basePath;
    private Duration timeout = Duration.ofSeconds(5);
}
