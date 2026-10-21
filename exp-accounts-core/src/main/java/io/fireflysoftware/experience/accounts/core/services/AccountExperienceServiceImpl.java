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


package io.fireflysoftware.experience.accounts.core.services;

import io.fireflysoftware.experience.accounts.core.ports.AccessAction;
import io.fireflysoftware.experience.accounts.core.ports.AccessGuard;
import io.fireflysoftware.experience.accounts.core.ports.IdentityPort;
import io.fireflysoftware.experience.accounts.core.ports.AccessPort;
import io.fireflysoftware.experience.accounts.core.ports.AccessRequest;
import io.fireflysoftware.experience.accounts.core.ports.CloseAccount;
import io.fireflysoftware.experience.accounts.core.ports.OpenAccount;
import io.fireflysoftware.experience.accounts.core.ports.ReadAccount;
import io.fireflysoftware.experience.accounts.core.ports.UpdateAccount;
import io.fireflysoftware.experience.accounts.interfaces.dtos.AccountSummaryResponse;
import io.fireflysoftware.experience.accounts.interfaces.dtos.OpenAccountRequest;
import io.fireflysoftware.experience.accounts.interfaces.dtos.OpenAccountResponse;
import io.fireflysoftware.experience.accounts.interfaces.dtos.UpdateAccountRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Account journey. Order: access check, then the domain port. The domain adapter calls
 * domain-banking-accounts. A denial does not call the domain.
 */
@Service
@RequiredArgsConstructor
public class AccountExperienceServiceImpl implements AccountExperienceService {

    private final IdentityPort identityPort;
    private final AccessPort accessPort;
    private final OpenAccount openAccount;
    private final ReadAccount readAccount;
    private final UpdateAccount updateAccount;
    private final CloseAccount closeAccount;

    @Override
    public Mono<OpenAccountResponse> openAccount(OpenAccountRequest request) {
        return AccessGuard.require(identityPort, accessPort, IdentityPort.SAMPLE_USERNAME,
                AccessRequest.of(request.getPartyId(), AccessAction.CREATE, "ACCOUNT", null),
                () -> openAccount.open(request));
    }

    @Override
    public Mono<AccountSummaryResponse> getAccountSummary(UUID accountId) {
        return AccessGuard.require(identityPort, accessPort, IdentityPort.SAMPLE_USERNAME,
                AccessRequest.of(accountId, AccessAction.READ, "ACCOUNT", accountId),
                () -> readAccount.get(accountId));
    }

    @Override
    public Mono<AccountSummaryResponse> updateAccount(UUID accountId, UpdateAccountRequest request) {
        return AccessGuard.require(identityPort, accessPort, IdentityPort.SAMPLE_USERNAME,
                AccessRequest.of(accountId, AccessAction.UPDATE, "ACCOUNT", accountId),
                () -> updateAccount.update(accountId, request));
    }

    @Override
    public Mono<Void> closeAccount(UUID accountId, String reason) {
        return AccessGuard.require(identityPort, accessPort, IdentityPort.SAMPLE_USERNAME,
                AccessRequest.of(accountId, AccessAction.CLOSE, "ACCOUNT", accountId),
                () -> closeAccount.close(accountId, reason));
    }
}
