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
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Account journey calls for the six records the cores already store.
 * Both clients are domain SDKs.
 */
@Service
public class AccountRecordsJourney {

    private final AccountRecordsApi accounts;
    private final PaymentRecordsApi payments;

    public AccountRecordsJourney(AccountRecordsApi accounts, PaymentRecordsApi payments) {
        this.accounts = accounts;
        this.payments = payments;
    }

    public Mono<Void> record(UUID accountId, UUID partyId, UUID sourceSpaceId, UUID targetSpaceId,
                              UUID paymentScheduleId, UUID creditorPartyId) {
        return accounts.closeStatement(accountId, "USD", new BigDecimal("100.00"),
                        List.of(new BigDecimal("25.00"), new BigDecimal("-10.00")), "statement")
                .then(accounts.recordWithdrawalLimit(accountId, "USD", new BigDecimal("50.00"),
                        new BigDecimal("10.00"), new BigDecimal("40.00"), new BigDecimal("45.00"), "limit"))
                .then(accounts.recordSpaceSweep(accountId, sourceSpaceId, targetSpaceId,
                        new BigDecimal("15.00"), "USD", "sweep"))
                .then(accounts.createServicingMandate(accountId, partyId, "OPERATOR", true, "mandate"))
                .then(payments.runStandingOrder(paymentScheduleId, new BigDecimal("25.00"), "GBP",
                        LocalDate.of(2026, 10, 21), "standing-order"))
                .then(payments.createDirectDebit(accountId, creditorPartyId, new BigDecimal("100.00"),
                        "GBP", "MANDATE-1", "direct-debit"))
                .then();
    }
}
