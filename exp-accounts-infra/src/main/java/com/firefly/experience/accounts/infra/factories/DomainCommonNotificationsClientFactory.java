package com.firefly.experience.accounts.infra.factories;

import com.firefly.domain.common.notifications.sdk.api.NotificationsApi;
import com.firefly.domain.common.notifications.sdk.invoker.ApiClient;
import com.firefly.experience.accounts.infra.properties.DomainCommonNotificationsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class DomainCommonNotificationsClientFactory {

    private final ApiClient apiClient;

    public DomainCommonNotificationsClientFactory(DomainCommonNotificationsProperties properties) {
        this.apiClient = new ApiClient();
        this.apiClient.setBasePath(properties.getBasePath());
    }

    @Bean
    public NotificationsApi notificationsApi() {
        return new NotificationsApi(apiClient);
    }
}
