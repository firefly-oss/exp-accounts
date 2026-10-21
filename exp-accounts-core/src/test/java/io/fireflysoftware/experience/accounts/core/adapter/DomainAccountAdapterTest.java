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
import io.fireflysoftware.domain.banking.accounts.sdk.api.AccountsApi;
import io.fireflysoftware.domain.banking.accounts.sdk.model.AccountDTO;
import io.fireflysoftware.domain.banking.accounts.sdk.model.OpenAccountCommand;
import io.fireflysoftware.domain.banking.accounts.sdk.model.OpenAccountResponse;
import io.fireflysoftware.experience.accounts.interfaces.dtos.OpenAccountRequest;
import io.fireflysoftware.experience.accounts.interfaces.dtos.UpdateAccountRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DomainAccountAdapterTest {

    @Mock private AccountsApi accountsApi;
    @Mock private AccountBackofficeApi accountBackofficeApi;

    @Test
    void openAccount_callsDomainSdk() {
        UUID partyId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID contractId = UUID.randomUUID();
        OpenAccountRequest request = OpenAccountRequest.builder()
                .partyId(partyId)
                .productId(UUID.randomUUID())
                .accountType("SAVINGS")
                .currency("EUR")
                .build();
        OpenAccountResponse sdkResponse = new OpenAccountResponse();
        sdkResponse.setAccountId(accountId);
        sdkResponse.setContractId(contractId);
        sdkResponse.setExecutionId("exec-123");
        sdkResponse.setStatus("COMPLETED");
        when(accountsApi.openAccount(any(OpenAccountCommand.class), any(String.class)))
                .thenReturn(Mono.just(sdkResponse));

        StepVerifier.create(new DomainOpenAccountAdapter(accountsApi).open(request))
                .assertNext(response -> {
                    assertThat(response.getAccountId()).isEqualTo(accountId);
                    assertThat(response.getContractId()).isEqualTo(contractId);
                    assertThat(response.getStatus()).isEqualTo("COMPLETED");
                })
                .verifyComplete();
    }

    @Test
    void readAccount_mapsDomainAccount() {
        UUID accountId = UUID.randomUUID();
        AccountDTO sdkResponse = new AccountDTO();
        sdkResponse.setAccountId(accountId);
        sdkResponse.setAccountNumber("ES1234567890123456");
        sdkResponse.setAccountType(AccountDTO.AccountTypeEnum.SAVINGS);
        sdkResponse.setAccountStatus(AccountDTO.AccountStatusEnum.OPEN);
        sdkResponse.setCurrency("EUR");
        when(accountBackofficeApi.getAccount(eq(accountId), any(String.class)))
                .thenReturn(Mono.just(sdkResponse));

        DomainReadAccountAdapter adapter = new DomainReadAccountAdapter(accountBackofficeApi);
        StepVerifier.create(adapter.get(accountId))
                .assertNext(summary -> {
                    assertThat(summary.getAccountType()).isEqualTo("SAVINGS");
                    assertThat(summary.getStatus()).isEqualTo("OPEN");
                    assertThat(summary.getCurrency()).isEqualTo("EUR");
                })
                .verifyComplete();
    }

    @Test
    void updateAccount_readsDomainThenEchoesRequestedFields() {
        UUID accountId = UUID.randomUUID();
        AccountDTO sdkResponse = new AccountDTO();
        sdkResponse.setAccountId(accountId);
        sdkResponse.setCurrency("EUR");
        sdkResponse.setAccountType(AccountDTO.AccountTypeEnum.SAVINGS);
        when(accountBackofficeApi.getAccount(eq(accountId), any(String.class)))
                .thenReturn(Mono.just(sdkResponse));
        DomainReadAccountAdapter read = new DomainReadAccountAdapter(accountBackofficeApi);

        StepVerifier.create(new DomainUpdateAccountAdapter(read).update(accountId,
                        UpdateAccountRequest.builder().currency("USD").build()))
                .assertNext(summary -> assertThat(summary.getCurrency()).isEqualTo("USD"))
                .verifyComplete();
        verify(accountBackofficeApi).getAccount(eq(accountId), any(String.class));
    }

    @Test
    void closeAccount_callsDomainSdk() {
        UUID accountId = UUID.randomUUID();
        when(accountsApi.closeAccount(eq(accountId), eq("Customer request"), any(String.class)))
                .thenReturn(Mono.empty());

        StepVerifier.create(new DomainCloseAccountAdapter(accountsApi).close(accountId, "Customer request"))
                .verifyComplete();
    }
}
