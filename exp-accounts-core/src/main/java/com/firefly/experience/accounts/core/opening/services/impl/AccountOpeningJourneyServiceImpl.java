package com.firefly.experience.accounts.core.opening.services.impl;

import com.firefly.experience.accounts.core.opening.commands.ConfigureFundingSourceCommand;
import com.firefly.experience.accounts.core.opening.commands.InitiateAccountOpeningCommand;
import com.firefly.experience.accounts.core.opening.commands.SubmitIncomeProofCommand;
import com.firefly.experience.accounts.core.opening.queries.AccountOpeningJourneyStatusDTO;
import com.firefly.experience.accounts.core.opening.services.AccountOpeningJourneyService;
import com.firefly.experience.accounts.core.opening.workflows.AccountOpeningJourneyWorkflow;
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
