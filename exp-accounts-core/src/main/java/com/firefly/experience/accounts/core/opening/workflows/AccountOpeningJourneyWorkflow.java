package com.firefly.experience.accounts.core.opening.workflows;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firefly.domain.banking.accounts.sdk.api.AccountsApi;
import com.firefly.domain.banking.accounts.sdk.model.OpenAccountCommand;
import com.firefly.domain.common.notifications.sdk.api.NotificationsApi;
import com.firefly.domain.common.notifications.sdk.model.SendNotificationCommand;
import com.firefly.experience.accounts.core.opening.commands.ConfigureFundingSourceCommand;
import com.firefly.experience.accounts.core.opening.commands.InitiateAccountOpeningCommand;
import com.firefly.experience.accounts.core.opening.commands.SubmitIncomeProofCommand;
import com.firefly.experience.accounts.core.opening.queries.AccountOpeningJourneyStatusDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fireflyframework.orchestration.core.argument.FromStep;
import org.fireflyframework.orchestration.core.argument.Input;
import org.fireflyframework.orchestration.core.argument.SetVariable;
import org.fireflyframework.orchestration.core.argument.Variable;
import org.fireflyframework.orchestration.core.context.ExecutionContext;
import org.fireflyframework.orchestration.core.model.StepStatus;
import org.fireflyframework.orchestration.core.model.TriggerMode;
import org.fireflyframework.orchestration.workflow.annotation.OnWorkflowComplete;
import org.fireflyframework.orchestration.workflow.annotation.OnWorkflowError;
import org.fireflyframework.orchestration.workflow.annotation.WaitForSignal;
import org.fireflyframework.orchestration.workflow.annotation.Workflow;
import org.fireflyframework.orchestration.workflow.annotation.WorkflowQuery;
import org.fireflyframework.orchestration.workflow.annotation.WorkflowStep;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

/**
 * Signal-driven workflow for savings account opening journey.
 * <p>
 * Execution flow:
 * <pre>
 * Layer 0:  [create-application]
 * Layer 1:  [send-welcome] [eligibility-check]          ← parallel
 * Layer 2:  [receive-income-proof]                       ← @WaitForSignal("income-proof-submitted")
 * Layer 3:  [verify-documents]
 * Layer 4:  [receive-funding-source]                     ← @WaitForSignal("funding-configured")
 * Layer 5:  [open-account]
 * Layer 6:  [send-welcome-kit]
 * </pre>
 */
@Slf4j
@RequiredArgsConstructor
@Component
@Workflow(
    id = AccountOpeningJourneyWorkflow.WORKFLOW_ID,
    name = "Account Opening Journey",
    triggerMode = TriggerMode.SYNC,
    timeoutMs = 86400000,
    publishEvents = true,
    layerConcurrency = 0
)
public class AccountOpeningJourneyWorkflow {

    public static final String WORKFLOW_ID = "account-opening-journey";
    public static final String QUERY_JOURNEY_STATUS = "journeyStatus";

    public static final String STEP_CREATE_APPLICATION = "create-application";
    public static final String STEP_SEND_WELCOME = "send-welcome";
    public static final String STEP_ELIGIBILITY_CHECK = "eligibility-check";
    public static final String STEP_RECEIVE_INCOME_PROOF = "receive-income-proof";
    public static final String STEP_VERIFY_DOCUMENTS = "verify-documents";
    public static final String STEP_RECEIVE_FUNDING = "receive-funding-source";
    public static final String STEP_OPEN_ACCOUNT = "open-account";
    public static final String STEP_SEND_WELCOME_KIT = "send-welcome-kit";

    public static final String SIGNAL_INCOME_PROOF = "income-proof-submitted";
    public static final String SIGNAL_FUNDING_CONFIGURED = "funding-configured";

    public static final String VAR_CUSTOMER_ID = "customerId";
    public static final String VAR_ACCOUNT_ID = "accountId";
    public static final String VAR_CONTRACT_ID = "contractId";
    public static final String VAR_DOC_VERIFICATION_STATUS = "docVerificationStatus";

    public static final String PHASE_APPLICATION_CREATED = "APPLICATION_CREATED";
    public static final String PHASE_AWAITING_INCOME_PROOF = "AWAITING_INCOME_PROOF";
    public static final String PHASE_VERIFYING_DOCUMENTS = "VERIFYING_DOCUMENTS";
    public static final String PHASE_AWAITING_FUNDING = "AWAITING_FUNDING_CONFIGURATION";
    public static final String PHASE_OPENING_ACCOUNT = "OPENING_ACCOUNT";
    public static final String PHASE_COMPLETED = "COMPLETED";

    private static final String NOTIFICATION_CHANNEL = "AUTO";
    private static final String TEMPLATE_WELCOME = "ACCOUNT_OPENING_WELCOME";
    private static final String TEMPLATE_COMPLETED = "ACCOUNT_OPENING_COMPLETED";

    private final AccountsApi accountsApi;
    private final NotificationsApi notificationsApi;
    private final ObjectMapper objectMapper;

