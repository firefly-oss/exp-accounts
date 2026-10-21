/*
 * Copyright 2025 Firefly Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package io.fireflysoftware.experience.accounts.infra.factories;

import io.fireflysoftware.domain.banking.accounts.sdk.api.AccountRecordsApi;
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

    @Bean
    public AccountRecordsApi accountRecordsApi() {
        return new AccountRecordsApi(apiClient);
    }
}
