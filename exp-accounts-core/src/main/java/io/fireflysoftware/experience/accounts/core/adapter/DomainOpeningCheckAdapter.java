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
import io.fireflysoftware.domain.banking.accounts.sdk.model.DocumentVerificationRequest;
import io.fireflysoftware.domain.banking.accounts.sdk.model.EligibilityRequest;
import io.fireflysoftware.experience.accounts.core.ports.CheckEligibility;
import io.fireflysoftware.experience.accounts.core.ports.VerifyDocuments;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@ConditionalOnProperty(name = "firefly.experience.accounts.ports.open-account", havingValue = "domain", matchIfMissing = true)
public class DomainOpeningCheckAdapter implements CheckEligibility, VerifyDocuments {

    private final AccountsApi accountsApi;

    public DomainOpeningCheckAdapter(AccountsApi accountsApi) {
        this.accountsApi = accountsApi;
    }

    @Override
    public Mono<Boolean> check(UUID customerId, UUID productId) {
        EligibilityRequest request = new EligibilityRequest()
                .partyId(customerId)
                .productId(productId);
        return accountsApi.recordEligibility(request, UUID.randomUUID().toString())
                .flatMap(stored -> {
                    if (stored.getVerificationId() == null || stored.getEligible() == null || stored.getStatus() == null
                            || stored.getStatus().isBlank()) {
                        return Mono.error(new IllegalStateException("Eligibility was not recorded"));
                    }
                    return Mono.just(stored.getEligible());
                });
    }

    @Override
    public Mono<String> verify(UUID customerId) {
        DocumentVerificationRequest request = new DocumentVerificationRequest().partyId(customerId);
        return accountsApi.recordDocumentVerification(request, UUID.randomUUID().toString())
                .flatMap(stored -> {
                    if (stored.getVerificationId() == null || stored.getStatus() == null || stored.getStatus().isBlank()) {
                        return Mono.error(new IllegalStateException("Document verification was not recorded"));
                    }
                    return Mono.just(stored.getStatus());
                });
    }
}
