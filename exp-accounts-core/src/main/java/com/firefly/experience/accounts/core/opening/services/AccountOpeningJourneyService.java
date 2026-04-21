package com.firefly.experience.accounts.core.opening.services;

import com.firefly.experience.accounts.core.opening.commands.ConfigureFundingSourceCommand;
import com.firefly.experience.accounts.core.opening.commands.InitiateAccountOpeningCommand;
import com.firefly.experience.accounts.core.opening.commands.SubmitIncomeProofCommand;
import com.firefly.experience.accounts.core.opening.queries.AccountOpeningJourneyStatusDTO;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Service interface for the account opening journey.
 * Each method corresponds to an atomic endpoint that starts or advances the workflow.
 */
public interface AccountOpeningJourneyService {

    Mono<AccountOpeningJourneyStatusDTO> initiateAccountOpening(InitiateAccountOpeningCommand command);

    Mono<Void> submitIncomeProof(UUID journeyId, SubmitIncomeProofCommand command);

    Mono<Void> configureFundingSource(UUID journeyId, ConfigureFundingSourceCommand command);

    Mono<AccountOpeningJourneyStatusDTO> getJourneyStatus(UUID journeyId);
}