    @WorkflowStep(id = STEP_CREATE_APPLICATION)
    @SetVariable(VAR_CUSTOMER_ID)
    public Mono<UUID> createApplication(@Input InitiateAccountOpeningCommand cmd) {
        log.info("Account opening application created for customer: {}", cmd.getCustomerId());
        return Mono.just(cmd.getCustomerId());
    }

    @WorkflowStep(id = STEP_SEND_WELCOME, dependsOn = STEP_CREATE_APPLICATION)
    public Mono<Void> sendWelcomeNotification(@Variable(VAR_CUSTOMER_ID) UUID customerId,
                                               @Input InitiateAccountOpeningCommand cmd) {
        SendNotificationCommand notifCmd = new SendNotificationCommand()
                .partyId(customerId)
                .channel(NOTIFICATION_CHANNEL)
                .templateCode(TEMPLATE_WELCOME)
                .subject("Account Application Received")
                .recipientEmail(cmd.getEmail());

        if (cmd.getPhone() != null) {
            notifCmd.recipientPhone(cmd.getPhone());
        }

        return notificationsApi.sendNotification(notifCmd, UUID.randomUUID().toString())
                .doOnNext(r -> log.info("Sent welcome notification for customer: {}", customerId))
                .then();
    }

    @WorkflowStep(id = STEP_ELIGIBILITY_CHECK, dependsOn = STEP_CREATE_APPLICATION)
    public Mono<Boolean> checkEligibility(@Variable(VAR_CUSTOMER_ID) UUID customerId,
                                           @Input InitiateAccountOpeningCommand cmd) {
        log.info("Checking account eligibility for customer: {}", customerId);
        return Mono.just(true);
    }

    @WorkflowStep(id = STEP_RECEIVE_INCOME_PROOF, dependsOn = {STEP_SEND_WELCOME, STEP_ELIGIBILITY_CHECK})
    @WaitForSignal(SIGNAL_INCOME_PROOF)
    public Mono<Void> receiveIncomeProof(@Variable(VAR_CUSTOMER_ID) UUID customerId,
                                          Object signalData,
                                          ExecutionContext ctx) {
        SubmitIncomeProofCommand cmd = mapSignalPayload(signalData, SubmitIncomeProofCommand.class);
        log.info("Income proof received for customer: {} - employer: {}, income: {}",
                customerId, cmd.getEmployerName(), cmd.getDeclaredAnnualIncome());
        return Mono.empty();
    }

    @WorkflowStep(id = STEP_VERIFY_DOCUMENTS, dependsOn = STEP_RECEIVE_INCOME_PROOF)
    @SetVariable(VAR_DOC_VERIFICATION_STATUS)
    public Mono<String> verifyDocuments(@Variable(VAR_CUSTOMER_ID) UUID customerId) {
        log.info("Verifying documents for customer: {}", customerId);
        return Mono.just("VERIFIED");
    }

    @WorkflowStep(id = STEP_RECEIVE_FUNDING, dependsOn = STEP_VERIFY_DOCUMENTS)
    @WaitForSignal(SIGNAL_FUNDING_CONFIGURED)
    public Mono<Void> receiveFundingConfiguration(@Variable(VAR_CUSTOMER_ID) UUID customerId,
                                                   Object signalData) {
        ConfigureFundingSourceCommand cmd = mapSignalPayload(signalData, ConfigureFundingSourceCommand.class);
        log.info("Funding source configured for customer: {} - method: {}, amount: {}",
                customerId, cmd.getFundingMethod(), cmd.getInitialDepositAmount());
        return Mono.empty();
    }

    @WorkflowStep(id = STEP_OPEN_ACCOUNT, dependsOn = STEP_RECEIVE_FUNDING,
                  compensatable = true, compensationMethod = "compensateCloseAccount")
    @SetVariable(VAR_ACCOUNT_ID)
    public Mono<UUID> openAccount(@Variable(VAR_CUSTOMER_ID) UUID customerId,
                                   @Input InitiateAccountOpeningCommand cmd,
                                   ExecutionContext ctx) {
        OpenAccountCommand openCmd = new OpenAccountCommand();
        openCmd.setPartyId(customerId);
        openCmd.setProductId(cmd.getProductId());
        openCmd.setProductCatalogId(cmd.getProductCatalogId());
        openCmd.setAccountType(cmd.getAccountType());
        openCmd.setCurrency(cmd.getCurrency());
        openCmd.setBranchId(cmd.getBranchId());

        return accountsApi.openAccount(openCmd, UUID.randomUUID().toString())
                .map(response -> {
                    UUID accountId = response.getAccountId();
                    ctx.putVariable(VAR_CONTRACT_ID, response.getContractId());
                    log.info("Account opened: accountId={} for customer: {}", accountId, customerId);
                    return accountId;
                });
    }

