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


package io.fireflysoftware.experience.accounts.core.opening.services;

import io.fireflysoftware.experience.accounts.core.opening.commands.ConfigureFundingSourceCommand;
import io.fireflysoftware.experience.accounts.core.opening.commands.InitiateAccountOpeningCommand;
import io.fireflysoftware.experience.accounts.core.opening.commands.SubmitIncomeProofCommand;
import io.fireflysoftware.experience.accounts.core.opening.queries.AccountOpeningJourneyStatusDTO;
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
