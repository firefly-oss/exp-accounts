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


package io.fireflysoftware.experience.accounts.core.opening.services.impl;

import io.fireflysoftware.experience.accounts.core.opening.commands.ConfigureFundingSourceCommand;
import io.fireflysoftware.experience.accounts.core.opening.commands.InitiateAccountOpeningCommand;
import io.fireflysoftware.experience.accounts.core.opening.commands.SubmitIncomeProofCommand;
import io.fireflysoftware.experience.accounts.core.opening.queries.AccountOpeningJourneyStatusDTO;
import io.fireflysoftware.experience.accounts.core.opening.services.AccountOpeningJourneyService;
import io.fireflysoftware.experience.accounts.core.opening.workflows.AccountOpeningJourneyWorkflow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fireflyframework.orchestration.workflow.engine.WorkflowEngine;
import org.fireflyframework.orchestration.workflow.query.WorkflowQueryService;
import org.fireflyframework.orchestration.workflow.signal.SignalService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

/**
 * Signal-driven workflow implementation of the account opening journey service.
 * <p>
 * The initiation endpoint starts a long-running workflow (SYNC mode - blocks until the
 * first @WaitForSignal gate). All subsequent endpoints send signals to advance the workflow.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AccountOpeningJourneyServiceImpl implements AccountOpeningJourneyService {

    private final WorkflowEngine workflowEngine;
    private final SignalService signalService;
    private final WorkflowQueryService queryService;

    @Override
    public Mono<AccountOpeningJourneyStatusDTO> initiateAccountOpening(InitiateAccountOpeningCommand command) {
        String correlationId = UUID.randomUUID().toString();
        Map<String, Object> input = Map.of("command", command);

        return workflowEngine.startWorkflow(
                        AccountOpeningJourneyWorkflow.WORKFLOW_ID, input, correlationId, "api", false)
                .flatMap(state -> queryService.executeQuery(
                        correlationId, AccountOpeningJourneyWorkflow.QUERY_JOURNEY_STATUS))
                .cast(AccountOpeningJourneyStatusDTO.class)
                .doOnNext(status -> log.info("Initiated account opening journey: journeyId={}", correlationId));
    }

    @Override
    public Mono<Void> submitIncomeProof(UUID journeyId, SubmitIncomeProofCommand command) {
        return signalService.signal(
                        journeyId.toString(), AccountOpeningJourneyWorkflow.SIGNAL_INCOME_PROOF, command)
                .doOnNext(r -> log.info("Signal delivered: income-proof-submitted for journeyId={}", journeyId))
                .then();
    }

    @Override
    public Mono<Void> configureFundingSource(UUID journeyId, ConfigureFundingSourceCommand command) {
        return signalService.signal(
                        journeyId.toString(), AccountOpeningJourneyWorkflow.SIGNAL_FUNDING_CONFIGURED, command)
                .doOnNext(r -> log.info("Signal delivered: funding-configured for journeyId={}", journeyId))
                .then();
    }

    @Override
    public Mono<AccountOpeningJourneyStatusDTO> getJourneyStatus(UUID journeyId) {
        return queryService.executeQuery(
                        journeyId.toString(), AccountOpeningJourneyWorkflow.QUERY_JOURNEY_STATUS)
                .cast(AccountOpeningJourneyStatusDTO.class);
    }
}
