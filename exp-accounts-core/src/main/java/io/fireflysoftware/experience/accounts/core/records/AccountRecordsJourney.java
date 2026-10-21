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
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Account journey calls for the six records the cores already store.
 * Both clients are generated domain SDKs.
 */
@Service
public class AccountRecordsJourney {

    private final AccountRecordsControllerApi accounts;
    private final PaymentRecordsApi payments;

    public AccountRecordsJourney(AccountRecordsControllerApi accounts, PaymentRecordsApi payments) {
        this.accounts = accounts;
        this.payments = payments;
    }

    public Mono<Void> record(UUID accountId, UUID partyId, UUID sourceSpaceId, UUID targetSpaceId,
                              UUID paymentScheduleId, UUID creditorPartyId) {
        return closeStatement(accountId)
                .then(accounts.withdrawalLimit(accountId, new WithdrawalLimit()
                        .currency("USD")
                        .withdrawalLimit(new BigDecimal("50.00"))
                        .overdraftLimit(new BigDecimal("10.00"))
                        .balance(new BigDecimal("40.00"))
                        .withdrawalAmount(new BigDecimal("45.00")), "limit"))
                .then(accounts.spaceSweep(accountId, new SpaceSweep()
                        .sourceSpaceId(sourceSpaceId)
                        .targetSpaceId(targetSpaceId)
                        .amount(new BigDecimal("15.00"))
                        .currency("USD"), "sweep"))
                .then(accounts.servicingMandate(accountId, new ServicingMandate()
                        .partyId(partyId)
                        .role("OPERATOR")
                        .active(true), "mandate"))
                .then(payments.runStandingOrder(new StandingOrderBody()
                        .paymentScheduleId(paymentScheduleId)
                        .amount(new BigDecimal("25.00"))
                        .currency("GBP")
                        .valueDate(LocalDate.of(2026, 10, 21)), "standing-order"))
                .then(payments.createDirectDebit(new DirectDebitBody()
                        .debtorAccountId(accountId)
                        .creditorPartyId(creditorPartyId)
                        .amountLimit(new BigDecimal("100.00"))
                        .currency("GBP")
                        .reference("MANDATE-1"), "direct-debit"))
                .then();
    }

    public Mono<UUID> closeStatement(UUID accountId) {
        return accounts.closeStatement(accountId, new StatementClose()
                        .currency("USD")
                        .openingBalance(new BigDecimal("100.00"))
                        .lineAmounts(List.of(new BigDecimal("25.00"), new BigDecimal("-10.00"))), "statement")
                .map(RecordedId::getId);
    }

    public Mono<StatementView> readStatement(UUID accountId, UUID statementId) {
        return accounts.statement(accountId, statementId, null);
    }
}
