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

import io.fireflysoftware.experience.accounts.core.services.AccountExperienceService;
import io.fireflysoftware.experience.accounts.interfaces.dtos.AccountSummaryResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Account experience operations")
public class AccountsController {

    private final AccountExperienceService accountService;

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

    @GetMapping("/party/{partyId}")
    @Operation(
            summary = "Get party accounts",
            description = "Retrieves all accounts for a specific party (customer)"
    )
    @ApiResponse(responseCode = "200", description = "Accounts retrieved")
    public Flux<AccountSummaryResponse> getPartyAccounts(
            @Parameter(description = "Party identifier")
            @PathVariable UUID partyId) {
        return accountService.getCustomerAccounts(partyId);
    }

    @DeleteMapping("/{accountId}")
    @Operation(
            summary = "Close an account",
            description = "Closes an account with the specified reason"
    )
    @ApiResponse(responseCode = "204", description = "Account closed successfully")
    @ApiResponse(responseCode = "404", description = "Account not found")
    public Mono<ResponseEntity<Void>> closeAccount(
            @Parameter(description = "Account identifier")
            @PathVariable UUID accountId,
            @Parameter(description = "Reason for closure")
            @RequestParam(required = false) String reason) {
        return accountService.closeAccount(accountId, reason)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}
