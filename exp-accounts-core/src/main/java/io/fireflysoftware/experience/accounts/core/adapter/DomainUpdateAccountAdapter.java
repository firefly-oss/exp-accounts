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

import io.fireflysoftware.experience.accounts.core.ports.ReadAccount;
import io.fireflysoftware.experience.accounts.core.ports.UpdateAccount;
import io.fireflysoftware.experience.accounts.interfaces.dtos.AccountSummaryResponse;
import io.fireflysoftware.experience.accounts.interfaces.dtos.UpdateAccountRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * domain-banking-accounts exposes open and close, not a general account update.
 * This adapter still calls the domain read. Replace this class when the domain adds an
 * update operation or when a process engine should apply the change.
 */
@Component
@ConditionalOnProperty(name = "firefly.experience.accounts.ports.update-account", havingValue = "domain", matchIfMissing = true)
public class DomainUpdateAccountAdapter implements UpdateAccount {

    private final ReadAccount readAccount;

    public DomainUpdateAccountAdapter(ReadAccount readAccount) {
        this.readAccount = readAccount;
    }

    @Override
    public Mono<AccountSummaryResponse> update(UUID accountId, UpdateAccountRequest request) {
        return readAccount.get(accountId).map(summary -> {
            if (request != null && request.getAccountType() != null) {
                summary.setAccountType(request.getAccountType());
            }
            if (request != null && request.getCurrency() != null) {
                summary.setCurrency(request.getCurrency());
            }
            return summary;
        });
    }
}
