package com.firefly.experience.accounts.core.services;

import com.firefly.domain.banking.accounts.sdk.api.AccountBackofficeApi;
import com.firefly.domain.banking.accounts.sdk.api.AccountsApi;
import com.firefly.domain.banking.accounts.sdk.model.AccountDTO;
import com.firefly.domain.banking.accounts.sdk.model.OpenAccountCommand;
import com.firefly.experience.accounts.interfaces.dtos.AccountSummaryResponse;
import com.firefly.experience.accounts.interfaces.dtos.OpenAccountRequest;
import com.firefly.experience.accounts.interfaces.dtos.OpenAccountResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountExperienceServiceImpl implements AccountExperienceService {

    private final AccountsApi accountsApi;
    private final AccountBackofficeApi accountBackofficeApi;

    @Override
    public Mono<OpenAccountResponse> openAccount(OpenAccountRequest request) {
        log.info("Opening account for party: {}", request.getPartyId());

        OpenAccountCommand command = new OpenAccountCommand();
        command.setPartyId(request.getPartyId());
        command.setProductId(request.getProductId());
        command.setProductCatalogId(request.getProductCatalogId());
        command.setRoleInContractId(request.getRoleInContractId());
        command.setAccountType(request.getAccountType());
        command.setCurrency(request.getCurrency());
        command.setBranchId(request.getBranchId());

        String idempotencyKey = UUID.randomUUID().toString();

        return accountsApi.openAccount(command, idempotencyKey)
                .map(result -> OpenAccountResponse.builder()
                        .accountId(result.getAccountId())
                        .contractId(result.getContractId())
                        .executionId(result.getExecutionId())
                        .status(result.getStatus())
                        .build());
    }

    @Override
    public Mono<AccountSummaryResponse> getAccountSummary(UUID accountId) {
        log.debug("Fetching account summary for: {}", accountId);

        String idempotencyKey = UUID.randomUUID().toString();

        return accountBackofficeApi.getAccount(accountId, idempotencyKey)
                .map(this::mapAccountDTOToSummary);
    }

    @Override
    public Flux<AccountSummaryResponse> getCustomerAccounts(UUID customerId) {
        log.debug("Fetching accounts for customer: {}", customerId);

        // TODO: Domain Banking Accounts SDK does not currently expose a listAccountsByParty endpoint.
        // Once the domain API provides this capability, implement the call here.
        // For now, throw UnsupportedOperationException to make the missing functionality explicit.
        return Flux.error(new UnsupportedOperationException(
                "Listing accounts by party is not yet supported. " +
                "The domain-banking-accounts service does not expose a listAccountsByParty endpoint."));
    }

    @Override
    public Mono<Void> closeAccount(UUID accountId, String reason) {
        log.info("Closing account: {} with reason: {}", accountId, reason);

        String idempotencyKey = UUID.randomUUID().toString();

        return accountsApi.closeAccount(accountId, reason, idempotencyKey);
    }

    private AccountSummaryResponse mapAccountDTOToSummary(AccountDTO account) {
        String status = account.getAccountStatus() != null ? account.getAccountStatus().getValue() : null;
        String accountType = account.getAccountType() != null ? account.getAccountType().getValue() : null;
        return AccountSummaryResponse.builder()
                .accountId(account.getAccountId())
                .accountNumber(account.getAccountNumber())
                .accountType(accountType)
                .status(status)
                .currency(account.getCurrency())
                .build();
    }
}
