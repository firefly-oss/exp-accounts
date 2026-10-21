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

import io.fireflysoftware.domain.banking.accounts.sdk.api.AccountRecordsApi;
import io.fireflysoftware.domain.payments.sdk.api.PaymentRecordsApi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountRecordsJourneyTest {

    @Mock
    private AccountRecordsApi accounts;
    @Mock
    private PaymentRecordsApi payments;

    @Test
    void recordsTheSixRowsThroughTheDomainSdks() {
        when(accounts.closeStatement(any(), anyString(), any(), anyList(), anyString())).thenReturn(Mono.just(UUID.randomUUID()));
        when(accounts.recordWithdrawalLimit(any(), anyString(), any(), any(), any(), any(), anyString()))
                .thenReturn(Mono.just(UUID.randomUUID()));
        when(accounts.recordSpaceSweep(any(), any(), any(), any(), anyString(), anyString()))
                .thenReturn(Mono.just(UUID.randomUUID()));
        when(accounts.createServicingMandate(any(), any(), anyString(), anyBoolean(), anyString()))
                .thenReturn(Mono.just(UUID.randomUUID()));
        when(payments.runStandingOrder(any(), any(), anyString(), any(), anyString())).thenReturn(Mono.just(UUID.randomUUID()));
        when(payments.createDirectDebit(any(), any(), any(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.just(UUID.randomUUID()));

        UUID accountId = UUID.randomUUID();
        UUID partyId = UUID.randomUUID();
        UUID source = UUID.randomUUID();
        UUID target = UUID.randomUUID();
        UUID schedule = UUID.randomUUID();
        UUID creditor = UUID.randomUUID();
        new AccountRecordsJourney(accounts, payments)
                .record(accountId, partyId, source, target, schedule, creditor)
                .block();

        verify(accounts).closeStatement(eq(accountId), eq("USD"), eq(new BigDecimal("100.00")), anyList(), eq("statement"));
        verify(accounts).recordWithdrawalLimit(eq(accountId), eq("USD"), eq(new BigDecimal("50.00")),
                eq(new BigDecimal("10.00")), eq(new BigDecimal("40.00")), eq(new BigDecimal("45.00")), eq("limit"));
        verify(accounts).recordSpaceSweep(eq(accountId), eq(source), eq(target), eq(new BigDecimal("15.00")), eq("USD"), eq("sweep"));
        verify(accounts).createServicingMandate(eq(accountId), eq(partyId), eq("OPERATOR"), eq(true), eq("mandate"));
        verify(payments).runStandingOrder(eq(schedule), eq(new BigDecimal("25.00")), eq("GBP"),
                eq(LocalDate.of(2026, 10, 21)), eq("standing-order"));
        verify(payments).createDirectDebit(eq(accountId), eq(creditor), eq(new BigDecimal("100.00")),
                eq("GBP"), eq("MANDATE-1"), eq("direct-debit"));
    }
}
