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


package io.fireflysoftware.experience.accounts.core.adapter;

import io.fireflysoftware.domain.banking.accounts.sdk.api.AccountsApi;
import io.fireflysoftware.experience.accounts.core.ports.CloseAccount;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@ConditionalOnProperty(name = "firefly.experience.accounts.ports.close-account", havingValue = "domain", matchIfMissing = true)
public class DomainCloseAccountAdapter implements CloseAccount {

    private final AccountsApi accountsApi;

    public DomainCloseAccountAdapter(AccountsApi accountsApi) {
        this.accountsApi = accountsApi;
    }

    @Override
    public Mono<Void> close(UUID accountId, String reason) {
        return accountsApi.closeAccount(accountId, reason, UUID.randomUUID().toString());
    }
}
