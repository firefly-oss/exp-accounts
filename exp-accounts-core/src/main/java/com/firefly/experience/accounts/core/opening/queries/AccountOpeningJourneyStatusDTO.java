package com.firefly.experience.accounts.core.opening.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountOpeningJourneyStatusDTO {
    private UUID journeyId;
    private UUID customerId;
    private UUID accountId;
    private UUID contractId;
    private String currentPhase;
    private List<String> completedSteps;
    private String nextStep;
    private String accountStatus;
    private String documentVerificationStatus;
}
