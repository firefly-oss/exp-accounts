package com.firefly.experience.accounts.core.services;

import com.firefly.experience.accounts.interfaces.dtos.AccountSummaryResponse;
import com.firefly.experience.accounts.interfaces.dtos.OpenAccountRequest;
import com.firefly.experience.accounts.interfaces.dtos.OpenAccountResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface AccountExperienceService {

    Mono<OpenAccountResponse> openAccount(OpenAccountRequest request);

    Mono<AccountSummaryResponse> getAccountSummary(UUID accountId);

    Flux<AccountSummaryResponse> getCustomerAccounts(UUID customerId);

    Mono<Void> closeAccount(UUID accountId, String reason);
}
