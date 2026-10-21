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


package io.fireflysoftware.experience.accounts.web.controllers;

import io.fireflysoftware.domain.banking.accounts.sdk.api.AccountRecordsApi;
import io.fireflysoftware.experience.accounts.core.services.AccountExperienceService;
import io.fireflysoftware.experience.accounts.interfaces.dtos.AccountSummaryResponse;
import io.fireflysoftware.experience.accounts.interfaces.dtos.OpenAccountRequest;
import io.fireflysoftware.experience.accounts.interfaces.dtos.OpenAccountResponse;
import io.fireflysoftware.experience.accounts.interfaces.dtos.UpdateAccountRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Account experience operations")
public class AccountsController {

    private final AccountExperienceService accountService;

    @PostMapping
    @Operation(summary = "Open an account", description = "Checks access, then opens the account through the domain port")
    public Mono<ResponseEntity<OpenAccountResponse>> openAccount(@Valid @RequestBody OpenAccountRequest request) {
        return accountService.openAccount(request)
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @PutMapping("/{accountId}")
    @Operation(summary = "Update an account", description = "Checks access, then updates the account through the domain port")
    public Mono<ResponseEntity<AccountSummaryResponse>> updateAccount(
            @PathVariable UUID accountId,
            @Valid @RequestBody UpdateAccountRequest request) {
        return accountService.updateAccount(accountId, request)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/{accountId}")
    @Operation(
            summary = "Get account summary",
            description = "Retrieves the account summary including balances and status"
    )
    @ApiResponse(responseCode = "200", description = "Account found")
    @ApiResponse(responseCode = "404", description = "Account not found")
    public Mono<ResponseEntity<AccountSummaryResponse>> getAccountSummary(
            @Parameter(description = "Account identifier")
            @PathVariable UUID accountId) {
        return accountService.getAccountSummary(accountId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{accountId}")
    @Operation(
            summary = "Close an account",
            description = "Closes an account with the specified reason"
    )
    @ApiResponse(responseCode = "204", description = "Account closed successfully")
    @ApiResponse(responseCode = "404", description = "Account not found")
    public Mono<ResponseEntity<Void>> closeAccount(
            @PathVariable UUID accountId,
            @RequestParam(required = false) String reason) {
        return accountService.closeAccount(accountId, reason)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    @PostMapping("/{accountId}/statements")
    @Operation(summary = "Close a statement", description = "Checks access, then stores the statement through the domain port")
    public Mono<ResponseEntity<java.util.Map<String, UUID>>> closeStatement(@PathVariable UUID accountId) {
        return accountService.closeStatement(accountId)
                .map(statementId -> ResponseEntity.status(HttpStatus.CREATED).body(java.util.Map.of("statementId", statementId)));
    }

    @GetMapping("/{accountId}/statements/{statementId}")
    @Operation(summary = "Read a statement", description = "Checks access, then reads the stored statement through the domain port")
    public Mono<ResponseEntity<AccountRecordsApi.StatementView>> readStatement(@PathVariable UUID accountId,
                                                                                @PathVariable UUID statementId) {
        return accountService.readStatement(accountId, statementId)
                .map(ResponseEntity::ok);
    }
}
