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
import io.fireflysoftware.domain.banking.accounts.sdk.model.OpenAccountCommand;
import io.fireflysoftware.experience.accounts.core.ports.OpenAccount;
import io.fireflysoftware.experience.accounts.interfaces.dtos.OpenAccountRequest;
import io.fireflysoftware.experience.accounts.interfaces.dtos.OpenAccountResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@ConditionalOnProperty(name = "firefly.experience.accounts.ports.open-account", havingValue = "domain", matchIfMissing = true)
public class DomainOpenAccountAdapter implements OpenAccount {

    private final AccountsApi accountsApi;

    public DomainOpenAccountAdapter(AccountsApi accountsApi) {
        this.accountsApi = accountsApi;
    }

    @Override
    public Mono<OpenAccountResponse> open(OpenAccountRequest request) {
        OpenAccountCommand command = new OpenAccountCommand();
        command.setPartyId(request.getPartyId());
        command.setProductId(request.getProductId());
        command.setProductCatalogId(request.getProductCatalogId());
        command.setRoleInContractId(request.getRoleInContractId());
        command.setAccountType(request.getAccountType());
        command.setCurrency(request.getCurrency());
        command.setBranchId(request.getBranchId());
        return accountsApi.openAccount(command, UUID.randomUUID().toString())
                .map(result -> OpenAccountResponse.builder()
                        .accountId(result.getAccountId())
                        .contractId(result.getContractId())
                        .executionId(result.getExecutionId())
                        .status(result.getStatus())
                        .build());
    }
}
