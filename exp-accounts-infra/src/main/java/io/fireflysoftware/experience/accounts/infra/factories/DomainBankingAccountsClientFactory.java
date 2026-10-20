package io.fireflysoftware.experience.accounts.infra.factories;

import io.fireflysoftware.domain.banking.accounts.sdk.api.AccountsApi;
import io.fireflysoftware.domain.banking.accounts.sdk.api.AccountBackofficeApi;
import io.fireflysoftware.domain.banking.accounts.sdk.api.AccountSpacesApi;
import io.fireflysoftware.domain.banking.accounts.sdk.invoker.ApiClient;
import io.fireflysoftware.experience.accounts.infra.properties.DomainBankingAccountsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class DomainBankingAccountsClientFactory {

    private final ApiClient apiClient;

    public DomainBankingAccountsClientFactory(DomainBankingAccountsProperties properties) {
        this.apiClient = new ApiClient();
        this.apiClient.setBasePath(properties.getBasePath());
    }

    @Bean
    public AccountsApi accountsApi() {
        return new AccountsApi(apiClient);
    }

    @Bean
    public AccountBackofficeApi accountBackofficeApi() {
        return new AccountBackofficeApi(apiClient);
    }

    @Bean
    public AccountSpacesApi accountSpacesApi() {
        return new AccountSpacesApi(apiClient);
    }
}
