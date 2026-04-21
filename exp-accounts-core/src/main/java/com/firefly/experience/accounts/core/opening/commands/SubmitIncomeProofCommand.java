package com.firefly.experience.accounts.core.opening.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitIncomeProofCommand {
    private String documentType;
    private String documentContent;
    private String mimeType;
    private BigDecimal declaredAnnualIncome;
    private String employerName;
    private String occupation;
}
