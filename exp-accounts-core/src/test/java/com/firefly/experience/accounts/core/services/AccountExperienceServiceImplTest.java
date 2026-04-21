package com.firefly.experience.accounts.core.services;

import com.firefly.domain.banking.accounts.sdk.api.AccountBackofficeApi;
import com.firefly.domain.banking.accounts.sdk.api.AccountsApi;
import com.firefly.domain.banking.accounts.sdk.model.AccountDTO;
import com.firefly.domain.banking.accounts.sdk.model.OpenAccountCommand;
import com.firefly.domain.banking.accounts.sdk.model.OpenAccountResponse;
import com.firefly.experience.accounts.interfaces.dtos.OpenAccountRequest;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountExperienceServiceImplTest {

    @Mock
    private AccountsApi accountsApi;

    @Mock
    private AccountBackofficeApi accountBackofficeApi;

    private AccountExperienceServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AccountExperienceServiceImpl(accountsApi, accountBackofficeApi);
    }

    @Test
    void openAccount_callsAccountsApiAndReturnsResponse() {
        UUID partyId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID contractId = UUID.randomUUID();

        OpenAccountRequest request = OpenAccountRequest.builder()
                .partyId(partyId)
                .productId(productId)
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

        StepVerifier.create(service.openAccount(request))
                .assertNext(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getAccountId()).isEqualTo(accountId);
                    assertThat(response.getContractId()).isEqualTo(contractId);
                    assertThat(response.getExecutionId()).isEqualTo("exec-123");
                    assertThat(response.getStatus()).isEqualTo("COMPLETED");
                })
                .verifyComplete();

        verify(accountsApi).openAccount(any(OpenAccountCommand.class), any(String.class));
    }

    @Test
    void getAccountSummary_callsBackofficeApiAndMapsResponse() {
        UUID accountId = UUID.randomUUID();

        AccountDTO sdkResponse = new AccountDTO();
        sdkResponse.setAccountId(accountId);
        sdkResponse.setAccountNumber("ES1234567890123456");
        sdkResponse.setAccountType(AccountDTO.AccountTypeEnum.SAVINGS);
        sdkResponse.setAccountStatus(AccountDTO.AccountStatusEnum.OPEN);
        sdkResponse.setCurrency("EUR");

        when(accountBackofficeApi.getAccount(eq(accountId), any(String.class)))
                .thenReturn(Mono.just(sdkResponse));

        StepVerifier.create(service.getAccountSummary(accountId))
                .assertNext(summary -> {
                    assertThat(summary).isNotNull();
                    assertThat(summary.getAccountId()).isEqualTo(accountId);
                    assertThat(summary.getAccountNumber()).isEqualTo("ES1234567890123456");
                    assertThat(summary.getAccountType()).isEqualTo("SAVINGS");
                    assertThat(summary.getStatus()).isEqualTo("OPEN");
                    assertThat(summary.getCurrency()).isEqualTo("EUR");
                })
                .verifyComplete();

        verify(accountBackofficeApi).getAccount(eq(accountId), any(String.class));
    }

    @Test
    void getAccountSummary_handlesEmptyResponse() {
        UUID accountId = UUID.randomUUID();

        when(accountBackofficeApi.getAccount(eq(accountId), any(String.class)))
                .thenReturn(Mono.empty());

        StepVerifier.create(service.getAccountSummary(accountId))
                .verifyComplete();
    }

    @Test
    void getCustomerAccounts_returnsUnsupportedOperationError() {
        UUID customerId = UUID.randomUUID();

        StepVerifier.create(service.getCustomerAccounts(customerId))
                .expectErrorMatches(throwable ->
                        throwable instanceof UnsupportedOperationException &&
                        throwable.getMessage().contains("Listing accounts by party is not yet supported"))
                .verify();
    }

    @Test
    void closeAccount_callsAccountsApiCloseAccount() {
        UUID accountId = UUID.randomUUID();
        String reason = "Customer request";

        when(accountsApi.closeAccount(eq(accountId), eq(reason), any(String.class)))
                .thenReturn(Mono.empty());

        StepVerifier.create(service.closeAccount(accountId, reason))
                .verifyComplete();

        verify(accountsApi).closeAccount(eq(accountId), eq(reason), any(String.class));
    }

    @Test
    void closeAccount_handlesNullReason() {
        UUID accountId = UUID.randomUUID();

        when(accountsApi.closeAccount(eq(accountId), eq(null), any(String.class)))
                .thenReturn(Mono.empty());

        StepVerifier.create(service.closeAccount(accountId, null))
                .verifyComplete();

        verify(accountsApi).closeAccount(eq(accountId), eq(null), any(String.class));
    }
}
