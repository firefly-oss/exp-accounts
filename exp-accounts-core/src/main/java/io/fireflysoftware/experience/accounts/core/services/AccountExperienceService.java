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

import io.fireflysoftware.experience.accounts.interfaces.dtos.AccountSummaryResponse;
import io.fireflysoftware.experience.accounts.interfaces.dtos.OpenAccountRequest;
import io.fireflysoftware.experience.accounts.interfaces.dtos.OpenAccountResponse;
import io.fireflysoftware.experience.accounts.interfaces.dtos.UpdateAccountRequest;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface AccountExperienceService {

    Mono<OpenAccountResponse> openAccount(OpenAccountRequest request);

    Mono<AccountSummaryResponse> getAccountSummary(UUID accountId);

    Mono<AccountSummaryResponse> updateAccount(UUID accountId, UpdateAccountRequest request);

    Mono<Void> closeAccount(UUID accountId, String reason);

    Mono<UUID> closeStatement(UUID accountId);
}
