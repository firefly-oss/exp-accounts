/*
 * Copyright 2026 Firefly Software Foundation
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

package io.fireflysoftware.experience.accounts.core.records;

import io.fireflysoftware.domain.banking.accounts.sdk.api.AccountRecordsControllerApi;
import io.fireflysoftware.domain.banking.accounts.sdk.model.RecordedId;
import io.fireflysoftware.domain.banking.accounts.sdk.model.ServicingMandate;
import io.fireflysoftware.domain.banking.accounts.sdk.model.SpaceSweep;
import io.fireflysoftware.domain.banking.accounts.sdk.model.StatementClose;
import io.fireflysoftware.domain.banking.accounts.sdk.model.StatementView;
import io.fireflysoftware.domain.banking.accounts.sdk.model.WithdrawalLimit;
import io.fireflysoftware.domain.payments.sdk.api.PaymentRecordsApi;
import io.fireflysoftware.domain.payments.sdk.model.DirectDebitBody;
import io.fireflysoftware.domain.payments.sdk.model.StandingOrderBody;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountRecordsJourneyTest {

    @Mock
    private AccountRecordsControllerApi accounts;
    @Mock
    private PaymentRecordsApi payments;

    @Test
    void recordsTheSixRowsThroughTheDomainSdks() {
        when(accounts.closeStatement(any(), any(), any())).thenReturn(Mono.just(new RecordedId().id(UUID.randomUUID())));
        when(accounts.withdrawalLimit(any(), any(), any())).thenReturn(Mono.just(new RecordedId().id(UUID.randomUUID())));
        when(accounts.spaceSweep(any(), any(), any())).thenReturn(Mono.just(new RecordedId().id(UUID.randomUUID())));
        when(accounts.servicingMandate(any(), any(), any())).thenReturn(Mono.just(new RecordedId().id(UUID.randomUUID())));
        when(payments.runStandingOrder(any(), any())).thenReturn(Mono.just(new io.fireflysoftware.domain.payments.sdk.model.RecordedId().id(UUID.randomUUID())));
        when(payments.createDirectDebit(any(), any())).thenReturn(Mono.just(new io.fireflysoftware.domain.payments.sdk.model.RecordedId().id(UUID.randomUUID())));

        UUID accountId = UUID.randomUUID();
        UUID partyId = UUID.randomUUID();
        UUID source = UUID.randomUUID();
        UUID target = UUID.randomUUID();
        UUID schedule = UUID.randomUUID();
        UUID creditor = UUID.randomUUID();
        new AccountRecordsJourney(accounts, payments)
                .record(accountId, partyId, source, target, schedule, creditor)
                .block();

        verify(accounts).closeStatement(eq(accountId), eq(new StatementClose()
                .currency("USD")
                .openingBalance(new BigDecimal("100.00"))
                .lineAmounts(List.of(new BigDecimal("25.00"), new BigDecimal("-10.00")))), eq("statement"));
        verify(accounts).withdrawalLimit(eq(accountId), eq(new WithdrawalLimit()
                .currency("USD")
                .withdrawalLimit(new BigDecimal("50.00"))
                .overdraftLimit(new BigDecimal("10.00"))
                .balance(new BigDecimal("40.00"))
                .withdrawalAmount(new BigDecimal("45.00"))), eq("limit"));
        verify(accounts).spaceSweep(eq(accountId), eq(new SpaceSweep()
                .sourceSpaceId(source).targetSpaceId(target).amount(new BigDecimal("15.00")).currency("USD")), eq("sweep"));
        verify(accounts).servicingMandate(eq(accountId), eq(new ServicingMandate()
                .partyId(partyId).role("OPERATOR").active(true)), eq("mandate"));
        verify(payments).runStandingOrder(eq(new StandingOrderBody()
                .paymentScheduleId(schedule).amount(new BigDecimal("25.00")).currency("GBP")
                .valueDate(LocalDate.of(2026, 10, 21))), eq("standing-order"));
        verify(payments).createDirectDebit(eq(new DirectDebitBody()
                .debtorAccountId(accountId).creditorPartyId(creditor).amountLimit(new BigDecimal("100.00"))
                .currency("GBP").reference("MANDATE-1")), eq("direct-debit"));
    }

    @Test
    void readsTheStoredStatementThroughTheAccountsDomain() {
        UUID accountId = UUID.randomUUID();
        UUID statementId = UUID.randomUUID();
        StatementView stored = new StatementView().id(statementId).currency("USD")
                .openingBalance(new BigDecimal("100.00"))
                .lineAmounts(List.of(new BigDecimal("25.00"), new BigDecimal("-10.00")))
                .credits(new BigDecimal("25.00")).debits(new BigDecimal("10.00"))
                .closingBalance(new BigDecimal("115.00"));
        when(accounts.statement(accountId, statementId, null)).thenReturn(Mono.just(stored));

        StatementView read = new AccountRecordsJourney(accounts, payments).readStatement(accountId, statementId).block();

        assertThat(read.getClosingBalance()).isEqualByComparingTo("115.00");
        verify(accounts).statement(accountId, statementId, null);
    }
}
