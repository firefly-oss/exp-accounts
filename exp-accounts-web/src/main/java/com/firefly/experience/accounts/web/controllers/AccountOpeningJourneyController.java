package com.firefly.experience.accounts.web.controllers;

import com.firefly.experience.accounts.core.opening.commands.ConfigureFundingSourceCommand;
import com.firefly.experience.accounts.core.opening.commands.InitiateAccountOpeningCommand;
import com.firefly.experience.accounts.core.opening.commands.SubmitIncomeProofCommand;
import com.firefly.experience.accounts.core.opening.queries.AccountOpeningJourneyStatusDTO;
import com.firefly.experience.accounts.core.opening.services.AccountOpeningJourneyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

/**
 * REST controller for the account opening journey.
 * Each endpoint is atomic: it either starts the workflow or sends a signal to advance it.
 */
@RestController
@RequestMapping("/api/v1/journeys/account-opening")
@RequiredArgsConstructor
@Tag(name = "Account Opening Journey",
     description = "Endpoints for the account opening journey workflow")
public class AccountOpeningJourneyController {

    private static final String KEY_JOURNEY_ID = "journeyId";
    private static final String KEY_STATUS = "status";

    private static final String STATUS_INCOME_PROOF_SUBMITTED = "INCOME_PROOF_SUBMITTED";
    private static final String STATUS_FUNDING_CONFIGURED = "FUNDING_CONFIGURED";

    private final AccountOpeningJourneyService journeyService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Initiate Account Opening",
               description = "Start a new account opening journey - sends welcome notification, "
                   + "checks eligibility, and waits for income proof submission")
    public Mono<ResponseEntity<AccountOpeningJourneyStatusDTO>> initiateAccountOpening(
            @Valid @RequestBody InitiateAccountOpeningCommand command) {
        return journeyService.initiateAccountOpening(command)
                .map(status -> ResponseEntity.status(HttpStatus.CREATED).body(status));
    }

    @GetMapping(value = "/{journeyId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get Journey Status",
               description = "Retrieve the current state of an account opening journey. "
                   + "Returns completed steps, current phase, and next expected action.")
    public Mono<ResponseEntity<AccountOpeningJourneyStatusDTO>> getJourneyStatus(
            @PathVariable UUID journeyId) {
        return journeyService.getJourneyStatus(journeyId)
                .map(ResponseEntity::ok);
    }

    @PostMapping(value = "/{journeyId}/income-proof",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Submit Income Proof",
               description = "Submit income proof documents for the account opening. "
                   + "Advances the journey past the income proof gate.")
    public Mono<ResponseEntity<Map<String, Object>>> submitIncomeProof(
            @PathVariable UUID journeyId,
            @Valid @RequestBody SubmitIncomeProofCommand command) {
        return journeyService.submitIncomeProof(journeyId, command)
                .thenReturn(ResponseEntity.ok(Map.of(
                        KEY_JOURNEY_ID, (Object) journeyId,
                        KEY_STATUS, STATUS_INCOME_PROOF_SUBMITTED)));
    }

    @PostMapping(value = "/{journeyId}/funding",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Configure Funding Source",
               description = "Configure the funding source for the initial deposit. "
                   + "Advances the journey past the funding gate and triggers account creation.")
    public Mono<ResponseEntity<Map<String, Object>>> configureFundingSource(
            @PathVariable UUID journeyId,
            @Valid @RequestBody ConfigureFundingSourceCommand command) {
        return journeyService.configureFundingSource(journeyId, command)
                .thenReturn(ResponseEntity.ok(Map.of(
                        KEY_JOURNEY_ID, (Object) journeyId,
                        KEY_STATUS, STATUS_FUNDING_CONFIGURED)));
    }
}