    @WorkflowStep(id = STEP_SEND_WELCOME_KIT, dependsOn = STEP_OPEN_ACCOUNT)
    public Mono<Void> sendWelcomeKit(@Variable(VAR_CUSTOMER_ID) UUID customerId,
                                      @Variable(VAR_ACCOUNT_ID) UUID accountId) {
        SendNotificationCommand notifCmd = new SendNotificationCommand()
                .partyId(customerId)
                .channel(NOTIFICATION_CHANNEL)
                .templateCode(TEMPLATE_COMPLETED)
                .subject("Your Account is Ready!");

        return notificationsApi.sendNotification(notifCmd, UUID.randomUUID().toString())
                .doOnNext(r -> log.info("Sent welcome kit for account: {}", accountId))
                .then();
    }

    public Mono<Void> compensateCloseAccount(@FromStep(STEP_OPEN_ACCOUNT) UUID accountId) {
        log.warn("Compensating: closing account accountId={}", accountId);
        return accountsApi.closeAccount(accountId, "Workflow compensation", UUID.randomUUID().toString())
                .then()
                .onErrorResume(ex -> {
                    log.warn("Failed to compensate account closure accountId={}: {}", accountId, ex.getMessage());
                    return Mono.empty();
                });
    }

    @WorkflowQuery(QUERY_JOURNEY_STATUS)
    public AccountOpeningJourneyStatusDTO getJourneyStatus(ExecutionContext ctx) {
        Map<String, StepStatus> steps = ctx.getStepStatuses();
        return AccountOpeningJourneyStatusDTO.builder()
                .journeyId(UUID.fromString(ctx.getCorrelationId()))
                .customerId(toUuid(ctx.getVariable(VAR_CUSTOMER_ID)))
                .accountId(toUuid(ctx.getVariable(VAR_ACCOUNT_ID)))
                .contractId(toUuid(ctx.getVariable(VAR_CONTRACT_ID)))
                .currentPhase(deriveCurrentPhase(steps))
                .completedSteps(steps.entrySet().stream()
                        .filter(e -> e.getValue() == StepStatus.DONE)
                        .map(Map.Entry::getKey)
                        .toList())
                .nextStep(deriveNextStep(steps))
                .accountStatus(deriveAccountStatus(steps))
                .documentVerificationStatus((String) ctx.getVariable(VAR_DOC_VERIFICATION_STATUS))
                .build();
    }

    @OnWorkflowComplete
    public void onJourneyComplete(ExecutionContext ctx) {
        log.info("Account opening journey completed for customer: {}, accountId: {}",
                ctx.getVariable(VAR_CUSTOMER_ID), ctx.getVariable(VAR_ACCOUNT_ID));
    }

    @OnWorkflowError
    public void onJourneyError(Throwable error, ExecutionContext ctx) {
        log.error("Account opening journey failed for customer: {}: {}",
                ctx.getVariable(VAR_CUSTOMER_ID), error.getMessage());
    }

    private String deriveCurrentPhase(Map<String, StepStatus> steps) {
        if (steps.getOrDefault(STEP_CREATE_APPLICATION, StepStatus.PENDING) == StepStatus.PENDING) {
            return PHASE_APPLICATION_CREATED;
        }
        if (steps.getOrDefault(STEP_RECEIVE_INCOME_PROOF, StepStatus.PENDING) == StepStatus.PENDING) {
            return PHASE_AWAITING_INCOME_PROOF;
        }
        if (steps.getOrDefault(STEP_VERIFY_DOCUMENTS, StepStatus.PENDING) == StepStatus.PENDING) {
            return PHASE_VERIFYING_DOCUMENTS;
        }
        if (steps.getOrDefault(STEP_RECEIVE_FUNDING, StepStatus.PENDING) == StepStatus.PENDING) {
            return PHASE_AWAITING_FUNDING;
        }
        if (steps.getOrDefault(STEP_OPEN_ACCOUNT, StepStatus.PENDING) == StepStatus.PENDING) {
            return PHASE_OPENING_ACCOUNT;
        }
        return PHASE_COMPLETED;
    }

    private String deriveAccountStatus(Map<String, StepStatus> steps) {
        if (steps.getOrDefault(STEP_OPEN_ACCOUNT, StepStatus.PENDING) == StepStatus.DONE) {
            return "ACTIVE";
        }
        if (steps.getOrDefault(STEP_VERIFY_DOCUMENTS, StepStatus.PENDING) == StepStatus.DONE) {
            return "DOCUMENTS_VERIFIED";
        }
        return "PENDING";
    }

    private String deriveNextStep(Map<String, StepStatus> steps) {
        if (steps.getOrDefault(STEP_RECEIVE_INCOME_PROOF, StepStatus.PENDING) == StepStatus.PENDING) {
            return STEP_RECEIVE_INCOME_PROOF;
        }
        if (steps.getOrDefault(STEP_RECEIVE_FUNDING, StepStatus.PENDING) == StepStatus.PENDING) {
            return STEP_RECEIVE_FUNDING;
        }
        return null;
    }

    private <T> T mapSignalPayload(Object signalData, Class<T> type) {
        return objectMapper.convertValue(signalData, type);
    }

    private UUID toUuid(Object value) {
        if (value instanceof UUID uuid) return uuid;
        if (value instanceof String s) return UUID.fromString(s);
        return null;
    }
}
