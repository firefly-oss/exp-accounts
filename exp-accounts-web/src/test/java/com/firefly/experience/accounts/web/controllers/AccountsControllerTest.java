package com.firefly.experience.accounts.web.controllers;

import com.firefly.experience.accounts.core.services.AccountExperienceService;
import com.firefly.experience.accounts.interfaces.dtos.AccountSummaryResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountsControllerTest {

    @Mock
    private AccountExperienceService accountService;

    @InjectMocks
    private AccountsController controller;

    @Test
    void getAccountSummary_returnsOkWithSummary() {
        UUID accountId = UUID.randomUUID();

        AccountSummaryResponse summary = AccountSummaryResponse.builder()
                .accountId(accountId)
                .accountNumber("ES9876543210987654")
                .accountType("SAVINGS")
                .status("ACTIVE")
                .availableBalance(new BigDecimal("5000.00"))
                .currentBalance(new BigDecimal("5250.00"))
                .currency("EUR")
                .holderName("Jane Smith")
                .build();

        when(accountService.getAccountSummary(accountId))
                .thenReturn(Mono.just(summary));

        StepVerifier.create(controller.getAccountSummary(accountId))
                .assertNext(entity -> {
                    assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
                    AccountSummaryResponse body = entity.getBody();
                    assertThat(body).isNotNull();
                    assertThat(body.getAccountId()).isEqualTo(accountId);
                    assertThat(body.getAccountNumber()).isEqualTo("ES9876543210987654");
                    assertThat(body.getAccountType()).isEqualTo("SAVINGS");
                    assertThat(body.getStatus()).isEqualTo("ACTIVE");
                    assertThat(body.getAvailableBalance()).isEqualByComparingTo(new BigDecimal("5000.00"));
                    assertThat(body.getCurrency()).isEqualTo("EUR");
                    assertThat(body.getHolderName()).isEqualTo("Jane Smith");
                })
                .verifyComplete();
    }

    @Test
    void getAccountSummary_returnsNotFoundWhenAccountDoesNotExist() {
        UUID accountId = UUID.randomUUID();

        when(accountService.getAccountSummary(accountId))
                .thenReturn(Mono.empty());

        StepVerifier.create(controller.getAccountSummary(accountId))
                .assertNext(entity -> {
                    assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(entity.getBody()).isNull();
                })
                .verifyComplete();
    }

    @Test
    void getPartyAccounts_returnsAccountsForParty() {
        UUID partyId = UUID.randomUUID();
        UUID accountId1 = UUID.randomUUID();
        UUID accountId2 = UUID.randomUUID();

        AccountSummaryResponse account1 = AccountSummaryResponse.builder()
                .accountId(accountId1)
                .accountNumber("ES1111111111111111")
                .accountType("CHECKING")
                .status("ACTIVE")
                .currency("EUR")
                .build();

        AccountSummaryResponse account2 = AccountSummaryResponse.builder()
                .accountId(accountId2)
                .accountNumber("ES2222222222222222")
                .accountType("SAVINGS")
                .status("ACTIVE")
                .currency("EUR")
                .build();

        when(accountService.getCustomerAccounts(partyId))
                .thenReturn(Flux.just(account1, account2));

        StepVerifier.create(controller.getPartyAccounts(partyId))
                .assertNext(summary -> {
                    assertThat(summary.getAccountId()).isEqualTo(accountId1);
                    assertThat(summary.getAccountType()).isEqualTo("CHECKING");
                })
                .assertNext(summary -> {
                    assertThat(summary.getAccountId()).isEqualTo(accountId2);
                    assertThat(summary.getAccountType()).isEqualTo("SAVINGS");
                })
                .verifyComplete();
    }

    @Test
    void getPartyAccounts_returnsEmptyFluxWhenNoAccounts() {
        UUID partyId = UUID.randomUUID();

        when(accountService.getCustomerAccounts(partyId))
                .thenReturn(Flux.empty());

        StepVerifier.create(controller.getPartyAccounts(partyId))
                .verifyComplete();
    }

    @Test
    void closeAccount_returnsNoContent() {
        UUID accountId = UUID.randomUUID();
        String reason = "Account consolidation";

        when(accountService.closeAccount(eq(accountId), eq(reason)))
                .thenReturn(Mono.empty());

        StepVerifier.create(controller.closeAccount(accountId, reason))
                .assertNext(entity -> {
                    assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
                    assertThat(entity.getBody()).isNull();
                })
                .verifyComplete();
    }

    @Test
    void closeAccount_handlesNullReason() {
        UUID accountId = UUID.randomUUID();

        when(accountService.closeAccount(eq(accountId), eq(null)))
                .thenReturn(Mono.empty());

        StepVerifier.create(controller.closeAccount(accountId, null))
                .assertNext(entity -> {
                    assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
                })
                .verifyComplete();
    }
}
