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
import io.fireflysoftware.experience.accounts.core.ports.AccessDecision;
import io.fireflysoftware.experience.accounts.core.ports.AccessDeniedException;
import io.fireflysoftware.experience.accounts.core.ports.AccessPort;
import io.fireflysoftware.experience.accounts.core.ports.IdentityPort;
import io.fireflysoftware.experience.accounts.core.ports.SubjectRef;
import io.fireflysoftware.experience.accounts.core.ports.AccessRequest;
import io.fireflysoftware.experience.accounts.core.ports.CloseAccount;
import io.fireflysoftware.experience.accounts.core.ports.OpenAccount;
import io.fireflysoftware.experience.accounts.core.ports.ReadAccount;
import io.fireflysoftware.experience.accounts.core.ports.UpdateAccount;
import io.fireflysoftware.experience.accounts.core.records.AccountRecordsJourney;
import io.fireflysoftware.experience.accounts.interfaces.dtos.OpenAccountRequest;
import io.fireflysoftware.experience.accounts.interfaces.dtos.OpenAccountResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountExperienceServiceImplTest {

    @Mock private IdentityPort identityPort;
    @Mock private AccessPort accessPort;
    @Mock private OpenAccount openAccount;
    @Mock private ReadAccount readAccount;
    @Mock private UpdateAccount updateAccount;
    @Mock private CloseAccount closeAccount;
    @Mock private AccountRecordsJourney records;

    private AccountExperienceServiceImpl service;

    @BeforeEach
    void setUp() {
        lenient().when(identityPort.resolve(any())).thenReturn(Mono.just(new SubjectRef(IdentityPort.SAMPLE_ACTOR, "sandbox")));
        service = new AccountExperienceServiceImpl(
                identityPort, accessPort, openAccount, readAccount, updateAccount, closeAccount, records);
    }

    @Test
    void openAccount_checksAccessThenDomainPort() {
        UUID partyId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        OpenAccountRequest request = OpenAccountRequest.builder()
                .partyId(partyId)
                .productId(UUID.randomUUID())
                .accountType("SAVINGS")
                .currency("EUR")
                .build();
        when(accessPort.check(any(AccessRequest.class))).thenReturn(Mono.just(AccessDecision.grant()));
        when(openAccount.open(request)).thenReturn(Mono.just(OpenAccountResponse.builder()
                .accountId(accountId)
                .status("COMPLETED")
                .build()));

        StepVerifier.create(service.openAccount(request))
                .assertNext(response -> assertThat(response.getAccountId()).isEqualTo(accountId))
                .verifyComplete();

        verify(openAccount).open(request);
    }

    @Test
    void openAccount_deniedAccessDoesNotCallDomain() {
        OpenAccountRequest request = OpenAccountRequest.builder()
                .partyId(UUID.randomUUID())
                .accountType("SAVINGS")
                .currency("EUR")
                .build();
        when(accessPort.check(any(AccessRequest.class)))
                .thenReturn(Mono.just(AccessDecision.denied("not allowed")));

        StepVerifier.create(service.openAccount(request))
                .expectError(AccessDeniedException.class)
                .verify();

        verify(openAccount, never()).open(any());
    }

    @Test
    void closeAccount_usesCloseAction() {
        UUID accountId = UUID.randomUUID();
        when(accessPort.check(any(AccessRequest.class))).thenAnswer(invocation -> {
            AccessRequest request = invocation.getArgument(0);
            assertThat(request.action()).isEqualTo(AccessAction.CLOSE);
            assertThat(request.resourceType()).isEqualTo("ACCOUNT");
            return Mono.just(AccessDecision.grant());
        });
        when(closeAccount.close(accountId, "Customer request")).thenReturn(Mono.empty());

        StepVerifier.create(service.closeAccount(accountId, "Customer request")).verifyComplete();
        verify(closeAccount).close(accountId, "Customer request");
    }

    @Test
    void closeStatement_returnsStoredIdAfterAccessCheck() {
        UUID accountId = UUID.randomUUID();
        UUID statementId = UUID.randomUUID();
        when(accessPort.check(any(AccessRequest.class))).thenReturn(Mono.just(AccessDecision.grant()));
        when(records.closeStatement(accountId)).thenReturn(Mono.just(statementId));

        StepVerifier.create(service.closeStatement(accountId))
                .expectNext(statementId)
                .verifyComplete();
    }

    @Test
    void closeStatement_deniedAccessDoesNotCallDomain() {
        UUID accountId = UUID.randomUUID();
        when(accessPort.check(any(AccessRequest.class)))
                .thenReturn(Mono.just(AccessDecision.denied("ACTOR_NOT_ENTITLED")));

        StepVerifier.create(service.closeStatement(accountId))
                .expectError(AccessDeniedException.class)
                .verify();

        verify(records, never()).closeStatement(any());
    }
}
