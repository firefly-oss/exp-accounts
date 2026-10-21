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

import io.fireflysoftware.domain.banking.accounts.sdk.api.AccountBackofficeApi;
import io.fireflysoftware.domain.banking.accounts.sdk.model.AccountDTO;
import io.fireflysoftware.experience.accounts.core.ports.ReadAccount;
import io.fireflysoftware.experience.accounts.interfaces.dtos.AccountSummaryResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@ConditionalOnProperty(name = "firefly.experience.accounts.ports.read-account", havingValue = "domain", matchIfMissing = true)
public class DomainReadAccountAdapter implements ReadAccount {

    private final AccountBackofficeApi accountBackofficeApi;

    public DomainReadAccountAdapter(AccountBackofficeApi accountBackofficeApi) {
        this.accountBackofficeApi = accountBackofficeApi;
    }

    @Override
    public Mono<AccountSummaryResponse> get(UUID accountId) {
        return accountBackofficeApi.getAccount(accountId, UUID.randomUUID().toString())
                .map(DomainReadAccountAdapter::toSummary);
    }

    public static AccountSummaryResponse toSummary(AccountDTO account) {
        String status = account.getAccountStatus() != null ? account.getAccountStatus().getValue() : null;
        String accountType = account.getAccountType() != null ? account.getAccountType().getValue() : null;
        return AccountSummaryResponse.builder()
                .accountId(account.getAccountId())
                .accountNumber(account.getAccountNumber())
                .accountType(accountType)
                .status(status)
                .currency(account.getCurrency())
                .build();
    }
}
